package boldtrn.routing.acceptor;

import boldtrn.storage.Edge;
import boldtrn.storage.acessor.EdgeAccess;

/**
 * Created by robin on 15/11/15.
 */
public class PedAcceptor implements Acceptor {
    @Override
    public boolean accept(EdgeAccess edge) {
        return edge.pedestriansAllowed();
    }
}
