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

import MM.modules.reconstruction.gapfilling.network.Edge;
import MM.modules.reconstruction.gapfilling.network.Graph;
import MM.modules.reconstruction.gapfilling.network.Node;
import MM.parameters.ParameterSet;
import MM.taskcontrol.AbstractTask;
import MM.taskcontrol.TaskStatus;
import com.csvreader.CsvReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author scsandra
 */
public class GapFillingTask extends AbstractTask {

        private float progress = 0.0f;
        File reactionsFile, boundsFile;
        private String[] toBeRemoved;
        private String message = "Gap Filling... ";
        HashMap<String, Node> nodesSet = new HashMap<>();
        List<Edge> edges = new ArrayList<>();
        List<Node> nodes = new ArrayList<>();
        int EdgeCount = 1;

        public GapFillingTask(ParameterSet parameters) {
                this.reactionsFile = parameters.getParameter(GapFillingParameters.reactionFile).getValue();
                this.boundsFile = parameters.getParameter(GapFillingParameters.boundsFile).getValue();
                this.toBeRemoved = (String[]) parameters.getParameter(GapFillingParameters.removing).getValue();
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
                        HashMap<String, String> reactions = readReactionFile();
                        HashMap<String, String> bounds = readBoundsFile();
                        createGraph(reactions, bounds);
                } catch (Exception e) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = e.toString();
                }
        }

        private void createGraph(HashMap<String, String> reactions, HashMap<String, String> bounds) {


                for (Map.Entry<String, String> reaction : reactions.entrySet()) {
                        Node node = new Node(reaction.getKey(), reaction.getValue());
                        nodesSet.put(reaction.getKey(), node);
                        nodes.add(node);
                }

                for (Map.Entry<String, String> reaction1 : reactions.entrySet()) {
                        for (Map.Entry<String, String> reaction2 : reactions.entrySet()) {
                                if (!reaction1.getKey().equals(reaction2.getKey())) {
                                        List<Edge> edgesSet = getEdges(reaction1, reaction2, bounds, nodesSet);
                                        if (edgesSet != null) {
                                                for (Edge e : edgesSet) {
                                                        edges.add(e);
                                                }
                                        }
                                }
                        }
                }

                Graph completeGraph = new Graph(nodes, edges);

        }        

        private HashMap<String, String> readReactionFile() {
                HashMap<String, String> reactions = new HashMap<>();
                try {

                        CsvReader reader = new CsvReader(new FileReader(this.reactionsFile.getAbsolutePath()));
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

        private List<Edge> getEdges(Map.Entry<String, String> reaction1, Map.Entry<String, String> reaction2, HashMap<String, String> bounds, HashMap<String, Node> nodesSet) {
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
        }

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
