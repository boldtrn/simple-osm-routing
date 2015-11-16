package boldtrn.storage;

/**
 * Created by robin on 05/11/15.
 */
public abstract class Element {

    public final long osmId;


    public Element(long osmId){
        this.osmId = osmId;
    }


    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Element){
            return this.osmId == ((Element) obj).osmId;
        }
        return super.equals(obj);
    }
}
