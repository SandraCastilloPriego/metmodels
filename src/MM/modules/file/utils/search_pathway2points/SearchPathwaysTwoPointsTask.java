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
package MM.modules.file.utils.search_pathway2points;

import MM.data.Dataset;
import MM.main.MMCore;
import MM.parameters.ParameterSet;
import MM.taskcontrol.AbstractTask;
import MM.taskcontrol.TaskStatus;
import java.awt.Dimension;
import java.util.ArrayList;
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
public class SearchPathwaysTwoPointsTask extends AbstractTask {

        private Dataset[] datasets;
        private float progress = 0.0f;
        private String message = "Search for Pathways between two points... ";
        private String dataset, initialId, finalId;
        private String[] toBeRemoved;
        private JInternalFrame frame;
        private JScrollPane panel;
        private JTextArea tf;
        private List<Pathway> pathway;
        private List<Reaction> initialReaction;
        private List<String> nodes, edges;
        private int nID = 0;

        public SearchPathwaysTwoPointsTask(Dataset[] datasets, ParameterSet parameters) {
                this.datasets = datasets;
                this.dataset = (String) parameters.getParameter(SearchPathwaysTwoPointsParameters.data).getValue();
                this.initialId = (String) parameters.getParameter(SearchPathwaysTwoPointsParameters.idFrom).getValue();
                this.finalId = (String) parameters.getParameter(SearchPathwaysTwoPointsParameters.idTo).getValue();
                this.toBeRemoved = (String[]) parameters.getParameter(SearchPathwaysTwoPointsParameters.removing).getValue();

                this.pathway = new ArrayList<>();
                this.initialReaction = new ArrayList<>();

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
                        this.findPathway();
                } catch (Exception e) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = e.toString();
                }
        }

        public void findPathway() {
                setStatus(TaskStatus.PROCESSING);
                try {
                        if (getStatus() == TaskStatus.PROCESSING) {

                                // opens a frame to show the results

                                frame.setSize(new Dimension(700, 500));
                                frame.add(this.panel);


                                MMCore.getDesktop().addInternalFrame(frame);


                                // opens the data sets and selects the one writen by the user
                                SBMLDocument doc = null;
                                for (Dataset d : datasets) {
                                        if (this.dataset.contains(d.getDatasetName())) {
                                                doc = d.getDocument();
                                        }
                                }

                                Model m = doc.getModel();

                                // Searchs for the reactions that contains the initial metabolite between the reactans or products and adds them in the array "reactionsIds"

                                // List<String> reactionsIds = new ArrayList<>();
                                // List<Boolean> inReactantsList = new ArrayList<>();
                                for (Reaction r : m.getListOfReactions()) {
                                        boolean isInProducts = isInProducts(r, this.initialId);
                                        boolean isInReactants = isInReactants(r, this.initialId);
                                        if (isInProducts || isInReactants) {
                                                System.out.println(r.getId() + " - " + isInProducts + " - " + isInReactants);
                                                Pathway path = new Pathway();
                                                String Id1 = String.valueOf("n" + nID);
                                                nID++;
                                                path.addNodes(Id1 + " - " + this.initialId);
                                                this.pathway.add(path);
                                                printPathway(this.initialId, path, m.getReaction(r.getId()), isInReactants, m);
                                        }

                                        /* for (SpeciesReference reactant : r.getListOfReactants()) {
                                         Species s = reactant.getSpeciesInstance();

                                         if (s.getId() == null ? initialId == null : s.getId().equals(initialId)) {
                                         reactionsIds.add(r.getId());
                                         inReactantsList.add(true);
                                         found = true;
                                         break;
                                         }
                                         }
                                         if (!found) {
                                         for (SpeciesReference product : r.getListOfProducts()) {
                                         Species s = product.getSpeciesInstance();
                                         if (s.getId() == null ? initialId == null : s.getId().equals(initialId)) {
                                         reactionsIds.add(r.getId());
                                         inReactantsList.add(false);
                                         }
                                         }
                                         }*/
                                }


                                this.cleanPathway();
                                this.printPathway();

                                /* // For each reaction in the array "reactionsIds" search the pathway until the final metabolite
                                 for (int i = 0; i < reactionsIds.size(); i++) {
                                 String reactionId = reactionsIds.get(i);
                                 Boolean inReactants = inReactantsList.get(i);
                                 // Keeps a list of used reactions to avoid infinite loops.

                                 // this.used.add(reactionId);
                                 String s = this.tf.getText();
                                 s = s.concat("Reaction: " + reactionId + "\n");
                                 this.tf.setText(s);

                                 this.initialReaction.add(m.getReaction(reactionId));

                                 // If the pathway is found, prints it
                                 Pathway path = new Pathway();
                                 String Id1 = String.valueOf("n" + nID);
                                 nID++;
                                 path.addNodes(Id1 + " - " + this.initialId);
                                 this.pathway.add(path);
                                 printPathway(this.initialId, path, m.getReaction(reactionId), inReactants, m);




                                 }*/
                        }


                } catch (Exception ex) {
                        Logger.getLogger(SearchPathwaysTwoPointsTask.class.getName()).log(Level.ERROR, null, ex);
                }

                setStatus(TaskStatus.FINISHED);
        }

        private void printPathway(String initialID, Pathway path, Reaction reaction, Boolean inReactants, Model m) {
                // For each product of the reaction
                searchThread t = new searchThread(initialID, path, reaction, inReactants, m);
                t.start();               
        }

        private void setPath(String rName, String rID, String pName, String pID, String rName2, String rID2, Pathway path) {

                String Id1 = path.getNodeID(rName + "-" + rID);
                if (Id1 == null) {
                        Id1 = String.valueOf("n" + nID);
                        nID++;
                        path.addNodes(Id1 + " - " + rName + "-" + rID);
                }

                String Id2 = path.getNodeID(pName + "-" + pID);
                if (Id2 == null) {
                        Id2 = String.valueOf("n" + nID);
                        path.addNodes(Id2 + " - " + pName + "-" + pID);
                        nID++;
                }

                path.addEdges(Id1 + "-" + Id2);

                if (rName2 != null) {
                        String Id3 = path.getNodeID(rName2 + "-" + rID2);
                        if (Id3 == null) {
                                Id3 = String.valueOf("n" + nID);
                                nID++;
                                path.addNodes(Id3 + " - " + rName2 + "-" + rID2);
                        }
                        path.addEdges(Id2 + "-" + Id3);
                }


        }

        private void printPathway() {
                String text = this.tf.getText();

                String initial = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?> <graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:y=\"http://www.yworks.com/xml/graphml\" xmlns:yed=\"http://www.yworks.com/xml/yed/3\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd\">";
                text = text.concat(initial + "\n");



                String opening = "\t<key for=\"graphml\" id=\"d0\" yfiles.type=\"resources\"/>"
                        + "\t<key for=\"port\" id=\"d1\" yfiles.type=\"portgraphics\"/>"
                        + "\t<key for=\"port\" id=\"d2\" yfiles.type=\"portgeometry\"/>"
                        + "\t<key for=\"port\" id=\"d3\" yfiles.type=\"portuserdata\"/>"
                        + "\t<key attr.name=\"url\" attr.type=\"string\" for=\"node\" id=\"d4\"/>"
                        + "\t<key attr.name=\"description\" attr.type=\"string\" for=\"node\" id=\"d5\"/>"
                        + "\t<key for=\"node\" id=\"d6\" yfiles.type=\"nodegraphics\"/>"
                        + "\t<key attr.name=\"url\" attr.type=\"string\" for=\"edge\" id=\"d7\"/>"
                        + "\t<key attr.name=\"description\" attr.type=\"string\" for=\"edge\" id=\"d8\"/>"
                        + "\t<key for=\"edge\" id=\"d9\" yfiles.type=\"edgegraphics\"/>"
                        + "\t<graph edgedefault=\"directed\" id=\"G\">";
                text = text.concat(opening + "\n");




                for (String node : nodes) {
                        //  System.out.println(node);
                        String[] ids = node.split(" - ");
                        String n = "\t <node id=\"" + ids[0] + "\">\n\t\t<data key=\"d6\">\n"
                                + "\t\t\t<y:ShapeNode>\n"
                                + "\t\t\t\t<y:NodeLabel alignment=\"center\" autoSizePolicy=\"content\" fontFamily=\"Dialog\" fontSize=\"20\" fontStyle=\"plain\" hasBackgroundColor=\"false\" hasLineColor=\"false\"  modelName=\"internal\" modelPosition=\"c\" textColor=\"#000000\" visible=\"true\">" + ids[1] + "</y:NodeLabel>\n"
                                + "\t\t\t</y:ShapeNode>\n"
                                + "\t\t</data>\n"
                                + "\t</node>";
                        text = text.concat(n + "\n");
                }


                int id = 0;
                for (String e : edges) {
                        String[] est = e.split("-");
                        String ed = "\t<edge id=\"e" + id + "\" source=\"" + est[0] + "\" target=\"" + est[1] + "\">\n\t\t<data key=\"d9\">\n"
                                + "\t\t\t<y:PolyLineEdge>\n"
                                + "\t\t\t\t<y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/>\n"
                                + "\t\t\t\t<y:LineStyle color=\"#000000\" type=\"line\" width=\"1.0\"/>\n"
                                + "\t\t\t\t<y:Arrows source=\"none\" target=\"none\"/>\n"
                                + "\t\t\t\t<y:BendStyle smoothed=\"false\"/>\n"
                                + "\t\t\t</y:PolyLineEdge>\n"
                                + "\t\t</data>\n"
                                + "\t</edge>";
                        text = text.concat(ed + "\n");
                        id++;
                }

                String fin = "</graph>\n</graphml>";
                text = text.concat(fin + "\n");

                this.tf.setText(text);
        }

        private boolean isNotCofactor(Species p) {
                for (String toRemove : this.toBeRemoved) {
                        if (p.getId().contains(toRemove)) {
                                return false;
                        }
                }

                return true;

        }

//        private List<String> getNodes() {
//                List<String> nodes = new ArrayList<>();
//                for (int i = 0; i < pathway.size(); i++) {
//                        String node = pathway.get(i);
//                        String[] ss = node.split("--->");
//                        for (String s : ss) {
//                                s = s.replaceAll("\\W", "");
//                                if (!nodes.contains(s)) {
//                                        nodes.add(s);
//                                }
//                        }
//                }
//                return nodes;
//        }
//        private List<String> getEdges(List<String> nodes) {
//                List<String> edges = new ArrayList<>();
//
//                for (int i = 0; i < pathway.size(); i++) {
//                        try {
//                                String node = pathway.get(i);
//                                String[] ss = node.split("--->");
//                                for (int e = 0; e < ss.length; e++) {
//                                        ss[e] = ss[e].replaceAll("\\W", "");
//                                }
//
//                                String index1 = String.valueOf(nodes.indexOf(ss[0]));
//                                String index2 = String.valueOf(nodes.indexOf(ss[1]));
//                                String e = "n" + index1 + "-" + "n" + index2;
//                                if (!edges.contains(e)) {
//                                        edges.add(e);
//                                }
//
//                                try {
//                                        String index3 = String.valueOf(nodes.indexOf(ss[2]));
//                                        String e2 = "n" + index2 + "-" + "n" + index3;
//                                        if (!edges.contains(e2)) {
//                                                edges.add(e2);
//                                        }
//                                } catch (Exception ee) {
//                                }
//
//
//                        } catch (Exception ex) {
//                                ex.printStackTrace();
//                        }
//                }
//
//                return edges;
//        }
        private void cleanPathway() {
                List<Pathway> finalPaths = new ArrayList<>();

                for (Pathway path : pathway) {
                        if (path.contains(this.finalId)) {
                                finalPaths.add(path);
                        }
                }

                System.out.println(finalPaths.size() + " - " + pathway.size());
                int l = Integer.MAX_VALUE;
                Pathway p = null;
                for (Pathway path : finalPaths) {
                        if (path.getSize() < l) {
                                p = path;
                                l = path.getSize();
                        }
                }

                if (p != null) {
                        this.nodes = p.getNodes();
                        this.edges = p.getEdges();
                }


        }

        public class searchThread extends Thread {

                String initialID;
                Pathway path;
                Reaction reaction;
                Boolean inReactants;
                Model m;
                private List<String> used;
                boolean exit;

                public searchThread(String initialID, Pathway path, Reaction reaction, Boolean inReactants, Model m) {
                        this.inReactants = inReactants;
                        this.initialID = initialID;
                        this.reaction = reaction;
                        this.m = m;
                        this.path = path;
                        this.used = new ArrayList<>();
                        this.used.add(initialID);
                }

                public void run2() {
                        if (inReactants && !exit) {
                                for (SpeciesReference pref : reaction.getListOfProducts()) {

                                        Species p = pref.getSpeciesInstance();
                                        if (p.getId().equals(initialID)) {
                                                continue;
                                        }

                                        //if the product id is the same as the final id, the algorithm is over.
                                        if (p.getId().equals(finalId)) {
                                                setPath(reaction.getName(), reaction.getId(), p.getName(), p.getId(), null, null, path);
                                                exit = true;
                                                System.out.println("Bieeeen");
                                                break;
                                        }
                                        if (isNotCofactor(p)) {
                                                for (Reaction r : m.getListOfReactions()) {
                                                        if (r != reaction && !exit) {
                                                                ListOf<SpeciesReference> reactants = r.getListOfReactants();
                                                                if (!used.contains(r.getId())) {
                                                                        for (SpeciesReference reactantRef : reactants) {
                                                                                Species reactant = reactantRef.getSpeciesInstance();

                                                                                if (!used.contains(reactant.getId())) {

                                                                                        if (reactant.getId() == null ? p.getId() == null : reactant.getId().equals(p.getId())) {
                                                                                                Pathway newPath = path.getCopy();
                                                                                                setPath(reaction.getName(), reaction.getId(), p.getName(), p.getId(), r.getName(), r.getId(), newPath);

                                                                                                printPathway(reactant.getId(), newPath, r, true, m);
                                                                                                used.add(reactant.getId());
                                                                                                pathway.add(newPath);
                                                                                                this.used.add(r.getId());
                                                                                                System.out.println("P - reactants: " + newPath.toString());
                                                                                        }
                                                                                }

                                                                        }

                                                                        ListOf<SpeciesReference> products = r.getListOfProducts();
                                                                        for (SpeciesReference reactantRef : products) {
                                                                                Species reactant = reactantRef.getSpeciesInstance();
                                                                                if (!used.contains(reactant.getId())) {
                                                                                        if (reactant.getId() == null ? p.getId() == null : reactant.getId().equals(p.getId())) {
                                                                                                Pathway newPath = path.getCopy();
                                                                                                setPath(reaction.getName(), reaction.getId(), p.getName(), p.getId(), r.getName(), r.getId(), newPath);

                                                                                                printPathway(reactant.getId(), newPath, r, false, m);
                                                                                                used.add(reactant.getId());
                                                                                                pathway.add(newPath);
                                                                                                this.used.add(r.getId());
                                                                                                System.out.println("P - products: " + newPath.toString());
                                                                                        }

                                                                                }
                                                                        }
                                                                }
                                                        }

                                                }
                                        }
                                        pathway.remove(path);

                                }
                        } else if (!exit) {

                                for (SpeciesReference pref : reaction.getListOfReactants()) {

                                        Species p = pref.getSpeciesInstance();

                                        if (p.getId().equals(initialID)) {
                                                continue;
                                        }
                                        //if the product id is the same as the final id, the algorithm is over.
                                        if (p.getId().equals(finalId)) {
                                                System.out.println("Bieeeen");
                                                setPath(reaction.getName(), reaction.getId(), p.getName(), p.getId(), null, null, path);
                                                exit = true;
                                                break;
                                        }
                                        if (isNotCofactor(p)) {
                                                for (Reaction r : m.getListOfReactions()) {
                                                        if (r != reaction && !exit) {
                                                                ListOf<SpeciesReference> products = r.getListOfProducts();
                                                                if (!used.contains(r.getId())) {
                                                                        for (SpeciesReference reactantRef : products) {
                                                                                Species reactant = reactantRef.getSpeciesInstance();
                                                                                if (!used.contains(reactant.getId())) {
                                                                                        if (reactant.getId() == null ? p.getId() == null : reactant.getId().equals(p.getId())) {
                                                                                                Pathway newPath = path.getCopy();
                                                                                                setPath(reaction.getName(), reaction.getId(), p.getName(), p.getId(), r.getName(), r.getId(), newPath);

                                                                                                this.used.add(reactant.getId());

                                                                                                printPathway(reactant.getId(), newPath, r, false, m);

                                                                                                pathway.add(newPath);

                                                                                                System.out.println("R - products: " + newPath.toString());
                                                                                                this.used.add(r.getId());
                                                                                        }
                                                                                }
                                                                        }

                                                                        ListOf<SpeciesReference> reactants = r.getListOfReactants();
                                                                        for (SpeciesReference reactantRef : reactants) {
                                                                                Species reactant = reactantRef.getSpeciesInstance();
                                                                                if (!used.contains(reactant.getId())) {
                                                                                        if (reactant.getId() == null ? p.getId() == null : reactant.getId().equals(p.getId())) {
                                                                                                Pathway newPath = path.getCopy();
                                                                                                setPath(reaction.getName(), reaction.getId(), p.getName(), p.getId(), r.getName(), r.getId(), newPath);

                                                                                                this.used.add(reactant.getId());

                                                                                                printPathway(reactant.getId(), newPath, r, true, m);
                                                                                                this.used.add(r.getId());
                                                                                                pathway.add(newPath);
                                                                                                System.out.println("R - reactants: " + newPath.toString());
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                        }

                                                }
                                        }
                                        pathway.remove(path);

                                }
                        }
                }

                public void run() {
                        if (this.inReactants) {
                                //for each product of the reaction check what are the other reactions that contain it..
                                for (SpeciesReference pref : reaction.getListOfProducts()) {
                                        Species specie = pref.getSpeciesInstance();

                                        if (specie.getId().equals(finalId)) {
                                                System.out.println("Bieeeen");
                                                setPath(reaction.getName(), reaction.getId(), specie.getName(), specie.getId(), null, null, path);
                                                exit = true;
                                                break;
                                        }
                                        // check the reactions where the product is
                                        if (isNotCofactor(specie)) {
                                                for (Reaction r : m.getListOfReactions()) {
                                                        if (!r.getId().equals(reaction.getId())) {
                                                                boolean isInProducts = isInProducts(r, specie.getId());
                                                                boolean isInReactants = isInReactants(r, specie.getId());
                                                                if (isInProducts || isInReactants) {
                                                                        Pathway newPath = path.getCopy();
                                                                        setPath(reaction.getName(), reaction.getId(), specie.getName(), specie.getId(), r.getName(), r.getId(), newPath);
                                                                        printPathway(specie.getId(), newPath, r, isInReactants, m);
                                                                        System.out.println("R - reactants: " + newPath.toString());
                                                                }
                                                        }

                                                }
                                        }
                                }
                        } else {
                                //for each product of the reaction..
                                for (SpeciesReference pref : reaction.getListOfReactants()) {
                                        Species specie = pref.getSpeciesInstance();

                                        if (specie.getId().equals(finalId)) {
                                                System.out.println("Bieeeen");
                                                setPath(reaction.getName(), reaction.getId(), specie.getName(), specie.getId(), null, null, path);
                                                exit = true;
                                                break;
                                        }
                                        if (isNotCofactor(specie)) {
                                                for (Reaction r : m.getListOfReactions()) {
                                                        if (!r.getId().equals(reaction.getId())) {
                                                                boolean isInProducts = isInProducts(r, specie.getId());
                                                                boolean isInReactants = isInReactants(r, specie.getId());
                                                                if (isInProducts || isInReactants) {
                                                                        Pathway newPath = path.getCopy();
                                                                        setPath(reaction.getName(), reaction.getId(), specie.getName(), specie.getId(), r.getName(), r.getId(), newPath);
                                                                        printPathway(specie.getId(), newPath, r, isInReactants, m);
                                                                        System.out.println("P - reactants: " + newPath.toString());
                                                                }
                                                        }

                                                }
                                        }
                                }
                        }

                }
        }

        public boolean isInProducts(Reaction reaction, String s) {
                ListOf<SpeciesReference> products = reaction.getListOfProducts();
                for (SpeciesReference reactantRef : products) {
                        Species product = reactantRef.getSpeciesInstance();
                        // if (!used.contains(product.getId())) {
                        if (product.getId() != null && product.getId().equals(s)) {
                                return true;
                        }
                        // }
                }
                return false;
        }

        public boolean isInReactants(Reaction reaction, String s) {
                ListOf<SpeciesReference> reactants = reaction.getListOfReactants();
                for (SpeciesReference reactantRef : reactants) {
                        Species reactant = reactantRef.getSpeciesInstance();
                        // if (!used.contains(reactant.getId())) {
                        if (reactant.getId() != null && reactant.getId().equals(s)) {
                                return true;
                        }
                        // }
                }
                return false;
        }
}
