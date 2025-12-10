package com.example.demo.repository;

import com.example.demo.model.Vozilo;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VoziloRepository extends Neo4jRepository<Vozilo, Long> {

    @Query("""
        CREATE (v:Vozilo {marka: $marka, model: $model, registracija: $registracija, 
                kapacitetKg: $kapacitetKg, status: $status}) 
        RETURN v
    """)
    Vozilo createVozilo(@Param("marka") String marka,
                        @Param("model") String model,
                        @Param("registracija") String registracija,
                        @Param("kapacitetKg") Double kapacitetKg,
                        @Param("status") String status);

    @Query("MATCH (v:Vozilo) WHERE id(v) = $id RETURN v")
    Vozilo findVoziloById(@Param("id") Long id);

    @Query("MATCH (v:Vozilo {registracija: $registracija}) RETURN v")
    Vozilo findByRegistracija(@Param("registracija") String registracija);

    @Query("MATCH (v:Vozilo {marka: $marka}) RETURN v")
    List<Vozilo> findByMarka(@Param("marka") String marka);

    @Query("MATCH (v:Vozilo {marka: $marka, model: $model}) RETURN v")
    List<Vozilo> findByMarkaModel(@Param("marka") String marka,
                                  @Param("model") String model);

    @Query("MATCH (v:Vozilo {status: $status}) RETURN v")
    List<Vozilo> findByStatus(@Param("status") String status);

    @Query("""
        MATCH (v:Vozilo) 
        WHERE id(v) = $id
        SET v.marka = $marka, v.model = $model, v.registracija = $registracija, 
            v.kapacitetKg = $kapacitetKg, v.status = $status
        RETURN v
    """)
    Vozilo updateVozilo(@Param("id") Long id,
                        @Param("marka") String marka,
                        @Param("model") String model,
                        @Param("registracija") String registracija,
                        @Param("kapacitetKg") Double kapacitetKg,
                        @Param("status") String status);

    @Query("""
        MATCH (v:Vozilo) 
        WHERE id(v) = $id
        SET v.status = $status
        RETURN v
    """)
    Vozilo updateStatus(@Param("id") Long id,
                        @Param("status") String status);

    @Query("""
        MATCH (v:Vozilo) 
        WHERE id(v) = $id
        DETACH DELETE v
    """)
    void deleteVozilo(@Param("id") Long id);

    // COMPLEX - Vozila sa kapacitetom većim od određenog
    @Query("""
        MATCH (v:Vozilo) 
        WHERE v.kapacitetKg >= $minKapacitet
        RETURN v
        ORDER BY v.kapacitetKg DESC
    """)
    List<Vozilo> findByKapacitetKgGreaterThanEqual(@Param("minKapacitet") Double minKapacitet);

    // COMPLEX - Slobodna vozila sa dovoljnim kapacitetom
    @Query("""
        MATCH (v:Vozilo) 
        WHERE v.status = 'slobodno' AND v.kapacitetKg >= $minKapacitet
        RETURN v
        ORDER BY v.kapacitetKg
    """)
    List<Vozilo> findByStatusAndKapacitet(@Param("status") String status,
                                          @Param("minKapacitet") Double minKapacitet);
}