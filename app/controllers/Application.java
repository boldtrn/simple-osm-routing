package controllers;

import boldtrn.routing.Dijkstra;
import boldtrn.routing.Route;
import boldtrn.storage.Graph;
import boldtrn.storage.GraphUsingObjects;
import boldtrn.storage.GraphProvider;
import boldtrn.storage.Node;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.*;
import play.libs.Json;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {



    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public Result showMap() {
        return ok(showMap.render());
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
