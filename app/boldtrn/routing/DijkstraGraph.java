package boldtrn.routing;

import boldtrn.routing.acceptor.Acceptor;
import boldtrn.routing.weight.Weight;
import boldtrn.storage.Edge;
import boldtrn.storage.Graph;
import boldtrn.storage.Node;
import play.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by robin on 12/11/15.
 */
public class DijkstraGraph {

    protected final DijkstraNode[] dNodes;
    protected final Graph graph;

    public DijkstraGraph(Graph graph) {

        this.graph = graph;

        Node[] nodes = graph.getNodes();
        dNodes = new DijkstraNode[nodes.length];

        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            DijkstraNode dNode = new DijkstraNode(node, i);
            dNodes[i] = dNode;
        }

    }

    public DijkstraNode[] getNodes() {
        return dNodes;
    }

    public void updateNode(DijkstraNode node) {
        dNodes[node.index] = node;
    }

    public Set<DijkstraNode> getUnvisitedNeighborsWithGreaterDistance(int nodeIndex, double distance, Weight weight, Acceptor acceptor) {
        DijkstraNode fromNode = dNodes[nodeIndex];
        int edgeCount = fromNode.node.getEdgeCount();
        int edgeIndex = fromNode.node.getEdgeIndex();
        Set<DijkstraNode> neighbors = new HashSet<>(edgeCount/2);

        for (int i = 0; i < edgeCount; i++) {
            Edge edge = graph.getEdge(edgeIndex + i);
            if (!acceptor.accept(edge))
                continue;
            DijkstraNode toNode = dNodes[edge.toIndex];
            if (toNode.visited)
                continue;

            double edgeWeight = weight.getWeight(edge);
            double tmpDist = distance + edgeWeight;

            if (tmpDist < toNode.distance) {
                toNode.distance = tmpDist;
                toNode.source = fromNode;
                this.updateNode(toNode);
                neighbors.add(toNode);
            }
        }
        return neighbors;
    }

    @Override
    protected void finalize() throws Throwable {
        for (int i = 0; i < dNodes.length; i++) {
            dNodes[i] = null;
        }
        super.finalize();
    }

}
