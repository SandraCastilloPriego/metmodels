/*
 * Copyright 2007-2012 
 * This file is part of MetModels.
 *
 * MetModels is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * MetModels is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * MetModels; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package MM.taskcontrol.impl;

import MM.desktop.preferences.NumOfThreadsParameter;
import MM.main.MMCore;
import MM.modules.configuration.general.GeneralconfigurationParameters;
import MM.taskcontrol.Task;
import MM.taskcontrol.TaskControlListener;
import MM.taskcontrol.TaskController;
import MM.taskcontrol.TaskPriority;
import MM.taskcontrol.TaskStatus;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 * 
 * Task controller implementation
 */
public class TaskControllerImpl implements TaskController, Runnable {

        private Logger logger = Logger.getLogger(this.getClass().getName());
        ArrayList<TaskControlListener> listeners = new ArrayList<TaskControlListener>();
        /**
         * Update the task progress window every 300 ms
         */
        private final int TASKCONTROLLER_THREAD_SLEEP = 300;
        private Thread taskControllerThread;
        private TaskQueue taskQueue;
        private TaskProgressWindow taskWindow;
        /**
         * This vector contains references to all running threads of NORMAL
         * priority. Maximum number of concurrent threads is specified in the
         * preferences dialog.
         */
        private Vector<WorkerThread> runningThreads;

        /**
         * Initialize the task controller
         */
        public void initModule() {

                taskQueue = new TaskQueue();

                runningThreads = new Vector<WorkerThread>();

                // Create a low-priority thread that will manage the queue and start
                // worker threads for tasks
                taskControllerThread = new Thread(this, "Task controller thread");
                taskControllerThread.setPriority(Thread.MIN_PRIORITY);
                taskControllerThread.start();

                // Create the task progress window and add it to desktop
                taskWindow = new TaskProgressWindow();
                MMCore.getDesktop().addInternalFrame(taskWindow);

                // Initially, hide the task progress window
                taskWindow.setVisible(false);

        }

        TaskQueue getTaskQueue() {
                return taskQueue;
        }

        public void addTask(Task task) {
                addTasks(new Task[]{task}, TaskPriority.NORMAL);
        }

        public void addTask(Task task, TaskPriority priority) {
                addTasks(new Task[]{task}, priority);
        }

        public void addTasks(Task tasks[]) {
                addTasks(tasks, TaskPriority.NORMAL);
        }

        public void addTasks(Task tasks[], TaskPriority priority) {

                // It can sometimes happen during a batch that no tasks are actually
                // executed --> tasks[] array may be empty
                if ((tasks == null) || (tasks.length == 0)) {
                        return;
                }

                for (Task task : tasks) {
                        WrappedTask newQueueEntry = new WrappedTask(task, priority);
                        taskQueue.addWrappedTask(newQueueEntry);
                }

                // Wake up the task controller thread
                synchronized (this) {
                        this.notifyAll();
                }

                // Show the task list component
                SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                                taskWindow.setVisible(true);
                        }
                });

        }

        /**
         * Task controller thread main method.
         *
         * @see java.lang.Runnable#run()
         */
        public void run() {

                int previousQueueSize = -1;

                while (true) {

                        int currentQueueSize = taskQueue.getNumOfWaitingTasks();
                        if (currentQueueSize != previousQueueSize) {
                                previousQueueSize = currentQueueSize;
                                for (TaskControlListener listener : listeners) {
                                        listener.numberOfWaitingTasksChanged(currentQueueSize);
                                }
                        }

                        // If the queue is empty, we can sleep. When new task is added into
                        // the queue, we will be awaken by notify()
                        synchronized (this) {
                                while (taskQueue.isEmpty()) {
                                        try {
                                                this.wait();
                                        } catch (InterruptedException e) {
                                                // Ignore
                                        }
                                }
                        }

                        // Check if all tasks in the queue are finished
                        if (taskQueue.allTasksFinished()) {
                                SwingUtilities.invokeLater(new Runnable() {

                                        public void run() {
                                                taskWindow.setVisible(false);
                                        }
                                });
                                taskQueue.clear();
                                continue;
                        }

                        // Remove already finished threads from runningThreads
                        Iterator<WorkerThread> threadIterator = runningThreads.iterator();
                        while (threadIterator.hasNext()) {
                                WorkerThread thread = threadIterator.next();
                                if (thread.isFinished()) {
                                        threadIterator.remove();
                                }
                        }

                        // Get a snapshot of the queue
                        WrappedTask[] queueSnapshot = taskQueue.getQueueSnapshot();

                        // Obtain the settings of max concurrent threads
                        NumOfThreadsParameter parameter = MMCore.getPreferences().getParameter(GeneralconfigurationParameters.numOfThreads);
                        int maxRunningThreads;
                        if (parameter.isAutomatic() || (parameter.getValue() == null)) {
                                maxRunningThreads = Runtime.getRuntime().availableProcessors();
                        } else {
                                maxRunningThreads = parameter.getValue();
                        }

                        // Check all tasks in the queue
                        for (WrappedTask task : queueSnapshot) {

                                // Skip assigned and canceled tasks
                                if (task.isAssigned()
                                        || (task.getActualTask().getStatus() == TaskStatus.CANCELED)) {
                                        continue;
                                }

                                // Create a new thread if the task is high-priority or if we
                                // have less then maximum # of threads running
                                if ((task.getPriority() == TaskPriority.HIGH)
                                        || (runningThreads.size() < maxRunningThreads)) {
                                        WorkerThread newThread = new WorkerThread(task);

                                        if (task.getPriority() == TaskPriority.NORMAL) {
                                                runningThreads.add(newThread);
                                        }

                                        newThread.start();
                                }
                        }

                        // Tell the queue to refresh the Task progress window
                        taskQueue.refresh();

                        // Sleep for a while until next update
                        try {
                                Thread.sleep(TASKCONTROLLER_THREAD_SLEEP);
                        } catch (InterruptedException e) {
                                // Ignore
                        }

                }

        }

        public void setTaskPriority(Task task, TaskPriority priority) {

                // Get a snapshot of current task queue
                WrappedTask currentQueue[] = taskQueue.getQueueSnapshot();

                // Find the requested task
                for (WrappedTask wrappedTask : currentQueue) {

                        if (wrappedTask.getActualTask() == task) {
                                logger.finest("Setting priority of task \""
                                        + task.getTaskDescription() + "\" to " + priority);
                                wrappedTask.setPriority(priority);

                                // Call refresh to re-sort the queue according to new priority
                                // and update the Task progress window
                                taskQueue.refresh();
                        }
                }
        }

        @Override
        public void addTaskControlListener(TaskControlListener listener) {
                listeners.add(listener);
        }
}
