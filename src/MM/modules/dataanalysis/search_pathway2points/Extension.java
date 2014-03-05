package MM.modules.dataanalysis.search_pathway2points;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
                                        getReactionConnected(n, nodes, edges);

                                } catch (Exception e) {
                                        System.out.println(e.toString());
                                }
                        }

                        Graph newG = new Graph(nodes, this.removeRepeatedEdges(edges));
                        newPaths.add(newG);
                }
        }

        public List<Edge> removeRepeatedEdges(List<Edge> pedges) {
                List<Edge> result = new ArrayList<Edge>();
                Set<String> ids = new HashSet<String>();
                for (Edge item : pedges) {
                        if (ids.add(item.getId())) {
                                result.add(item);
                        }
                }
                return result;
        }

        public List<Graph> getNewPaths() {
                return this.newPaths;
        }

        private void getReactionConnected(Node n, List<Node> pnodes, List<Edge> pedges) {
                for (Reaction r : m.getListOfReactions()) {
                        if (isNotInEdges(r.getId(), pedges)) {
                                for (SpeciesReference re : r.getListOfReactants()) {
                                        if (isNotCofactor(re.getSpeciesInstance()) && re.getSpeciesInstance().getId().equals(n.getId())) {
                                                for (SpeciesReference p : r.getListOfProducts()) {
                                                        if (isNotCofactor(p.getSpeciesInstance())) {
                                                                Node pNode = new Node(p.getSpeciesInstance().getId(), p.getSpeciesInstance().getName(), true);
                                                                pnodes.add(pNode);
                                                                addLane(r.getId(), pNode, n, pedges);
                                                        }
                                                }
                                        }
                                }
                                for (SpeciesReference re : r.getListOfProducts()) {
                                        if (isNotCofactor(re.getSpeciesInstance()) && re.getSpeciesInstance().getId().equals(n.getId())) {
                                                for (SpeciesReference p : r.getListOfReactants()) {
                                                        if (isNotCofactor(p.getSpeciesInstance())) {
                                                                Node pNode = new Node(p.getSpeciesInstance().getId(), p.getSpeciesInstance().getName(), true);
                                                                pnodes.add(pNode);
                                                                addLane(r.getId() + "rev", pNode, n, pedges);
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
