package boldtrn.storage;

import boldtrn.reader.PbfReader;
import play.Logger;
import play.Play;

import java.io.File;
import java.io.IOException;

/**
 * Created by robin on 06/11/15.
 */
public enum GraphProvider {

    INSTANCE;

    private Graph graph = null;

    GraphProvider(){
    }

    public synchronized Graph getGraph(){
        if(graph == null){
            reloadGraph();
        }
        return graph;
    }

    public void reloadGraph(){
        graph = null;

        //File osmFile = Play.application().getFile("/conf/germany-latest.osm.pbf");
        //File osmFile = Play.application().getFile("/conf/baden-wuerttemberg-latest.osm.pbf");
        File osmFile = Play.application().getFile("/conf/stuttgart-regbez-latest.osm.pbf");
        //File osmFile = Play.application().getFile("/conf/monaco-latest.osm.pbf");

        //File osmFile = new File("stuttgart-regbez-latest.osm.pbf");
        PbfReader reader = new PbfReader(osmFile);
        try {
            reader.readGraph();
        } catch (IOException e) {
            Logger.error("Cannot load the graph", e);
            e.printStackTrace();
        }

        graph = reader.getGraph();
    }

}
