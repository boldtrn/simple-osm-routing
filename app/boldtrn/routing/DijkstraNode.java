package boldtrn.routing;

import boldtrn.storage.Node;

import javax.validation.constraints.NotNull;

/**
 * Created by robin on 10/11/15.
 */
public class DijkstraNode implements Comparable {

    public boolean visited = false;
    public double distance = Double.POSITIVE_INFINITY;
    public DijkstraNode source = null;
    public final int index;

    public DijkstraNode(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DijkstraNode && this.index == ((DijkstraNode) obj).index;
    }

    @NotNull
    @Override
    public int compareTo(Object o) {
        return (int) (this.distance - ((DijkstraNode) o).distance);
    }
}
