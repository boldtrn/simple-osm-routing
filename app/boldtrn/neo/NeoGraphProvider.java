package boldtrn.neo;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.io.fs.FileUtils;
import play.Logger;
import play.Play;

import java.io.File;
import java.io.IOException;

/**
 * Created by robin on 17/11/15.
 */
public enum NeoGraphProvider {

    INSTANCE;

    NeoGraphProvider() {
    }

    public synchronized GraphDatabaseService getGraph(boolean reload) {
        if (reload) {
            deleteDatabase(true);
        }
        GraphDatabaseService graph = null;
        //File osmFile = Play.application().getFile("/conf/monaco-latest.osm.pbf");
        //File osmFile = Play.application().getFile("/conf/baden-wuerttemberg-latest.osm.pbf");
        File osmFile = Play.application().getFile("/conf/stuttgart-regbez-latest.osm.pbf");

        GraphDatabaseFactory factory = new GraphDatabaseFactory();
        GraphDatabaseBuilder builder = factory.newEmbeddedDatabaseBuilder( getNeoPath() );
        builder.setConfig(GraphDatabaseSettings.pagecache_memory, "8g");
        factory.setUserLogProvider(new NeoLogProvider());
        graph = builder.newGraphDatabase();
        registerShutdownHook(graph);

        if (reload) {
            NeoReader reader = new NeoReader(osmFile, graph);
            try {
                reader.readGraph();
            } catch (IOException e) {
                Logger.error(e.getMessage(), e);
            }
        }
        return graph;
    }

    protected File getNeoPath() {
        return Play.application().getFile("/conf/neo4j");
    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }

    protected void deleteDatabase(boolean synchronous) {
        if (synchronous)
        {
            try {
                FileUtils.deleteRecursively(getNeoPath());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FileUtils.deleteRecursively(getNeoPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

}
