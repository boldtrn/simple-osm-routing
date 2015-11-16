package boldtrn.routing;

import boldtrn.storage.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by robin on 14/11/15.
 */
public class Route {

    public final List<Node> nodes;
    public final double distance;

    public Route(DijkstraNode end){
        this.distance = end.distance;
        nodes = new ArrayList<>();

        DijkstraNode node = end;
        DijkstraNode source;
        nodes.add(node.node);
        while ((source = node.source) != null) {
            node = source;
            nodes.add(node.node);
        }
        Collections.reverse(nodes);
    }

}
