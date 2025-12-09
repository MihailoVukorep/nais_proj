package com.example.demo.service;

import com.example.demo.model.Isporuka;
import com.example.demo.model.Location;
import com.example.demo.model.Route;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OnRouteService {

    private final Driver driver;
    private final LocationService locationService;

    public OnRouteService(Driver driver, LocationService locationService) {
        this.driver = driver;
        this.locationService = locationService;
    }

    public void assignRoute(Long isporukaId, Long routeId) {
        String query = """
            MATCH (i:Isporuka), (r:Route) 
            WHERE id(i) = $isporukaId AND id(r) = $routeId
            MERGE (i)-[:ON_ROUTE]->(r)
            """;

        try (Session session = driver.session()) {
            session.run(query,
                    Values.parameters("isporukaId", isporukaId, "routeId", routeId)
            );
        }
    }

    public Route getRouteForIsporuka(Long isporukaId) {
        String query = """
            MATCH (i:Isporuka)-[:ON_ROUTE]->(r:Route) 
            WHERE id(i) = $isporukaId
            RETURN r
            """;

        try (Session session = driver.session()) {
            Result result = session.run(query,
                    Values.parameters("isporukaId", isporukaId)
            );

            if (result.hasNext()) {
                var record = result.next();
                var node = record.get("r").asNode();
                Route route = new Route();
                route.setId(node.id());
                route.setStartLocation((Location) node.get("startLocation").asObject());
                //route.setStartLocation(locationService.node.get("startLocation"));
                route.setEndLocation((Location) node.get("endLocation").asObject());
                //route.setEndLocation(node.get("endLocation").asString());
                route.setDistanceKm(node.get("distanceKm").asDouble());
                route.setDurationHours(node.get("durationHours").asDouble());
                route.setStatus(node.get("status").asString());
                return route;
            }
            return null;
        }
    }

    public List<Isporuka> getIsporukeForRoute(Long routeId) {
        String query = """
            MATCH (i:Isporuka)-[:ON_ROUTE]->(r:Route) 
            WHERE id(r) = $routeId
            RETURN i
            """;

        try (Session session = driver.session()) {
            Result result = session.run(query,
                    Values.parameters("routeId", routeId)
            );

            return result.list().stream()
                    .map(record -> {
                        var node = record.get("i").asNode();
                        Isporuka isporuka = new Isporuka();
                        isporuka.setId(node.id());
                        isporuka.setKolicinaKg(node.get("kolicinaKg").asDouble());
                        isporuka.setStatus(node.get("status").asString());
                        return isporuka;
                    })
                    .collect(Collectors.toList());
        }
    }

    public void changeRoute(Long isporukaId, Long novaRouteId) {
        String query = """
            MATCH (i:Isporuka)-[r:ON_ROUTE]->(:Route) 
            WHERE id(i) = $isporukaId
            DELETE r
            WITH i
            MATCH (rt:Route)
            WHERE id(rt) = $novaRouteId
            MERGE (i)-[:ON_ROUTE]->(rt)
            """;

        try (Session session = driver.session()) {
            session.run(query,
                    Values.parameters("isporukaId", isporukaId, "novaRouteId", novaRouteId)
            );
        }
    }

    public void removeRoute(Long isporukaId, Long routeId) {
        String query = """
            MATCH (i:Isporuka)-[r:ON_ROUTE]->(rt:Route) 
            WHERE id(i) = $isporukaId AND id(rt) = $routeId
            DELETE r
            """;

        try (Session session = driver.session()) {
            session.run(query,
                    Values.parameters("isporukaId", isporukaId, "routeId", routeId)
            );
        }
    }
}