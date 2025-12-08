package com.example.demo.repository;

import com.example.demo.model.Route;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RouteRepository extends Neo4jRepository<Route, Long> {

    @Query("""
        CREATE (r:Route {startLocation: $startLocation, endLocation: $endLocation, 
                distanceKm: $distanceKm, durationHours: $durationHours, status: $status}) 
        RETURN r
    """)
    Route createRoute(@Param("startLocation") String startLocation,
                      @Param("endLocation") String endLocation,
                      @Param("distanceKm") Double distanceKm,
                      @Param("durationHours") Double durationHours,
                      @Param("status") String status);

    @Query("MATCH (r:Route) WHERE id(r) = $id RETURN r")
    Route findRouteById(@Param("id") Long id);

    @Query("MATCH (r:Route {status: $status}) RETURN r")
    List<Route> findByStatus(@Param("status") String status);

    @Query("MATCH (r:Route {startLocation: $startLocation}) RETURN r")
    List<Route> findByStartLocation(@Param("startLocation") String startLocation);

    @Query("""
        MATCH (r:Route) 
        WHERE id(r) = $id
        SET r.startLocation = $startLocation, r.endLocation = $endLocation, 
            r.distanceKm = $distanceKm, r.durationHours = $durationHours, r.status = $status
        RETURN r
    """)
    Route updateRoute(@Param("id") Long id,
                      @Param("startLocation") String startLocation,
                      @Param("endLocation") String endLocation,
                      @Param("distanceKm") Double distanceKm,
                      @Param("durationHours") Double durationHours,
                      @Param("status") String status);

    @Query("""
        MATCH (r:Route) 
        WHERE id(r) = $id
        DETACH DELETE r
    """)
    void deleteRoute(@Param("id") Long id);

    // COMPLEX - Rute sa dužinom manjom od određene
    @Query("""
        MATCH (r:Route) 
        WHERE r.distanceKm < $maxDistance
        RETURN r
        ORDER BY r.distanceKm
    """)
    List<Route> findByDistanceKmLessThan(@Param("maxDistance") Double maxDistance);

    // COMPLEX - Najkraće rute između dve lokacije
    @Query("""
        MATCH (r:Route) 
        WHERE r.startLocation = $startLocation AND r.endLocation = $endLocation
        RETURN r
        ORDER BY r.distanceKm
        LIMIT 5
    """)
    List<Route> findShortestRoutesBetween(@Param("startLocation") String startLocation,
                                          @Param("endLocation") String endLocation);
}