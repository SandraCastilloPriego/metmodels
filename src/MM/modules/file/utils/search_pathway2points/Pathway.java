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
package MM.modules.file.utils.search_pathway2points;

import java.util.ArrayList;

/**
 *
 * @author scsandra
 */
public class Pathway {

        private ArrayList<String> nodes;
        private ArrayList<String> edges;

        public Pathway() {
                this.nodes = new ArrayList<>();
                this.edges = new ArrayList<>();
        }

        public void addNodes(String nodeID) {
                if (!nodes.contains(nodeID)) {
                        this.nodes.add(nodeID);
                }
        }

        public void addEdges(String edgeID) {
                if (!edges.contains(edgeID)) {
                        this.edges.add(edgeID);
                }
        }

        public void setNodes(ArrayList<String> nodes) {
                this.nodes = nodes;
        }

        public void setEdges(ArrayList<String> edges) {
                this.edges = edges;
        }

        public Pathway getCopy() {
                Pathway newPathway = new Pathway();
                newPathway.setEdges((ArrayList<String>) this.edges.clone());
                newPathway.setNodes((ArrayList<String>) this.nodes.clone());
                return newPathway;
        }

        public boolean contains(String finalMet) {               
                boolean m = false;
                for (String node : nodes) {                       
                        if (node.contains(finalMet)) {
                                m = true;
                        }
                }

                if (m) {
                        return true;
                }
                return false;
        }
        
        public int getSize(){
                return edges.size();
        }
        
        public ArrayList<String> getNodes(){
                return nodes;
        }
        
        public ArrayList<String> getEdges(){
                return edges;
        }

        public String getNodeID(String string) {
                for(String node: nodes){
                        if(node.contains(string)){
                             //   System.out.println(node);
                                String[] ids = node.split(" - ");
                                return ids[0];
                        }
                }
                return null;
        }
        
        @Override
        public String toString(){
               
                String s = "";                
                for(String node : nodes){
                        s = s + " - " +node ;
                } 
                return s;
        }
}
