/*
 * Copyright 2007-2012 
 * This file is part of MM.
 *
 * MM is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * MM is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * MM; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package MM.modules.configuration.general;

import MM.main.MMCore;
import MM.modules.MMModuleCategory;
import MM.modules.MMProcessingModule;
import MM.parameters.ParameterSet;
import MM.taskcontrol.Task;

public class GeneralConfiguration implements MMProcessingModule {

        public static final String MODULE_NAME = "General configuration";
        private GeneralconfigurationParameters parameters = MMCore.getPreferences();

        public ParameterSet getParameterSet() {
                return parameters;
        }

        @Override
        public String toString() {
                return MODULE_NAME;
        }

        public Task[] runModule(ParameterSet parameters) {
                MMCore.setPreferences((GeneralconfigurationParameters) parameters);
                return null;
        }

        public MMModuleCategory getModuleCategory() {
                return MMModuleCategory.CONFIGURATION;
        }

        public String getIcon() {
                return "icons/configuration.png";
        }

        public boolean setSeparator() {
                return false;
        }
}
