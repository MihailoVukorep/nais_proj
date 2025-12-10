package com.example.demo.repository;

import com.example.demo.model.Isporuka;
import com.example.demo.model.Location;
import com.example.demo.model.Road;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Map;

public interface RoadCustomRepository extends Neo4jRepository<Location, Long> {

    @Query("""
        MATCH (from:Location), (to:Location)
        WHERE id(from) = $fromId AND id(to) = $toId
        CREATE (from)-[r:ROAD {distanceKm: $distanceKm, durationHours: $durationHours, 
                blocked: false, reason: null}]->(to)
        RETURN r
    """)
    Road createRoad(@Param("fromId") Long fromId,
                    @Param("toId") Long toId,
                    @Param("distanceKm") Double distanceKm,
                    @Param("durationHours") Double durationHours);

    @Query("""
        MATCH (from:Location)-[r:ROAD]->(to:Location)
        WHERE id(from) = $fromId AND id(to) = $toId
        RETURN r
    """)
    List<Road> findRoadsBetween(@Param("fromId") Long fromId,
                                @Param("toId") Long toId);

    @Query("""
        MATCH (l:Location)-[r:ROAD]->(dest:Location)
        WHERE id(l) = $locationId
        RETURN r
    """)
    List<Road> findRoadsFromLocation(@Param("locationId") Long locationId);

    @Query("""
        MATCH (src:Location)-[r:ROAD]->(l:Location)
        WHERE id(l) = $locationId
        RETURN r
    """)
    List<Road> findRoadsToLocation(@Param("locationId") Long locationId);

    @Query("""
        MATCH ()-[r:ROAD]-()
        WHERE id(r) = $roadId
        SET r.distanceKm = COALESCE($distanceKm, r.distanceKm),
            r.durationHours = COALESCE($durationHours, r.durationHours),
            r.blocked = COALESCE($blocked, r.blocked),
            r.reason = COALESCE($reason, r.reason)
        RETURN r
    """)
    Road updateRoadAttributes(@Param("roadId") Long roadId,
                              @Param("distanceKm") Double distanceKm,
                              @Param("durationHours") Double durationHours,
                              @Param("blocked") Boolean blocked,
                              @Param("reason") String reason);

    @Query("""
    MATCH ()-[r:ROAD]-()
    WHERE id(r) = $roadId
    SET r.blocked = $blocked,
        r.reason = $reason
    RETURN {
        id: id(r),
        blocked: r.blocked,
        reason: r.reason
    } AS road
""")
    List<Map<String, Object>> blockRoadRaw(@Param("roadId") Long roadId,
                                           @Param("blocked") Boolean blocked,
                                           @Param("reason") String reason);
    @Query("""
        MATCH ()-[r:ROAD]-()
        WHERE id(r) = $roadId
        DELETE r
        RETURN COUNT(r) > 0
    """)
    boolean deleteRoad(@Param("roadId") Long roadId);

    @Query("""
        MATCH ()-[r:ROAD]-()
        WHERE r.blocked = true
        RETURN r
        ORDER BY r.reason
    """)
    List<Road> findBlockedRoads();

    @Query("""
        MATCH path = shortestPath((from:Location)-[:ROAD*]-(to:Location))
        WHERE id(from) = $fromId AND id(to) = $toId
        UNWIND relationships(path) as r
        RETURN DISTINCT r
    """)
    List<Road> findShortestPath(@Param("fromId") Long fromId,
                                @Param("toId") Long toId);
}