package MM.modules.dataanalysis.search_pathway2points;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class Extension {

        private Model m;
        private String[] toBeRemoved;
        private List<Graph> paths, newPaths;
        private List<Node> nodes;
        private List<Edge> edges;
        private int count = 0;

        
        public Extension(Model m, String[] toBeRemoved, List<Graph> paths) {
                this.m = m;
                this.toBeRemoved = toBeRemoved;
                this.paths = paths;
                nodes = new ArrayList<>();
                edges = new ArrayList<>();
                newPaths = new ArrayList<>();
        }

        public void extension() {
                for (Graph g : paths) {
                        nodes.clear();
                        edges.clear();
                        nodes.addAll(g.getVertexes());
                        edges.addAll(g.getEdges());
                        for (Node n : g.getVertexes()) {
                                try {
                                        getReactionConnected(n);

                                } catch (Exception e) {
                                        System.out.println(e.toString());
                                }
                        }
                        
                        //edges = this.removeRepeatedEdges(edges);
                      //  nodes = this.removeOrphanNodes(nodes, edges);
                        Graph newG = new Graph(nodes, edges);
                        newPaths.add(newG);
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
                                item.setId(item.getId()+"-"+i++);
                                result.add(item);
                        }
                }
                return result;
        }

        public List<Node> removeOrphanNodes(List<Node> pnodes, List<Edge> pedges) {  
                List<Node> newList = new ArrayList<>();                
                for (Node n : pnodes) {
                        for(Edge e : pedges){
                                if (e.getSource() == n || e.getDestination() == n) {
                                        newList.add(n);
                                }
                        }
                }
                return newList;
        }

        public List<Graph> getNewPaths() {
                return this.newPaths;
        }

        private void getReactionConnected(Node n) {
                for (Reaction r : m.getListOfReactions()) {
                        if (isNotInEdges(r.getId(), edges)) {
                                for (SpeciesReference re : r.getListOfReactants()) {
                                        if (isNotCofactor(re.getSpeciesInstance()) && re.getSpeciesInstance().getId().equals(n.getId())) {
                                                for (SpeciesReference p : r.getListOfProducts()) {
                                                        if (isNotCofactor(p.getSpeciesInstance())) {
                                                                Node pNode = new Node(p.getSpeciesInstance().getId(), p.getSpeciesInstance().getName(), true);
                                                                nodes.add(pNode);
                                                                addLane(r.getId()+ "-"+String.valueOf(count++), pNode, n, edges);
                                                        }
                                                }
                                        }
                                }
                                for (SpeciesReference re : r.getListOfProducts()) {
                                        if (isNotCofactor(re.getSpeciesInstance()) && re.getSpeciesInstance().getId().equals(n.getId())) {
                                                for (SpeciesReference p : r.getListOfReactants()) {
                                                        if (isNotCofactor(p.getSpeciesInstance())) {
                                                                Node pNode = new Node(p.getSpeciesInstance().getId(), p.getSpeciesInstance().getName(), true);
                                                                nodes.add(pNode);
                                                                addLane(r.getId() + "rev-", pNode, n, edges);
                                                        }
                                                }
                                        }
                                }

                        }
                }
        }

        private void addLane(String laneId, Node source, Node target, List<Edge> pedges) {
                Edge lane = new Edge(laneId, source, target, 10);
                pedges.add(lane);
        }

        private boolean isNotInEdges(String id, List<Edge> pedges) {
                for (Edge e : pedges) {
                        String eid = e.getId().replace("rev", "");
                        if (id.equals(eid)) {
                                return false;
                        }
                }
                return true;
        }

        private boolean isNotCofactor(Species p) {
                for (String toRemove : this.toBeRemoved) {
                        if (p.getId().contains(toRemove)) {
                                return false;
                        }
                }

                return true;

        }
}
