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
package MM.desktop.preferences;

import MM.data.LCMSColumnName;
import MM.parameters.SimpleParameterSet;
import MM.parameters.UserParameter;
import MM.parameters.parametersType.MultiChoiceParameter;

/**
 *
 * @author scsandra
 */
public class ColumnsLCMSParameters extends SimpleParameterSet {
     
        public static final MultiChoiceParameter<LCMSColumnName> LCMSdata = new MultiChoiceParameter<LCMSColumnName>(
                "Select columns for LC-MS", "Select columns for LC-MS",LCMSColumnName.values());

        public ColumnsLCMSParameters() {
                super(new UserParameter[]{LCMSdata});
        }
      
}

