(function () {
    'use strict';

    L.Routing = L.Routing || {};

    L.Routing.Boldtrn = L.Class.extend({
        options: {
            serviceUrl: 'http://localhost:9000/route',
            timeout: 30 * 1000000,
            routingOptions: {
                vehicle: 'car',
                weight: 'fastest'
            }
        },

        initialize: function (options) {
            L.Util.setOptions(this, options);
            this._hints = {
                locations: {}
            };
        },

        route: function (waypoints, callback, context, options) {
            var url,
                wp;

            wp = waypoints[0];
            var from = new L.Routing.Waypoint(wp.latLng, wp.name, wp.options);
            wp = waypoints[waypoints.length - 1];
            var to = new L.Routing.Waypoint(wp.latLng, wp.name, wp.options);

            url = this.options.serviceUrl;
            url = url + "/" + this.options.routingOptions.vehicle;
            url = url + "/" + this.options.routingOptions.weight;
            url = url + "/" + from.latLng.lat;
            url = url + "/" + from.latLng.lng;
            url = url + "/" + to.latLng.lat;
            url = url + "/" + to.latLng.lng;

            $.get(url)
                .done(function (data) {

                    if(data.status != 'ok' || !data.coordinates){
                        alert(data.message);
                        callback.call(context, {
                            status: data.status,
                            message: data.message
                        });
                        return;
                    }

                    var summary = {
                        'totalTime': 12345,
                        'totalDistance': data.distance
                    };

                    var coordinates = [];

                    for (var i = 0; i < data.coordinates.length; i++){
                        coordinates.push(L.latLng(data.coordinates[i].lat,data.coordinates[i].lon))
                    }

                    var alts = [{
                        name: 'ssp',
                        coordinates: coordinates,
                        instructions: [],
                        summary: summary,
                        inputWaypoints: waypoints,
                        waypoints: waypoints,
                    }];

                    callback.call(context, null, alts);
                });

            return this;
        },

        updateVehicle: function(vehicle){
            this.options.routingOptions.vehicle = vehicle;
        },

        updateWeight: function(weight){
            this.options.routingOptions.weight = weight;
        }

    });

    L.Routing.boldtrn = function (options) {
        return new L.Routing.Boldtrn(options);
    };

})();