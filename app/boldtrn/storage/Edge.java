package boldtrn.storage;

/**
 * Created by robin on 05/11/15.
 */
public class Edge extends Element{

    public final int toIndex;
    public final double distance;
    public final short speed;
    public final boolean carsAllowed;
    public final boolean pedestriansAllowed;

    public Edge(Long id, int toIndex, double distance, short speed, boolean carsAllowed, boolean pedestriansAllowed){
        super(id);
        this.toIndex = toIndex;
        this.distance = distance;
        this.speed = speed;
        this.carsAllowed = carsAllowed;
        this.pedestriansAllowed = pedestriansAllowed;
    }

}
