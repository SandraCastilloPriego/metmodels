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

import MM.main.MMCore;
import MM.taskcontrol.Task;
import MM.taskcontrol.TaskPriority;
import MM.taskcontrol.TaskStatus;
import MM.util.GUIUtils;
import MM.util.components.ComponentCellRenderer;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 *
 * This class represents a window with a table of running tasks
 */
public class TaskProgressWindow extends JInternalFrame implements
		ActionListener {

	private JTable taskTable;

	private JPopupMenu popupMenu;
	private JMenu priorityMenu;
	private JMenuItem cancelTaskMenuItem, cancelAllMenuItem,
			highPriorityMenuItem, normalPriorityMenuItem;

	/**
	 * Constructor
	 */
	public TaskProgressWindow() {

		super("Tasks in progress...", true, true, true, true);

		// We don't want this window to be closed until all tasks are finished
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		TaskControllerImpl taskController = (TaskControllerImpl) MMCore
				.getTaskController();

		taskTable = new JTable(taskController.getTaskQueue());
		taskTable.setCellSelectionEnabled(false);
		taskTable.setColumnSelectionAllowed(false);
		taskTable.setRowSelectionAllowed(true);
		taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		taskTable.setDefaultRenderer(JComponent.class,
				new ComponentCellRenderer());
		taskTable.getTableHeader().setReorderingAllowed(false);

		JScrollPane jJobScroll = new JScrollPane(taskTable);
		add(jJobScroll, BorderLayout.CENTER);

		// Create popup menu and items
		popupMenu = new JPopupMenu();

		priorityMenu = new JMenu("Set priority...");
		highPriorityMenuItem = GUIUtils.addMenuItem(priorityMenu, "High", this);
		normalPriorityMenuItem = GUIUtils.addMenuItem(priorityMenu, "Normal",
				this);
		popupMenu.add(priorityMenu);

		cancelTaskMenuItem = GUIUtils.addMenuItem(popupMenu, "Cancel task",
				this);
		cancelAllMenuItem = GUIUtils.addMenuItem(popupMenu, "Cancel all tasks",
				this);

		// Addd popup menu to the task table
		taskTable.setComponentPopupMenu(popupMenu);

		// Set the width for first column (task description)
		taskTable.getColumnModel().getColumn(0).setPreferredWidth(350);

		pack();

		// Set position and size
		setBounds(20, 20, 600, 150);

	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {

		TaskControllerImpl taskController = (TaskControllerImpl) MMCore
				.getTaskController();

		WrappedTask currentQueue[] = taskController.getTaskQueue()
				.getQueueSnapshot();

		Task selectedTask = null;

		int selectedRow = taskTable.getSelectedRow();

		if ((selectedRow < currentQueue.length) && (selectedRow >= 0))
			selectedTask = currentQueue[selectedRow].getActualTask();

		Object src = event.getSource();

		if (src == cancelTaskMenuItem) {
			if (selectedTask == null)
				return;
			TaskStatus status = selectedTask.getStatus();
			if ((status == TaskStatus.WAITING)
					|| (status == TaskStatus.PROCESSING)) {
				selectedTask.cancel();
			}
		}

		if (src == cancelAllMenuItem) {
			for (WrappedTask wrappedTask : currentQueue) {
				Task task = wrappedTask.getActualTask();
				TaskStatus status = task.getStatus();
				if ((status == TaskStatus.WAITING)
						|| (status == TaskStatus.PROCESSING)) {
					task.cancel();
				}
			}
		}

		if (src == highPriorityMenuItem) {
			if (selectedTask == null)
				return;
			taskController.setTaskPriority(selectedTask, TaskPriority.HIGH);
		}

		if (src == normalPriorityMenuItem) {
			if (selectedTask == null)
				return;
			taskController.setTaskPriority(selectedTask, TaskPriority.NORMAL);
		}

	}

}
