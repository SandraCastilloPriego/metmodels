/*
 * Copyright 2007-2013 VTT Biotechnology
 * This file is part of Guineu.
 *
 * Guineu is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * Guineu is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Guineu; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package MM.modules.file.utils.get_ebi_ids;

import MM.main.MMCore;
import MM.modules.MMModuleCategory;
import MM.modules.MMProcessingModule;
import MM.parameters.ParameterSet;
import MM.taskcontrol.Task;

/**
 *
 * @author scsandra
 */
public class GetEBIIDSModule implements MMProcessingModule {

        public static final String MODULE_NAME = "Get EBI Ids..";
        @Override
        public ParameterSet getParameterSet() {
                return null;
        }

        @Override
        public String toString() {
                return MODULE_NAME;
        }

        @Override
        public Task[] runModule(ParameterSet parameters) {
                // prepare a new group of tasks
                Task tasks[] = new GetEBIIDSTask[1];
                tasks[0] = new GetEBIIDSTask(MMCore.getDesktop().getSelectedDataFiles());

                MMCore.getTaskController().addTasks(tasks);

                return tasks;
        }

        @Override
        public MMModuleCategory getModuleCategory() {
                return MMModuleCategory.UTILS;
        }

        @Override
        public String getIcon() {
                return "icons/others.png";
        }

        @Override
        public boolean setSeparator() {
                return false;
        }
}
