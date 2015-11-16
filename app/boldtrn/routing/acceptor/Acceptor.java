package boldtrn.routing.acceptor;

import boldtrn.storage.Edge;

/**
 * Created by robin on 15/11/15.
 */
public interface Acceptor {

    boolean accept(Edge edge);
}
