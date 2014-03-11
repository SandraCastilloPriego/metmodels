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
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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
        private int count = 0;
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
                                PrintPaths printer = new PrintPaths(this.initialId, this.finalId);
                                printer.printPathwayInFile(paths, this.file);
                                this.pn.add(printer.printPathwayInFrame(paths));
                                

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
                                                        addLane(r.getId()+"-"+ String.valueOf(count++), location.get(re.getSpeciesInstance().getId()), location.get(p.getSpeciesInstance().getId()), 10);
                                                        addLane(r.getId() + "rev-"+ String.valueOf(count++), location.get(p.getSpeciesInstance().getId()), location.get(re.getSpeciesInstance().getId()), 10);
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
