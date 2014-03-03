package MM.modules.dataanalysis.search_pathway2pointsFluxes;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author scsandra
 */
public class Edge {

        private final String id;
        private final String label;
        private final Node source;
        private final Node destination;
        private final int weight;

        public Edge(String id, String label, Node source, Node destination, int weight) {
                this.id = id;
                this.label = label;
                this.source = source;
                this.destination = destination;
                this.weight = weight;
        }

        public String getId() {
                return id;
        }
        
        public String getLabel() {
                return label;
        }

        public Node getDestination() {
                return destination;
        }

        public Node getSource() {
                return source;
        }

        public int getWeight() {
                return weight;
        }

        @Override
        public String toString() {
                return source + " " + destination;
        }
}
