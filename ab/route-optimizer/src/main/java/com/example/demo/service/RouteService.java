package com.example.demo.service;

import com.example.demo.dto.RouteRequest;
import com.example.demo.model.Location;
import com.example.demo.model.Route;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RouteService {

    private final Driver driver;
    private final LocationService locationService;

    public RouteService(Driver driver, LocationService locationService) {
        this.driver = driver;
        this.locationService = locationService;
    }

    public Route createRoute(RouteRequest routeRequest) {
        try (Session session = driver.session()) {
            // 1. Proveri da li start lokacija već postoji ili je kreiraj
            String startQuery = """
                MERGE (start:Location {name: $startName})
                ON CREATE SET start.lat = $startLat, start.lon = $startLon
                ON MATCH SET start.lat = COALESCE($startLat, start.lat), 
                             start.lon = COALESCE($startLon, start.lon)
                RETURN id(start) as startId
                """;

            Result startResult = session.run(startQuery,
                    Values.parameters(
                            "startName", routeRequest.getStart(),
                            "startLat", routeRequest.getStartLat() != null ? routeRequest.getStartLat() : 44.8125,
                            "startLon", routeRequest.getStartLon() != null ? routeRequest.getStartLon() : 20.4612
                    )
            );

            Long startId = startResult.single().get("startId").asLong();

            // 2. Proveri da li end lokacija već postoji ili je kreiraj
            String endQuery = """
                MERGE (end:Location {name: $endName})
                ON CREATE SET end.lat = $endLat, end.lon = $endLon
                ON MATCH SET end.lat = COALESCE($endLat, end.lat), 
                             end.lon = COALESCE($endLon, end.lon)
                RETURN id(end) as endId
                """;

            Result endResult = session.run(endQuery,
                    Values.parameters(
                            "endName", routeRequest.getEnd(),
                            "endLat", routeRequest.getEndLat() != null ? routeRequest.getEndLat() : 45.2671,
                            "endLon", routeRequest.getEndLon() != null ? routeRequest.getEndLon() : 19.8335
                    )
            );

            Long endId = endResult.single().get("endId").asLong();

            // 3. Kreiraj ROAD vezu između lokacija
            String createRoadQuery = """
                MATCH (start:Location), (end:Location)
                WHERE id(start) = $startId AND id(end) = $endId
                MERGE (start)-[r:ROAD]->(end)
                ON CREATE SET r.distanceKm = $distanceKm, 
                              r.durationHours = $durationHours,
                              r.blocked = false,
                              r.reason = null
                ON MATCH SET r.distanceKm = $distanceKm,
                             r.durationHours = $durationHours
                RETURN id(r) as roadId
                """;

            session.run(createRoadQuery,
                    Values.parameters(
                            "startId", startId,
                            "endId", endId,
                            "distanceKm", routeRequest.getDistanceKm(),
                            "durationHours", routeRequest.getDurationHours()
                    )
            );

            // 4. Kreiraj rutu sa STARTS_AT i ENDS_AT vezama
            String createRouteQuery = """
                CREATE (r:Route {
                    distanceKm: $distanceKm, 
                    durationHours: $durationHours, 
                    status: 'planirana'
                })
                WITH r
                MATCH (start:Location), (end:Location)
                WHERE id(start) = $startId AND id(end) = $endId
                CREATE (r)-[:STARTS_AT]->(start)
                CREATE (r)-[:ENDS_AT]->(end)
                RETURN r, id(r) as routeId, start, end
                """;

            Result routeResult = session.run(createRouteQuery,
                    Values.parameters(
                            "startId", startId,
                            "endId", endId,
                            "distanceKm", routeRequest.getDistanceKm(),
                            "durationHours", routeRequest.getDurationHours()
                    )
            );

            var record = routeResult.single();
            Long routeId = record.get("routeId").asLong();
            var routeNode = record.get("r").asNode();
            var startNode = record.get("start").asNode();
            var endNode = record.get("end").asNode();

            // 5. Kreiraj Route objekat za povrat
            Route route = new Route();
            route.setId(routeId);
            route.setDistanceKm(routeNode.get("distanceKm").asDouble());
            route.setDurationHours(routeNode.get("durationHours").asDouble());
            route.setStatus(routeNode.get("status").asString());

            // Start Location
            Location startLocation = new Location();
            startLocation.setId(startId);
            startLocation.setName(startNode.get("name").asString());
            startLocation.setLat(startNode.get("lat").asDouble());
            startLocation.setLon(startNode.get("lon").asDouble());
            route.setStartLocation(startLocation);

            // End Location
            Location endLocation = new Location();
            endLocation.setId(endId);
            endLocation.setName(endNode.get("name").asString());
            endLocation.setLat(endNode.get("lat").asDouble());
            endLocation.setLon(endNode.get("lon").asDouble());
            route.setEndLocation(endLocation);

            // 6. Ako postoji path, kreiraj FOLLOWS_ROUTE veze
            if (routeRequest.getPath() != null && !routeRequest.getPath().isEmpty()) {
                List<Location> pathLocations = new ArrayList<>();

                for (String locationName : routeRequest.getPath()) {
                    String mergePathQuery = """
                        MERGE (loc:Location {name: $name})
                        ON CREATE SET loc.lat = 0.0, loc.lon = 0.0
                        RETURN id(loc) as locId, loc
                        """;

                    var pathResult = session.run(mergePathQuery,
                            Values.parameters("name", locationName)
                    );

                    var pathRecord = pathResult.single();
                    Long locId = pathRecord.get("locId").asLong();
                    var locNode = pathRecord.get("loc").asNode();

                    // Kreiraj FOLLOWS_ROUTE vezu
                    String createFollowsQuery = """
                        MATCH (r:Route), (loc:Location)
                        WHERE id(r) = $routeId AND id(loc) = $locId
                        MERGE (r)-[:FOLLOWS_ROUTE]->(loc)
                        """;

                    session.run(createFollowsQuery,
                            Values.parameters("routeId", routeId, "locId", locId)
                    );

                    // Dodaj lokaciju u path
                    Location pathLocation = new Location();
                    pathLocation.setId(locId);
                    pathLocation.setName(locNode.get("name").asString());
                    pathLocation.setLat(locNode.get("lat").asDouble());
                    pathLocation.setLon(locNode.get("lon").asDouble());
                    pathLocations.add(pathLocation);
                }

                route.setPath(pathLocations);
            }

            return route;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Greška pri kreiranju rute: " + e.getMessage());
        }
    }

    public Route getRouteById(Long id) {
        String query = """
            MATCH (r:Route)
            WHERE id(r) = $id
            OPTIONAL MATCH (r)-[:STARTS_AT]->(start:Location)
            OPTIONAL MATCH (r)-[:ENDS_AT]->(end:Location)
            OPTIONAL MATCH (r)-[:FOLLOWS_ROUTE]->(path:Location)
            RETURN r, 
                   COLLECT(DISTINCT start) as starts,
                   COLLECT(DISTINCT end) as ends,
                   COLLECT(DISTINCT path) as paths
            """;

        try (Session session = driver.session()) {
            Result result = session.run(query, Values.parameters("id", id));

            if (result.hasNext()) {
                var record = result.next();
                var routeNode = record.get("r").asNode();

                Route route = new Route();
                route.setId(routeNode.id());
                route.setDistanceKm(routeNode.get("distanceKm").asDouble());
                route.setDurationHours(routeNode.get("durationHours").asDouble());
                route.setStatus(routeNode.get("status").asString());

                // Start location
                var startList = record.get("starts").asList(v -> v.asNode());
                if (!startList.isEmpty()) {
                    var startNode = startList.get(0);
                    Location startLocation = new Location();
                    startLocation.setId(startNode.id());
                    startLocation.setName(startNode.get("name").asString());
                    startLocation.setLat(startNode.get("lat").asDouble());
                    startLocation.setLon(startNode.get("lon").asDouble());
                    route.setStartLocation(startLocation);
                }

                // End location
                var endList = record.get("ends").asList(v -> v.asNode());
                if (!endList.isEmpty()) {
                    var endNode = endList.get(0);
                    Location endLocation = new Location();
                    endLocation.setId(endNode.id());
                    endLocation.setName(endNode.get("name").asString());
                    endLocation.setLat(endNode.get("lat").asDouble());
                    endLocation.setLon(endNode.get("lon").asDouble());
                    route.setEndLocation(endLocation);
                }

                // Path locations
                var pathList = record.get("paths").asList(v -> v.asNode());
                List<Location> pathLocations = new ArrayList<>();
                for (var pathNode : pathList) {
                    Location pathLocation = new Location();
                    pathLocation.setId(pathNode.id());
                    pathLocation.setName(pathNode.get("name").asString());
                    pathLocation.setLat(pathNode.get("lat").asDouble());
                    pathLocation.setLon(pathNode.get("lon").asDouble());
                    pathLocations.add(pathLocation);
                }
                route.setPath(pathLocations);

                return route;
            }
            return null;
        }
    }

    public List<Route> getAllRoutes() {
        String query = """
            MATCH (r:Route)
            OPTIONAL MATCH (r)-[:STARTS_AT]->(start:Location)
            OPTIONAL MATCH (r)-[:ENDS_AT]->(end:Location)
            RETURN r, 
                   COLLECT(DISTINCT start) as starts,
                   COLLECT(DISTINCT end) as ends
            ORDER BY r.distanceKm
            """;

        try (Session session = driver.session()) {
            Result result = session.run(query);
            return result.list().stream()
                    .map(record -> {
                        var routeNode = record.get("r").asNode();
                        Route route = new Route();
                        route.setId(routeNode.id());
                        route.setDistanceKm(routeNode.get("distanceKm").asDouble());
                        route.setDurationHours(routeNode.get("durationHours").asDouble());
                        route.setStatus(routeNode.get("status").asString());

                        // Start location
                        var startList = record.get("starts").asList(v -> v.asNode());
                        if (!startList.isEmpty()) {
                            var startNode = startList.get(0);
                            Location startLocation = new Location();
                            startLocation.setId(startNode.id());
                            startLocation.setName(startNode.get("name").asString());
                            startLocation.setLat(startNode.get("lat").asDouble());
                            startLocation.setLon(startNode.get("lon").asDouble());
                            route.setStartLocation(startLocation);
                        }

                        // End location
                        var endList = record.get("ends").asList(v -> v.asNode());
                        if (!endList.isEmpty()) {
                            var endNode = endList.get(0);
                            Location endLocation = new Location();
                            endLocation.setId(endNode.id());
                            endLocation.setName(endNode.get("name").asString());
                            endLocation.setLat(endNode.get("lat").asDouble());
                            endLocation.setLon(endNode.get("lon").asDouble());
                            route.setEndLocation(endLocation);
                        }

                        return route;
                    })
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    public List<Route> getRoutesByStatus(String status) {
        String query = """
            MATCH (r:Route {status: $status})
            OPTIONAL MATCH (r)-[:STARTS_AT]->(start:Location)
            OPTIONAL MATCH (r)-[:ENDS_AT]->(end:Location)
            RETURN r, 
                   COLLECT(DISTINCT start) as starts,
                   COLLECT(DISTINCT end) as ends
            """;

        try (Session session = driver.session()) {
            Result result = session.run(query, Values.parameters("status", status));
            return result.list().stream()
                    .map(record -> {
                        var routeNode = record.get("r").asNode();
                        Route route = new Route();
                        route.setId(routeNode.id());
                        route.setDistanceKm(routeNode.get("distanceKm").asDouble());
                        route.setDurationHours(routeNode.get("durationHours").asDouble());
                        route.setStatus(routeNode.get("status").asString());

                        var startList = record.get("starts").asList(v -> v.asNode());
                        if (!startList.isEmpty()) {
                            var startNode = startList.get(0);
                            Location startLocation = new Location();
                            startLocation.setId(startNode.id());
                            startLocation.setName(startNode.get("name").asString());
                            route.setStartLocation(startLocation);
                        }

                        var endList = record.get("ends").asList(v -> v.asNode());
                        if (!endList.isEmpty()) {
                            var endNode = endList.get(0);
                            Location endLocation = new Location();
                            endLocation.setId(endNode.id());
                            endLocation.setName(endNode.get("name").asString());
                            route.setEndLocation(endLocation);
                        }

                        return route;
                    })
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    public List<Route> getRoutesByStartLocation(String startLocationName) {
        String query = """
            MATCH (r:Route)-[:STARTS_AT]->(start:Location {name: $name})
            RETURN r, start
            """;

        try (Session session = driver.session()) {
            Result result = session.run(query, Values.parameters("name", startLocationName));
            return result.list().stream()
                    .map(record -> {
                        var routeNode = record.get("r").asNode();
                        var startNode = record.get("start").asNode();

                        Route route = new Route();
                        route.setId(routeNode.id());
                        route.setDistanceKm(routeNode.get("distanceKm").asDouble());
                        route.setDurationHours(routeNode.get("durationHours").asDouble());
                        route.setStatus(routeNode.get("status").asString());

                        Location startLocation = new Location();
                        startLocation.setId(startNode.id());
                        startLocation.setName(startNode.get("name").asString());
                        route.setStartLocation(startLocation);

                        return route;
                    })
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    public Route updateRoute(Long id, Route route) {
        String query = """
            MATCH (r:Route)
            WHERE id(r) = $id
            SET r.distanceKm = $distanceKm, 
                r.durationHours = $durationHours, 
                r.status = $status
            WITH r
            
            // Ukloni postojeće veze ka lokacijama
            OPTIONAL MATCH (r)-[s:STARTS_AT]->()
            DELETE s
            WITH r
            
            OPTIONAL MATCH (r)-[e:ENDS_AT]->()
            DELETE e
            
            RETURN r
            """;

        try (Session session = driver.session()) {
            // Prvo ažuriraj osnovne podatke rute
            session.run(query,
                    Values.parameters(
                            "id", id,
                            "distanceKm", route.getDistanceKm(),
                            "durationHours", route.getDurationHours(),
                            "status", route.getStatus()
                    )
            );

            // Ako su dostupni start i end location, ažuriraj veze
            if (route.getStartLocation() != null && route.getEndLocation() != null) {
                String updateLocationsQuery = """
                    MATCH (r:Route), (start:Location), (end:Location)
                    WHERE id(r) = $routeId 
                      AND id(start) = $startId 
                      AND id(end) = $endId
                    MERGE (r)-[:STARTS_AT]->(start)
                    MERGE (r)-[:ENDS_AT]->(end)
                    """;

                session.run(updateLocationsQuery,
                        Values.parameters(
                                "routeId", id,
                                "startId", route.getStartLocation().getId(),
                                "endId", route.getEndLocation().getId()
                        )
                );
            }

            return getRouteById(id);
        }
    }

    public void deleteRoute(Long id) {
        String query = """
            MATCH (r:Route)
            WHERE id(r) = $id
            DETACH DELETE r
            """;

        try (Session session = driver.session()) {
            session.run(query, Values.parameters("id", id));
        }
    }

    public List<Route> getRoutesByDistanceLessThan(Double maxDistance) {
        String query = """
            MATCH (r:Route)
            WHERE r.distanceKm < $maxDistance
            OPTIONAL MATCH (r)-[:STARTS_AT]->(start:Location)
            OPTIONAL MATCH (r)-[:ENDS_AT]->(end:Location)
            RETURN r, 
                   COLLECT(DISTINCT start) as starts,
                   COLLECT(DISTINCT end) as ends
            ORDER BY r.distanceKm
            """;

        try (Session session = driver.session()) {
            Result result = session.run(query, Values.parameters("maxDistance", maxDistance));
            return result.list().stream()
                    .map(record -> {
                        var routeNode = record.get("r").asNode();
                        Route route = new Route();
                        route.setId(routeNode.id());
                        route.setDistanceKm(routeNode.get("distanceKm").asDouble());
                        route.setDurationHours(routeNode.get("durationHours").asDouble());
                        route.setStatus(routeNode.get("status").asString());

                        var startList = record.get("starts").asList(v -> v.asNode());
                        if (!startList.isEmpty()) {
                            var startNode = startList.get(0);
                            Location startLocation = new Location();
                            startLocation.setId(startNode.id());
                            startLocation.setName(startNode.get("name").asString());
                            route.setStartLocation(startLocation);
                        }

                        var endList = record.get("ends").asList(v -> v.asNode());
                        if (!endList.isEmpty()) {
                            var endNode = endList.get(0);
                            Location endLocation = new Location();
                            endLocation.setId(endNode.id());
                            endLocation.setName(endNode.get("name").asString());
                            route.setEndLocation(endLocation);
                        }

                        return route;
                    })
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    public List<Route> getShortestRoutesBetween(String startLocationName, String endLocationName) {
        String query = """
            MATCH (r:Route)-[:STARTS_AT]->(start:Location),
                  (r)-[:ENDS_AT]->(end:Location)
            WHERE start.name = $startName AND end.name = $endName
            RETURN r, start, end
            ORDER BY r.distanceKm
            LIMIT 5
            """;

        try (Session session = driver.session()) {
            Result result = session.run(query,
                    Values.parameters("startName", startLocationName, "endName", endLocationName));

            return result.list().stream()
                    .map(record -> {
                        var routeNode = record.get("r").asNode();
                        var startNode = record.get("start").asNode();
                        var endNode = record.get("end").asNode();

                        Route route = new Route();
                        route.setId(routeNode.id());
                        route.setDistanceKm(routeNode.get("distanceKm").asDouble());
                        route.setDurationHours(routeNode.get("durationHours").asDouble());
                        route.setStatus(routeNode.get("status").asString());

                        Location startLocation = new Location();
                        startLocation.setId(startNode.id());
                        startLocation.setName(startNode.get("name").asString());
                        route.setStartLocation(startLocation);

                        Location endLocation = new Location();
                        endLocation.setId(endNode.id());
                        endLocation.setName(endNode.get("name").asString());
                        route.setEndLocation(endLocation);

                        return route;
                    })
                    .collect(java.util.stream.Collectors.toList());
        }
    }
}