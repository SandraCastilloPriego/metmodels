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
package MM.modules.database.neo4j.modelRelationships;

import MM.parameters.Parameter;
import MM.parameters.SimpleParameterSet;
import MM.parameters.parametersType.DirNameParameter;
import MM.parameters.parametersType.FileNameParameter;
import MM.parameters.parametersType.StringParameter;

public class CombineModelParameters extends SimpleParameterSet {        

        public static final DirNameParameter fileName = new DirNameParameter("Database dir", "Set the path of the neo4j database", null);
        public static final StringParameter modelName = new StringParameter("Compared model name", "Name of the model that was compared with.", null);
        public static final FileNameParameter comparisonFile = new FileNameParameter("Comparison file", "Set the path of the comparison file", null);
        public CombineModelParameters() {
                super(new Parameter[]{fileName, modelName, comparisonFile});
        }
      
}
