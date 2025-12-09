package com.example.demo.service;

import com.example.demo.model.Isporuka;
import com.example.demo.model.Vozilo;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsesVehicleService {

    private final Driver driver;
    @Autowired
    private IsporukaService isporukaService;

    @Autowired
    private VoziloService voziloService;

    public UsesVehicleService(Driver driver) {
        this.driver = driver;
    }

    @Transactional
    public void zauzmiVozilo(Long isporukaId, Long voziloId) {
        String query = """
            MATCH (i:Isporuka), (v:Vozilo) 
            WHERE id(i) = $isporukaId AND id(v) = $voziloId
            MERGE (i)-[:USES_VEHICLE]->(v)
            """;

        try (Session session = driver.session()) {
            session.run(query,
                    Values.parameters("isporukaId", isporukaId, "voziloId", voziloId)
            );
            Isporuka i = isporukaService.findById(isporukaId);
            Vozilo vozilo = voziloService.findById(voziloId);
            i.setVozilo(vozilo);
            isporukaService.save(i);

        }
    }

    public Vozilo getVoziloForIsporuka(Long isporukaId) {
        String query = """
            MATCH (i:Isporuka)-[:USES_VEHICLE]->(v:Vozilo) 
            WHERE id(i) = $isporukaId
            RETURN v
            """;

        try (Session session = driver.session()) {
            Result result = session.run(query,
                    Values.parameters("isporukaId", isporukaId)
            );

            if (result.hasNext()) {
                var record = result.next();
                var node = record.get("v").asNode();
                Vozilo vozilo = new Vozilo();
                vozilo.setId(node.id());
                vozilo.setRegistracija(node.get("registracija").asString());
                vozilo.setMarka(node.get("marka").asString());
                vozilo.setModel(node.get("model").asString());
                vozilo.setKapacitetKg(node.get("kapacitetKg").asDouble());
                vozilo.setStatus(node.get("status").asString());
                return vozilo;
            }
            return null;
        }
    }

    public List<Isporuka> getIsporukeForVozilo(Long voziloId) {
        String query = """
            MATCH (i:Isporuka)-[:USES_VEHICLE]->(v:Vozilo) 
            WHERE id(v) = $voziloId
            RETURN i
            """;

        try (Session session = driver.session()) {
            Result result = session.run(query,
                    Values.parameters("voziloId", voziloId)
            );

            return result.list().stream()
                    .map(record -> {
                        var node = record.get("i").asNode();
                        Isporuka isporuka = new Isporuka();
                        isporuka.setId(node.id());
                        isporuka.setKolicinaKg(node.get("kolicinaKg").asDouble());
                        isporuka.setStatus(node.get("status").asString());
                        return isporuka;
                    })
                    .collect(Collectors.toList());
        }
    }

    @Transactional
    public void obrisiVozilo(Long isporukaId, Long voziloId) {
        String query = """
            MATCH (i:Isporuka)-[r:USES_VEHICLE]->(v:Vozilo) 
            WHERE id(i) = $isporukaId AND id(v) = $voziloId
            DELETE r
            """;

        try (Session session = driver.session()) {
            session.run(query,
                    Values.parameters("isporukaId", isporukaId, "voziloId", voziloId)
            );
        }
    }

    @Transactional
    public void izmeniVozilo(Long isporukaId, Long noviVoziloId) {
        String query = """
            MATCH (i:Isporuka)-[r:USES_VEHICLE]->(:Vozilo) 
            WHERE id(i) = $isporukaId
            DELETE r
            WITH i
            MATCH (v:Vozilo)
            WHERE id(v) = $noviVoziloId
            MERGE (i)-[:USES_VEHICLE]->(v)
            """;

        try (Session session = driver.session()) {
            session.run(query,
                    Values.parameters("isporukaId", isporukaId, "noviVoziloId", noviVoziloId)
            );
        }
    }
}