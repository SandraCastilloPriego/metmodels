/*
 * Copyright 2007-2013 VTT Biotechnology
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
package MM.modules.file.utils.search_pathway2points;

import MM.data.Dataset;
import MM.main.MMCore;
import MM.parameters.Parameter;
import MM.parameters.SimpleParameterSet;
import MM.parameters.parametersType.ComboParameter;
import MM.parameters.parametersType.StringParameter;
import MM.util.dialogs.ExitCode;

public class SearchPathwaysTwoPointsParameters extends SimpleParameterSet {        

        public static final ComboParameter data = new ComboParameter("Search in model: ", "Choose the model where you want to search", new String[0]);
        public static final StringParameter idFrom = new StringParameter("Id of the initial compound", "Write the id of the compound where the pathway is starting");
        public static final StringParameter idTo = new StringParameter("Id of the final compound", "Write the id of the compound where the pathway is ending");
              

        public SearchPathwaysTwoPointsParameters() {
                super(new Parameter[]{data, idFrom, idTo});
        }

        @Override
        public ExitCode showSetupDialog() {
                Dataset[] dataset = MMCore.getDesktop().getSelectedDataFiles();
                String[] names = new String[dataset.length];
                for (int i = 0; i < dataset.length; i++) {
                        names[i] = dataset[i].getDatasetName();
                }
                
                getParameter(SearchPathwaysTwoPointsParameters.data).setChoices(names);                
               
                return super.showSetupDialog();
        }
}
