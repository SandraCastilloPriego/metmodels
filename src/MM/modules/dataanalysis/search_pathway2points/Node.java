/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MM.modules.dataanalysis.search_pathway2points;

/**
 *
 * @author scsandra
 */
public class Node {

        final private String id;
        final private String name;
        final private boolean extended;

        public Node(String id, String name, boolean extended) {
                this.id = id;
                this.name = name;
                this.extended = extended;
        }

        public String getId() {
                return id;
        }

        public String getName() {
                return name;
        }
        
        public boolean isExtended() {
                return extended;
        }

        @Override
        public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((id == null) ? 0 : id.hashCode());
                return result;
        }

        @Override
        public boolean equals(Object obj) {
                if (this == obj) {
                        return true;
                }
                if (obj == null) {
                        return false;
                }
                if (getClass() != obj.getClass()) {
                        return false;
                }
                Node other = (Node) obj;
                if (id == null) {
                        if (other.id != null) {
                                return false;
                        }
                } else if (!id.equals(other.id)) {
                        return false;
                }
                return true;
        }

        @Override
        public String toString() {
                return name;
        }
}
