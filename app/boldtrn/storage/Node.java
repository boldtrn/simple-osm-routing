package boldtrn.storage;

import com.graphhopper.reader.OSMNode;

/**
 * Created by robin on 05/11/15.
 */
public class Node extends Element{

    public final double lat;
    public final double lon;

    protected int edgeCount = 0;
    protected  final int edgeIndex;

    public Node(OSMNode node, int edgeIndex) {
        super(node.getId());
        this.lat = node.getLat();
        this.lon = node.getLon();
        this.edgeIndex = edgeIndex;

    }

    public int addEdge(){
        int newEdgeIndex = edgeIndex + edgeCount;
        edgeCount++;
        return newEdgeIndex;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public int getEdgeIndex() {
        return edgeIndex;
    }


    @Override
    public String toString() {
        return "Node with Id: "+ osmId +" lat:"+lat+" lon:"+lon;
    }
}
