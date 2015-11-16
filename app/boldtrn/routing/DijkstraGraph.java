package boldtrn.routing;

import boldtrn.routing.acceptor.Acceptor;
import boldtrn.routing.weight.Weight;
import boldtrn.storage.Edge;
import boldtrn.storage.Graph;
import boldtrn.storage.GraphUsingObjects;
import boldtrn.storage.Node;
import boldtrn.storage.acessor.EdgeAccess;
import boldtrn.storage.acessor.NodeAccess;

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

        int nodeCount = graph.getNodeCount();
        dNodes = new DijkstraNode[nodeCount];

        for (int i = 0; i < nodeCount; i++) {
            DijkstraNode dNode = new DijkstraNode(i);
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
        NodeAccess fromNodeAccess = graph.getNodeAccess(nodeIndex);
        int edgeCount = fromNodeAccess.getEdgeCount();
        int edgeIndex = fromNodeAccess.getEdgeOffset();

        Set<DijkstraNode> neighbors = new HashSet<>(edgeCount/2);

        for (int i = 0; i < edgeCount; i++) {
            EdgeAccess edge = graph.getEdgeAccess(edgeIndex + i);
            if (!acceptor.accept(edge))
                continue;
            DijkstraNode toNode = dNodes[edge.toIndex()];
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

    public NodeAccess getNodeAccess(int index){
        return graph.getNodeAccess(index);
    }

}
