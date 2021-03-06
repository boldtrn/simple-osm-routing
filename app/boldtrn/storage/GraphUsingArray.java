package boldtrn.storage;

import boldtrn.storage.acessor.EdgeAccess;
import boldtrn.storage.acessor.NodeAccess;
import com.graphhopper.reader.OSMNode;
import play.Logger;

/**
 * Created by robin on 16/11/15.
 */
public class GraphUsingArray extends Graph {

    //public static final int OSMID_UPPER = 0;
    //public static final int OSMID_LOWER = 1;
    public static final int LAT_UPPER = 0;
    public static final int LAT_LOWER = 1;
    public static final int LON_UPPER = 2;
    public static final int LON_LOWER = 3;
    public static final int EDGE_COUNT = 4;
    public static final int EDGE_OFFSET = 5;

    public static final int TO_INDEX = 0;
    public static final int DISTANCE_UPPER = 1;
    public static final int DISTANCE_LOWER = 2;
    public static final int EDGE_DATA = 3;
    public static final int SPEED = 3;
    public static final int CARS_ALLOWED = 1;
    public static final int PEDESTRIANS_ALLOWED = 1;

    private static final int offsetPerNode = 6;
    private static final int offsetPerEdge = 4;

    protected final int[] nodes;
    protected int nodeCounter = 0;
    protected int edgeOffset = 0;
    private final NodeCreator nodeCreator;

    protected final int[] edges;
    private final EdgeCreator edgeCreator;

    private NodeAccess nodeAccess = new ArrayNodeAcces(0);

    public GraphUsingArray(String name, int nodeCount, int edgeCount) {
        super(name);
        nodeCreator = new NodeCreator();
        int nodeArrSize = nodeCount * offsetPerNode;
        Logger.info("Creating Node Array with: " + nodeArrSize + " entries");
        nodes = new int[nodeArrSize];
        edgeCreator = new EdgeCreator();
        int edgeArrSize = edgeCount * offsetPerEdge;
        Logger.info("Creating Edge Array with: " + edgeArrSize + " entries");
        edges = new int[edgeArrSize];
        Logger.info("Creating Graph with " + nodeCount + " Nodes and " + edgeCount + " Edges");
    }

    @Override
    public void addNode(OSMNode osmNode, int numberOfEdges) {
        nodeCreator.create(nodeCounter, osmNode.getId(), osmNode.getLat(), osmNode.getLon());
        osmIdToIndex.put(osmNode.getId(), nodeCounter);
        nodeCounter++;
        edgeOffset = edgeOffset + numberOfEdges;
    }

    @Override
    protected void addEdge(long id, int from, int to, double distance, short speed, boolean carAllowed, boolean pedestrianAllowed) {
        int index = nodeAccess.get(from).addEdgeWithIndex();
        edgeCreator.create(index, id, to, distance, speed, carAllowed, pedestrianAllowed);
    }

    @Override
    protected double calculateDistance(int fromIndex, int toIndex) {
        NodeAccess fromAccess = nodeAccess.get(fromIndex);
        NodeAccess toAccess = nodeAccess.get(toIndex);
        double normDist = distCalc.calcNormalizedDist(fromAccess.getLat(), fromAccess.getLon(), toAccess.getLat(), toAccess.getLon());
        return distCalc.calcDenormalizedDist(normDist);
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
        return new ArrayNodeAcces(index);
    }

    @Override
    public EdgeAccess getEdgeAccess(int index) {
        return new ArrayEdgeAccess(index);
    }

    @Override
    public int findClosestIndex(double lat, double lon) {
        int closestIndex = -1;
        double distance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < nodeCounter; i++) {
            NodeAccess nodeAccess = new ArrayNodeAcces(i);
            double tmpDist = distCalc.calcNormalizedDist(lat, lon, nodeAccess.getLat(), nodeAccess.getLon());
            if (tmpDist < distance) {
                distance = tmpDist;
                closestIndex = i;
            }
        }
        NodeAccess nodeAccess = new ArrayNodeAcces(closestIndex);
        Logger.info("Found Closest Node for " + lat + ":" + lon + " is " + nodeAccess.getLat() + ":" + nodeAccess.getLon());
        return closestIndex;
    }

    public class ArrayNodeAcces implements NodeAccess {

        private final int index;

        public ArrayNodeAcces(int index) {
            this.index = index * offsetPerNode;
        }

        public ArrayNodeAcces get(int index) {
            return new ArrayNodeAcces(index);
        }

        public double getLat() {
            return getDouble(LAT_UPPER, LAT_LOWER);
        }

        public double getLon() {
            return getDouble(LON_UPPER, LON_LOWER);
        }

        public int addEdgeWithIndex() {
            int edgeIndex = nodes[this.index + EDGE_COUNT] + nodes[this.index + EDGE_OFFSET];
            nodes[this.index + EDGE_COUNT] = nodes[this.index + EDGE_COUNT] + 1;
            return edgeIndex;
        }

        @Override
        public int getEdgeCount() {
            return nodes[index + EDGE_COUNT];
        }

        @Override
        public int getEdgeOffset() {
            return nodes[index + EDGE_OFFSET];
        }

        private double getDouble(int upperIndex, int lowerIndex) {
            int upper = nodes[index + upperIndex];
            int lower = nodes[index + lowerIndex];
            return Double.longBitsToDouble(((long) upper << 32) | (lower & 0xFFFFFFFFL));
        }

    }

    public class ArrayEdgeAccess implements EdgeAccess {

        private final int index;

        public ArrayEdgeAccess(int index) {
            this.index = index * offsetPerEdge;
        }

        @Override
        public EdgeAccess get(int index) {
            return new ArrayEdgeAccess(index);
        }

        @Override
        public int toIndex() {
            return edges[this.index + TO_INDEX];
        }

        @Override
        public double distance() {
            return getDouble(DISTANCE_UPPER, DISTANCE_LOWER);
        }

        @Override
        public short speed() {
            return (short) (edges[this.index + EDGE_DATA] % 1000);
        }

        @Override
        public boolean pedestriansAllowed() {
            return (edges[this.index + EDGE_DATA] / 10000) == 1;
        }

        @Override
        public boolean carsAllowed() {
            return ((edges[this.index + EDGE_DATA] / 1000) % 10) == 1;
        }

        private double getDouble(int upperIndex, int lowerIndex) {
            int upper = edges[this.index + upperIndex];
            int lower = edges[this.index + lowerIndex];
            return Double.longBitsToDouble(((long) upper << 32) | (lower & 0xFFFFFFFFL));
        }

    }

    public class EdgeCreator extends Creator {

        public void create(int index, long osmId, int toIndex, double distance, int speed, boolean carsAllowed, boolean pedestriansAllowed) {
            //saveLongToArray(index, OSMID_UPPER, OSMID_LOWER, osmId);

            index = index * offsetPerEdge;

            edges[index + TO_INDEX] = toIndex;

            long distanceAsLong = Double.doubleToLongBits(distance);
            saveLongToArray(index, DISTANCE_UPPER, DISTANCE_LOWER, distanceAsLong);

            int carsAllowedInt = (carsAllowed) ? 1000 : 0;
            int pedsAllowedInt = (pedestriansAllowed) ? 10000 : 0;

            edges[index + EDGE_DATA] = pedsAllowedInt + carsAllowedInt + speed;
        }

        protected void saveLongToArray(int index, int upper, int lower, long value) {
            edges[index + upper] = (int) (value >> 32);
            edges[index + lower] = (int) (value);
        }
    }

    public class NodeCreator extends Creator {

        public void create(int index, long osmId, double lat, double lon) {
            //saveLongToArray(index, OSMID_UPPER, OSMID_LOWER, osmId);

            index = index * offsetPerNode;

            long latAsLong = Double.doubleToLongBits(lat);
            saveLongToArray(index, LAT_UPPER, LAT_LOWER, latAsLong);

            long lonAsLong = Double.doubleToLongBits(lon);
            saveLongToArray(index, LON_UPPER, LON_LOWER, lonAsLong);

            nodes[index + EDGE_COUNT] = 0;
            nodes[index + EDGE_OFFSET] = edgeOffset;
        }

        protected void saveLongToArray(int index, int upper, int lower, long value) {
            nodes[index + upper] = (int) (value >> 32);
            nodes[index + lower] = (int) (value);
        }
    }

    public abstract class Creator {
        abstract void saveLongToArray(int index, int upper, int lower, long value);
    }
}
