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
package MM.modules.reconstruction.gapfilling;

import MM.main.MMCore;
import MM.modules.dataanalysis.search_pathway2points.Dijkstra;
import MM.modules.dataanalysis.search_pathway2points.Edge;
import MM.modules.dataanalysis.search_pathway2points.Graph;
import MM.modules.dataanalysis.search_pathway2points.Node;
import MM.modules.dataanalysis.search_pathway2points.PrintPaths;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author scsandra
 */
public class GapFillingTask extends AbstractTask {

        private float progress = 0.0f;
        File molFile, boundsFile, reactionFile;
        private String initialId, finalId;
        private String[] toBeRemoved;
        private String message = "Gap Filling... ";
        List<Edge> edges;
        List<Node> nodes;
        private Map<String, Integer> location;
        int EdgeCount = 1;
        private int k;
        private String file;
        private JInternalFrame frame;
        private JScrollPane panel;
        private JPanel pn;
        private int count = 0;

        public GapFillingTask(ParameterSet parameters) {
                this.reactionFile = parameters.getParameter(GapFillingParameters.reactionFile).getValue();
                this.molFile = parameters.getParameter(GapFillingParameters.molFile).getValue();
                this.boundsFile = parameters.getParameter(GapFillingParameters.boundsFile).getValue();
                this.initialId = (String) parameters.getParameter(GapFillingParameters.idFrom).getValue();
                this.finalId = (String) parameters.getParameter(GapFillingParameters.idTo).getValue();
                this.toBeRemoved = (String[]) parameters.getParameter(GapFillingParameters.removing).getValue();
                this.k = parameters.getParameter(GapFillingParameters.k).getValue();
                this.file = parameters.getParameter(GapFillingParameters.saveName).getValue().getAbsolutePath();
                this.pn = new JPanel();
                this.panel = new JScrollPane(pn);
                this.frame = new JInternalFrame("Result", true, true, true, true);
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
                        edges = new ArrayList<>();
                        nodes = new ArrayList<>();
                        location = new HashMap<>();
                        this.findPathway();
                } catch (Exception e) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = e.toString();
                }
        }

        private void createGraph(HashMap<String, String> mols, HashMap<String, String> reactions, HashMap<String, String> bounds) {
                int l = 0;
                for (Map.Entry<String, String> mol : mols.entrySet()) {
                        Node node = new Node(mol.getKey(), mol.getValue(), false);
                        nodes.add(node);
                        location.put(node.getId(), l++);
                }
                System.out.println("Creating edges");
                int count = 0;
                System.out.println("Reactions: " + reactions.size());
                System.out.println("Mols: " + mols.size());
                System.out.println("Bounds: " + bounds.size());
                for (Map.Entry<String, String> reaction1 : reactions.entrySet()) {
                        String bound = bounds.get(reaction1.getKey());
                        if (bound == null) {
                                bound = "bi";
                        }

                        String[] reactants = getMols(reaction1.getValue(), 0);
                        String[] products = getMols(reaction1.getValue(), 1);



                        //  System.out.println(products.length);
                        if (reactants.length > 0 && products.length > 0) {
                                String[] src = null, target = null;

                                switch (bound) {
                                        case "fwd":
                                        case "bi":
                                                src = reactants;
                                                target = products;
                                                break;
                                        case "rev":
                                                src = products;
                                                target = reactants;
                                                break;

                                }
                                for (String s : src) {
                                        for (String t : target) {
                                                count++;
                                                addLane(reaction1.getKey()+"-"+ String.valueOf(count++), this.location.get(s), this.location.get(t), 1);
                                                if (bound.equals("bi")) {
                                                        addLane(reaction1.getKey() + "rev-"+String.valueOf(count++), this.location.get(t), this.location.get(s), 1);
                                                        count++;
                                                }
                                        }
                                }

                        }
                }
                System.out.println("Edges: " + count);

        }

        public void findPathway() {
                setStatus(TaskStatus.PROCESSING);
                try {
                        if (getStatus() == TaskStatus.PROCESSING) {
                                HashMap<String, String> molecules = readMolFile();
                                HashMap<String, String> bounds = readBoundsFile();
                                HashMap<String, String> reactions = readReactionFile();
                                createGraph(molecules, reactions, bounds);
                                frame.setSize(new Dimension(700, 500));
                                frame.add(this.panel);
                                MMCore.getDesktop().addInternalFrame(frame);


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
                                                System.out.println(e.toString());
                                        }
                                }
                                System.out.println(paths.size());
                                PrintPaths printer = new PrintPaths(this.initialId, this.finalId);
                                this.pn.add(printer.printPathwayInFrame(paths));
                                printer.printPathwayInFile(paths, this.file);

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
        
        private void addLane(String laneId, int sourceLocNo, int destLocNo,
                int duration) {                
                Edge lane = new Edge(laneId, nodes.get(sourceLocNo), nodes.get(destLocNo), duration);
                edges.add(lane);
        }

        private String[] getMols(String mols, int part) {
                String[] parts = mols.split(" <=> ");
                return removeCofactors(parts[part].split(" \\+ "));
        }

        private HashMap<String, String> readReactionFile() {
                HashMap<String, String> reactions = new HashMap<>();
                try {

                        CsvReader reader = new CsvReader(new FileReader(this.reactionFile.getAbsolutePath()));
                        reader.readHeaders();
                        String[] header = reader.getHeaders();
                        while (reader.readRecord()) {
                                String[] data = reader.getValues();
                                reactions.put(data[0], data[4]);
                        }
                } catch (FileNotFoundException ex) {
                        java.util.logging.Logger.getLogger(GapFillingParameters.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(GapFillingParameters.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
                return reactions;
        }

        private HashMap<String, String> readMolFile() {
                HashMap<String, String> reactions = new HashMap<>();
                try {

                        CsvReader reader = new CsvReader(new FileReader(this.molFile.getAbsolutePath()));
                        while (reader.readRecord()) {
                                String[] data = reader.getValues();
                                reactions.put(data[0], data[1]);
                        }
                } catch (FileNotFoundException ex) {
                        java.util.logging.Logger.getLogger(GapFillingParameters.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(GapFillingParameters.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
                return reactions;
        }

        private HashMap<String, String> readBoundsFile() {
                HashMap<String, String> bounds = new HashMap<>();
                try {

                        CsvReader reader = new CsvReader(new FileReader(this.boundsFile.getAbsolutePath()));
                        reader.readHeaders();
                        String[] header = reader.getHeaders();
                        while (reader.readRecord()) {
                                String[] data = reader.getValues();
                                String direction = "bi";
                                if (data[3].equals("0")) {
                                        direction = "fwd";
                                } else if (data[4].equals("0")) {
                                        direction = "rev";
                                }
                                bounds.put(data[0], direction);
                        }
                } catch (FileNotFoundException ex) {
                        java.util.logging.Logger.getLogger(GapFillingParameters.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(GapFillingParameters.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
                return bounds;
        }

        /*  private List<Edge> getEdges(Map.Entry<String, String> reaction1, Map.Entry<String, String> reaction2, HashMap<String, String> bounds, HashMap<String, Node> nodesSet) {
         List<Edge> edges = new ArrayList();
         String reactionName1 = reaction1.getKey();
         String reactionName2 = reaction2.getKey();
         String direction1;
         if (bounds.containsKey(reactionName1)) {
         direction1 = bounds.get(reactionName1);
         } else {
         direction1 = "bi";
         }
         String direction2;
         if (bounds.containsKey(reactionName2)) {
         direction2 = bounds.get(reactionName2);
         } else {
         direction2 = "bi";
         }

         String reactionM1 = reaction1.getValue();
         String reactionM2 = reaction2.getValue();

         String[] rM1 = reactionM1.split(" <=> ");
         String[] rM2 = reactionM2.split(" <=> ");
         // System.out.println("reaction: " + rM2[1]);

         String[] reactants1 = removeCofactors(rM1[0].split(" \\+ "));
         String[] products1 = removeCofactors(rM1[1].split(" \\+ "));
         String[] reactants2 = removeCofactors(rM2[0].split(" \\+ "));
         String[] products2 = removeCofactors(rM2[1].split(" \\+ "));
         //System.out.println(rM2[1].split(" \\+ ")[0]);

         switch (direction1) {
         case "fwd":
         switch (direction2) {
         case "fwd":
         if (common(products1, reactants2)) {
         Edge e = new Edge(String.valueOf(this.EdgeCount++), nodesSet.get(reactionName1), nodesSet.get(reactionName2), 1);
         edges.add(e);
         }
         if (common(products2, reactants1)) {
         Edge e = new Edge(String.valueOf(this.EdgeCount++), nodesSet.get(reactionName2), nodesSet.get(reactionName1), 1);
         edges.add(e);
         }
         break;
         case "rev":
         if (common(reactants1, reactants2)) {
         Edge e = new Edge(String.valueOf(this.EdgeCount++), nodesSet.get(reactionName2), nodesSet.get(reactionName1), 1);
         edges.add(e);
         }
         if (common(products2, products1)) {
         Edge e = new Edge(String.valueOf(this.EdgeCount++), nodesSet.get(reactionName1), nodesSet.get(reactionName2), 1);
         edges.add(e);
         }
         break;
         case "bi":
         if (common(reactants1, reactants2) || common(reactants1, products2)) {
         Edge e = new Edge(String.valueOf(this.EdgeCount++), nodesSet.get(reactionName2), nodesSet.get(reactionName1), 1);
         edges.add(e);
         }
         if (common(products2, products1) || common(products1, reactants2)) {
         Edge e = new Edge(String.valueOf(this.EdgeCount++), nodesSet.get(reactionName1), nodesSet.get(reactionName2), 1);
         edges.add(e);
         }
         break;
         }
         break;
         case "rev":
         switch (direction2) {
         case "fwd":
         if (common(products1, products2)) {
         Edge e = new Edge(String.valueOf(this.EdgeCount++), nodesSet.get(reactionName2), nodesSet.get(reactionName1), 1);
         edges.add(e);
         }
         if (common(reactants2, reactants1)) {
         Edge e = new Edge(String.valueOf(this.EdgeCount++), nodesSet.get(reactionName1), nodesSet.get(reactionName2), 1);
         edges.add(e);
         }
         break;
         case "rev":
         if (common(reactants1, products1)) {
         Edge e = new Edge(String.valueOf(this.EdgeCount++), nodesSet.get(reactionName2), nodesSet.get(reactionName1), 1);
         edges.add(e);
         }
         if (common(products2, reactants1)) {
         Edge e = new Edge(String.valueOf(this.EdgeCount++), nodesSet.get(reactionName1), nodesSet.get(reactionName2), 1);
         edges.add(e);
         }
         break;
         case "bi":
         if (common(reactants1, reactants2) || common(reactants1, products2)) {
         Edge e = new Edge(String.valueOf(this.EdgeCount++), nodesSet.get(reactionName1), nodesSet.get(reactionName2), 1);
         edges.add(e);
         }
         if (common(products2, products1) || common(products1, reactants2)) {
         Edge e = new Edge(String.valueOf(this.EdgeCount++), nodesSet.get(reactionName2), nodesSet.get(reactionName1), 1);
         edges.add(e);
         }
         break;
         }
         break;

         case "bi":
         switch (direction2) {
         case "fwd":
         if (common(products1, reactants2) || common(reactants1, reactants2)) {
         Edge e = new Edge(String.valueOf(this.EdgeCount++), nodesSet.get(reactionName1), nodesSet.get(reactionName2), 1);
         edges.add(e);
         }
         if (common(products2, products1) || common(products2, reactants1)) {
         Edge e = new Edge(String.valueOf(this.EdgeCount++), nodesSet.get(reactionName2), nodesSet.get(reactionName1), 1);
         edges.add(e);
         }
         break;
         case "rev":
         if (common(products1, products2) || common(reactants1, products2)) {
         Edge e = new Edge(String.valueOf(this.EdgeCount++), nodesSet.get(reactionName1), nodesSet.get(reactionName2), 1);
         edges.add(e);
         }
         if (common(reactants2, products1) || common(reactants1, reactants2)) {
         Edge e = new Edge(String.valueOf(this.EdgeCount++), nodesSet.get(reactionName2), nodesSet.get(reactionName1), 1);
         edges.add(e);
         }
         break;
         case "bi":
         Edge e = new Edge(String.valueOf(this.EdgeCount++), nodesSet.get(reactionName1), nodesSet.get(reactionName2), 1);
         edges.add(e);
         Edge e2 = new Edge(String.valueOf(this.EdgeCount++), nodesSet.get(reactionName2), nodesSet.get(reactionName1), 1);
         edges.add(e2);
         break;
         }
         break;

         }


         return edges;
         }

         private boolean common(String[] products, String[] reactants) {
         try {
         for (String p : products) {
         for (String r : reactants) {
         if (p.equals(r)) {
         return true;
         }

         }
         }
         return false;
         } catch (Exception e) {
         return false;
         }
         }*/
        private String[] removeCofactors(String[] metabolites) {
                List<String> m = new ArrayList<>();
                for (String met : metabolites) {
                        String[] metst = met.split(" ");
                        m.add(metst[metst.length - 1]);
                }
                for (String cofactor : this.toBeRemoved) {
                        m.remove(cofactor);
                }
                return m.toArray(new String[0]);
        }
}
