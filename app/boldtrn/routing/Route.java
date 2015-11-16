package boldtrn.routing;

import boldtrn.storage.Node;
import boldtrn.storage.acessor.NodeAccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by robin on 14/11/15.
 */
public class Route {

    public final List<RouteNode> nodes;
    public final double distance;

    public Route(DijkstraNode end, DijkstraGraph graph){
        this.distance = end.distance;
        nodes = new ArrayList<>();

        DijkstraNode node = end;
        DijkstraNode source;
        nodes.add(getRouteNodeForNode(node, graph));
        while ((source = node.source) != null) {
            node = source;
            nodes.add(getRouteNodeForNode(node, graph));
        }
        Collections.reverse(nodes);
    }

    private RouteNode getRouteNodeForNode(DijkstraNode node, DijkstraGraph graph){
        NodeAccess access = graph.getNodeAccess(node.index);
        return new RouteNode(access.getLat(), access.getLon());
    }

    public class RouteNode{
        public final double lat;
        public final double lon;

        public RouteNode(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }

}
