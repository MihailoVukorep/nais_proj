package com.example.demo.repository;

import com.example.demo.model.Route;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RouteRepository extends Neo4jRepository<Route, Long> {

    /*@Query("""
        MATCH (start:Location {name: $start})
        MATCH (end:Location {name: $end})
        CALL gds.shortestPath.stream({
            nodeProjection: 'Location',
            relationshipProjection: 'ROAD',
            startNode: start,
            endNode: end
        })
        YIELD nodeIds, totalCost
        RETURN nodeIds, totalCost
    """)
    Map<String,Object> findShortestPath(String start, String end);*/

    List<Route> findAll();

    Route findById(long id);

    @Query("""
        MATCH (start:Location {name: $start})
        MATCH (end:Location {name: $end})
        CALL gds.shortestPath.stream({
            nodeProjection: 'Location',
            relationshipProjection: {
                ROAD: {
                    type: 'ROAD',
                    properties: 'distance'
                }
            },
            startNode: id(start),
            endNode: id(end),
            relationshipWeightProperty: 'distance'
        })
        YIELD nodeIds, totalCost
        RETURN nodeIds, totalCost
    """)
    Map<String, Object> findShortestPath(String start, String end);


}
