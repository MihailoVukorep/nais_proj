package com.example.demo.repository;

import com.example.demo.model.Isporuka;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IsporukaRepository extends Neo4jRepository<Isporuka, Long> {

    @Query("""
        CREATE (i:Isporuka {kolicinaKg: $kolicinaKg, status: $status, 
                datumKreiranja: $datumKreiranja}) 
        RETURN i
    """)
    Isporuka createIsporuka(@Param("kolicinaKg") Double kolicinaKg,
                            @Param("status") String status,
                            @Param("datumKreiranja") LocalDateTime datumKreiranja);

    @Query("MATCH (i:Isporuka) WHERE id(i) = $id RETURN i")
    Isporuka findIsporukaById(@Param("id") Long id);

    @Query("MATCH (i:Isporuka {status: $status}) RETURN i")
    List<Isporuka> findByStatus(@Param("status") String status);

    @Query("MATCH (i:Isporuka) WHERE i.kolicinaKg > $minKolicina RETURN i")
    List<Isporuka> findByKolicinaKgGreaterThan(@Param("minKolicina") Double minKolicina);

    @Query("""
        MATCH (i:Isporuka) 
        WHERE id(i) = $id
        SET i.kolicinaKg = $kolicinaKg, i.status = $status, 
            i.datumPolaska = $datumPolaska, i.datumDolaska = $datumDolaska
        RETURN i
    """)
    Isporuka updateIsporuka(@Param("id") Long id,
                            @Param("kolicinaKg") Double kolicinaKg,
                            @Param("status") String status,
                            @Param("datumPolaska") LocalDateTime datumPolaska,
                            @Param("datumDolaska") LocalDateTime datumDolaska);

    @Query("""
        MATCH (i:Isporuka) 
        WHERE id(i) = $id
        SET i.status = $status
        RETURN i
    """)
    Isporuka updateStatus(@Param("id") Long id,
                          @Param("status") String status);

    @Query("""
        MATCH (i:Isporuka) 
        WHERE id(i) = $id
        DETACH DELETE i
    """)
    void deleteIsporuka(@Param("id") Long id);

    @Query("""
        MATCH (i:Isporuka) 
        WHERE i.status IN ['aktivna']
        RETURN i
        ORDER BY i.datumKreiranja DESC
    """)
    List<Isporuka> findAktivneIsporuke();

    @Query("""
        MATCH (i:Isporuka)-[:DRIVEN_BY]->(v:Vozac) 
        WHERE id(v) = $vozacId 
        RETURN i
    """)
    List<Isporuka> findByVozacId(@Param("vozacId") Long vozacId);

    @Query("""
        MATCH (i:Isporuka)-[:USES_VEHICLE]->(v:Vozilo) 
        WHERE id(v) = $voziloId 
        RETURN i
    """)
    List<Isporuka> findByVoziloId(@Param("voziloId") Long voziloId);

    @Query("""
        MATCH (i:Isporuka)-[:ON_ROUTE]->(r:Route) 
        WHERE id(r) = $routeId 
        RETURN i
    """)
    List<Isporuka> findByRouteId(@Param("routeId") Long routeId);
}