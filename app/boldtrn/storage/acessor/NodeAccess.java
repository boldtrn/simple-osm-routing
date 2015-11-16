package boldtrn.storage.acessor;

/**
 * Created by robin on 16/11/15.
 */
public interface NodeAccess {

    NodeAccess get(int index);

    double getLat();

    double getLon();

    int addEdgeWithIndex();

    int getEdgeCount();

    int getEdgeOffset();
}
