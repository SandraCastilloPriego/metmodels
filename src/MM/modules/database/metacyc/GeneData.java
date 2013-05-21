/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MM.modules.database.metacyc;

/**
 *
 * @author scsandra
 */
public class GeneData {
        public String enzymeLink, enzymeName, geneLink, geneName, organismLink, organismName, enzymeSynonyms, genesSynonyms, product;  
        
        public String getString(){
                String data = "Enzyme link: "+ enzymeLink + " Enzyme name: "+ enzymeName 
                        + " Gene link: "+ geneLink + " Gene name: "+ geneName +
                        " Organism link: " + organismLink + " Organism name: " + organismName 
                        + " Enzyme synonyms: " + enzymeSynonyms + " Gene Synonyms: " + genesSynonyms + 
                        " Products: " + product;
                
                return data;
        }
        
}
