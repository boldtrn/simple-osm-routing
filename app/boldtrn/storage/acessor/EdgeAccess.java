package boldtrn.storage.acessor;

/**
 * Created by robin on 16/11/15.
 */
public interface EdgeAccess {

    EdgeAccess get(int index);

    int toIndex();

    double distance();
    short speed();

    boolean pedestriansAllowed();
    boolean carsAllowed();

}
