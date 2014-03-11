package MM.modules.dataanalysis.search_pathway2points;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author scsandra
 */
public class Dijkstra {

        private final List<Edge> edges;
        private Set<Node> settledNodes;
        private Set<Node> unSettledNodes;
        private List<String> settledReactions;
        private Map<Node, Node> predecessors;
        private Map<Node, Integer> distance;

        public Dijkstra(Graph graph) {               // Create a copy of the array so that we can operate on this array               
                this.edges = new ArrayList<>(graph.getEdges());
        }

        public void execute(Node source) {
                settledNodes = new HashSet<>();
                unSettledNodes = new HashSet<>();
                distance = new HashMap<>();
                predecessors = new HashMap<>();
                settledReactions = new ArrayList<>();
                distance.put(source, 0);
                unSettledNodes.add(source);
                while (unSettledNodes.size() > 0) {
                        Node node = getMinimum(unSettledNodes);
                        settledNodes.add(node);
                        unSettledNodes.remove(node);
                        findMinimalDistances(node);
                }
        }

        private void addSettleReaction(Node n, Node n2) {
                for (Edge e : this.edges) {
                        if ((e.getSource() == n && e.getDestination() == n2) || (e.getSource() == n2 && e.getDestination() == n)) {
                                String[] name = e.getId().split("-");
                                String reaction = name[0].replace("rev", "");
                                if (!settledReactions.contains(reaction)) {
                                        settledReactions.add(reaction);
                                }
                        }
                }
        }

        private void findMinimalDistances(Node node) {
                List<Node> adjacentNodes = getNeighbors(node);
                for (Node target : adjacentNodes) {
                        if (getShortestDistance(target) > getShortestDistance(node)
                                + getDistance(node, target)) {
                                distance.put(target, getShortestDistance(node)
                                        + getDistance(node, target));
                                predecessors.put(target, node);
                                this.addSettleReaction(target, node);
                                unSettledNodes.add(target);
                        }
                }

        }

        private int getDistance(Node node, Node target) {
                for (Edge edge : edges) {
                        if (edge.getSource().equals(node)
                                && edge.getDestination().equals(target)) {
                                return edge.getWeight();
                        }
                }
                throw new RuntimeException("Should not happen");
        }

        private List<Node> getNeighbors(Node node) {
                List<Node> neighbors = new ArrayList<>();
                for (Edge edge : edges) {
                        if (edge.getSource().equals(node)
                                && !isSettled(edge.getDestination()) && !isReactionSettled(edge)) {
                                neighbors.add(edge.getDestination());
                        }
                }
                return neighbors;
        }

        private Node getMinimum(Set<Node> vertexes) {
                Node minimum = null;
                for (Node vertex : vertexes) {
                        if (minimum == null) {
                                minimum = vertex;
                        } else {
                                if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
                                        minimum = vertex;
                                }
                        }
                }
                return minimum;
        }

        private boolean isSettled(Node vertex) {
                return settledNodes.contains(vertex);
        }

        private int getShortestDistance(Node destination) {
                Integer d = distance.get(destination);
                if (d == null) {
                        return Integer.MAX_VALUE;
                } else {
                        return d;
                }
        }

        /*
         * This method returns the path from the source to the selected target and
         * NULL if no path exists
         */
        public LinkedList<Node> getPath(Node target) {
                LinkedList<Node> path = new LinkedList<>();
                Node step = target;
                // Check if a path exists
                if (predecessors.get(step) == null) {
                        return null;
                }
                path.add(step);
                while (predecessors.get(step) != null) {
                        step = predecessors.get(step);
                        path.add(step);
                }
                // Put it into the correct order
                Collections.reverse(path);
                return path;
        }

        private boolean isReactionSettled(Edge edge) {
                String[] name = edge.getId().split("-");               
                if (this.settledReactions.contains(name[0].replace("rev", ""))) {
                        return true;
                }
                return false;
        }
}
