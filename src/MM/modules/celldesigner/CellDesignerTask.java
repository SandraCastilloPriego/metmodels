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
package MM.modules.celldesigner;

import MM.data.Dataset;
import MM.main.MMCore;
import MM.modules.file.utils.compare_reactions.CompareReactionsParameters;
import MM.parameters.ParameterSet;
import MM.taskcontrol.AbstractTask;
import MM.taskcontrol.TaskStatus;
import com.csvreader.CsvReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.sbml.jsbml.*;

/**
 *
 * @author scsandra
 */
public class CellDesignerTask extends AbstractTask {

        private Dataset dataset;
        private float progress = 0.0f;
        private String message = "Change map... ";
        private File fileName;

        public CellDesignerTask(Dataset[] datasets, ParameterSet parameters) {
                this.dataset = datasets[0];
                this.fileName = (File) parameters.getParameter(CompareReactionsParameters.fileName).getValue();
        }

        @Override
        public String getTaskDescription() {
                return message;
        }

        @Override
        public double getFinishedPercentage() {
                return progress;
        }

        @Override
        public void cancel() {
                setStatus(TaskStatus.CANCELED);
        }

        @Override
        public void run() {
                try {
                        setStatus(TaskStatus.PROCESSING);
                        List<String> reactionIds = readFile();
                        changeColor(reactionIds);
                        setStatus(TaskStatus.FINISHED);
                } catch (Exception e) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = e.toString();
                }
        }

        private List<String> readFile() {
                try {
                        List<String> reactionIds = new ArrayList<>();
                        CsvReader reader = new CsvReader(new FileReader(this.fileName.getAbsolutePath()));
                        try {
                                while (reader.readRecord()) {
                                        String[] data = reader.getValues();
                                        reactionIds.add(data[0]);

                                }
                        } catch (IOException ex) {
                                java.util.logging.Logger.getLogger(CellDesignerTask.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                        }
                        return reactionIds;
                } catch (FileNotFoundException ex) {
                        java.util.logging.Logger.getLogger(CellDesignerTask.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                        return null;
                }

        }

        private void changeColor(List<String> ids) {
                Dataset[] data = MMCore.getDesktop().getSelectedDataFiles();
                Dataset map = data[0];
                SBMLDocument doc = map.getDocument();
                Model m = doc.getModel();
                ListOf<Species> species = m.getListOfSpecies();

                for (String id : ids) {
                        String idFixed = id.substring(2);
                        System.out.println(idFixed);
                        String reaction = null;
                        for (Species s : species) {
                                if (s.getName().equals(idFixed)) {
                                        Annotation a = s.getAnnotation();
                                        reaction = a.getNonRDFannotation();
                                        if (reaction.contains("<celldesigner:catalyzed reaction=")) {
                                                reaction = reaction.substring(reaction.indexOf("<celldesigner:catalyzed reaction=") + 34, reaction.indexOf("></celldesigner:catalyzed>") - 1);
                                        }
                                        break;
                                }
                        }
                        if (reaction != null) {
                                Reaction r = m.getReaction(reaction);
                                Annotation a = r.getAnnotation();
                                String colorAnnotation = a.getNonRDFannotation();
                                colorAnnotation = colorAnnotation.replace("ff000000", "FFFF0000");
                                a.setNonRDFAnnotation(colorAnnotation);
                        }
                }
        }
}
