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

import MM.data.Dataset;
import MM.parameters.ParameterSet;
import MM.taskcontrol.AbstractTask;
import MM.taskcontrol.TaskStatus;
import com.csvreader.CsvWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.sbml.jsbml.*;

/**
 *
 * @author scsandra
 */
public class CompareReactionsTask extends AbstractTask {

        private Dataset[] datasets;
        private float progress = 0.0f;
        private String message = "Comparing Model Reactions... ";
        private String[] removedCompounds;
        HashMap<Reaction, String> names1, names2;
        ListOf<Species> species1, species2;
        HashMap<Species, List<String>> speciesid1, speciesid2;
        HashMap<Reaction, List<Reaction>> common, common2;
        private File fileName;

        public CompareReactionsTask(Dataset[] datasets, ParameterSet parameters) {
                this.datasets = datasets;
                this.removedCompounds = (String[]) parameters.getParameter(CompareReactionsParameters.removing).getValue();
                this.fileName = (File) parameters.getParameter(CompareReactionsParameters.fileName).getValue();
                this.names1 = new HashMap<>();
                this.names2 = new HashMap<>();
                this.speciesid1 = new HashMap<>();
                this.speciesid2 = new HashMap<>();
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
                                addChebToRemoved();


                                SBMLDocument doc1 = datasets[0].getDocument();
                                SBMLDocument doc2 = datasets[1].getDocument();
                                Model m1 = doc1.getModel();
                                Model m2 = doc2.getModel();
                                ListOf<Reaction> reactions1 = m1.getListOfReactions();
                                ListOf<Reaction> reactions2 = m2.getListOfReactions();
                                this.species1 = m1.getListOfSpecies();
                                this.species2 = m2.getListOfSpecies();


                                common = new HashMap<>();
                                common2 = new HashMap<>();

                                this.message = "Identifying reactions in Kegg..";
                                double total = reactions1.size() + reactions2.size();
                                double done = 0.0;

                                for (Reaction r1 : reactions1) {
                                        String name1 = checkName(r1.getName(), r1);
                                        names1.put(r1, name1);
                                        done++;
                                        progress = (float) (done / total);
                                }

                                for (Reaction r2 : reactions2) {
                                        String name2 = checkName(r2.getName(), r2);
                                        names2.put(r2, name2);
                                        done++;
                                        progress = (float) (done / total);
                                }



                                this.message = "Identifying species in Kegg..";
                                total = species1.size() + species2.size();
                                done = 0.0;
                                for (Species s1 : species1) {
                                        List<String> id = checkSpecieID(s1);
                                        speciesid1.put(s1, id);
                                        done++;
                                        progress = (float) (done / total);
                                }

                                for (Species s2 : species2) {
                                        List<String> id = checkSpecieID(s2);
                                        speciesid2.put(s2, id);
                                        done++;
                                        progress = (float) (done / total);
                                }


                                progress = 0.0f;
                                this.message = "Comparing names..";
                                done = 0.0;

                                for (Reaction r1 : reactions1) {
                                        boolean isThere = false;
                                        for (Reaction r2 : reactions2) {
                                                isThere = comparingReactionsRRPP(r1, speciesid1, r2, speciesid2);

                                                if (isThere) {
                                                        List<Reaction> reactions = common.get(r1);
                                                        if (reactions == null) {
                                                                reactions = new ArrayList<>();
                                                        }
                                                        reactions.add(r2);
                                                        common.put(r1, reactions);
                                                } else {
                                                        isThere = comparingReactionsRPRP(r1, speciesid1, r2, speciesid2);
                                                        if (isThere) {
                                                                List<Reaction> reactions = common.get(r1);
                                                                if (reactions == null) {
                                                                        reactions = new ArrayList<>();
                                                                }
                                                                reactions.add(r2);
                                                                common.put(r1, reactions);
                                                        }/*else{
                                                                System.out.println(r1.getId());
                                                        }*/
                                                        
                                                }
                                        }

                                        done++;
                                        progress = (float) (done / total);
                                }


                                /*
                                 * for (Reaction r2 : reactions2) { boolean
                                 * isThere = false; for (Reaction r1 :
                                 * reactions1) { isThere =
                                 * comparingReactionsRRPP(r2, speciesid2, r1,
                                 * speciesid1); if (isThere) { List<Reaction>
                                 * reactions = common2.get(r2); if (reactions ==
                                 * null) { reactions = new ArrayList<>(); }
                                 * reactions.add(r1); common2.put(r2,
                                 * reactions); } else { isThere =
                                 * comparingReactionsRPRP(r1, speciesid1, r2,
                                 * speciesid2); if (isThere) { List<Reaction>
                                 * reactions = common2.get(r2); if (reactions ==
                                 * null) { reactions = new ArrayList<>(); }
                                 * reactions.add(r1); common2.put(r2,
                                 * reactions); } } } done++; progress = (float)
                                 * (done / total);
                                }
                                 */

                                printResultsFile(reactions1, reactions2);

                        }
                } catch (Exception ex) {
                        Logger.getLogger(CompareReactionsTask.class.getName()).log(Level.ERROR, null, ex);
                }

                setStatus(TaskStatus.FINISHED);
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
                        return inputLine;

                } catch (IOException ex) {
                        return KeggID + " No reaction in Kegg";
                }
        }

        private String checkName(String name, Reaction r) {
                if (name.isEmpty()) {
                        return r.getId();
                } else {
                        return name;
                }
        }

        private boolean comparingReactionsRRPP(Reaction r1, HashMap<Species, List<String>> ids1, Reaction r2, HashMap<Species, List<String>> ids2) {
                ListOf<SpeciesReference> reactans1 = r1.getListOfReactants();
                ListOf<SpeciesReference> reactans2 = r2.getListOfReactants();

                ListOf<SpeciesReference> products1 = r1.getListOfProducts();
                ListOf<SpeciesReference> products2 = r2.getListOfProducts();

                int compoundsR1 = 0, compoundsR2 = 0;

                for (SpeciesReference sr1 : reactans1) {

                        List<String> ids = ids1.get(sr1.getSpeciesInstance());

                        // Remove the compounds that shouldn't be taken into account                       
                        if (hasToBeRemoved(ids)) {
                                continue;
                        }

                        // Compare the ids of the reactants
                        boolean isThere = false;
                        for (SpeciesReference sr2 : reactans2) {
                                List<String> idsFrom2 = ids2.get(sr2.getSpeciesInstance());
                                if (hasToBeRemoved(idsFrom2)) {
                                        continue;
                                }
                                for (String id : ids) {
                                        compoundsR1++;
                                        for (String idFrom2 : idsFrom2) {
                                                if (id.equals(idFrom2)) {
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
                        if (hasToBeRemoved(ids)) {
                                continue;
                        }

                        // Compare the ids of the reactants
                        boolean isThere = false;
                        for (SpeciesReference sr1 : reactans1) {
                                List<String> idsFrom1 = ids1.get(sr1.getSpeciesInstance());
                                if (hasToBeRemoved(idsFrom1)) {
                                        continue;
                                }
                                for (String id : ids) {
                                        compoundsR2++;
                                        for (String idFrom1 : idsFrom1) {
                                                if (id.equals(idFrom1)) {
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
                        if (hasToBeRemoved(ids)) {
                                continue;
                        }

                        // Compare the ids of the reactants
                        boolean isThere = false;
                        for (SpeciesReference sr2 : products2) {
                                List<String> idsFrom2 = ids2.get(sr2.getSpeciesInstance());
                                if (hasToBeRemoved(idsFrom2)) {
                                        continue;
                                }
                                for (String id : ids) {
                                        compoundsR1++;
                                        for (String idFrom2 : idsFrom2) {
                                                if (id.equals(idFrom2)) {
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
                        if (hasToBeRemoved(ids)) {
                                continue;
                        }
                        // Compare the ids of the reactants
                        boolean isThere = false;
                        for (SpeciesReference sr1 : products1) {
                                List<String> idsFrom1 = ids1.get(sr1.getSpeciesInstance());
                                if (hasToBeRemoved(idsFrom1)) {
                                        continue;
                                }
                                for (String id : ids) {
                                        compoundsR2++;
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


                if (compoundsR1 > 0 && compoundsR2 > 0) {
                        return true;
                } else {
                        return false;
                }
        }

        private boolean hasToBeRemoved(List<String> ids) {
                boolean hasToBeRemoved = false;
                for (String id : ids) {
                        for (String reactant : this.removedCompounds) {
                                if (id.equals(reactant)) {
                                        hasToBeRemoved = true;
                                }
                        }
                }
                return hasToBeRemoved;
        }

        private boolean comparingReactionsRPRP(Reaction r1, HashMap<Species, List<String>> ids1, Reaction r2, HashMap<Species, List<String>> ids2) {
                ListOf<SpeciesReference> reactans1 = r1.getListOfReactants();
                ListOf<SpeciesReference> reactans2 = r2.getListOfReactants();

                ListOf<SpeciesReference> products1 = r1.getListOfProducts();
                ListOf<SpeciesReference> products2 = r2.getListOfProducts();

                int compoundsR1 = 0, compoundsR2 = 0;

                for (SpeciesReference sr1 : reactans1) {

                        List<String> ids = ids1.get(sr1.getSpeciesInstance());

                        // Remove the compounds that shouldn't be taken into account                         
                        if (hasToBeRemoved(ids)) {
                                continue;
                        }

                        // Compare the ids of the reactants
                        boolean isThere = false;
                        for (SpeciesReference sr2 : products2) {
                                List<String> idsFrom2 = ids2.get(sr2.getSpeciesInstance());
                                if (hasToBeRemoved(idsFrom2)) {
                                        continue;
                                }
                                for (String id : ids) {
                                        compoundsR1++;
                                        for (String idFrom2 : idsFrom2) {
                                                if (id.equals(idFrom2)) {
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
                        if (hasToBeRemoved(ids)) {
                                continue;
                        }

                        // Compare the ids of the reactants
                        boolean isThere = false;
                        for (SpeciesReference sr1 : products1) {
                                List<String> idsFrom1 = ids1.get(sr1.getSpeciesInstance());
                                if (hasToBeRemoved(idsFrom1)) {
                                        continue;
                                }
                                for (String id : ids) {
                                        compoundsR2++;
                                        for (String idFrom1 : idsFrom1) {
                                                if (id.equals(idFrom1)) {
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
                        if (hasToBeRemoved(ids)) {
                                continue;
                        }

                        // Compare the ids of the reactants
                        boolean isThere = false;
                        for (SpeciesReference sr2 : reactans2) {
                                List<String> idsFrom2 = ids2.get(sr2.getSpeciesInstance());
                                if (hasToBeRemoved(idsFrom2)) {
                                        continue;
                                }
                                for (String id : ids) {
                                        compoundsR1++;
                                        for (String idFrom2 : idsFrom2) {
                                                if (id.equals(idFrom2)) {
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
                        if (hasToBeRemoved(ids)) {
                                continue;
                        }

                        // Compare the ids of the reactants
                        boolean isThere = false;
                        for (SpeciesReference sr1 : reactans1) {
                                List<String> idsFrom1 = ids1.get(sr1.getSpeciesInstance());
                                if (hasToBeRemoved(idsFrom1)) {
                                        continue;
                                }
                                for (String id : ids) {
                                        compoundsR2++;
                                        for (String idFrom1 : idsFrom1) {
                                                if (id.equals(idFrom1)) {
                                                        isThere = true;
                                                }
                                        }
                                }
                        }
                        if (!isThere) {
                                return false;
                        }
                }

                if (compoundsR1 > 0 && compoundsR2 > 0) {
                        return true;
                } else {
                        return false;
                }
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

        private void printResultsFile(ListOf<Reaction> reactions1, ListOf<Reaction> reactions2) {
                try {
                        CsvWriter w = new CsvWriter(this.fileName.getAbsolutePath());
                        String[] data = new String[8];
                        data[0] = "ID";
                        data[1] = "Name";
                        data[2] = "Reactants";
                        data[3] = "Products";
                        data[4] = "ID";
                        data[5] = "Name";
                        data[6] = "Reactants";
                        data[7] = "Products";

                        w.writeRecord(data);

                        //Common reactions
                        Iterator it = common.entrySet().iterator();
                        List<Reaction> reactionsKey = new ArrayList<>();
                        List<Reaction> reactionsVal = new ArrayList<>();
                        while (it.hasNext()) {
                                Map.Entry pairs = (Map.Entry) it.next();
                                Reaction r1 = (Reaction) pairs.getKey();
                                List<Reaction> r2s = (List<Reaction>) pairs.getValue();
                                for (Reaction r2 : r2s) {
                                        data = this.printReactionCSV(r1, r2);
                                        w.writeRecord(data);
                                        reactionsVal.add(r2);
                                }
                                if(r2s != null&& r2s.size() > 0){
                                        reactionsKey.add(r1);
                                }

                                it.remove(); // avoids a ConcurrentModificationException
                        }
                        //Reactions only in 1
                        for (Reaction r : reactions1) {
                                if (!reactionsKey.contains(r)) {
                                        data = this.printReactionCSV(r, 1);
                                        w.writeRecord(data);
                                }
                        }

                        //Reactions only in 2
                        for (Reaction r : reactions2) {
                                if (!reactionsVal.contains(r)) {
                                        data = this.printReactionCSV(r, 2);
                                        w.writeRecord(data);
                                }
                        }

                        w.close();

                } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(CompareReactionsTask.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
        }

        private String[] printReactionCSV(Reaction r1, Reaction r2) {
                String[] data = new String[8];
                data[0] = r1.getId();
                data[1] = r1.getName();
                String results = "";

                ListOf<SpeciesReference> reactans = r1.getListOfReactants();
                ListOf<SpeciesReference> products = r1.getListOfProducts();

                for (SpeciesReference reactan : reactans) {
                        Species s = reactan.getSpeciesInstance();
                        List<String> ids;

                        ids = speciesid1.get(s);
                        results = results.concat("ID: ");
                        for (String id : ids) {
                                results = results.concat(id + ",");
                        }
                        results = results = results.concat("-Name: " + s.getName() + ";");
                }

                data[2] = results;
                results = "";

                for (SpeciesReference product : products) {
                        Species s = product.getSpeciesInstance();
                        List<String> ids;

                        ids = speciesid1.get(s);

                        results = results.concat("ID: ");
                        for (String id : ids) {
                                results = results.concat(id + ",");
                        }
                        results = results.concat("-Name: " + s.getName() + ";");
                }
                data[3] = results;

                data[4] = r2.getId();
                data[5] = r2.getName();

                results = "";
                reactans = r2.getListOfReactants();
                products = r2.getListOfProducts();

                for (SpeciesReference reactan : reactans) {
                        Species s = reactan.getSpeciesInstance();
                        List<String> ids;

                        ids = speciesid2.get(s);

                        results = results.concat("ID: ");
                        for (String id : ids) {
                                results = results.concat(id + ",");
                        }
                        results = results.concat("-Name: " + s.getName() + ";");
                }
                data[6] = results;
                results = "";
                for (SpeciesReference product : products) {
                        Species s = product.getSpeciesInstance();
                        List<String> ids;

                        ids = speciesid2.get(s);

                        results = results.concat("ID: ");
                        for (String id : ids) {
                                results = results.concat(id + ",");
                        }
                        results = results.concat("-Name: " + s.getName() + ";");
                }

                data[7] = results;

                return data;
        }

        private String[] printReactionCSV(Reaction r, int dataset) {
                String[] data = new String[8];

                ListOf<SpeciesReference> reactans = r.getListOfReactants();
                ListOf<SpeciesReference> products = r.getListOfProducts();

                if (dataset == 1) {
                        data[0] = r.getId();
                        data[1] = r.getName();
                } else {
                        data[4] = r.getId();
                        data[5] = r.getName();
                }
                String results = "";

                for (SpeciesReference reactan : reactans) {
                        Species s = reactan.getSpeciesInstance();
                        List<String> ids;
                        if (dataset == 1) {
                                ids = speciesid1.get(s);
                        } else {
                                ids = speciesid2.get(s);
                        }
                        results = results.concat("ID: ");
                        for (String id : ids) {
                                results = results.concat(id + ",");
                        }
                        results = results.concat("-Name: " + s.getName() + ";");
                }
                if (dataset == 1) {
                        data[2] = results;
                } else {
                        data[6] = results;
                }

                results = "";
                for (SpeciesReference product : products) {
                        Species s = product.getSpeciesInstance();
                        List<String> ids;
                        if (dataset == 1) {
                                ids = speciesid1.get(s);
                        } else {
                                ids = speciesid2.get(s);
                        }
                        results = results.concat("ID: ");
                        for (String id : ids) {
                                results = results.concat(id + ",");
                        }
                        results = results.concat("-Name: " + s.getName() + ";");
                }

                if (dataset == 1) {
                        data[3] = results;
                } else {
                        data[7] = results;
                }

                return data;
        }

        private void addChebToRemoved() {
                List<String> chebR = new ArrayList<>();

                for (String kegg : this.removedCompounds) {
                        List<String> chebis = getChEbi(kegg);
                        for (String chebi : chebis) {
                                chebR.add(chebi);
                        }
                }

                this.removedCompounds = ArrayUtils.addAll(this.removedCompounds, chebR.toArray(new String[0]));
        }

        public List<String> getChEbi(String KeggID) {
                BufferedReader in = null;
                List<String> chebis = new ArrayList<>();
                try {
                        String query = "http://rest.kegg.jp/conv/chebi/" + KeggID;
                        URL kegg = new URL(query);
                        URLConnection yc = kegg.openConnection();
                        in = new BufferedReader(new InputStreamReader(
                                yc.getInputStream()));
                        String inputLine = null;
                        while ((inputLine = in.readLine()) != null) {
                                try {
                                        if (!inputLine.isEmpty()) {
                                                inputLine = inputLine.substring(inputLine.indexOf("chebi:") + 6);
                                                chebis.add(inputLine);
                                        } else {
                                        }
                                } catch (Exception e) {
                                }
                        }

                        in.close();

                        return chebis;
                } catch (IOException ex) {
                        return null;
                }
        }
}
