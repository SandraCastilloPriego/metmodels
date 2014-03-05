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

import MM.data.Dataset;
import MM.main.MMCore;
import MM.parameters.ParameterSet;
import MM.taskcontrol.AbstractTask;
import MM.taskcontrol.TaskStatus;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ChainedTransformer;

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
        private JPanel pn;
        private List<Node> nodes;
        private List<Edge> edges;
        private Map<String, Integer> location;
        private Model m;
        private int k;
        private String file;
        private boolean extension;

        public SearchPathwaysTwoPointsTask(Dataset[] datasets, ParameterSet parameters) {
                this.datasets = datasets;
                this.dataset = (String) parameters.getParameter(SearchPathwaysTwoPointsParameters.data).getValue();
                this.initialId = (String) parameters.getParameter(SearchPathwaysTwoPointsParameters.idFrom).getValue();
                this.finalId = (String) parameters.getParameter(SearchPathwaysTwoPointsParameters.idTo).getValue();
                this.toBeRemoved = (String[]) parameters.getParameter(SearchPathwaysTwoPointsParameters.removing).getValue();
                this.k = parameters.getParameter(SearchPathwaysTwoPointsParameters.k).getValue();
                this.file = parameters.getParameter(SearchPathwaysTwoPointsParameters.fileName).getValue().getAbsolutePath();
                this.extension = parameters.getParameter(SearchPathwaysTwoPointsParameters.extension).getValue();
                this.frame = new JInternalFrame("Result", true, true, true, true);

                this.pn = new JPanel();
                this.panel = new JScrollPane(pn);
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
                                List<Graph> paths = new ArrayList<>();

                                for (int exp = 0; exp < k; exp++) {
                                        try {
                                                List<Node> fnodes = new ArrayList<>();
                                                List<Edge> fedges = new ArrayList<>();
                                                Graph graph = new Graph(nodes, edges);
                                                Dijkstra dijkstra = new Dijkstra(graph);
                                                dijkstra.execute(nodes.get(location.get(this.initialId)));
                                                LinkedList<Node> path = dijkstra.getPath(nodes.get(location.get(this.finalId)));
                                                if (path.size() == 0) {
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
                                                paths.add(new Graph(fnodes, fedges));

                                        } catch (Exception e) {
                                        }
                                }

                                if (extension) {
                                        Extension ext = new Extension(this.m, this.toBeRemoved, paths);
                                        ext.extension();
                                        paths = ext.getNewPaths();
                                }
                                printPathway3(paths);
                                printPathway(paths);

                        }


                } catch (Exception ex) {
                        System.out.println("Something failed: " + ex.toString());
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

        private void printPathway3(List<Graph> paths) {
                edu.uci.ics.jung.graph.Graph<String, String> g = new SparseMultigraph<>();               
                for (Graph ga : paths) {
                        List<Node> fnodes = ga.getVertexes();
                        List<Edge> fedges = ga.getEdges();   
                        for (Node node : fnodes) {
                                String name = node.getId() + " - " + node.getName();
                                if (node.isExtended()) {
                                        name = node.getId() + " - " + node.getName() + "-Ext";
                                }
                                g.addVertex(name);
                        }
                        for (Edge e : fedges) {
                                Node source = e.getSource();
                                Node target = e.getDestination();
                                String sourceName = source.getId() + " - " + source.getName();
                                String targetName = target.getId() + " - " + target.getName();
                                boolean isExtended = false;
                                if (source.isExtended()) {
                                        sourceName = sourceName + "-Ext";
                                        isExtended = true;
                                }
                                if (target.isExtended()) {
                                        targetName = targetName + "-Ext";
                                        isExtended = true;
                                }
                                String reactionName = e.getId();
                                if (isExtended) {
                                        reactionName = e.getId() + "-Ext";
                                }
                                g.addEdge(reactionName, sourceName, targetName, EdgeType.DIRECTED);
                        }
                }
                Layout<String, String> layout = new KKLayout(g);
                layout.setSize(new Dimension(1400, 1000)); // sets the initial size of the space
                VisualizationViewer<String, String> vv = new VisualizationViewer<>(layout);
                vv.setPreferredSize(new Dimension(1400, 1000));
                Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
                        @Override
                        public Paint transform(String id) {
                                if (id.contains("-Ext")) {
                                        return Color.RED;
                                } else {
                                        return Color.GREEN;
                                }
                        }
                };

                float dash[] = {1.0f};
                final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
                Transformer<String, Stroke> edgeStrokeTransformer =
                        new Transformer<String, Stroke>() {
                        @Override
                        public Stroke transform(String s) {
                                return edgeStroke;
                        }
                };

                Transformer labelTransformer = new ChainedTransformer<>(new Transformer[]{
                        new ToStringLabeller<>(),
                        new Transformer<String, String>() {
                                @Override
                                public String transform(String input) {
                                        return "<html><b><font color=\"red\">" + input;
                                }
                        }});
                Transformer labelTransformer2 = new ChainedTransformer<>(new Transformer[]{
                        new ToStringLabeller<>(),
                        new Transformer<String, String>() {
                                @Override
                                public String transform(String input) {
                                        return "<html><b><font color=\"black\">" + input;
                                }
                        }});

                vv.getRenderContext().setVertexLabelTransformer(labelTransformer2);
                vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
                vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
                vv.getRenderContext().getEdgeLabelRenderer().setRotateEdgeLabels(false);
                //vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
                vv.getRenderContext().setEdgeLabelTransformer(labelTransformer);
                vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

                // vv.getRenderContext().setLabelOffset(30);

                DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
                gm.setMode(ModalGraphMouse.Mode.PICKING);
                vv.setGraphMouse(gm);
                vv.addKeyListener(gm.getModeKeyListener());
                pn.add(vv);
        }

        private void printPathway(List<Graph> paths) {
                String text = "";

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


                for (Graph g : paths) {
                        List<Node> fnodes = g.getVertexes();
                        List<Edge> fedges = g.getEdges();
                        for (Node node : fnodes) {
                                String n = "\t <node id=\"" + node.getId() + "\">\n\t\t<data key=\"d6\">\n"
                                        + "\t\t\t<y:ShapeNode>\n";
                                if (node.isExtended()) {
                                        n = n + "\t\t\t\t<y:Fill color=\"#FF0000\" transparent=\"false\"/>";
                                }
                                n = n + "\t\t\t\t<y:NodeLabel alignment=\"center\" autoSizePolicy=\"content\" fontFamily=\"Dialog\" fontSize=\"20\" fontStyle=\"plain\" hasBackgroundColor=\"false\" hasLineColor=\"false\"  modelName=\"internal\" modelPosition=\"c\" textColor=\"#000000\" visible=\"true\">" + node.getId() + " - " + node.getName() + "</y:NodeLabel>\n"
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

                }

                String fin = "</graph>\n</graphml>";
                text = text.concat(fin + "\n");

                Writer writer = null;

                try {
                        writer = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(this.file), "utf-8"));
                        writer.write(text);
                } catch (IOException ex) {
                        // report
                } finally {
                        try {
                                writer.close();
                        } catch (Exception ex) {
                        }
                }
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

                m = doc.getModel();

                int l = 0;
                for (Species s : m.getListOfSpecies()) {
                        if (isNotCofactor(s)) {
                                Node n = new Node(s.getId(), s.getName(), false);
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
