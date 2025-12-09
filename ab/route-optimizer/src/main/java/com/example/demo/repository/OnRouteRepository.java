package com.example.demo.repository;

import com.example.demo.model.Isporuka;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OnRouteRepository extends Neo4jRepository<Isporuka, Long> {

    @Query("""
        MATCH (i:Isporuka), (r:Route) 
        WHERE id(i) = $isporukaId AND id(r) = $routeId
        CREATE (i)-[rel:ON_ROUTE]->(r)
        RETURN rel
    """)
    Object createOnRoute(@Param("isporukaId") Long isporukaId,
                         @Param("routeId") Long routeId);

    @Query("""
        MATCH (i:Isporuka)-[r:ON_ROUTE]->(rt:Route) 
        WHERE id(i) = $isporukaId
        RETURN rt
    """)
    Object getRouteByIsporuka(@Param("isporukaId") Long isporukaId);

    @Query("""
        MATCH (i:Isporuka)-[r:ON_ROUTE]->(rt:Route) 
        WHERE id(rt) = $routeId
        RETURN i
    """)
    Object getIsporukeByRoute(@Param("routeId") Long routeId);

    @Query("""
        MATCH (i:Isporuka)-[r:ON_ROUTE]->(rt:Route) 
        WHERE id(i) = $isporukaId AND id(rt) = $routeId
        DELETE r
    """)
    void deleteOnRoute(@Param("isporukaId") Long isporukaId,
                       @Param("routeId") Long routeId);

    @Query("""
        MATCH (i:Isporuka)-[r:ON_ROUTE]->(rt:Route) 
        WHERE id(i) = $isporukaId
        DELETE r
        WITH i
        MATCH (rt2:Route)
        WHERE id(rt2) = $novaRouteId
        CREATE (i)-[r2:ON_ROUTE]->(rt2)
        RETURN r2
    """)
    Object updateOnRoute(@Param("isporukaId") Long isporukaId,
                         @Param("novaRouteId") Long novaRouteId);
}