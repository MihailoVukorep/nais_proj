package com.example.demo.repository;

import com.example.demo.model.Isporuka;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IsporukaRepository extends Neo4jRepository<Isporuka, Long> {
    List<Isporuka> findByStatus(String status);
}
