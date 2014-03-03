/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MM.modules.reconstruction.gapfilling.network;

import java.util.List;

/**
 *
 * @author scsandra
 */
public class Graph {
  private final List<Node> vertexes;
  private final List<Edge> edges;

  public Graph(List<Node> vertexes, List<Edge> edges) {
    this.vertexes = vertexes;
    this.edges = edges;
  }

  public List<Node> getVertexes() {
    return vertexes;
  }

  public List<Edge> getEdges() {
    return edges;
  }
  
  
  
} 