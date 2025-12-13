package com.example.demo.repository;

import com.example.demo.dto.IsporukaDTO;
import com.example.demo.dto.IsporukaReportDTO;
import com.example.demo.model.Isporuka;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    @Query("""
        MATCH (i:Isporuka)
        RETURN id(i) AS id,
               i.kolicinaKg AS kolicinaKg,
               i.status AS status,
               COALESCE(i.datumKreiranja, null) AS datumKreiranja,
               COALESCE(i.datumPolaska, null) AS datumPolaska,
               COALESCE(i.datumDolaska, null) AS datumDolaska
        ORDER BY i.datumKreiranja DESC
    """)
    List<IsporukaDTO> findAllDTO();

    // UPIT 2: DTO sa ID-evima relacija
    @Query("""
        MATCH (i:Isporuka)
        OPTIONAL MATCH (i)-[:USES_VEHICLE]->(v:Vozilo)
        OPTIONAL MATCH (i)-[:DRIVEN_BY]->(voz:Vozac)
        OPTIONAL MATCH (i)-[:ON_ROUTE]->(r:Route)
        RETURN i.id AS id,
               i.kolicinaKg AS kolicinaKg,
               i.status AS status,
               i.datumKreiranja AS datumKreiranja,
               i.datumPolaska AS datumPolaska,
               i.datumDolaska AS datumDolaska,
               v.id AS voziloId,
               v.registracija AS voziloRegistracija,
               voz.id AS vozacId,
               voz.ime AS vozacIme,
               voz.prezime AS vozacPrezime,
               r.id AS routeId,
               r.name AS routeNaziv
        ORDER BY i.datumKreiranja DESC
    """)
    List<IsporukaDTO> findAllDTOWithIds();

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

    @Query("""
        MATCH (i:Isporuka)
        WHERE i.datumPolaska >= date($startDate)
        OPTIONAL MATCH (i)-[:ON_ROUTE]->(r:Route)
        RETURN i.id AS id,
               i.brojIsporuke AS brojIsporuke,
               i.status AS status,
               i.kolicinaKg AS kolicinaKg,
               i.datumPolaska AS datumPolaska,
               i.datumDolaska AS datumDolaska,
               r.id AS routeId,
               r.name AS routeName
        ORDER BY i.datumPolaska DESC
    """)
    List<IsporukaReportDTO> findIsporukeReportDTO(@Param("startDate") LocalDate startDate);

    @Query("""
        MATCH (i:Isporuka)
        WHERE i.datumPolaska >= date($startDate) AND i.datumPolaska IS NOT NULL
        RETURN i.status AS status,
               i.kolicinaKg AS kolicinaKg
    """)
    List<IsporukaReportDTO> findIsporukeSimpleDTO(@Param("startDate") LocalDate startDate);
    //i.datumPolaska AS datumPolaska,
    //i.datumDolaska AS datumDolaska
    //ORDER BY i.datumPolaska DESC
    @Query("""
    MATCH (i:Isporuka)
    WHERE i.datumPolaska >= $start
    RETURN i ORDER BY i.datumPolaska
""")
    List<Isporuka> findIsporukeFromDate(@Param("start") LocalDate startDate);
    /*@Query("""
    MATCH (i:Isporuka)
    WHERE i.datumPolaska >= datetime($start)
    RETURN i ORDER BY i.datumPolaska
""")
    List<Isporuka> findIsporukeFromDate(@Param("start") LocalDate startDate);*/
}