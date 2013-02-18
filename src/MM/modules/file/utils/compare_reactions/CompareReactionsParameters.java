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
package MM.modules.file.utils.compare_reactions;

import MM.parameters.Parameter;
import MM.parameters.SimpleParameterSet;
import MM.parameters.parametersType.FileNameParameter;
import MM.parameters.parametersType.MultiChoiceParameter;

public class CompareReactionsParameters extends SimpleParameterSet {

        public static final FileNameParameter fileName = new FileNameParameter("File name", "Set the path of the file", null);
        private static final String choices[] = {"C00001", "C00002", "C00003", "C00004", "C00005", "C00006", "C00007", "C00008", "C00009", "C00010", "C00011", "C00012", "C00013", "C00014", "C00015", "C00016", "C00017", "C00080"};
        public static final MultiChoiceParameter removing = new MultiChoiceParameter("Compounds removed from the comparison of the reactions",
                "Compounds removed from the comparison of the reactions", choices);

        public CompareReactionsParameters() {
                super(new Parameter[]{fileName, removing});
        }       
}
