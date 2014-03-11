/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MM.modules.dataanalysis.search_pathway2points;

/**
 *
 * @author scsandra
 */
public class Edge {

        private String id;
        private final Node source;
        private final Node destination;
        private final int weight;

        public Edge(String id, Node source, Node destination, int weight) {
                this.id = id;
                this.source = source;
                this.destination = destination;
                this.weight = weight;
        }

        public String getId() {
                return id;
        }

        public Node getDestination() {
                return destination;
        }

        public Node getSource() {
                return source;
        }
        
        public void setId(String id) {
                this.id = id;
        }

        public int getWeight() {
                return weight;
        }

        @Override
        public String toString() {
                return source + " " + destination;
        }
}
