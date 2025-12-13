package com.example.demo.service;

import com.example.demo.model.Location;
import com.example.demo.model.Road;
import com.example.demo.repository.LocationRepository;
import com.example.demo.repository.RoadCustomRepository;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoadService {

    @Autowired
    private RoadCustomRepository roadCustomRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private Driver driver;

    public Road createRoad(Long fromLocationId, Long toLocationId,
                           Double distanceKm, Double durationHours) {
        /*String query = """
            MATCH (from:Location), (to:Location)
            WHERE id(from) = $fromId AND id(to) = $toId
            MERGE (from)-[r:ROAD {
                distanceKm: $distanceKm,
                durationHours: $durationHours,
                blocked: false,
                reason: null
            }]->(to)
            RETURN r, id(r) as roadId
            """;*/
        String query = """
            MATCH (from:Location), (to:Location)
            WHERE id(from) = $fromId AND id(to) = $toId
            MERGE (from)-[r:ROAD {
                distanceKm: $distanceKm,
                durationHours: $durationHours,
                blocked: false
            }]->(to)
            RETURN r, id(r) as roadId
            """;

        try (Session session = driver.session()) {
            Result result = session.run(query,
                    Values.parameters(
                            "fromId", fromLocationId,
                            "toId", toLocationId,
                            "distanceKm", distanceKm,
                            "durationHours", durationHours
                    )
            );

            if (result.hasNext()) {
                var record = result.next();
                var roadNode = record.get("r").asRelationship();
                Long roadId = record.get("roadId").asLong();

                Road road = new Road();
                road.setId(roadId);
                road.setDistanceKm(roadNode.get("distanceKm").asDouble());
                road.setDurationHours(roadNode.get("durationHours").asDouble());
                road.setBlocked(false);
                road.setReason("Nije blokiran");
                //road.setReason(roadNode.get("reason").asString(null));

                return road;
            }
            return null;
        }
    }

    public List<Road> getRoadsBetween(Long fromLocationId, Long toLocationId) {
        String query = """
            MATCH (from:Location)-[r:ROAD]->(to:Location)
            WHERE id(from) = $fromId AND id(to) = $toId
            RETURN r, id(r) as id
            """;

        try (Session session = driver.session()) {
            Result result = session.run(query,
                    Values.parameters("fromId", fromLocationId, "toId", toLocationId)
            );

            return result.list().stream()
                    .map(record -> {
                        var roadNode = record.get("r").asRelationship();
                        Long roadId = record.get("roadId").asLong();

                        Road road = new Road();
                        road.setId(roadId);
                        road.setDistanceKm(roadNode.get("distanceKm").asDouble());
                        road.setDurationHours(roadNode.get("durationHours").asDouble());
                        road.setBlocked(roadNode.get("blocked").asBoolean());
                        road.setReason(roadNode.get("reason").asString(null));

                        return road;
                    })
                    .collect(Collectors.toList());
        }
    }

    public List<Road> getRoadsFromLocation(Long locationId) {
        String query = """
            MATCH (l:Location)-[r:ROAD]->(dest:Location)
            WHERE id(l) = $locationId
            RETURN r, id(r) as roadId
            """;

        try (Session session = driver.session()) {
            Result result = session.run(query,
                    Values.parameters("locationId", locationId)
            );

            return result.list().stream()
                    .map(record -> {
                        var roadNode = record.get("r").asRelationship();
                        Long roadId = record.get("roadId").asLong();

                        Road road = new Road();
                        road.setId(roadId);
                        road.setDistanceKm(roadNode.get("distanceKm").asDouble());
                        road.setDurationHours(roadNode.get("durationHours").asDouble());
                        road.setBlocked(roadNode.get("blocked").asBoolean());
                        road.setReason(roadNode.get("reason").asString(null));

                        return road;
                    })
                    .collect(Collectors.toList());
        }
    }

    public Road updateRoadAttributes(Long roadId, Double distanceKm, Double durationHours,
                                     Boolean blocked, String reason) {
        String query = """
            MATCH ()-[r:ROAD]-()
            WHERE id(r) = $roadId
            SET r.distanceKm = COALESCE($distanceKm, r.distanceKm),
                r.durationHours = COALESCE($durationHours, r.durationHours),
                r.blocked = COALESCE($blocked, r.blocked),
                r.reason = COALESCE($reason, r.reason)
            RETURN r
            """;

        try (Session session = driver.session()) {
            Result result = session.run(query,
                    Values.parameters(
                            "roadId", roadId,
                            "distanceKm", distanceKm,
                            "durationHours", durationHours,
                            "blocked", blocked,
                            "reason", reason
                    )
            );

            if (result.hasNext()) {
                var roadNode = result.next().get("r").asRelationship();

                Road road = new Road();
                road.setId(roadId);
                road.setDistanceKm(roadNode.get("distanceKm").asDouble());
                road.setDurationHours(roadNode.get("durationHours").asDouble());
                road.setBlocked(roadNode.get("blocked").asBoolean());
                road.setReason(roadNode.get("reason").asString(null));

                return road;
            }
            return null;
        }
    }

    public boolean deleteRoad(Long roadId) {
        String query = """
            MATCH ()-[r:ROAD]-()
            WHERE id(r) = $roadId
            DELETE r
            RETURN COUNT(r) > 0 as deleted
            """;

        try (Session session = driver.session()) {
            Result result = session.run(query,
                    Values.parameters("roadId", roadId)
            );

            return result.single().get("deleted").asBoolean();
        }
    }

    /*public List<Road> getRoadsBetween(Long fromLocationId, Long toLocationId) {
        return roadCustomRepository.findRoadsBetween(fromLocationId, toLocationId);
    }

    public List<Road> getRoadsFromLocation(Long locationId) {
        return roadCustomRepository.findRoadsFromLocation(locationId);
    }*/

    public List<Road> getRoadsToLocation(Long locationId) {
        String query = """
            MATCH (l:Location)-[r:ROAD]->(dest:Location)
            WHERE id(dest) = $locationId
            RETURN r, id(r) as roadId
            """;

        try (Session session = driver.session()) {
            Result result = session.run(query,
                    Values.parameters("locationId", locationId)
            );

            return result.list().stream()
                    .map(record -> {
                        var roadNode = record.get("r").asRelationship();
                        Long roadId = record.get("roadId").asLong();

                        Road road = new Road();
                        road.setId(roadId);
                        road.setDistanceKm(roadNode.get("distanceKm").asDouble());
                        road.setDurationHours(roadNode.get("durationHours").asDouble());
                        road.setBlocked(roadNode.get("blocked").asBoolean());
                        road.setReason(roadNode.get("reason").asString(null));

                        return road;
                    })
                    .collect(Collectors.toList());
        }
        //return roadCustomRepository.findRoadsToLocation(locationId);
    }

    /*public Road updateRoadAttributes(Long roadId, Double distanceKm, Double durationHours,
                                     Boolean blocked, String reason) {
        return roadCustomRepository.updateRoadAttributes(roadId, distanceKm,
                durationHours, blocked, reason);
    }*/

    public Map<String, Object> blockRoad(Long roadId, String reason) {
        List<Map<String, Object>> result =
                roadCustomRepository.blockRoadRaw(roadId, true, reason);

        if (result.isEmpty()) {
            throw new RuntimeException("Put nije pronađen.");
        }

        return result.get(0);
    }

    public Map<String, Object>  unblockRoad(Long roadId) {
        List<Map<String, Object>> result =
                roadCustomRepository.blockRoadRaw(roadId, false, null);

        if (result.isEmpty()) {
            throw new RuntimeException("Put nije pronađen.");
        }

        return result.get(0);
    }

    /*public boolean deleteRoad(Long roadId) {
        return roadCustomRepository.deleteRoad(roadId);
    }*/

    public List<Road> findBlockedRoads() {
        return roadCustomRepository.findBlockedRoads();
    }

    public List<Road> findShortestPath(Long fromLocationId, Long toLocationId) {
        return roadCustomRepository.findShortestPath(fromLocationId, toLocationId);
    }
}