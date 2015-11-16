package boldtrn.storage;

import boldtrn.storage.acessor.EdgeAccess;
import boldtrn.storage.acessor.NodeAccess;
import com.graphhopper.reader.OSMNode;
import play.Logger;

import java.util.*;

/**
 * Created by robin on 05/11/15.
 */
public class GraphUsingObjects extends Graph{

    protected final Node[] nodes;
    protected final Edge[] edges;

    protected int nodeIndex = 0;
    protected int edgeOffset = 0;

    public GraphUsingObjects(String name, int nodeCount, int edgeCount) {
        super(name);
        nodes = new Node[nodeCount];
        edges = new Edge[edgeCount];
    }

    public void addNode(OSMNode osmNode, int numberOfEdges) {
        Node node = new Node(osmNode, edgeOffset);
        nodes[nodeIndex] = node;
        osmIdToIndex.put(osmNode.getId(), nodeIndex);
        nodeIndex++;
        edgeOffset += numberOfEdges;
    }

    protected void addEdge(long id, int from, int to, double distance, short speed, boolean carAllowed, boolean pedestrianAllowed) {
        Edge edge = new Edge(id, to, distance, speed, carAllowed, pedestrianAllowed);
        Node node = nodes[from];
        edges[node.addEdge()] = edge;
    }

    protected double calculateDistance(int fromIndex, int toIndex) {
        Node from = nodes[fromIndex];
        Node to = nodes[toIndex];
        return distCalc.calcDenormalizedDist(distCalc.calcNormalizedDist(from.lat, from.lon, to.lat, to.lon));
    }

    public Node findNodeByOsmId(long osmId) {
        int index = findNodeIndexByOsmId(osmId);
        if (index >= 0) {
            return nodes[index];
        }
        return null;
    }

    public int findNodeIndexByOsmId(long osmId) {
        return osmIdToIndex.get(osmId);
    }

    @Override
    public int getNodeCount() {
        return nodes.length;
    }

    @Override
    public NodeAccess getNodeAccess(int index) {
        return new ObjectNodeAcces(index);
    }

    @Override
    public EdgeAccess getEdgeAccess(int index) {
        return new ObjectEdgeAccess(index);
    }

    public Node[] getNodes() {
        return nodes;
    }

    public Edge getEdge(int index) {
        return edges[index];
    }

    public Set<Edge> getEdgesOfNode(int nodeIndex) {
        Node node = nodes[nodeIndex];
        int edgeCount = node.edgeCount;
        Set<Edge> edgesOfNode = new HashSet<>(edgeCount);
        for (int i = 0; i < edgeCount; i++) {
            edgesOfNode.add(edges[node.edgeIndex + edgeCount]);
        }
        return edgesOfNode;
    }

    public int getRandomNodeIndex() {
        return new Random().nextInt(nodes.length);
    }

    public int findClosestIndex(double lat, double lon) {
        int closestIndex = -1;
        double distance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < nodes.length; i++) {
            Node tmpNode = nodes[i];
            double tmpDist = distCalc.calcNormalizedDist(lat, lon, tmpNode.lat, tmpNode.lon);
            if (tmpDist < distance) {
                distance = tmpDist;
                closestIndex = i;
            }
        }
        Node node = nodes[closestIndex];
        Logger.info("Retrieved closest Node with a distance of: " + distance + " and Coordinates " + node.lat + ":" + node.lon);
        return closestIndex;
    }

    @Override
    protected void finalize() throws Throwable {
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = null;
        }
        for (int i = 0; i < edges.length; i++) {
            edges[i] = null;
        }
        super.finalize();
    }

    public class ObjectNodeAcces implements NodeAccess {

        private final int index;

        public ObjectNodeAcces(int index) {
            this.index = index;
        }

        public ObjectNodeAcces get(int index) {
            return new ObjectNodeAcces(index);
        }

        public double getLat() {
            return nodes[index].lat;
        }

        public double getLon() {
            return nodes[index].lon;
        }

        public int addEdgeWithIndex() {
            return nodes[index].addEdge();
        }

        @Override
        public int getEdgeCount() {
            return nodes[index].getEdgeCount();
        }

        @Override
        public int getEdgeOffset() {
            return nodes[index].getEdgeIndex();
        }

    }

    public class ObjectEdgeAccess implements EdgeAccess {

        private final int index;

        public ObjectEdgeAccess(int index){
            this.index = index;
        }

        @Override
        public EdgeAccess get(int index) {
            return new ObjectEdgeAccess(index);
        }

        @Override
        public int toIndex() {
            return edges[index].toIndex;
        }

        @Override
        public double distance() {
            return edges[index].distance;
        }

        @Override
        public short speed() {
            return edges[index].speed;
        }

        @Override
        public boolean pedestriansAllowed() {
            return edges[index].pedestriansAllowed;
        }

        @Override
        public boolean carsAllowed() {
            return edges[index].pedestriansAllowed;
        }
    }

}
