package com.example.demo.repository;

import com.example.demo.model.Road;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface RoadRepository extends Neo4jRepository<Road, Long> {

    @Query("""
    MATCH (a:Location)-[r:ROAD]->(b:Location)
    WHERE r.distance < $max
    RETURN a,b,r
""")
    List<Map<String,Object>> findAllShortRoads(Double max);
}
