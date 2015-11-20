package boldtrn.neo;

import boldtrn.neo.type.RelTypes;
import boldtrn.storage.Graph;
import boldtrn.storage.GraphUsingObjects;
import com.graphhopper.reader.*;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.Helper;
import gnu.trove.list.TLongList;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import play.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by robin on 05/11/15.
 */
public class NeoReader implements DataReader {

    private final File pbfFile;
    private final int workerThreads = 1;

    private final GraphDatabaseService graph;

    protected static final DistanceCalcEarth distCalc = new DistanceCalcEarth();

    private static final String nodeLabelName = "Node";
    private static final String osmIdProperty = "osmId";
    private static final String latProperty = "lat";
    private static final String lonProperty = "lon";

    private static final String speedProperty = "speed";
    private static final String distanceProperty = "distance";

    public NeoReader(File pbfFile, GraphDatabaseService graph) {
        this.pbfFile = pbfFile;
        this.graph = graph;
    }

    @Override
    public void readGraph() throws IOException {
        Logger.info("Creating Graph Schema");
        createGraphSchema();
        Logger.info("Processing Nodes");
        processNodes();
        Logger.info("Processing Edges");
        processEdges();
        Logger.info("GraphUsingObjects was generated!");

        graph.shutdown();
    }

    public void createGraphSchema() {

        IndexDefinition nodeOsmIdIndex;
        IndexDefinition nodeLatIndex;
        IndexDefinition nodeLonIndex;
        Schema schema;

        try (Transaction tx = graph.beginTx()) {
            schema = graph.schema();
            nodeOsmIdIndex = schema.indexFor(DynamicLabel.label(nodeLabelName))
                    .on(osmIdProperty)
                    .create();
            nodeLatIndex = schema.indexFor(DynamicLabel.label(nodeLabelName))
                    .on(latProperty)
                    .create();
            nodeLonIndex = schema.indexFor(DynamicLabel.label(nodeLabelName))
                    .on(lonProperty)
                    .create();
            tx.success();
        }

        try (Transaction tx = graph.beginTx()) {
            schema.awaitIndexOnline(nodeOsmIdIndex, 10, TimeUnit.SECONDS);
            schema.awaitIndexOnline(nodeLatIndex, 10, TimeUnit.SECONDS);
            schema.awaitIndexOnline(nodeLonIndex, 10, TimeUnit.SECONDS);
            tx.success();
        }

    }


    protected void processNodes() {

        OSMInputFile in = null;

        try {
            in = new OSMInputFile(pbfFile).setWorkerThreads(workerThreads).open();

            int nodeCounter = 0;
            int allCounter = 0;

            Logger.info("Start to create the Nodes. Currently " + Helper.getMemInfo());

            Transaction tx = graph.beginTx();
            try {

                Label nodeLabel = DynamicLabel.label(nodeLabelName);

                OSMElement item;
                while ((item = in.getNext()) != null) {
                    if (item.isType(OSMElement.NODE)) {
                        final OSMNode node = (OSMNode) item;
                        Node osmNode = graph.createNode(nodeLabel);
                        osmNode.setProperty(osmIdProperty, node.getId());
                        osmNode.setProperty(latProperty, node.getLat());
                        osmNode.setProperty(lonProperty, node.getLon());
                        nodeCounter++;
                    }

                    allCounter++;

                    if (nodeCounter == 50000) {
                        Logger.debug("50000 Nodes created");
                        nodeCounter = 0;
                        tx.success();
                        tx.close();
                        tx = graph.beginTx();
                    }

                    if (allCounter == 500000) {
                        Logger.debug("500000 Instances Processed");
                        allCounter = 0;
                        tx.success();
                        tx.close();
                        tx = graph.beginTx();
                    }

                }


            }finally {
                tx.success();
                tx.close();
            }

        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
        } finally {
            Helper.close(in);
        }
    }

    private void processEdges() {

        OSMInputFile in = null;

        try {
            in = new OSMInputFile(pbfFile).setWorkerThreads(workerThreads).open();

            int allCounter = 0;
            int wayCounter = 0;

            Logger.info("Start to create the Edges. Currently " + Helper.getMemInfo());

            Label nodeLabel = DynamicLabel.label(nodeLabelName);

            OSMElement item;
            while ((item = in.getNext()) != null) {
                if (item.isType(OSMElement.WAY)) {
                    final OSMWay way = (OSMWay) item;
                    if (GraphUsingObjects.isValid(way)) {
                        final boolean isOneWay = GraphUsingObjects.wayIsOneway(way);
                        final short speed = Graph.getSpeed(way);
                        final TLongList wayNodes = way.getNodes();
                        final int size = wayNodes.size();
                        long formerKey = Long.MIN_VALUE;
                        for (int i = 0; i < size; i++) {
                            long key = wayNodes.get(i);

                            if (formerKey > Long.MIN_VALUE) {
                                try (Transaction tx = graph.beginTx()) {
                                    // Get the Nodes and Stuff
                                    Node from = graph.findNode(nodeLabel, osmIdProperty, formerKey);
                                    Node to = graph.findNode(nodeLabel, osmIdProperty, key);
                                    double distance = calculateDistance(from, to);
                                    createRelationship(from, to, distance, speed);
                                    if(!isOneWay){
                                        createRelationship(to, from, distance, speed);
                                    }
                                    wayCounter++;
                                    tx.success();
                                }
                            }

                            formerKey = key;
                        }

                    }
                }

                allCounter++;

                if (wayCounter == 50000) {
                    Logger.debug("50000 Ways created");
                    wayCounter = 0;
                }

                if (allCounter == 500000) {
                    Logger.debug("500000 Elements processed");
                    allCounter = 0;
                }

            }

        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
        } finally {
            Helper.close(in);
        }


    }

    private void createRelationship(Node from, Node to, double distance, short speed){
        Relationship relationship = from.createRelationshipTo(to, RelTypes.CONNECTS);
        relationship.setProperty(speedProperty, speed);
        relationship.setProperty(distanceProperty, calculateDistance(from, to));
    }

    protected double calculateDistance(Node from, Node to) {
        double fromLat = (double) from.getProperty(latProperty);
        double fromLon = (double) from.getProperty(lonProperty);
        double toLat = (double) to.getProperty(latProperty);
        double toLon = (double) to.getProperty(lonProperty);
        double normDist = distCalc.calcNormalizedDist(fromLat, fromLon, toLat, toLon);
        return distCalc.calcDenormalizedDist(normDist);
    }

}
