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
package MM.modules.dataanalysis.search_pathway2points;

import MM.modules.file.utils.search_pathway2points.*;
import MM.data.Dataset;
import MM.main.MMCore;
import MM.parameters.ParameterSet;
import MM.taskcontrol.AbstractTask;
import MM.taskcontrol.TaskStatus;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
        private List<Node> nodes, fnodes;
        private List<Edge> edges, fedges;
        private Map<String, Integer> location;
        private int nID = 0;

        public SearchPathwaysTwoPointsTask(Dataset[] datasets, ParameterSet parameters) {
                this.datasets = datasets;
                this.dataset = (String) parameters.getParameter(SearchPathwaysTwoPointsParameters.data).getValue();
                this.initialId = (String) parameters.getParameter(SearchPathwaysTwoPointsParameters.idFrom).getValue();
                this.finalId = (String) parameters.getParameter(SearchPathwaysTwoPointsParameters.idTo).getValue();
                this.toBeRemoved = (String[]) parameters.getParameter(SearchPathwaysTwoPointsParameters.removing).getValue();


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
                        nodes = new ArrayList<>();
                        edges = new ArrayList<>();
                        fnodes = new ArrayList<>();
                        fedges = new ArrayList<>();
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

                                // opens a frame to show the results

                                frame.setSize(new Dimension(700, 500));
                                frame.add(this.panel);


                                MMCore.getDesktop().addInternalFrame(frame);

                                // creates the network representation
                                this.createNetwork();
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

                                printPathway();
                        }


                } catch (Exception ex) {
                        Logger.getLogger(SearchPathwaysTwoPointsTask.class.getName()).log(Level.ERROR, null, ex);
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
                                + "\t\t\t\t<y:EdgeLabel alignment=\"center\" configuration=\"AutoFlippingLabel\" fontFamily=\"Dialog\" fontSize=\"12\" fontStyle=\"plain\" hasBackgroundColor=\"false\" hasLineColor=\"false\" modelName=\"custom\" preferredPlacement=\"anywhere\" ratio=\"0.5\" textColor=\"#000000\" visible=\"true\">" + e.getId()
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

        private void createNetwork() {
                SBMLDocument doc = null;
                for (Dataset d : datasets) {
                        if (this.dataset.contains(d.getDatasetName())) {
                                doc = d.getDocument();
                        }
                }

                Model m = doc.getModel();

                int l = 0;
                for (Species s : m.getListOfSpecies()) {
                        if (isNotCofactor(s)) {
                                Node n = new Node(s.getId(), s.getName());
                                nodes.add(n);
                                location.put(n.getId(), l++);
                        }
                }

                for (Reaction r : m.getListOfReactions()) {
                        for (SpeciesReference re : r.getListOfReactants()) {
                                if (isNotCofactor(re.getSpeciesInstance())) {
                                        for (SpeciesReference p : r.getListOfProducts()) {
                                                if (isNotCofactor(p.getSpeciesInstance())) {
                                                        addLane(r.getId(), location.get(re.getSpeciesInstance().getId()), location.get(p.getSpeciesInstance().getId()), 10);
                                                        addLane(r.getId() + "rev", location.get(p.getSpeciesInstance().getId()), location.get(re.getSpeciesInstance().getId()), 10);
                                                }
                                        }
                                }
                        }
                }
        }

        private void addLane(String laneId, int sourceLocNo, int destLocNo,
                int duration) {
                Edge lane = new Edge(laneId, nodes.get(sourceLocNo), nodes.get(destLocNo), duration);
                edges.add(lane);
        }
}
