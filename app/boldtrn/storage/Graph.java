package boldtrn.storage;

import com.graphhopper.coll.GHLongIntBTree;
import com.graphhopper.coll.LongIntMap;
import com.graphhopper.reader.OSMNode;
import com.graphhopper.reader.OSMWay;
import com.graphhopper.util.DistanceCalcEarth;
import gnu.trove.list.TLongList;
import play.Logger;

import java.util.*;

/**
 * Created by robin on 05/11/15.
 */
public class Graph {

    protected final Node[] nodes;
    protected final Edge[] edges;

    protected final LongIntMap osmIdToIndex;

    protected int nodeIndex = 0;
    protected int edgeOffset = 0;

    protected static final DistanceCalcEarth distCalc = new DistanceCalcEarth();

    protected final String name;

    public static final short pedestrianMaxSpeed = 5;
    public static final short carMaxSpeed = 130;

    protected static final Set<String> restrictedHighwayTags = new HashSet<>(Arrays.asList("private", "ford", "no", "restricted", "military"));
    protected final Set<String> pedestrianHighwayTags = new HashSet<>();
    protected final Map<String, Short> carSpeedMap = new HashMap<>();
    protected final Set<String> sidewalkTags = new HashSet<>();

    public Graph(String name, int nodeCount, int edgeCount) {
        this.name = name;
        nodes = new Node[nodeCount];
        edges = new Edge[edgeCount];

        osmIdToIndex = new GHLongIntBTree(200);

        pedestrianHighwayTags.add("living_street");
        pedestrianHighwayTags.add("pedestrian");
        pedestrianHighwayTags.add("track");
        pedestrianHighwayTags.add("footway");
        pedestrianHighwayTags.add("steps");
        pedestrianHighwayTags.add("path");
        pedestrianHighwayTags.add("residential");
        pedestrianHighwayTags.add("service");
        pedestrianHighwayTags.add("cycleway");
        pedestrianHighwayTags.add("unclassified");
        pedestrianHighwayTags.add("road");

        // autobahn
        carSpeedMap.put("motorway", (short) 100);
        carSpeedMap.put("motorway_link", (short) 70);
        carSpeedMap.put("motorroad", (short) 90);
        // bundesstraße
        carSpeedMap.put("trunk", (short) 70);
        carSpeedMap.put("trunk_link", (short) 65);
        // linking bigger town
        carSpeedMap.put("primary", (short) 65);
        carSpeedMap.put("primary_link", (short) 60);
        // linking towns + villages
        carSpeedMap.put("secondary", (short) 60);
        carSpeedMap.put("secondary_link", (short) 50);
        // streets without middle line separation
        carSpeedMap.put("tertiary", (short) 50);
        carSpeedMap.put("tertiary_link", (short) 40);
        carSpeedMap.put("unclassified", (short) 30);
        carSpeedMap.put("residential", (short) 30);
        // spielstraße
        carSpeedMap.put("living_street", (short) 5);
        carSpeedMap.put("service", (short) 20);
        // unknown road
        carSpeedMap.put("road", (short) 20);
        // forestry stuff
        carSpeedMap.put("track", (short) 15);

        sidewalkTags.add("both");
        sidewalkTags.add("left");
        sidewalkTags.add("right");
        sidewalkTags.add("yes");

    }

    public void addNode(OSMNode osmNode, int numberOfEdges) {
        Node node = new Node(osmNode, edgeOffset);
        nodes[nodeIndex] = node;
        osmIdToIndex.put(osmNode.getId(), nodeIndex);
        nodeIndex++;
        edgeOffset += numberOfEdges;
    }

    public void addWay(final OSMWay way) {
        TLongList wayNodes = way.getNodes();
        int size = wayNodes.size();
        boolean oneway = isOneWay(way);
        boolean carsAllowed = isCarAllowed(way);
        boolean pedestriansAllowed = isPedestrianAllowed(way);
        short speed = getSpeed(way);

        int formerNodeIndex = -1;

        for (int i = 0; i < size; i++) {
            long currentOsmId = wayNodes.get(i);
            int currentNodeIndex = this.findNodeIndexByOsmId(currentOsmId);
            if (currentNodeIndex < 0) {
                Logger.error("Couldn't find the node with id " + currentOsmId);
                continue;
            }
            if (formerNodeIndex >= 0) {
                double distance = calculateDistance(formerNodeIndex, currentNodeIndex);
                addEdge(way.getId(), formerNodeIndex, currentNodeIndex, distance, speed, carsAllowed, pedestriansAllowed);
                if (!oneway) {
                    addEdge(way.getId(), currentNodeIndex, formerNodeIndex, distance, speed, carsAllowed, pedestriansAllowed);
                }
            }
            formerNodeIndex = currentNodeIndex;
        }

    }

    private short getSpeed(OSMWay way) {
        String maxSpeed = way.getTag("maxspeed");

        if (maxSpeed == null || maxSpeed.isEmpty())
            return carSpeedMap.getOrDefault(way.getTag("highway"), (short) 1);

        try {
            return Short.parseShort(maxSpeed);
        } catch (NumberFormatException e) {
            if ("none".equals(maxSpeed))
                return carMaxSpeed;

            if (maxSpeed.equals("walk") || maxSpeed.endsWith(":living_street"))
                return pedestrianMaxSpeed;
            return carSpeedMap.getOrDefault(way.getTag("highway"), (short) 1);
        }
    }

    private boolean isCarAllowed(OSMWay way) {
        String highway = way.getTag("highway");
        if(carSpeedMap.containsKey(highway))
            return true;
        //Logger.info("Not a valid Car Way: "+way);
        return false;
    }

    private boolean isPedestrianAllowed(OSMWay way) {
        String highway = way.getTag("highway");
        if (pedestrianHighwayTags.contains(highway))
            return true;

        if (isCarAllowed(way)) {
            String sidewalk = way.getTag("sidewalk");
            return sidewalkTags.contains(sidewalk);
        }
        return false;
    }

    private void addEdge(long id, int from, int to, double distance, short speed, boolean carAllowed, boolean pedestrianAllowed) {
        Edge edge = new Edge(id, to, distance, speed, carAllowed, pedestrianAllowed);
        Node node = nodes[from];
        edges[node.addEdge()] = edge;
    }

    private double calculateDistance(int fromIndex, int toIndex) {
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

    public Node[] getNodes() {
        return nodes;
    }

    public Edge[] getEdges() {
        return edges;
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

    public static boolean isOneWay(OSMWay way) {
        return way.hasTag("oneway", "yes") || way.hasTag("highway", "motorway") || way.hasTag("junction", "roundabout")
                || way.hasTag("highway", "motorway_link")
                || way.hasTag("highway", "trunk_link")
                || way.hasTag("highway", "primary_link");
    }

    public static boolean isValid(OSMWay way) {
        if (way.getNodes().size() < 2)
            return false;

        if (!way.hasTags())
            return false;

        String highway = way.getTag("highway");
        if (highway == null || restrictedHighwayTags.contains(highway))
            return false;

        // TODO Consider more complex filtering
        return true;
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
}
