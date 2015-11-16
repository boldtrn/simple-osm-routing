package boldtrn.routing;

import boldtrn.routing.acceptor.Acceptor;
import boldtrn.routing.acceptor.CarAcceptor;
import boldtrn.routing.acceptor.PedAcceptor;
import boldtrn.routing.weight.FastestCarWeight;
import boldtrn.routing.weight.FastestPedWeight;
import boldtrn.routing.weight.ShortestDistanceWeight;
import boldtrn.routing.weight.Weight;
import boldtrn.storage.Graph;
import play.Logger;

import java.util.*;

/**
 * Created by robin on 10/11/15.
 */
public class Dijkstra {
    public static Route shortestPath(int fromIndex, int toIndex, Graph graph, boolean shortest, boolean car) throws Exception {

        Logger.info("Initializing Dijkstra");

        DijkstraGraph dGraph = new DijkstraGraph(graph);
        Queue<DijkstraNode> queue = new PriorityQueue<>();

        DijkstraNode fromTmp = dGraph.getNodes()[fromIndex];
        fromTmp.distance = 0;
        queue.add(fromTmp);
        dGraph.updateNode(fromTmp);

        Weight weight = getWeight(shortest, car);
        Acceptor acceptor = getAcceptor(car);

        Logger.info("Finished initializing Dijkstra");
        Logger.info("Starting with the actual Dijkstra");

        int workedNodes = 0;

        while (!queue.isEmpty()) {
            Logger.trace("Size of the Queue: " + queue.size());
            DijkstraNode currentNodeFromQue = queue.remove();
            DijkstraNode currentNode = dGraph.getNodes()[currentNodeFromQue.index];
            if (currentNode.distance != currentNodeFromQue.distance || currentNode.visited) {
                Logger.trace("Skipping node with the id:" + currentNode.index + " since the distance does not match");
                continue;
            }
            currentNode.visited = true;
            workedNodes++;

            if (isEndNode(currentNode, toIndex)) {
                Logger.info("Found a shortest path");
                return getPathFromNode(currentNode, dGraph);

            }

            Set<DijkstraNode> unvisitedNeighbors = dGraph.getUnvisitedNeighborsWithGreaterDistance(currentNode.index, currentNode.distance, weight, acceptor);
            for (DijkstraNode node : unvisitedNeighbors) {
                queue.add(node);
            }

            dGraph.updateNode(currentNode);
        }

        Logger.warn("Could not find shortest path");
        throw new Exception("No shortest path found after " + workedNodes + " iterations");

    }

    private static Acceptor getAcceptor(boolean car) {
        if(car)
            return new CarAcceptor();
        return new PedAcceptor();
    }

    private static Weight getWeight(boolean shortest, boolean car) {
        if(shortest)
            return new ShortestDistanceWeight();
        if (car)
            return new FastestCarWeight();
        return new FastestPedWeight();
    }

    private static boolean isEndNode(DijkstraNode node, int toIndex) {
        return node.index == toIndex;
    }

    public static Route getPathFromNode(DijkstraNode node, DijkstraGraph graph) {
        return new Route(node, graph);
    }
}
