package com.example.demo.repository;

import com.example.demo.model.Isporuka;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DrivenByRepository extends Neo4jRepository<Isporuka, Long> {

    @Query("""
        MATCH (i:Isporuka), (v:Vozac) 
        WHERE id(i) = $isporukaId AND id(v) = $vozacId
        CREATE (i)-[r:DRIVEN_BY]->(v)
        RETURN r
    """)
    Object createDrivenBy(@Param("isporukaId") Long isporukaId,
                          @Param("vozacId") Long vozacId);

    @Query("""
        MATCH (i:Isporuka)-[r:DRIVEN_BY]->(v:Vozac) 
        WHERE id(i) = $isporukaId
        RETURN v
    """)
    Object getDriverByIsporuka(@Param("isporukaId") Long isporukaId);

    @Query("""
        MATCH (i:Isporuka)-[r:DRIVEN_BY]->(v:Vozac) 
        WHERE id(v) = $vozacId
        RETURN i
    """)
    Object getIsporukeByVozac(@Param("vozacId") Long vozacId);

    @Query("""
        MATCH (i:Isporuka)-[r:DRIVEN_BY]->(v:Vozac) 
        WHERE id(i) = $isporukaId AND id(v) = $vozacId
        DELETE r
    """)
    void deleteDrivenBy(@Param("isporukaId") Long isporukaId,
                        @Param("vozacId") Long vozacId);

    @Query("""
        MATCH (i:Isporuka)-[r:DRIVEN_BY]->(v:Vozac) 
        WHERE id(i) = $isporukaId
        DELETE r
        WITH i
        MATCH (v2:Vozac)
        WHERE id(v2) = $noviVozacId
        CREATE (i)-[r2:DRIVEN_BY]->(v2)
        RETURN r2
    """)
    Object updateDrivenBy(@Param("isporukaId") Long isporukaId,
                          @Param("noviVozacId") Long noviVozacId);
}