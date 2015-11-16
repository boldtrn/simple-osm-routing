package boldtrn.routing.weight;

import boldtrn.storage.Edge;
import boldtrn.storage.acessor.EdgeAccess;

/**
 * Created by robin on 15/11/15.
 */
public abstract class FastestWeight implements Weight{
    @Override
    public double getWeight(EdgeAccess edge) {
        short speed = getSpeed(edge);
        return edge.distance()/speed;
    }

    protected abstract short getSpeed(EdgeAccess edge);
}
