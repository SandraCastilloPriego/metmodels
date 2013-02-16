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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import javax.swing.JInternalFrame;
import javax.swing.JTextField;
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
        HashMap<Reaction, String> names1, names2;
        ListOf<Species> species;
        HashMap<Species, List<String>> speciesidFrom, speciesidWhere;
        List<Reaction> areNotIn2, areNotIn1;
        HashMap<Reaction, Reaction> common;

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
                                        
                                SBMLDocument doc1 = null, doc2 = null;
                                for(Dataset d :datasets){
                                        if(this.datasetFrom.contains(d.getDatasetName())){
                                                doc1 = d.getDocument();
                                        }
                                        if(this.datasetWhere.contains(d.getDatasetName())){
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

                               /* this.message = "Getting ChEbi ids..";
                                if (this.datasetWhere.equals(m1.getId())) {
                                        this.getIDs(this.species, 1);
                                } else {
                                        this.getIDs(this.species, 2);
                                }*/


                                for (String reactionId : this.reactionsIds) {
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
                                                /*if (this.datasetFrom.equals(m1.getId())) {
                                                        this.getIDs(s, 1);
                                                } else {
                                                        this.getIDs(s, 2);
                                                }*/
                                        }
                                        ListOf<SpeciesReference> products = r.getListOfProducts();
                                        for (SpeciesReference sr : products) {
                                                Species s = sr.getSpeciesInstance();
                                                List<String> id = checkSpecieID(s);
                                                speciesidFrom.put(s, id);
                                               /* if (this.datasetFrom.equals(m1.getId())) {
                                                        this.getIDs(s, 1);
                                                } else {
                                                        this.getIDs(s, 2);
                                                }*/
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
                                                        this.printReaction(r, 1);
                                                        this.printReaction(r2, 2);
                                                        System.out.println("------------------------------------------------------------------------");
                                                        System.out.println("------------------------------------------------------------------------");

                                                }
                                        }
                                }
                        }


                } catch (Exception ex) {
                        Logger.getLogger(SearchPathwaysTask.class.getName()).log(Level.ERROR, null, ex);
                }

                setStatus(TaskStatus.FINISHED);
        }

        public void printReaction(Reaction r, int dataset) {
                ListOf<SpeciesReference> reactans = r.getListOfReactants();
                ListOf<SpeciesReference> products = r.getListOfProducts();
                if (dataset == 1) {
                        System.out.println("Name: " + names1.get(r) + " - ID: "
                                + r.getId());
                } else {
                        System.out.println("Name: " + names2.get(r) + " - ID: "
                                + r.getId());
                }
                System.out.println("Reactans");

                for (SpeciesReference reactan : reactans) {
                        Species s = reactan.getSpeciesInstance();
                        List<String> ids;
                        if (dataset == 1) {
                                ids = speciesidFrom.get(s);
                        } else {
                                ids = speciesidWhere.get(s);
                        }
                        System.out.print("ID: ");
                        for (String id : ids) {
                                System.out.print(id + ",");
                        }
                        System.out.print("-Name: " + s.getName() + "\n");
                }
                System.out.println("Products");
                for (SpeciesReference product : products) {
                        Species s = product.getSpeciesInstance();
                        List<String> ids;
                        if (dataset == 1) {
                                ids = speciesidFrom.get(s);
                        } else {
                                ids = speciesidWhere.get(s);
                        }
                        System.out.print("ID: ");
                        for (String id : ids) {
                                System.out.print(id + ",");
                        }
                        System.out.print("-Name: " + s.getName() + "\n");
                }
                System.out.println("------------------------------------------------------------------------");
        }

        public String getNameEnzime(String KeggID) {
                BufferedReader in = null;
                try {
                        String query = "http://rest.kegg.jp/get/" + KeggID;
                        URL kegg = new URL(query);
                        URLConnection yc = kegg.openConnection();
                        in = new BufferedReader(new InputStreamReader(
                                yc.getInputStream()));
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                                if (inputLine.contains("NAME") || inputLine.contains("ORTHOLOGY")) {
                                        break;
                                }
                        }
                        in.close();
                        if (inputLine == null) {
                                inputLine = KeggID;
                        }
                        // System.out.println(inputLine);                        
                        return inputLine;

                } catch (IOException ex) {
                        return KeggID + " No reaction in Kegg";
                }
        }

        private String checkName(String name, Reaction r) {
                if (name.isEmpty()) {
                        //String newName = getNameEnzime(r.getId());
                        return r.getId();
                        // return newName;
                } else {
                        return name;
                }
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
                /*
                 * try { SpeciesType s = m.getSpeciesType("meta_"+s1.getId());
                 * String annotationType = s.getAnnotationString(); if
                 * (annotationType.contains("kegg.compound")) { annotationType =
                 * annotationType.substring(annotationType.indexOf("kegg"),
                 * annotationType.length()); annotationType =
                 * annotationType.substring(annotationType.indexOf("C"),
                 * annotationType.indexOf("C") + 6); ids.add(annotationType); }
                 * annotationType = s.getAnnotationString(); if
                 * (annotationType.contains("CHEBI")) { annotationType =
                 * annotationType.substring(annotationType.indexOf("CHEBI"),
                 * annotationType.length()); annotationType =
                 * annotationType.substring(annotationType.indexOf(":") + 1,
                 * annotationType.indexOf(":") + 6); ids.add(annotationType); }
                 * annotationType = s.getAnnotationString(); if
                 * (annotationType.contains("kegg.genes")) { annotationType =
                 * annotationType.substring(annotationType.indexOf("sce"),
                 * annotationType.length()); annotationType =
                 * annotationType.substring(annotationType.indexOf(":") + 1,
                 * annotationType.indexOf(":") + 6); ids.add(annotationType); }
                 * annotationType = s.getAnnotationString(); if
                 * (annotationType.contains("uniprot")) { annotationType =
                 * annotationType.substring(annotationType.indexOf("uniprot"),
                 * annotationType.length()); annotationType =
                 * annotationType.substring(annotationType.indexOf("/") + 1,
                 * annotationType.indexOf(":") + 7); ids.add(annotationType); }
                 * } catch (Exception e) { }
                 */


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

//        private void getIDs(ListOf<Species> species, int dataset) {
//                progress = 0.0f;
//                double total = species.size();
//                double done = 0.0;
//                for (Species s : species) {
//                        if (s.getId().contains("C")) {
//                                List<String> ids = null;
//                                if (dataset == 1) {
//                                        ids = this.speciesidFrom.get(s);
//                                } else {
//                                        ids = this.speciesidWhere.get(s);
//                                }
//                                String newName = getChEbi(s.getId());
//                                if (newName != null) {
//                                        ids.add(newName);
//                                }
//                        }
//                        done++;
//                        progress = (float) (done / total);
//                }
//        }
//
//        private void getIDs(Species specie, int dataset) {
//
//                if (specie.getId().contains("C")) {
//                        List<String> ids = null;
//                        if (dataset == 1) {
//                                ids = this.speciesidFrom.get(specie);
//                        } else {
//                                ids = this.speciesidWhere.get(specie);
//                        }
//                        String newName = getChEbi(specie.getId());
//                        if (newName != null) {
//                                ids.add(newName);
//                        }
//                }
//
//        }

//        public String getChEbi(String KeggID) {
//                BufferedReader in = null;
//                try {
//                        String query = "http://rest.kegg.jp/conv/chebi/" + KeggID;
//                        URL kegg = new URL(query);
//                        URLConnection yc = kegg.openConnection();
//                        in = new BufferedReader(new InputStreamReader(
//                                yc.getInputStream()));
//                        String inputLine = in.readLine();
//
//                        in.close();
//                        try {
//                                if (!inputLine.isEmpty()) {
//                                        inputLine = inputLine.substring(inputLine.indexOf("chebi:") + 6);
//                                        return inputLine;
//                                } else {
//                                        return null;
//                                }
//                        } catch (Exception e) {
//                                e.printStackTrace();
//                                return null;
//                        }
//
//                } catch (IOException ex) {
//                        ex.printStackTrace();
//                        return null;
//                }
//        }

        private void showResults() {
                this.message = "Printing..";
                double total = areNotIn2.size() + areNotIn1.size() + common.size();
                double done = 0.0;
                JTextField t = new JTextField();
                String results = "";
                results.concat("List of reactions not present in " + datasets[1].getDatasetName() + "\n");

                for (Reaction r : areNotIn2) {
                        this.printReaction(r, 1, results);
                        done++;
                        progress = (float) (done / total);
                        results.concat("------------------------------------------------------------------------\n");
                }

                results.concat("------------------------------------------------------------------------\n");
                results.concat("------------------------------------------------------------------------\n");
                results.concat("------------------------------------------------------------------------\n");
                results.concat("------------------------------------------------------------------------\n");
                results.concat("------------------------------------------------------------------------\n");

                results.concat("List of reactions not present in " + datasets[0].getDatasetName() + "\n");
                for (Reaction r : areNotIn1) {
                        this.printReaction(r, 2, results);
                        done++;
                        progress = (float) (done / total);
                        results.concat("------------------------------------------------------------------------\n");
                }

                results.concat("------------------------------------------------------------------------\n");
                results.concat("------------------------------------------------------------------------\n");
                results.concat("------------------------------------------------------------------------\n");
                results.concat("------------------------------------------------------------------------\n");
                results.concat("------------------------------------------------------------------------\n");

                results.concat("Common reactions\n");

                Iterator it = common.entrySet().iterator();
                while (it.hasNext()) {
                        Map.Entry pairs = (Map.Entry) it.next();
                        Reaction r1 = (Reaction) pairs.getKey();
                        Reaction r2 = (Reaction) pairs.getValue();
                        this.printReaction(r1, 1, results);
                        this.printReaction(r2, 2, results);
                        results.concat("------------------------------------------------------------------------\n");
                        results.concat("------------------------------------------------------------------------\n");
                        it.remove(); // avoids a ConcurrentModificationException
                }

                t.setText(results);
                JInternalFrame internalFrame = new JInternalFrame("Results", true, true, true, true);
                internalFrame.add(t);
                MMCore.getDesktop().addInternalFrame(internalFrame);
                internalFrame.setVisible(true);
        }

        public void printReaction(Reaction r, int dataset, String results) {
                ListOf<SpeciesReference> reactans = r.getListOfReactants();
                ListOf<SpeciesReference> products = r.getListOfProducts();
                if (dataset == 1) {
                        results.concat("Name: " + names1.get(r) + " - ID: "
                                + r.getId() + "\n");
                } else {
                        results.concat("Name: " + names2.get(r) + " - ID: "
                                + r.getId() + "\n");
                }
                results.concat("Reactans\n");

                for (SpeciesReference reactan : reactans) {
                        Species s = reactan.getSpeciesInstance();
                        List<String> ids;
                        if (dataset == 1) {
                                ids = speciesidFrom.get(s);
                        } else {
                                ids = speciesidWhere.get(s);
                        }
                        results.concat("ID: ");
                        for (String id : ids) {
                                System.out.print(id + ",");
                        }
                        results.concat("-Name: " + s.getName() + "\n");
                }
                results.concat("Products\n");
                for (SpeciesReference product : products) {
                        Species s = product.getSpeciesInstance();
                        List<String> ids;
                        if (dataset == 1) {
                                ids = speciesidFrom.get(s);
                        } else {
                                ids = speciesidWhere.get(s);
                        }
                        results.concat("ID: ");
                        for (String id : ids) {
                                results.concat(id + ",");
                        }
                        results.concat("-Name: " + s.getName() + "\n");
                }
                results.concat("------------------------------------------------------------------------\n");
        }
}
