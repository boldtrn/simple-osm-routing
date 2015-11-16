package boldtrn.routing.weight;

import boldtrn.storage.Edge;

/**
 * Created by robin on 15/11/15.
 */
public abstract class FastestWeight implements Weight{
    @Override
    public double getWeight(Edge edge) {
        short speed = getSpeed(edge);
        return edge.distance/speed;
    }

    protected abstract short getSpeed(Edge edge);
}
