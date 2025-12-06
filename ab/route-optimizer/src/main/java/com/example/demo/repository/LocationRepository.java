package com.example.demo.repository;

import com.example.demo.model.Location;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.Optional;

@Repository
public interface LocationRepository extends Neo4jRepository<Location, Long> {

    List<Location>  findAll();

    @Query("MATCH (l:Location {name: $name}) RETURN l")
    Location findByName(String name);

    @Query("""
        CREATE (l:Location {name: $name, lat: $lat, lon: $lon})
        RETURN l
    """)
    Location createLocation(String name, Double lat, Double lon);

    @Query("""
        MATCH (l:Location {id: $id})
        SET l.lat = $lat, l.lon = $lon
        RETURN l
    """)
    Location updateLocation(Long id, Double lat, Double lon);

    @Query("""
        MATCH (l:Location {id: $id})
        DELETE l
    """)
    void deleteLocation(Long id);

    @Query("""
    MATCH (l:Location {name:$name})-[:ROAD]->(x:Location)
    RETURN x
""")
    List<Location> neighborsOf(String name);
}