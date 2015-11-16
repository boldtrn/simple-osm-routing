package boldtrn.routing.weight;

import boldtrn.storage.Edge;
import boldtrn.storage.GraphUsingObjects;
import boldtrn.storage.acessor.EdgeAccess;

/**
 * Created by robin on 15/11/15.
 */
public class FastestCarWeight extends FastestWeight{
    @Override
    protected short getSpeed(EdgeAccess edge) {
        short speed = edge.speed();
        if(speed > GraphUsingObjects.carMaxSpeed)
            return GraphUsingObjects.carMaxSpeed;
        return speed;
    }
}
