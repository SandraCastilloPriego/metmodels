/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MM.modules.file.utils.printAllSpecies;

/**
 *
 * @author scsandra
 */
public class Compound {

        public String KeggID = "";
        public String pubChemID = "";
        public String chebID = "";
        public String Name = "";
        private String[] data;

        public Compound(String[] data) {
                if (data != null) {
                        this.data = data;
                        this.KeggID = data[0];
                        this.chebID = data[1];
                        this.pubChemID = data[2];
                        this.Name = data[3];
                }
        }

        public String[] getData() {
                return data;
        }

        public boolean isSimilar(String[] newCompound) {
                if (newCompound == null) {
                        return true;
                }
                if (newCompound[0] != null && newCompound[0].equals(this.KeggID)) {
                        return true;
                }
                if (newCompound[1] != null && newCompound[1].equals(this.chebID)) {
                        return true;
                }
                if (newCompound[2] != null && newCompound[2].equals(this.pubChemID)) {
                        return true;
                }
                if (newCompound[3] != null && newCompound[3].equals(this.Name)) {
                        return true;
                }
                return false;
        }
}
