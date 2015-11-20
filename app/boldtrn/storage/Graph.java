package boldtrn.storage;

import boldtrn.storage.acessor.EdgeAccess;
import boldtrn.storage.acessor.NodeAccess;
import com.graphhopper.coll.GHLongIntBTree;
import com.graphhopper.coll.LongIntMap;
import com.graphhopper.reader.OSMNode;
import com.graphhopper.reader.OSMWay;
import com.graphhopper.util.DistanceCalcEarth;
import gnu.trove.list.TLongList;
import play.Logger;

import java.util.*;

/**
 * Created by robin on 16/11/15.
 */
public abstract class Graph {

    protected final String name;
    protected static final DistanceCalcEarth distCalc = new DistanceCalcEarth();

    public static final short pedestrianMaxSpeed = 5;
    public static final short carMaxSpeed = 130;

    protected static final Set<String> restrictedHighwayTags = new HashSet<>(Arrays.asList("private", "ford", "no", "restricted", "military"));
    protected static final Set<String> pedestrianHighwayTags = getPedestrianHighwayTags();
    protected static final Map<String, Short> carSpeedMap = getCarSpeedMap();
    protected static final Set<String> sidewalkTags = getSidewalkTags();

    protected final LongIntMap osmIdToIndex;

    protected Graph(String name){
        this.name = name;
        osmIdToIndex = new GHLongIntBTree(200);
    }

    public abstract void addNode(OSMNode osmNode, int numberOfEdges);
    protected abstract void addEdge(long id, int from, int to, double distance, short speed, boolean carAllowed, boolean pedestrianAllowed);
    protected abstract double calculateDistance(int fromIndex, int toIndex);
    public abstract int findNodeIndexByOsmId(long osmId);
    public abstract int getNodeCount();
    public abstract NodeAccess getNodeAccess(int index);
    public abstract EdgeAccess getEdgeAccess(int index);
    public abstract int findClosestIndex(double lat, double lon);

    public void addWay(final OSMWay way) {
        TLongList wayNodes = way.getNodes();
        int size = wayNodes.size();
        boolean oneway = wayIsOneway(way);
        boolean carsAllowed = wayIsCarAllowed(way);
        boolean pedestriansAllowed = wayIsPedestrianAllowed(way);
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



    public static short getSpeed(OSMWay way) {
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

    public static boolean wayIsCarAllowed(OSMWay way) {
        String highway = way.getTag("highway");
        if(carSpeedMap.containsKey(highway))
            return true;
        //Logger.info("Not a valid Car Way: "+way);
        return false;
    }

    public static boolean wayIsPedestrianAllowed(OSMWay way) {
        String highway = way.getTag("highway");
        if (pedestrianHighwayTags.contains(highway))
            return true;

        if (wayIsCarAllowed(way)) {
            String sidewalk = way.getTag("sidewalk");
            return sidewalkTags.contains(sidewalk);
        }
        return false;
    }

    public static boolean wayIsOneway(OSMWay way) {
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

    protected static Set<String> getPedestrianHighwayTags(){
        Set<String> pedestrianHighwayTags = new HashSet<>();

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

        return pedestrianHighwayTags;
    }

    protected static Set<String> getSidewalkTags(){
        Set<String> sidewalkTags = new HashSet<>();

        sidewalkTags.add("both");
        sidewalkTags.add("left");
        sidewalkTags.add("right");
        sidewalkTags.add("yes");

        return sidewalkTags;
    }

    protected static Map<String, Short> getCarSpeedMap(){
        Map<String, Short> carSpeedMap = new HashMap<>();

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

        return carSpeedMap;
    }
}
