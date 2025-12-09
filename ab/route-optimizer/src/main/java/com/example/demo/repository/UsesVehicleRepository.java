package com.example.demo.repository;

import com.example.demo.model.Isporuka;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsesVehicleRepository extends Neo4jRepository<Isporuka, Long> {

    @Query("""
        MATCH (i:Isporuka), (v:Vozilo) 
        WHERE id(i) = $isporukaId AND id(v) = $voziloId
        CREATE (i)-[r:USES_VEHICLE]->(v)
        RETURN r
    """)
    Object createUsesVehicle(@Param("isporukaId") Long isporukaId,
                             @Param("voziloId") Long voziloId);

    @Query("""
        MATCH (i:Isporuka)-[r:USES_VEHICLE]->(v:Vozilo) 
        WHERE id(i) = $isporukaId
        RETURN v
    """)
    Object getVehicleByIsporuka(@Param("isporukaId") Long isporukaId);

    @Query("""
        MATCH (i:Isporuka)-[r:USES_VEHICLE]->(v:Vozilo) 
        WHERE id(v) = $voziloId
        RETURN i
    """)
    Object getIsporukeByVozilo(@Param("voziloId") Long voziloId);

    @Query("""
        MATCH (i:Isporuka)-[r:USES_VEHICLE]->(v:Vozilo) 
        WHERE id(i) = $isporukaId AND id(v) = $voziloId
        DELETE r
    """)
    void deleteUsesVehicle(@Param("isporukaId") Long isporukaId,
                           @Param("voziloId") Long voziloId);

    @Query("""
        MATCH (i:Isporuka)-[r:USES_VEHICLE]->(v:Vozilo) 
        WHERE id(i) = $isporukaId
        DELETE r
        WITH i
        MATCH (v2:Vozilo)
        WHERE id(v2) = $noviVoziloId
        CREATE (i)-[r2:USES_VEHICLE]->(v2)
        RETURN r2
    """)
    Object updateUsesVehicle(@Param("isporukaId") Long isporukaId,
                             @Param("noviVoziloId") Long noviVoziloId);
}