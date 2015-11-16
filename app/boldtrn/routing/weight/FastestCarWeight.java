package boldtrn.routing.weight;

import boldtrn.storage.Edge;
import boldtrn.storage.Graph;

/**
 * Created by robin on 15/11/15.
 */
public class FastestCarWeight extends FastestWeight{
    @Override
    protected short getSpeed(Edge edge) {
        short speed = edge.speed;
        if(speed > Graph.carMaxSpeed)
            return Graph.carMaxSpeed;
        return speed;
    }
}
