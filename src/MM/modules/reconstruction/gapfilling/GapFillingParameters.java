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
package MM.modules.reconstruction.gapfilling;

import MM.parameters.Parameter;
import MM.parameters.SimpleParameterSet;
import MM.parameters.parametersType.FileNameParameter;
import MM.parameters.parametersType.IntegerParameter;
import MM.parameters.parametersType.MultiChoiceParameter;
import MM.parameters.parametersType.StringParameter;

public class GapFillingParameters extends SimpleParameterSet {

        public static final StringParameter idFrom = new StringParameter("Id of the initial compound", "Write the id of the compound where the pathway is starting");
        public static final StringParameter idTo = new StringParameter("Id of the final compound", "Write the id of the compound where the pathway is ending");
        public static final String choices[] = {"C00131", "B00002", "B00001", "C00020", "C00055", "C00105", "C00130", "C00144", "C00002", "C00044", "C00063", "C00075", "C00012",
                "C00081", "C00700", "C00008", "C00010", "C00015", "C00035", "C00068", "C00104", "C00112", "C00120", "C00194", "C01337", "C04628",
                "C05777", "C19637", "C00007", "C00013", "C00027", "C00034", "C00038", "C00070", "C00076", "C00087", "C00088", "C01327", "C00175",
                "C00205", "C00238", "C00244", "C00282", "C00283", "C00291", "C00305", "C05529", "C00533", "C00536", "C00697", "C00703", "C00704",
                "C00887", "C01330", "C01358", "C01413", "C11215", "C01528", "C02084", "C02466", "C05172", "C05684", "C05697", "C06232", "C06697",
                "C06701", "C09306", "C00094", "C14818", "C14819", "C19171", "C00192", "C00708", "C00742", "C01319", "C01324", "C01382", "C01486",
                "C01818", "C01861", "C02306", "C05361", "C05590", "C13645", "C16487", "C00080", "C00125", "C00139", "C00003", "C00006", "C00016",
                "C00061", "C00113", "C00255", "C00343", "C00828", "C16694", "C17568", "C17569", "C00399", "C00876", "C00001", "C00009", "C00059",
                "C01342", "C01328", "C00011", "C01353", "C00126", "C00138", "C00004", "C00005", "C00342", "C01007", "C01352", "C01359", "C01847",
                "C02185", "C05819", "C90001", "C90002", "C00390", "C01080", "C00288", "C00115", "C00698", "C11481", "C01478", "C00320", "C00058",
                "C00014", "C00206", "C00017", "C00018", "C00019", "C00021", "C00022", "C00023", "C00024", "C00025", "C00026", "C00032", "C00114",
                "C00145", "C00146", "C00201", "C06089", "C01417", "CHEBI16144", "CHEBI22984", "CHEBI26078", "C19970", "Cluster4564", "Cluster4563",
                "C00040", "C00069", "CHEBI17909"};
        public static final MultiChoiceParameter removing = new MultiChoiceParameter("Compounds removed from the pathway search",
                "Compounds removed from the comparison of the pathway search", choices);
        public static final FileNameParameter molFile = new FileNameParameter("Compounds file", "Get a file with all the compounds", null);
        public static final FileNameParameter reactionFile = new FileNameParameter("Reaction file", "Get a file with all the reactions", null);
        public static final FileNameParameter boundsFile = new FileNameParameter("Bounds file", "Get a file with all the bounds", null);
        public static final IntegerParameter k = new IntegerParameter("k", "Number of path the algorithm is going to find", 1);
        public static final FileNameParameter saveName = new FileNameParameter("Save the file", "Save the file", null);
       
        public GapFillingParameters() {
                super(new Parameter[]{molFile, reactionFile, boundsFile, saveName, k, idFrom, idTo, removing});
        }
}
