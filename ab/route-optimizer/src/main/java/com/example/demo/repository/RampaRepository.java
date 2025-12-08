package com.example.demo.repository;

import com.example.demo.model.Rampa;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RampaRepository extends Neo4jRepository<Rampa, Long> {

    @Query("CREATE (r:Rampa {oznaka: $oznaka, status: $status}) RETURN r")
    Rampa createRampa(@Param("oznaka") String oznaka,
                      @Param("status") String status);

    @Query("MATCH (r:Rampa) WHERE id(r) = $id RETURN r")
    Rampa findRampaById(@Param("id") Long id);

    @Query("MATCH (r:Rampa {oznaka: $oznaka}) RETURN r")
    Rampa findByOznaka(@Param("oznaka") String oznaka);

    @Query("MATCH (r:Rampa {status: $status}) RETURN r")
    List<Rampa> findByStatus(@Param("status") String status);

    @Query("""
        MATCH (r:Rampa) 
        WHERE id(r) = $id
        SET r.oznaka = $oznaka, r.status = $status
        RETURN r
    """)
    Rampa updateRampa(@Param("id") Long id,
                      @Param("oznaka") String oznaka,
                      @Param("status") String status);

    @Query("""
        MATCH (r:Rampa) 
        WHERE id(r) = $id
        SET r.status = $status
        RETURN r
    """)
    Rampa updateStatus(@Param("id") Long id,
                       @Param("status") String status);

    @Query("""
        MATCH (r:Rampa) 
        WHERE id(r) = $id
        DETACH DELETE r
    """)
    void deleteRampa(@Param("id") Long id);

    @Query("""
        MATCH (r:Rampa) 
        WHERE r.status = 'slobodna'
        RETURN r
        ORDER BY r.oznaka
    """)
    List<Rampa> findSlobodneRampa();

    // COMPLEX - Dodela isporuke rampi
    @Query("""
        MATCH (r:Rampa), (i:Isporuka) 
        WHERE id(r) = $rampaId AND id(i) = $isporukaId
        CREATE (i)-[:ASSIGNED_TO]->(r)
        SET r.status = 'zauzeta'
        RETURN r
    """)
    Rampa assignIsporukaToRampa(@Param("rampaId") Long rampaId,
                                @Param("isporukaId") Long isporukaId);

    @Query("""
        MATCH (r:Rampa) 
        WHERE id(r) = $id
        SET r.status = 'slobodna'
        RETURN r
    """)
    Rampa freeRampa(@Param("id") Long id);
}