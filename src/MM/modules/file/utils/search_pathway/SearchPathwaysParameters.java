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
package MM.modules.file.utils.search_pathway;

import MM.data.Dataset;
import MM.main.MMCore;
import MM.parameters.Parameter;
import MM.parameters.SimpleParameterSet;
import MM.parameters.parametersType.ComboParameter;
import MM.parameters.parametersType.MultiChoiceParameter;
import MM.parameters.parametersType.StringParameter;
import MM.util.dialogs.ExitCode;

public class SearchPathwaysParameters extends SimpleParameterSet {        

        public static final ComboParameter dataFrom = new ComboParameter("Reaction from model: ", "Choose the model containing the reaction", new String[0]);
        public static final StringParameter id = new StringParameter("Id of the reaction", "Write the id of the reaction");
        public static final ComboParameter dataWhere = new ComboParameter("Search in model: ", "Choose the model where you want to search", new String[0]);
        
        public static final String choices[] = {"C00001", "C00002", "C00003", "C00004", "C00005", "C00006", "C00007", "C00008", "C00009", "C00010", "C00011", "C00012", "C00013", "C00014", "C00015", "C00016", "C00017", "C00080"};
        public static final MultiChoiceParameter removing = new MultiChoiceParameter("Compounds removed from the comparison of the reactions",
                "Compounds removed from the comparison of the reactions", choices);

        public SearchPathwaysParameters() {
                super(new Parameter[]{dataFrom, id, dataWhere, removing});
        }

        @Override
        public ExitCode showSetupDialog() {
                Dataset[] data = MMCore.getDesktop().getSelectedDataFiles();
                String[] names = new String[data.length];
                for (int i = 0; i < data.length; i++) {
                        names[i] = data[i].getDatasetName();
                }

                getParameter(SearchPathwaysParameters.dataFrom).setChoices(names);
                getParameter(SearchPathwaysParameters.dataWhere).setChoices(names);                
               
                return super.showSetupDialog();
        }
}
