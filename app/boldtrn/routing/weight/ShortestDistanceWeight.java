package boldtrn.routing.weight;

import boldtrn.storage.Edge;

/**
 * Created by robin on 15/11/15.
 */
public class ShortestDistanceWeight implements Weight{
    @Override
    public double getWeight(Edge edge) {
        return edge.distance;
    }
}
