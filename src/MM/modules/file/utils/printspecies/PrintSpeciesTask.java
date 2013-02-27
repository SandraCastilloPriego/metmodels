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
package MM.modules.file.utils.printspecies;

import MM.data.Dataset;
import MM.modules.file.utils.compare_reactions.CompareReactionsParameters;
import MM.modules.file.utils.compare_reactions.CompareReactionsTask;
import MM.parameters.ParameterSet;
import MM.taskcontrol.AbstractTask;
import MM.taskcontrol.TaskStatus;
import com.csvreader.CsvWriter;
import java.io.File;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.sbml.jsbml.*;

/**
 *
 * @author scsandra
 */
public class PrintSpeciesTask extends AbstractTask {

        private Dataset dataset;
        private float progress = 0.0f;
        private String message = "Print Species... ";
        private File fileName;

        public PrintSpeciesTask(Dataset[] datasets, ParameterSet parameters) {
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
                        this.printReactions();
                } catch (Exception e) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = e.toString();
                }
        }

        private void printReactions() {
                setStatus(TaskStatus.PROCESSING);
                try {
                        if (getStatus() == TaskStatus.PROCESSING) {

                                CsvWriter w = new CsvWriter(this.fileName.getAbsolutePath());
                                SBMLDocument doc = dataset.getDocument();
                                Model m = doc.getModel();
                                w.write("Number or species: " + m.getListOfSpecies().size());
                                w.endRecord();
                                for (Species species : m.getListOfSpecies()) {
                                        String[] s = new String[5];
                                        s[0] = species.getId();
                                        s[1] = species.getName();
                                        if (species.getInitialConcentration() != Double.NaN) {
                                                s[2] = String.valueOf(species.getInitialConcentration());
                                        }
                                        s[3] = species.getCompartment();
                                        s[4] = String.valueOf(species.getSBOTerm());
                                        w.writeRecord(s);

                                        w.write("Annotations:");
                                        Annotation a = species.getAnnotation();
                                        for (CVTerm term : a.getListOfCVTerms()) {
                                                w.write(term.toString());
                                        }
                                        w.write(a.getNonRDFannotation());
                                        w.endRecord();


                                        w.write("------------------------------------------------------");
                                        w.endRecord();
                                }
                                w.close();

                        }
                } catch (Exception ex) {
                        Logger.getLogger(CompareReactionsTask.class.getName()).log(Level.ERROR, null, ex);
                }

                setStatus(TaskStatus.FINISHED);
        }
}
