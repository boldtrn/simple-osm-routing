package boldtrn.routing;

import boldtrn.storage.Edge;

/**
 * Created by robin on 10/11/15.
 */
@Deprecated
public class DijkstraEdge {

    public final DijkstraNode from;
    public final DijkstraNode to;
    public final Edge edge;

    public DijkstraEdge(DijkstraNode from, DijkstraNode to, Edge edge) {
        this.from = from;
        this.to = to;
        this.edge = edge;
    }
}
