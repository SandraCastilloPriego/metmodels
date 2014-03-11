/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MM.modules.dataanalysis.search_pathway2points;

import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ChainedTransformer;

/**
 *
 * @author scsandra
 */
public class PrintPaths {

        private String initialId, finalId;

        public PrintPaths(String initialId, String finalId) {
                this.initialId = initialId;
                this.finalId = finalId;
        }

        public VisualizationViewer printPathwayInFrame(List<Graph> paths) {
                edu.uci.ics.jung.graph.Graph<String, String> g = new SparseMultigraph<>();
                for (Graph ga : paths) {
                        List<Node> fnodes = ga.getVertexes();
                        List<Edge> fedges = removeRepeatedEdges(ga.getEdges());
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
                                } else if (id.contains(initialId) || id.contains(finalId)) {
                                        return Color.BLUE;
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
                vv.getRenderContext().setEdgeLabelTransformer(labelTransformer);
                vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
                DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
                gm.setMode(ModalGraphMouse.Mode.PICKING);
                vv.setGraphMouse(gm);
                vv.addKeyListener(gm.getModeKeyListener());
                return vv;
        }

        public void printPathwayInFile(List<Graph> paths, String file) {
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
                        List<Edge> fedges = removeRepeatedEdges(g.getEdges());
                        for (Node node : fnodes) {                                        
                                String n = "\t <node id=\"" + node.getId() + "\">\n\t\t<data key=\"d6\">\n"
                                        + "\t\t\t<y:ShapeNode>\n";
                                if (node.getId().contains("-Ext")) {
                                        n = n + "\t\t\t\t<y:Fill color=\"#FF0000\" transparent=\"false\"/>";
                                }
                                if(node.getId().contains(this.initialId) || node.getId().contains(this.finalId)){
                                        n = n + "\t\t\t\t<y:Fill color=\"#00FF00\" transparent=\"false\"/>";
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
                                new FileOutputStream(file), "utf-8"));
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
        
        
        public List<Edge> removeRepeatedEdges(List<Edge> pedges) {
                int i = 0;
                List<Edge> result = new ArrayList<>();
                Set<String> ids = new HashSet<>();
                for (Edge item : pedges) {
                        if (ids.add(item.getId())) {
                                result.add(item);
                        }else{
                                item.setId(item.getId()+i++);
                                result.add(item);
                        }
                }
                return result;
        }
}
