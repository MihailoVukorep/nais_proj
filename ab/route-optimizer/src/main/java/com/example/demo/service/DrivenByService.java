package com.example.demo.service;

import com.example.demo.model.Isporuka;
import com.example.demo.model.Vozac;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DrivenByService {

    private final Driver driver;

    public DrivenByService(Driver driver) {
        this.driver = driver;
    }

    public void assignDriverToDelivery(Long isporukaId, Long vozacId) {
        String query = """
            MATCH (i:Isporuka), (v:Vozac) 
            WHERE id(i) = $isporukaId AND id(v) = $vozacId
            MERGE (i)-[:DRIVEN_BY]->(v)
            """;

        try (Session session = driver.session()) {
            session.run(query,
                    Values.parameters("isporukaId", isporukaId, "vozacId", vozacId)
            );
        }
    }

    public List<Isporuka> getDeliveriesByDriver(Long vozacId) {
        String query = """
            MATCH (i:Isporuka)-[:DRIVEN_BY]->(v:Vozac) 
            WHERE id(v) = $vozacId
            RETURN i
            """;

        try (Session session = driver.session()) {
            Result result = session.run(query,
                    Values.parameters("vozacId", vozacId)
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

    public Vozac getDriverOfDelivery(Long isporukaId) {
        String query = """
            MATCH (i:Isporuka)-[:DRIVEN_BY]->(v:Vozac) 
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
                Vozac vozac = new Vozac();
                vozac.setId(node.id());
                vozac.setIme(node.get("ime").asString());
                vozac.setPrezime(node.get("prezime").asString());
                vozac.setStatus(node.get("status").asString());
                vozac.setBrVoznji(node.get("brVoznji").asInt());
                return vozac;
            }
            return null;
        }
    }

    public void changeDriver(Long isporukaId, Long noviVozacId) {
        String query = """
            MATCH (i:Isporuka)-[r:DRIVEN_BY]->(:Vozac) 
            WHERE id(i) = $isporukaId
            DELETE r
            WITH i
            MATCH (v:Vozac)
            WHERE id(v) = $noviVozacId
            MERGE (i)-[:DRIVEN_BY]->(v)
            """;

        try (Session session = driver.session()) {
            session.run(query,
                    Values.parameters("isporukaId", isporukaId, "noviVozacId", noviVozacId)
            );
        }
    }

    public void removeDriverFromDelivery(Long isporukaId) {
        String query = """
            MATCH (i:Isporuka)-[r:DRIVEN_BY]->(:Vozac) 
            WHERE id(i) = $isporukaId
            DELETE r
            """;

        try (Session session = driver.session()) {
            session.run(query,
                    Values.parameters("isporukaId", isporukaId)
            );
        }
    }

    public Vozac findMostExperiencedAvailableDriver() {
        String query = """
            MATCH (v:Vozac)
            WHERE v.status = 'slobodan'
            RETURN v
            ORDER BY v.brVoznji DESC
            LIMIT 1
            """;

        try (Session session = driver.session()) {
            Result result = session.run(query);

            if (result.hasNext()) {
                var record = result.next();
                var node = record.get("v").asNode();
                Vozac vozac = new Vozac();
                vozac.setId(node.id());
                vozac.setIme(node.get("ime").asString());
                vozac.setPrezime(node.get("prezime").asString());
                vozac.setStatus(node.get("status").asString());
                vozac.setBrVoznji(node.get("brVoznji").asInt());
                return vozac;
            }
            return null;
        }
    }

    public void assignDriverBasedOnExperience(Long isporukaId) {
        Vozac najiskusniji = findMostExperiencedAvailableDriver();
        if (najiskusniji != null) {
            assignDriverToDelivery(isporukaId, najiskusniji.getId());
        } else {
            throw new RuntimeException("Nema slobodnih vozača");
        }
    }
}