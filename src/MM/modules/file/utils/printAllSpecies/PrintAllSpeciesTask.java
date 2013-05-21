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
package MM.modules.file.utils.printAllSpecies;

import MM.data.Dataset;
import MM.modules.file.utils.compare_reactions.CompareReactionsParameters;
import MM.modules.file.utils.compare_reactions.CompareReactionsTask;
import MM.parameters.ParameterSet;
import MM.taskcontrol.AbstractTask;
import MM.taskcontrol.TaskStatus;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.sbml.jsbml.*;

/**
 *
 * @author scsandra
 */
public class PrintAllSpeciesTask extends AbstractTask {

        private Dataset[] datasets;
        private float progress = 0.0f;
        private String message = "Add Species... ";
        private File fileName;

        public PrintAllSpeciesTask(Dataset[] datasets, ParameterSet parameters) {
                this.datasets = datasets;
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
                        List<Compound> listOfCompounds = this.readReactions();
                        for (Dataset dataset : datasets) {
                                this.printReactions(listOfCompounds, dataset);
                        }
                } catch (Exception e) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = e.toString();
                }
        }

        private void printReactions(List<Compound> listOfCompounds, Dataset dataset) {
                setStatus(TaskStatus.PROCESSING);
                try {
                        if (getStatus() == TaskStatus.PROCESSING) {
                                if (listOfCompounds == null) {
                                        listOfCompounds = new ArrayList<>();
                                }
                                SBMLDocument doc = dataset.getDocument();
                                Model m = doc.getModel();
                                for (Species species : m.getListOfSpecies()) {
                                        String[] s = new String[5];
                                        s[3] = species.getName();

                                        Annotation a = species.getAnnotation();
                                        for (CVTerm term : a.getListOfCVTerms()) {
                                                String t = term.toString();
                                                if (t.contains("CHEBI")) {
                                                        s[1] = t.substring(t.indexOf("CHEBI") + 6, t.indexOf("CHEBI") + 11);
                                                }
                                                if (t.contains("kegg")) {
                                                        s[0] = t.substring(t.indexOf("kegg.compound") + 14, t.indexOf("kegg.compound") + 20);
                                                }
                                        }
                                        if(s[1]==null && s[0] == null){
                                                s[0] = species.getId();
                                        }
                                        s[4] = m.getId();
                                       
                                        if (!this.isInTheList(listOfCompounds, s)) {
                                                try {
                                                        listOfCompounds.add(new Compound(s));
                                                } catch (Exception e) {
                                                        e.printStackTrace();
                                                }
                                        }

                                }

                                CsvWriter w = new CsvWriter(this.fileName.getAbsolutePath());

                                for (Compound c : listOfCompounds) {
                                        String[] data = c.getData();
                                        w.writeRecord(data);
                                }

                                w.close();

                        }
                } catch (Exception ex) {
                        Logger.getLogger(CompareReactionsTask.class.getName()).log(Level.ERROR, null, ex);
                }

                setStatus(TaskStatus.FINISHED);
        }

        private boolean isInTheList(List<Compound> listOfCompounds, String[] newCompound) {
                boolean isThere = false;
                for (Compound c : listOfCompounds) {
                        isThere = c.isSimilar(newCompound);
                        if (isThere) {
                                return true;
                        }
                }
                return isThere;
        }

        private List<Compound> readReactions() {
                try {
                        CsvReader r = new CsvReader(this.fileName.getAbsolutePath());
                        List<Compound> listOfCompounds = new ArrayList<>();
                        while (r.readRecord()) {
                                String[] data = r.getValues();
                                listOfCompounds.add(new Compound(data));
                        }
                        r.close();
                        return listOfCompounds;
                } catch (IOException ex) {
                        ex.printStackTrace();
                        return null;
                }

        }
}
