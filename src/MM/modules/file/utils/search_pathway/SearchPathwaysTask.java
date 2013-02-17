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
import MM.parameters.ParameterSet;
import MM.taskcontrol.AbstractTask;
import MM.taskcontrol.TaskStatus;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.sbml.jsbml.*;

/**
 *
 * @author scsandra
 */
public class SearchPathwaysTask extends AbstractTask {

        private Dataset[] datasets;
        private float progress = 0.0f;
        private String message = "Search for Pathways... ";
        private String datasetFrom, datasetWhere, reactions;
        private String[] removedCompounds, reactionsIds;
        private HashMap<Reaction, String> names1, names2;
        private ListOf<Species> species;
        private HashMap<Species, List<String>> speciesidFrom, speciesidWhere;
        private List<String> used;
        private JInternalFrame frame;
        private JScrollPane panel;
        private JTextArea tf;

        public SearchPathwaysTask(Dataset[] datasets, ParameterSet parameters) {
                this.datasets = datasets;
                this.removedCompounds = (String[]) parameters.getParameter(SearchPathwaysParameters.removing).getChoices();
                this.datasetFrom = (String) parameters.getParameter(SearchPathwaysParameters.dataFrom).getValue();
                this.datasetWhere = (String) parameters.getParameter(SearchPathwaysParameters.dataWhere).getValue();
                this.reactions = (String) parameters.getParameter(SearchPathwaysParameters.id).getValue();

                this.reactionsIds = this.reactions.split(",");

                this.names1 = new HashMap<>();
                this.names2 = new HashMap<>();
                this.speciesidFrom = new HashMap<>();
                this.speciesidWhere = new HashMap<>();
                

                this.frame = new JInternalFrame("Result", true, true, true, true);

                this.tf = new JTextArea();
                this.panel = new JScrollPane(tf);
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
                        this.compareReactions();
                } catch (Exception e) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = e.toString();
                }
        }

        public void compareReactions() {
                setStatus(TaskStatus.PROCESSING);
                try {
                        if (getStatus() == TaskStatus.PROCESSING) {

                               
                                frame.setSize(new Dimension(700, 500));
                                frame.add(this.panel);


                                MMCore.getDesktop().addInternalFrame(frame);


                                SBMLDocument doc1 = null, doc2 = null;
                                for (Dataset d : datasets) {
                                        if (this.datasetFrom.contains(d.getDatasetName())) {
                                                doc1 = d.getDocument();
                                        }
                                        if (this.datasetWhere.contains(d.getDatasetName())) {
                                                doc2 = d.getDocument();
                                        }
                                }

                                Model m1 = doc1.getModel();
                                Model m2 = doc2.getModel();

                                if (this.datasetWhere.equals(m1.getId())) {
                                        this.species = m1.getListOfSpecies();
                                } else {
                                        this.species = m2.getListOfSpecies();
                                }

                                this.message = "Getting Ids..";
                                double total = species.size();
                                double done = 0.0;
                                for (Species s1 : species) {
                                        List<String> id = checkSpecieID(s1);
                                        speciesidWhere.put(s1, id);
                                        done++;
                                        progress = (float) (done / total);
                                }


                                for (String reactionId : this.reactionsIds) {
                                        this.used = new ArrayList<>();
                                        
                                        Reaction r = null;
                                        if (this.datasetFrom.equals(m1.getId())) {
                                                r = m1.getReaction(reactionId);
                                        } else {
                                                r = m2.getReaction(reactionId);
                                        }

                                        ListOf<SpeciesReference> reactants = r.getListOfReactants();
                                        for (SpeciesReference sr : reactants) {
                                                Species s = sr.getSpeciesInstance();
                                                List<String> id = checkSpecieID(s);
                                                speciesidFrom.put(s, id);
                                        }
                                        ListOf<SpeciesReference> products = r.getListOfProducts();
                                        for (SpeciesReference sr : products) {
                                                Species s = sr.getSpeciesInstance();
                                                List<String> id = checkSpecieID(s);
                                                speciesidFrom.put(s, id);
                                        }

                                        ListOf<Reaction> reactionList;
                                        if (this.datasetWhere.equals(m1.getId())) {
                                                reactionList = m1.getListOfReactions();
                                        } else {
                                                reactionList = m2.getListOfReactions();
                                        }

                                        for (Reaction r2 : reactionList) {
                                                Boolean isThere = comparingReactions(r, speciesidFrom, r2, speciesidWhere);
                                                if (isThere) {
                                                        String s = this.tf.getText();
                                                        s = s.concat("Reaction: " + reactionId);
                                                        System.out.println("Reaction: " + reactionId + "\n");
                                                        this.tf.setText(s);
                                                        printPathway(r2, m2);

                                                        System.out.println("--------------------------------");
                                                        s = this.tf.getText();
                                                        s = s.concat("--------------------------------" + "\n");
                                                        this.tf.setText(s);
                                                }
                                        }
                                }
                        }


                } catch (Exception ex) {
                        Logger.getLogger(SearchPathwaysTask.class.getName()).log(Level.ERROR, null, ex);
                }

                setStatus(TaskStatus.FINISHED);
        }

        private boolean comparingReactions(Reaction r1, HashMap<Species, List<String>> ids1, Reaction r2, HashMap<Species, List<String>> ids2) {
                ListOf<SpeciesReference> reactans1 = r1.getListOfReactants();
                ListOf<SpeciesReference> reactans2 = r2.getListOfReactants();

                ListOf<SpeciesReference> products1 = r1.getListOfProducts();
                ListOf<SpeciesReference> products2 = r2.getListOfProducts();

                for (SpeciesReference sr1 : reactans1) {

                        List<String> ids = ids1.get(sr1.getSpeciesInstance());

                        // Remove the compounds that shouldn't be taken into account
                        boolean cont = false;
                        for (String id : ids) {
                                for (String reactant : this.removedCompounds) {
                                        if (id.contains(reactant)) {
                                                cont = true;
                                        }
                                }
                        }
                        if (cont) {
                                continue;
                        }

                        // Compare the ids of the reactants
                        boolean isThere = false;
                        for (SpeciesReference sr2 : reactans2) {
                                for (String id : ids) {
                                        List<String> idsFrom2 = ids2.get(sr2.getSpeciesInstance());
                                        for (String idFrom2 : idsFrom2) {
                                                if (id.contains(idFrom2) || idFrom2.contains(id)) {
                                                        isThere = true;
                                                }
                                        }
                                }
                        }
                        if (!isThere) {
                                return false;
                        }
                }



                for (SpeciesReference sr2 : reactans2) {

                        List<String> ids = ids2.get(sr2.getSpeciesInstance());

                        // Remove the compounds that shouldn't be taken into account
                        boolean cont = false;
                        for (String id : ids) {
                                for (String reactant : this.removedCompounds) {
                                        if (id.contains(reactant)) {
                                                cont = true;
                                        }
                                }
                        }
                        if (cont) {
                                continue;
                        }

                        // Compare the ids of the reactants
                        boolean isThere = false;
                        for (SpeciesReference sr1 : reactans1) {
                                for (String id : ids) {
                                        List<String> idsFrom1 = ids1.get(sr1.getSpeciesInstance());
                                        for (String idFrom1 : idsFrom1) {
                                                if (id.contains(idFrom1) || idFrom1.contains(id)) {
                                                        isThere = true;
                                                }
                                        }
                                }
                        }
                        if (!isThere) {
                                return false;
                        }
                }

                for (SpeciesReference sr1 : products1) {
                        List<String> ids = ids1.get(sr1.getSpeciesInstance());

                        // Remove the compounds that shouldn't be taken into account
                        boolean cont = false;
                        for (String id : ids) {
                                for (String reactant : this.removedCompounds) {
                                        if (id.contains(reactant)) {
                                                cont = true;
                                        }
                                }
                        }
                        if (cont) {
                                continue;
                        }

                        // Compare the ids of the reactants
                        boolean isThere = false;
                        for (SpeciesReference sr2 : products2) {
                                for (String id : ids) {
                                        List<String> idsFrom2 = ids2.get(sr2.getSpeciesInstance());
                                        for (String idFrom2 : idsFrom2) {
                                                if (id.contains(idFrom2) || idFrom2.contains(id)) {
                                                        isThere = true;
                                                }
                                        }
                                }
                        }
                        if (!isThere) {
                                return false;
                        }

                }




                for (SpeciesReference sr2 : products2) {
                        List<String> ids = ids2.get(sr2.getSpeciesInstance());

                        // Remove the compounds that shouldn't be taken into account
                        boolean cont = false;
                        for (String id : ids) {
                                for (String reactant : this.removedCompounds) {
                                        if (id.contains(reactant)) {
                                                cont = true;
                                        }
                                }
                        }
                        if (cont) {
                                continue;
                        }

                        // Compare the ids of the reactants
                        boolean isThere = false;
                        for (SpeciesReference sr1 : products1) {
                                for (String id : ids) {
                                        List<String> idsFrom1 = ids1.get(sr1.getSpeciesInstance());
                                        for (String idFrom1 : idsFrom1) {
                                                if (id.contains(idFrom1) || idFrom1.contains(id)) {
                                                        isThere = true;
                                                }
                                        }
                                }
                        }
                        if (!isThere) {
                                return false;
                        }
                }


                return true;
        }

        private List<String> checkSpecieID(Species s1) {
                List<String> ids = new ArrayList<>();

                if (s1.getId().contains("C")) {
                        ids.add(s1.getId());

                }
                try {
                        String annotation = s1.getAnnotationString();

                        if (annotation.contains("kegg.compound")) {
                                annotation = annotation.substring(annotation.indexOf("kegg"), annotation.length());
                                annotation = annotation.substring(annotation.indexOf("C"), annotation.indexOf("C") + 6);
                                ids.add(annotation);
                        }
                        annotation = s1.getAnnotationString();
                        if (annotation.contains("CHEBI")) {
                                annotation = annotation.substring(annotation.indexOf("CHEBI"), annotation.length());
                                annotation = annotation.substring(annotation.indexOf(":") + 1, annotation.indexOf(":") + 6);
                                ids.add(annotation);
                        }
                        annotation = s1.getAnnotationString();
                        if (annotation.contains("kegg.genes")) {
                                annotation = annotation.substring(annotation.indexOf("sce"), annotation.length());
                                annotation = annotation.substring(annotation.indexOf(":") + 1, annotation.indexOf(":") + 6);
                                ids.add(annotation);
                        }
                        annotation = s1.getAnnotationString();
                        if (annotation.contains("uniprot")) {
                                annotation = annotation.substring(annotation.indexOf("uniprot"), annotation.length());
                                annotation = annotation.substring(annotation.indexOf("/") + 1, annotation.indexOf("/") + 7);
                                ids.add(annotation);
                        }

                } catch (Exception ex) {
                        //System.out.println(s1.getAnnotationString());                      
                }

                return ids;
        }

        private void printPathway(Reaction r2, Model m) {
                ListOf<SpeciesReference> products = r2.getListOfProducts();

                ListOf<Reaction> rs = m.getListOfReactions();


                for (SpeciesReference pref : products) {
                        Species p = pref.getSpeciesInstance();
                        List<String> ids = this.speciesidWhere.get(p);
                        boolean cont = false;
                        for (String id : ids) {
                                for (String reactant : this.removedCompounds) {
                                        if (id.contains(reactant)) {
                                                cont = true;
                                        }
                                }
                        }
                        if (cont) {
                                continue;
                        }

                        for (Reaction r : rs) {
                                if (r != r2) {
                                        ListOf<SpeciesReference> reactants = r.getListOfReactants();
                                        for (SpeciesReference reactantRef : reactants) {
                                                Species reactant = reactantRef.getSpeciesInstance();
                                                if (reactant.getId() == null ? p.getId() == null : reactant.getId().equals(p.getId())) {
                                                        String s = this.tf.getText();
                                                        s = s.concat(r2.getName() + " - " + r2.getId() + "--->" + p.getName() + " - " + p.getId() + "---> " + r.getName() + " - " + r.getId() + "\n");
                                                        this.tf.setText(s);
                                                        System.out.println(r2.getName() + " - " + r2.getId() + "--->" + p.getName() + " - " + p.getId() + "---> " + r.getName() + " - " + r.getId());
                                                        this.used.add(r2.getId());
                                                        if (!this.used.contains(r.getId())) {
                                                                printPathway(r, m);
                                                        }
                                                }

                                        }
                                }

                        }

                }
        }
}
