package com.example.demo.repository;

import com.example.demo.dto.DriverAnalyticsDTO;
import com.example.demo.model.Vozac;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface VozacRepository extends Neo4jRepository<Vozac, Long> {
    //List<Vozac> findByStatus(String status);
    @Query("CREATE (v:Vozac {email: $email, ime: $ime, prezime: $prezime, brVoznji: $brVoznji, status: $status}) RETURN v")
    Vozac createVozac(@Param("email") String email,
                      @Param("ime") String ime,
                      @Param("prezime") String prezime,
                      @Param("brVoznji") Integer brVoznji,
                      @Param("status") String status);

    @Query("MATCH (v:Vozac) WHERE id(v) = $id RETURN v")
    Vozac findVozacById(@Param("id") Long id);

    @Query("MATCH (v:Vozac {ime: $ime}) RETURN v")
    List<Vozac> findByIme(@Param("ime") String ime);

    @Query("MATCH (v:Vozac {prezime: $prezime}) RETURN v")
    List<Vozac> findByPrezime(@Param("prezime") String prezime);

    @Query("MATCH (v:Vozac {status: $status}) RETURN v")
    List<Vozac> findByStatus(@Param("status") String status);

    @Query("""
        MATCH (v:Vozac) 
        WHERE id(v) = $id
        SET v.email: $email, v.ime = $ime, v.prezime = $prezime, v.brVoznji = $brVoznji, v.status = $status
        RETURN v
    """)
    Vozac updateVozac(@Param("id") Long id,
                      @Param("email") String email,
                      @Param("ime") String ime,
                      @Param("prezime") String prezime,
                      @Param("brVoznji") Integer brVoznji,
                      @Param("status") String status);

    @Query("""
        MATCH (v:Vozac) 
        WHERE id(v) = $id
        SET v.status = $status
        RETURN v
    """)
    Vozac updateStatus(@Param("id") Long id,
                       @Param("status") String status);

    @Query("""
        MATCH (v:Vozac) 
        WHERE id(v) = $id
        DETACH DELETE v
    """)
    void deleteVozac(@Param("id") Long id);

    @Query("MATCH (v:Vozac) WHERE v.korisnikEmail CONTAINS $email RETURN v")
    Vozac findByEmail(String email);

    @Query("MATCH (v:Vozac) WHERE v.ime CONTAINS $ime AND v.prezime CONTAINS $prezime RETURN v")
    List<Vozac> findByImeAndPrezime(@Param("ime") String ime, @Param("prezime") String prezime);

    @Query("MATCH (v:Vozac) WHERE v.brVoznji > $minVoznji RETURN v ORDER BY v.brVoznji DESC")
    List<Vozac> findByBrojVoznjiGreaterThan(@Param("minVoznji") Integer minVoznji);

    @Query("""
      MATCH (v:Vozac)<-[:DRIVEN_BY]-(isp:Isporuka)-[:ON_ROUTE]->(r:Route)
      WHERE (isp.datumKreiranja >= datetime($from) AND isp.datumKreiranja <= datetime($to))
      WITH v, COUNT(isp) AS brojVoznji, avg(r.distanceKm) AS avgDistance
      WHERE brojVoznji > 0
      WITH v, brojVoznji, avgDistance, (toFloat(brojVoznji) * coalesce(avgDistance,0.0)) AS score
      RETURN v.ime AS ime, v.prezime AS prezime, brojVoznji AS brVoznji, avgDistance AS avgRouteDistance, score
      ORDER BY score DESC
      LIMIT $limit
    """)
    List<DriverAnalyticsDTO> recommendDrivers(@Param("from") String fromIso,
                                              @Param("to") String toIso,
                                              @Param("limit") Integer limit);
}
