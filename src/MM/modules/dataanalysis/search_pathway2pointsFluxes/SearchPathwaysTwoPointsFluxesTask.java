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
package MM.modules.dataanalysis.search_pathway2pointsFluxes;

import MM.data.Dataset;
import MM.main.MMCore;
import MM.parameters.ParameterSet;
import MM.taskcontrol.AbstractTask;
import MM.taskcontrol.TaskStatus;
import com.csvreader.CsvReader;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class SearchPathwaysTwoPointsFluxesTask extends AbstractTask {

        private Dataset[] datasets;
        private float progress = 0.0f;
        private String message = "Search for Pathways between two points using fluxes... ";
        private String dataset, initialId, finalId;
        private String[] toBeRemoved;
        private JInternalFrame frame;
        private JScrollPane panel;
        private JTextArea tf;
        private List<Node> nodes, fnodes, enodes;
        private List<Edge> edges, fedges, eedges;
        private Map<String, Integer> location;
        private File fluxes;
        private int expansion;
        private Model m;

        public SearchPathwaysTwoPointsFluxesTask(Dataset[] datasets, ParameterSet parameters) {
                this.datasets = datasets;
                this.dataset = (String) parameters.getParameter(SearchPathwaysTwoPointsFluxesParameters.data).getValue();
                this.initialId = (String) parameters.getParameter(SearchPathwaysTwoPointsFluxesParameters.idFrom).getValue();
                this.finalId = (String) parameters.getParameter(SearchPathwaysTwoPointsFluxesParameters.idTo).getValue();
                this.toBeRemoved = (String[]) parameters.getParameter(SearchPathwaysTwoPointsFluxesParameters.removing).getValue();
                this.fluxes = (File) parameters.getParameter(SearchPathwaysTwoPointsFluxesParameters.fileName).getValue();
                this.frame = new JInternalFrame("Result", true, true, true, true);
                this.expansion = parameters.getParameter(SearchPathwaysTwoPointsFluxesParameters.expansion).getValue();
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
                        nodes = new ArrayList<>();
                        edges = new ArrayList<>();
                        fnodes = new ArrayList<>();
                        fedges = new ArrayList<>();
                        enodes = new ArrayList<>();
                        eedges = new ArrayList<>();
                        location = new HashMap<>();
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
                                HashMap<String, Double> readFluxes = readFluxes();
                                // opens a frame to show the results

                                frame.setSize(new Dimension(700, 500));
                                frame.add(this.panel);
                                MMCore.getDesktop().addInternalFrame(frame);

                                // creates the network representation
                                this.createNetwork(readFluxes);
                                boolean working = true;
                                while (working) {
                                        try {
                                                Graph graph = new Graph(nodes, edges);
                                                Dijkstra dijkstra = new Dijkstra(graph);
                                                dijkstra.execute(nodes.get(location.get(this.initialId)));
                                                LinkedList<Node> path = dijkstra.getPath(nodes.get(location.get(this.finalId)));
                                                if (path.size() == 0) {
                                                        working = false;
                                                        break;
                                                }
                                                for (int i = 0; i < path.size(); i++) {
                                                        fnodes.add(path.get(i));
                                                        System.out.println(path.get(i).getId());
                                                        if (i < path.size() - 1) {
                                                                Edge e = this.searchReaction(path.get(i), path.get(i + 1));
                                                                System.out.println(e.getId());
                                                                fedges.add(e);
                                                                this.edges.remove(e);
                                                        }
                                                }
                                        } catch (Exception e) {
                                                working = false;
                                        }
                                }
                                expansion(readFluxes);
                                printPathway();
                        }


                } catch (Exception ex) {
                }

                setStatus(TaskStatus.FINISHED);
        }

        private Edge searchReaction(Node n, Node n2) {
                for (Edge e : this.edges) {
                        if (e.getSource() == n && e.getDestination() == n2) {
                                return e;
                        }
                }
                return null;
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




                for (Node node : fnodes) {
                        String n = "\t <node id=\"" + node.getId() + "\">\n\t\t<data key=\"d6\">\n"
                                + "\t\t\t<y:ShapeNode>\n"
                                + "\t\t\t\t<y:NodeLabel alignment=\"center\" autoSizePolicy=\"content\" fontFamily=\"Dialog\" fontSize=\"20\" fontStyle=\"plain\" hasBackgroundColor=\"false\" hasLineColor=\"false\"  modelName=\"internal\" modelPosition=\"c\" textColor=\"#000000\" visible=\"true\">" + node.getId() + " - " + node.getName() + "</y:NodeLabel>\n"
                                + "\t\t\t</y:ShapeNode>\n"
                                + "\t\t</data>\n"
                                + "\t</node>";
                        text = text.concat(n + "\n");
                }

                for (Node node : enodes) {
                        String n = "\t <node id=\"" + node.getId() + "\">\n\t\t<data key=\"d6\">\n"
                                + "\t\t\t<y:ShapeNode>\n"
                                + "\t\t\t\t<y:Fill color=\"#FF0000\" transparent=\"false\"/>"
                                + "\t\t\t\t<y:NodeLabel alignment=\"center\" autoSizePolicy=\"content\" fontFamily=\"Dialog\" fontSize=\"20\" fontStyle=\"plain\" hasBackgroundColor=\"false\" hasLineColor=\"false\"  modelName=\"internal\" modelPosition=\"c\" textColor=\"#000000\" visible=\"true\">" + node.getId() + " - " + node.getName() + "</y:NodeLabel>\n"
                                + "\t\t\t</y:ShapeNode>\n"
                                + "\t\t</data>\n"
                                + "\t</node>";
                        text = text.concat(n + "\n");
                }




                for (Edge e : fedges) {
                        String source = null, destination = null, arrowSource = "none", arrowTarget = "none";
                        if (e.getId().contains("rev")) {
                                destination = e.getSource().getId();
                                source = e.getDestination().getId();
                                arrowSource = "standard";
                        } else {
                                source = e.getSource().getId();
                                destination = e.getDestination().getId();
                                arrowTarget = "standard";
                        }
                        String ed = "\t<edge id=\"" + e.getId() + "\" source=\"" + source + "\" target=\"" + destination + "\">\n\t\t<data key=\"d9\">\n"
                                + "\t\t\t<y:PolyLineEdge>\n"
                                + "\t\t\t\t<y:EdgeLabel alignment=\"center\" configuration=\"AutoFlippingLabel\" fontFamily=\"Dialog\" fontSize=\"12\" fontStyle=\"plain\" hasBackgroundColor=\"false\" hasLineColor=\"false\" modelName=\"custom\" preferredPlacement=\"anywhere\" ratio=\"0.5\" textColor=\"#000000\" visible=\"true\">" + e.getLabel()
                                + "\t\t\t\t</y:EdgeLabel>"
                                + "\t\t\t\t<y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/>\n"
                                + "\t\t\t\t<y:LineStyle color=\"#000000\" type=\"line\" width=\"1.0\"/>\n"
                                + "\t\t\t\t<y:Arrows source=\"" + arrowSource + "\" target=\"" + arrowTarget + "\"/>\n"
                                + "\t\t\t\t<y:BendStyle smoothed=\"false\"/>\n"
                                + "\t\t\t</y:PolyLineEdge>\n"
                                + "\t\t</data>\n"
                                + "\t</edge>";
                        text = text.concat(ed + "\n");
                }

                for (Edge e : eedges) {
                        String source = null, destination = null, arrowSource = "none", arrowTarget = "none";
                        if (e.getId().contains("rev")) {
                                destination = e.getSource().getId();
                                source = e.getDestination().getId();
                                arrowSource = "standard";
                        } else {
                                source = e.getSource().getId();
                                destination = e.getDestination().getId();
                                arrowTarget = "standard";
                        }
                        String ed = "\t<edge id=\"" + e.getId() + "\" source=\"" + source + "\" target=\"" + destination + "\">\n\t\t<data key=\"d9\">\n"
                                + "\t\t\t<y:PolyLineEdge>\n"
                                + "\t\t\t\t<y:EdgeLabel alignment=\"center\" configuration=\"AutoFlippingLabel\" fontFamily=\"Dialog\" fontSize=\"12\" fontStyle=\"plain\" hasBackgroundColor=\"false\" hasLineColor=\"false\" modelName=\"custom\" preferredPlacement=\"anywhere\" ratio=\"0.5\" textColor=\"#000000\" visible=\"true\">" + e.getLabel()
                                + "\t\t\t\t</y:EdgeLabel>"
                                + "\t\t\t\t<y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/>\n"
                                + "\t\t\t\t<y:LineStyle color=\"#000000\" type=\"line\" width=\"1.0\"/>\n"
                                + "\t\t\t\t<y:Arrows source=\"" + arrowSource + "\" target=\"" + arrowTarget + "\"/>\n"
                                + "\t\t\t\t<y:BendStyle smoothed=\"false\"/>\n"
                                + "\t\t\t</y:PolyLineEdge>\n"
                                + "\t\t</data>\n"
                                + "\t</edge>";
                        text = text.concat(ed + "\n");
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

        private HashMap<String, Double> readFluxes() {
                HashMap<String, Double> readFluxes = new HashMap<>();
                try {

                        CsvReader reader = new CsvReader(new FileReader(this.fluxes.getAbsolutePath()));
                        reader.readHeaders();
                        String[] header = reader.getHeaders();
                        while (reader.readRecord()) {
                                String[] data = reader.getValues();
                                readFluxes.put(data[2], Double.parseDouble(data[1]));
                        }
                } catch (FileNotFoundException ex) {
                        java.util.logging.Logger.getLogger(SearchPathwaysTwoPointsFluxesTask.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(SearchPathwaysTwoPointsFluxesTask.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
                return readFluxes;
        }

        private void createNetwork(HashMap<String, Double> readFluxes) {
                SBMLDocument doc = null;
                for (Dataset d : datasets) {
                        if (this.dataset.contains(d.getDatasetName())) {
                                doc = d.getDocument();
                        }
                }

                m = doc.getModel();

                int l = 0;
                for (Species s : m.getListOfSpecies()) {
                        if (isNotCofactor(s)) {
                                Node n = new Node(s.getId(), s.getName());
                                nodes.add(n);
                                location.put(n.getId(), l++);
                        }
                }

                for (Reaction r : m.getListOfReactions()) {
                        if (Math.abs(readFluxes.get(r.getId())) > 0.0000000001) {
                                for (SpeciesReference re : r.getListOfReactants()) {
                                        if (isNotCofactor(re.getSpeciesInstance())) {
                                                for (SpeciesReference p : r.getListOfProducts()) {
                                                        if (isNotCofactor(p.getSpeciesInstance())) {
                                                                addLane(r.getId(), location.get(re.getSpeciesInstance().getId()), location.get(p.getSpeciesInstance().getId()), 10, readFluxes.get(r.getId()));
                                                                addLane(r.getId() + "rev", location.get(p.getSpeciesInstance().getId()), location.get(re.getSpeciesInstance().getId()), 10, readFluxes.get(r.getId()));
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }

        private void addLane(String laneId, int sourceLocNo, int destLocNo,
                int duration, double fluxes) {
                String label = laneId + " / " + String.valueOf(fluxes);
                Edge lane = new Edge(laneId, label, nodes.get(sourceLocNo), nodes.get(destLocNo), duration);
                edges.add(lane);
        }

        /* private void createNetwork(HashMap<String, Double> readFluxes) {
         SBMLDocument doc = null;
         for (Dataset d : datasets) {
         if (this.dataset.contains(d.getDatasetName())) {
         doc = d.getDocument();
         }
         }

         Model m = doc.getModel();

         GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase("/home/scsandra/Documents/Cellfactory/TriReconstruction/QC/temp");

         registerShutdownHook(graphDb);

         Map<String, Node> nodes = new HashMap<>();
         Transaction tx = graphDb.beginTx();
         try {


         for (Reaction r : m.getListOfReactions()) {
         boolean add = false;
         if (readFluxes.containsKey(r.getId())) {
         if (Math.abs(readFluxes.get(r.getId())) > 0) {
         add = true;
         }
         }

         if (add) {
         Node n = graphDb.createNode();
         n.setProperty("Id", r.getId());
         n.setProperty("Name", r.getName());
         n.setProperty("isReaction", true);
         n.setProperty("flux", Math.abs(readFluxes.get(r.getId())));
         if (readFluxes.get(r.getId()) < 0) {
         n.setProperty("fluxDirection", "Negative");
         } else {
         n.setProperty("fluxDirection", "Positive");
         }
         nodes.put(r.getId(), n);
         //                                        ListOf<SpeciesReference> s = r.getListOfReactants();
         //                                        for (SpeciesReference species : s) {
         //                                                Species sp = species.getSpeciesInstance();
         //                                                if (isNotCofactor(sp)) {
         //                                                        Node spReactant = graphDb.createNode();
         //                                                        spReactant.setProperty("Id", sp.getId());
         //                                                        spReactant.setProperty("Name", sp.getName());
         //                                                        spReactant.setProperty("isReaction", false);
         //                                                        spReactant.setProperty("Compartment", sp.getCompartment());
         //                                                        spReactant.setProperty("Rol", "Reactant");
         //                                                        nodes.put(sp.getId(), spReactant);
         //                                                        spReactant.createRelationshipTo(n, RelTypes.SPECIES);
         //                                                }
         //                                        }
         //
         //                                        s = r.getListOfProducts();
         //                                        for (SpeciesReference species : s) {
         //                                                Species sp = species.getSpeciesInstance();
         //                                                if (isNotCofactor(sp)) {
         //                                                        org.neo4j.graphdb.Node spProducts = graphDb.createNode();
         //                                                        spProducts.setProperty("Id", sp.getId());
         //                                                        spProducts.setProperty("Name", sp.getName());
         //                                                        spProducts.setProperty("isReaction", false);
         //                                                        spProducts.setProperty("Rol", "Product");
         //                                                        nodes.put(sp.getId(), spProducts);
         //                                                        spProducts.createRelationshipTo(n, RelTypes.SPECIES);
         //                                                }
         //                                        }
         }
         }

                       
         for (Map.Entry<String, Node> node1 : nodes.entrySet()) {
         for (Map.Entry<String, Node> node2 : nodes.entrySet()) {
         if (node1 != node2 && shareMetabolite(node1.getKey(), node2.getKey(), m)) {
         node1.getValue().createRelationshipTo(node2.getValue(), RelTypes.CONNECTED);
         }

         }
         }
         // Database operations go here
         tx.success();
         } finally {
         tx.finish();

         }

         graphDb.shutdown();

         }

         private List<String> getMetabolites(Reaction r) {
         List<String> met = new ArrayList<>();
         ListOf<SpeciesReference> s = r.getListOfReactants();
         ListOf<SpeciesReference> p = r.getListOfProducts();
         for (SpeciesReference sr : s) {
         if (isNotCofactor(sr.getSpeciesInstance())) {
         met.add(sr.getSpeciesInstance().getId());
         }
         }
         for (SpeciesReference sr : p) {
         if (isNotCofactor(sr.getSpeciesInstance())) {
         met.add(sr.getSpeciesInstance().getId());
         }
         }

         return met;
         }

         private boolean shareMetabolite(String node1, String node2, Model m) {
         Reaction r = m.getReaction(node1);
         Reaction r2 = m.getReaction(node2);
         if (r != null && r2 != null) {
         List<String> met1 = getMetabolites(r);
         List<String> met2 = getMetabolites(r2);
         if (intersection(met1, met2)) {
         return true;
         } else {
         return false;
         }

         } else {
         return false;
         }

         }

         private boolean intersection(List<String> met1, List<String> met2) {
         for (String s : met1) {
         for (String s2 : met2) {
         if (s.equals(s2)) {
         return true;
         }
         }
         }
         return false;

         }

         private static enum RelTypes implements RelationshipType {

         CONNECTED, PRODUCT, REACTANT, SPECIES
         }

         private static void registerShutdownHook(final GraphDatabaseService graphDb) {
         // Registers a shutdown hook for the Neo4j instance so that it
         // shuts down nicely when the VM exits (even if you "Ctrl-C" the
         // running application).
         Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run() {
         graphDb.shutdown();
         }
         });
         }*/
        private void expansion(HashMap<String, Double> readFluxes) {
                for (Node n : this.fnodes) {
                        getReactionConnected(n, readFluxes);
                }
        }

        private void getReactionConnected(Node n, HashMap<String, Double> readFluxes) {
                for (Reaction r : m.getListOfReactions()) {
                        if (isNotInEdges(r.getId())) {
                                if (Math.abs(readFluxes.get(r.getId())) > 0.0000000001) {
                                        for (SpeciesReference re : r.getListOfReactants()) {
                                                if (isNotCofactor(re.getSpeciesInstance()) && re.getSpeciesInstance().getId().equals(n.getId())) {
                                                        for (SpeciesReference p : r.getListOfProducts()) {
                                                                if (isNotCofactor(p.getSpeciesInstance())) {
                                                                        Node pNode = new Node(p.getSpeciesInstance().getId(), p.getSpeciesInstance().getName());
                                                                        this.enodes.add(pNode);
                                                                        addLane(r.getId(), pNode, n, readFluxes.get(r.getId()));
                                                                }
                                                        }
                                                }
                                        }
                                        for (SpeciesReference re : r.getListOfProducts()) {
                                                if (isNotCofactor(re.getSpeciesInstance()) && re.getSpeciesInstance().getId().equals(n.getId())) {
                                                        for (SpeciesReference p : r.getListOfReactants()) {
                                                                if (isNotCofactor(p.getSpeciesInstance())) {
                                                                        Node pNode = new Node(p.getSpeciesInstance().getId(), p.getSpeciesInstance().getName());
                                                                        this.enodes.add(pNode);
                                                                        addLane(r.getId() + "rev", pNode, n, readFluxes.get(r.getId()));
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }

        }

        private void addLane(String laneId, Node source, Node target, double fluxes) {
                String label = laneId + " / " + String.valueOf(fluxes);
                Edge lane = new Edge(laneId, label, source, target, 10);
                eedges.add(lane);
        }

        private boolean isNotInEdges(String id) {
                for(Edge e : fedges){
                        String eid = e.getId().replace("rev", "");
                        if(id.equals(eid)){
                                return false;
                        }
                }
                return true;
        }
}
