package boldtrn.reader;

import boldtrn.storage.Graph;
import boldtrn.storage.GraphUsingArray;
import boldtrn.storage.GraphUsingObjects;
import com.graphhopper.coll.GHLongIntBTree;
import com.graphhopper.coll.LongIntMap;
import com.graphhopper.reader.*;
import com.graphhopper.util.Helper;
import gnu.trove.list.TLongList;
import play.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Created by robin on 05/11/15.
 */
public class PbfReader implements DataReader {

    private final File pbfFile;
    private final int workerThreads = 2;

    private final LongIntMap nodeMap;
    //private final Map<Long, Integer> nodeMap;

    private Graph graph;

    public PbfReader(File pbfFile) {
        this.pbfFile = pbfFile;
        //nodeMap = new HashMap<>();
        nodeMap = new GHLongIntBTree(200);
    }

    public Graph getGraph() {
        return graph;
    }

    @Override
    public void readGraph() throws IOException {
        Logger.info("Pre Processing Started");
        preProcessAndInitGraph();
        Logger.info("Pre Processing Finished");
        generateGraph();
        Logger.info("GraphUsingObjects was generated!");
    }

    protected void generateGraph() {

        OSMInputFile in = null;

        try {
            in = new OSMInputFile(pbfFile).setWorkerThreads(workerThreads).open();
            int nodeCounter = 0;
            int wayCounter = 0;

            Logger.info("Start to create the GraphUsingObjects. Currently "+Helper.getMemInfo());

            OSMElement item;
            while ((item = in.getNext()) != null) {
                switch (item.getType()) {
                    case OSMElement.NODE:
                        processNode((OSMNode) item);
                        nodeCounter++;
                        break;

                    case OSMElement.WAY:
                        processWay((OSMWay) item);
                        wayCounter++;
                        break;
                    case OSMElement.RELATION:
                        // Skip Relation for now
                        break;
                }

                if (nodeCounter == 500000) {
                    Logger.debug("500000 Nodes created");
                    nodeCounter = 0;
                }

                if (wayCounter == 500000) {
                    System.out.println("500000 Ways created");
                    wayCounter = 0;
                }
            }
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
        } finally {
            Helper.close(in);
        }
    }

    protected void processNode(OSMNode node) {
        // Only process Nodes that are used in a way
        int edgeCount = nodeMap.get(node.getId());
        if (edgeCount >= 0)
            graph.addNode(node, edgeCount);
    }

    protected void processWay(OSMWay way) {
        if (Graph.isValid(way))
            graph.addWay(way);
    }


    protected void preProcessAndInitGraph() {

        OSMInputFile in = null;

        try {
            in = new OSMInputFile(pbfFile).setWorkerThreads(workerThreads).open();

            int edgeCounter = 0;
            int wayCounter = 0;

            OSMElement item;
            while ((item = in.getNext()) != null) {

                if (item.isType(OSMElement.WAY)) {
                    final OSMWay way = (OSMWay) item;
                    if (GraphUsingObjects.isValid(way)) {
                        TLongList wayNodes = way.getNodes();
                        int size = wayNodes.size();
                        for (int i = 0; i < size; i++) {
                            long key = wayNodes.get(i);

                            int add;
                            if (GraphUsingObjects.wayIsOneway(way)) {
                                add = 1;
                            } else {
                                add = 2;
                            }

                            if (i == size - 1) {
                                add = add - 1;
                            }

                            if (i == 0) {
                                add = 1;
                            }

                            increaseKeyOfNodemap(key, add);
                            edgeCounter = edgeCounter + add;
                            wayCounter++;
                        }

                    }
                }

                if (wayCounter == 1000000) {
                    System.out.println("1000000 Ways preprocessed");
                    wayCounter = 0;
                }

            /*
            // Care about relations later
            else if(item.isType(OSMElement.RELATION))
            {
                final OSMRelation relation = (OSMRelation) item;
                if (!relation.isMetaRelation() && relation.hasTag("type", "route"))
                    prepareWaysWithRelationInfo(relation);

                if (relation.hasTag("type", "restriction"))
                    prepareRestrictionRelation(relation);
            }
            */
            }
            //Logger.info("The GraphUsingObjects has " + nodeMap.size() + " nodes and " + edgeCounter + " number of edges");
            Logger.info("The Graph has " + nodeMap.getSize() + " nodes and " + edgeCounter + " number of edges");
            //graph = new GraphUsingObjects(pbfFile.getName(), (int) nodeMap.getSize(), edgeCounter);
            graph = new GraphUsingArray(pbfFile.getName(), (int) nodeMap.getSize(), edgeCounter);
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
        } finally {
            Helper.close(in);
        }
    }

    private void increaseKeyOfNodemap(long key, int add) {
        int count = nodeMap.get(key);
        if(count < 0){
            count = 0;
        }
        count = count + add;
        nodeMap.put(key, count);
    }
}
