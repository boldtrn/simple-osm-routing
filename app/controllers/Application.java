package controllers;

import boldtrn.neo.NeoGraphProvider;
import boldtrn.neo.type.RelTypes;
import boldtrn.routing.Dijkstra;
import boldtrn.routing.Route;
import boldtrn.storage.Graph;
import boldtrn.storage.GraphProvider;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.*;
import play.*;
import play.libs.Json;
import play.mvc.*;

import play.mvc.Result;
import views.html.*;

import java.io.File;

public class Application extends Controller {



    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public Result showMap() {
        return ok(showMap.render());
    }


    public Result neoTest() {

        Logger.debug("Called the neo Test!");

        NeoGraphProvider.INSTANCE.getGraph(true);

        return ok(readGraph.render("Reading the GraphUsingObjects"));
    }

    public Result neoRoute() {

        Logger.debug("Called the neo Route!");

        PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra(
                PathExpanders.forTypeAndDirection(RelTypes.CONNECTS, Direction.OUTGOING ), "distance" );

        Label nodeLabel = DynamicLabel.label("Node");


        Node nodeA = null;
        Node nodeB = null;
        GraphDatabaseService graph = NeoGraphProvider.INSTANCE.getGraph(false);

        int i = 0;
        int a = 13313;
        int b = 277121;


        try ( Transaction tx = graph.beginTx();
              ResourceIterator<Node> nodes = graph.findNodes(nodeLabel) )
        {

            Node curNode;
            while ( nodes.hasNext() )
            {
                curNode = nodes.next();
                if (i == a){
                    nodeA = curNode;
                }

                if(i == b){
                    nodeB = curNode;
                    break;
                }

                i++;

            }

            Logger.info("Starting to search for the shortest Path!");

            long startTime = System.nanoTime();

            WeightedPath path = finder.findSinglePath( nodeA, nodeB );

            long estimatedTime = (System.nanoTime() - startTime) / 1000000000;

            Logger.info("Took "+estimatedTime+" to find a shortest Path!");
            if(path != null){
                Logger.info(path.toString());
                Logger.info(String.valueOf(path.weight()));
            }else {
                Logger.info("Could not find a shortest Path");
            }


            nodes.close();

            tx.success();
        }


        graph.shutdown();

        return ok(readGraph.render("Routing Stuff"));
    }

    public Result readGraph() {

        Logger.debug("Called the read GraphUsingObjects method!");

        GraphProvider.INSTANCE.reloadGraph();

        return ok(readGraph.render("Reading the GraphUsingObjects"));
    }

    public Result route(String vehicle, String weight, double fromLat, double fromLon, double toLat, double toLon) {

        Logger.debug("Called the Routing! From "+fromLat+":"+fromLon+" To "+toLat+":"+toLon+" with vehicle: "+vehicle+" and weight: "+weight);

        Graph graph = GraphProvider.INSTANCE.getGraph();

        int fromIndex = graph.findClosestIndex(fromLat, fromLon);
        int toIndex = graph.findClosestIndex(toLat, toLon);

        Logger.info("Routing from the Node with the index: "+fromIndex+" to the Node with the index: "+toIndex);

        boolean car = vehicle.equals("car");
        boolean fastest = vehicle.equals("fastest");

        ObjectNode result = Json.newObject();

        Route route = null;
        try {
            route = Dijkstra.shortestPath(fromIndex, toIndex, graph, car, fastest);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "err");
            result.put("message", e.getMessage());
            return ok(result);
        }

        result.put("status", "ok");
        ArrayNode coordinates = result.arrayNode();

        for (Route.RouteNode node: route.nodes) {
            ObjectNode coordinate = Json.newObject();
            coordinate.put("lat", node.lat);
            coordinate.put("lon", node.lon);
            coordinates.add(coordinate);
        }

        result.putArray("coordinates").addAll(coordinates);
        result.put("distance", route.distance);

        return ok(result);
    }

}
