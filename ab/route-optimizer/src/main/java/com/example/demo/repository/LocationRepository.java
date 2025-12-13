package com.example.demo.repository;

import com.example.demo.model.Location;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.Optional;

@Repository
public interface LocationRepository extends Neo4jRepository<Location, Long> {

    /*@Query("MATCH (l:Location) WHERE id(l) = $id RETURN l")
    Location getById(Long id);*/

    Location save(Location l);

    @Query("MATCH (l:Location {name: $name}) RETURN l")
    Location findByName(String name);

    @Query("CREATE (l:Location {name: $name, lat: $lat, lon: $lon}) RETURN l")
    Location createLocation(@Param("name") String name,
                            @Param("lat") Double lat,
                            @Param("lon") Double lon);

    @Query("""
        MATCH (l:Location) 
        WHERE id(l) = $id
        SET l.name = $name, l.lat = $lat, l.lon = $lon
        RETURN l
    """)
    Location updateLocation(@Param("id") Long id,
                            @Param("name") String name,
                            @Param("lat") Double lat,
                            @Param("lon") Double lon);


    @Query("""
        MATCH (l:Location) 
        WHERE id(l) = $id
        DELETE l
    """)
    void deleteLocation(@Param("id") Long id);

    @Query("""
        MATCH (l:Location {name:$name})-[:ROAD]->(x:Location)
        RETURN x
    """)
    List<Location> neighborsOf(@Param("name") String name);
}