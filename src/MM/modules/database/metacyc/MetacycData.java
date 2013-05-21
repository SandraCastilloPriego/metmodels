/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MM.modules.database.metacyc;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author scsandra
 */
public class MetacycData {

        private String ECNumber;
        private List<GeneData> enzymesGenes;
        private String pathwayName;
        private String pathwayLink;
        private String pathwaySynonyms;
        private String reaction;
        private String[] reactants;
        private String[] products;
        private String direction;
        private String massBalance;
        private String enzPrimaryName, summary, enzSummary;
        private List<String> citations;
        private List<String> uniLinks;
        private List<String> relLinks;
        private List<String> references;
        private List<String> enzCommissionSynonyms;

        public MetacycData() {
                this.enzymesGenes = new ArrayList<>();
                this.citations = new ArrayList<>();
                this.uniLinks = new ArrayList<>();
                this.references = new ArrayList<>();
                this.relLinks = new ArrayList<>();
                this.enzCommissionSynonyms = new ArrayList<>();
        }

        public void setECNumber(String ECNumber) {
                this.ECNumber = ECNumber;
        }

        public void addMassBalance(String balance) {
                this.massBalance = balance;
        }

        public void addEnzCommissionSynonyms(String synonym) {
                this.enzCommissionSynonyms.add(synonym);
        }

        public void addEnzymesGenes(GeneData data) {
                this.enzymesGenes.add(data);
        }

        public void addCitations(String data) {
                this.citations.add(data);
        }

        public void addUnificationLinks(String data) {
                this.uniLinks.add(data);
        }

        public void addRelationalLinks(String data) {
                this.relLinks.add(data);
        }

        public void addReferences(String data) {
                this.references.add(data);
        }

        public void setReaction(String reaction) {
                this.reaction = reaction;
                String[] groups;
                if (reaction.contains("harr")) {
                        this.direction = "bidirectional";
                        groups = reaction.split("harr");
                        if (!groups[0].isEmpty()) {
                                this.reactants = groups[0].split(" + ");
                        }
                        if (!groups[1].isEmpty()) {
                                this.products = groups[1].split(" + ");
                        }
                } else if (reaction.contains("rarr")) {
                        this.direction = "unidirectional";
                        groups = reaction.split("rarr");
                        if (!groups[0].isEmpty()) {
                                this.reactants = groups[0].split(" + ");
                        }
                        if (!groups[1].isEmpty()) {
                                this.products = groups[1].split(" + ");
                        }
                } else if (reaction.contains("=")) {
                        this.direction = "equal";
                        groups = reaction.split("=");
                        if (!groups[0].isEmpty()) {
                                this.reactants = groups[0].split(" + ");
                        }
                        if (!groups[1].isEmpty()) {
                                this.products = groups[1].split(" + ");
                        }
                } else if (reaction.contains("larr")) {
                        this.direction = "unidirectional";
                        groups = reaction.split("larr");
                        if (!groups[0].isEmpty()) {
                                this.products = groups[0].split(" + ");
                        }
                        if (!groups[1].isEmpty()) {
                                this.reactants = groups[1].split(" + ");
                        }
                }
        }

        public void addPathwayName(String name) {
                this.pathwayName = name;
        }

        public void addPathwayLink(String link) {
                this.pathwayLink = link;
        }

        public void addPathwaySynonyms(String synonyms) {
                this.pathwaySynonyms = synonyms;
        }

        public void addPrimaryName(String name) {
                this.enzPrimaryName = name;
        }

        String[] getString() {
                String[] data = new String[11];
                if (!this.ECNumber.isEmpty()) {
                        data[0] = this.ECNumber;
                } else {
                        data[0] = "No EC Number";
                }
                
                String geneData = "";
                for (GeneData d : this.enzymesGenes) {
                        geneData += d.getString() + "\n";
                }
                data[1] = geneData;

                if (!this.pathwayName.isEmpty()) {
                        String path = "Pathway name: " + this.pathwayName + " Pathway Link: " + this.pathwayLink + " Pathway synonyms: " + this.pathwaySynonyms;
                        data[2] = path;
                }

                String react = "";
                for (String r : reactants) {
                        react += r + "\n";
                }
                data[3] = react;

                String prod = "";
                for (String p : products) {
                        prod += p + "\n";
                }
                data[4] = prod;

                data[5] = this.direction;

                data[6] = this.massBalance;

                data[7] = this.enzPrimaryName;

                String enzS = "";
                for (String p : this.enzCommissionSynonyms) {
                        enzS += p + "\n";
                }
                data[8] = enzS;

                String uni = "";
                for (String p : this.uniLinks) {
                        uni += p + "\n";
                }
                data[9] = uni;

                String rel = "";
                for (String p : this.relLinks) {
                        rel += p + "\n";
                }
                data[10] = rel;

                for(String s : data){
                        System.out.println(s);
                }
                return data;
        }
}
