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
package MM.modules.file.utils.get_pubchem_ids;

import MM.data.Dataset;
import MM.taskcontrol.AbstractTask;
import MM.taskcontrol.TaskStatus;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.sbml.jsbml.*;

/**
 *
 * @author scsandra
 */
public class GetPubchemIDSTask extends AbstractTask {

        private Dataset dataset;
        private float progress = 0.0f;
        private String message = "Getting IDs... ";

        public GetPubchemIDSTask(Dataset[] datasets) {
                this.dataset = datasets[0];
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
                        this.GetIds();
                } catch (Exception e) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = e.toString();
                }
        }

        public void GetIds() {
                setStatus(TaskStatus.PROCESSING);
                try {
                        if (getStatus() == TaskStatus.PROCESSING) {
                                Model model = dataset.getDocument().getModel();

                                ListOf<Species> species = model.getListOfSpecies();
                                // model.unsetListOfSpeciesTypes();
                                double total = species.size();
                                double done = 0.0;
                                for (Species s : species) {
                                        //   s.setMetaId("meta_"+s.getId());

                                        Annotation a = s.getAnnotation();
                                        List<CVTerm> terms = a.getListOfCVTerms();
                                        boolean isPubchemId = false;
                                        boolean isKeggId = false;
                                        boolean KeggCompound = false;
                                        String KeggId = "";
                                        CVTerm term = null;
                                        for (CVTerm t : terms) {
                                                if (t.isBiologicalQualifier() && t.getBiologicalQualifierType() == CVTerm.Qualifier.BQB_IS) {
                                                        term = t;
                                                }
                                                if (t.toString().contains("ubchem")) {
                                                        isPubchemId = true;
                                                }
                                                if (t.toString().contains("kegg.compound")) {
                                                        isKeggId = true;
                                                        String k = t.toString().substring(t.toString().indexOf("kegg"), t.toString().length());
                                                        KeggId = k.substring(k.indexOf("C"), k.indexOf("C") + 6);
                                                        KeggCompound = true;
                                                }
                                        }
                                        if (s.getId().contains("C")) {
                                                isKeggId = true;
                                                KeggId = s.getId();
                                        }
                                        if (!isPubchemId && isKeggId) {
                                                List<String> newPubchemIDs = this.getPubChemID(KeggId);
                                                if (term == null) {
                                                        term = new CVTerm();
                                                        term.setQualifierType(CVTerm.Type.BIOLOGICAL_QUALIFIER);
                                                        term.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS);
                                                }
                                                for (String newPubchemID : newPubchemIDs) {
                                                        if (newPubchemID != null) {
                                                                term.addResourceURI("http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?sid=" + newPubchemID);
                                                        }
                                                }
                                                if (!KeggCompound) {
                                                        term.addResourceURI("http://identifiers.org/kegg.compound/" + KeggId);
                                                }
                                                a.addCVTerm(term);
                                        }
                                        done++;
                                        this.progress = (float) (done / total);
                                }
                        }

                } catch (Exception ex) {
                        Logger.getLogger(GetPubchemIDSTask.class.getName()).log(Level.ERROR, null, ex);
                }

                setStatus(TaskStatus.FINISHED);
        }

        public List<String> getPubChemID(String KeggID) {
                BufferedReader in = null;
                List<String> pubchemIds = new ArrayList<>();
                try {
                        String query = "http://rest.kegg.jp/conv/pubchem/" + KeggID;
                        URL kegg = new URL(query);
                        URLConnection yc = kegg.openConnection();
                        in = new BufferedReader(new InputStreamReader(
                                yc.getInputStream()));
                        String inputLine = null;
                        while ((inputLine = in.readLine()) != null) {
                                try {
                                        if (!inputLine.isEmpty()) {
                                                inputLine = inputLine.substring(inputLine.indexOf("pubchem:") + 8);
                                                pubchemIds.add(inputLine);
                                        } else {
                                        }
                                } catch (Exception e) {
                                }
                        }

                        in.close();

                        return pubchemIds;
                } catch (IOException ex) {
                        return null;
                }
        }
}
