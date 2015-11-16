package boldtrn.routing.weight;

import boldtrn.storage.Edge;
import boldtrn.storage.acessor.EdgeAccess;

/**
 * Created by robin on 15/11/15.
 */
public interface Weight {

    double getWeight(EdgeAccess edge);

}
