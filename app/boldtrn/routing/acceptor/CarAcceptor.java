package boldtrn.routing.acceptor;

import boldtrn.storage.Edge;

/**
 * Created by robin on 15/11/15.
 */
public class CarAcceptor implements Acceptor {
    @Override
    public boolean accept(Edge edge) {
        return edge.carsAllowed;
    }
}
