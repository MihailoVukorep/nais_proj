package com.example.demo.service;

import com.example.demo.model.Vozac;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Service
public class ComplexService {

    @Autowired
    private  Driver driver;

    public Vozac pronadjiVozaca(){
        String query = """
                MATCH (v:Vozac)<-[:DRIVEN_BY]-(i:Isporuka)
                WITH v, COUNT(i) as brojIsporuka, SUM(i.kolicinaKg) as ukKGIsporuke, v.brVoznji as brVoznji
                WHERE brojIsporuka > 0
                RETURN v, brojIsporuka, ukKGIsporuke, brVoznji
                ORDER BY brojIsporuka DESC, ukKGIsporuke DESC, brVoznji DESC
                LIMIT 1;
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

    public Map<String, Object> updateRampaAndIsporukaStatus() {
        Map<String, Object> response = new HashMap<>();

        try (Session session = driver.session()) {

            // 1. Provera postojanja rampi za izmenu
            String checkQuery = """
            MATCH (r:Rampa)-[:ASSIGNED_TO]->(i:Isporuka)
            WHERE i.status = 'aktivna' AND r.status = 'slobodna'
            RETURN COUNT(*) AS brojZaPromenu
            """;

            Result checkResult = session.run(checkQuery);
            long brojZaPromenu = checkResult.single().get("brojZaPromenu").asLong();

            if (brojZaPromenu == 0) {
                response.put("success", true);
                response.put("message", "Nema rampi za izmenu — sve su već u odgovarajućem stanju.");
                response.put("brojIzmenjenih", 0);
                response.put("timestamp", LocalDateTime.now());
                return response;
            }

            // 2. Izmena statusa
            String updateQuery = """
            MATCH (r:Rampa)-[:ASSIGNED_TO]->(i:Isporuka)
            WHERE i.status = 'aktivna' AND r.status = 'slobodna'
            SET i.status = 'planirana', r.status = 'zauzeta'
            RETURN r.oznaka AS rampaOznaka,
                   i.id AS isporukaId,
                   datetime() AS vremePromene
            """;

            Result updateResult = session.run(updateQuery);

            List<Map<String, Object>> izmene = new ArrayList<>();
            while (updateResult.hasNext()) {
                Record record = updateResult.next();
                Map<String, Object> izmena = new HashMap<>();
                izmena.put("rampaOznaka", record.get("rampaOznaka").asString());
                izmena.put("isporukaId", record.get("isporukaId").asLong());
                izmena.put("vremePromene", record.get("vremePromene").asLocalDateTime().toString());
                izmene.add(izmena);
            }

            response.put("success", true);
            response.put("message", "Status uspešno izmenjen na " + izmene.size() + " rampi i isporuka.");
            response.put("brojIzmenjenih", izmene.size());
            response.put("izmene", izmene);
            response.put("timestamp", LocalDateTime.now());

            return response;

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Dogodila se greška prilikom izmene statusa.");
            response.put("detalji", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return response;
        }
    }


    public Map<String, Object> getRoadBlockageStatistics() {
        String query = """
            MATCH (l:Location)-[r:ROAD]->(d:Location)
            WHERE r.blocked = true
            WITH r.reason AS razlog, count(r) AS brojPuta
            RETURN razlog, brojPuta 
            ORDER BY brojPuta DESC
            """;

        try (Session session = driver.session()) {
            Result result = session.run(query);

            List<Map<String, Object>> statistics = new ArrayList<>();
            int totalBlockages = 0;

            while (result.hasNext()) {
                var record = result.next();
                Map<String, Object> stat = new HashMap<>();
                stat.put("razlog", record.get("razlog").asString());
                stat.put("brojPuta", record.get("brojPuta").asInt());
                statistics.add(stat);

                totalBlockages += record.get("brojPuta").asInt();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistika", statistics);
            response.put("ukupnoBlokada", totalBlockages);
            response.put("brojRazlicitihRazloga", statistics.size());
            response.put("timestamp", new java.util.Date());

            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Greška pri dobavljanju statistike blokada: " + e.getMessage());
            error.put("timestamp", new java.util.Date());
            return error;
        }
    }

    public Map<String, Object> getVehicleCapacityStatistics() {
        String query = """
            MATCH (v:Vozilo)
            WHERE v.kapacitetKg IS NOT NULL
            WITH
                avg(v.kapacitetKg) AS prosekKapaciteta,
                max(v.kapacitetKg) AS maxKapacitet,
                min(v.kapacitetKg) AS minKapacitet,
                count(v) AS brojVozila
            RETURN prosekKapaciteta, maxKapacitet, minKapacitet, brojVozila
            """;

        try (Session session = driver.session()) {
            Result result = session.run(query);

            if (result.hasNext()) {
                var record = result.next();

                Map<String, Object> statistics = new HashMap<>();
                statistics.put("prosekKapaciteta", record.get("prosekKapaciteta").asDouble());
                statistics.put("maxKapacitet", record.get("maxKapacitet").asDouble());
                statistics.put("minKapacitet", record.get("minKapacitet").asDouble());
                statistics.put("brojVozila", record.get("brojVozila").asInt());
                statistics.put("rasponKapaciteta",
                        record.get("maxKapacitet").asDouble() - record.get("minKapacitet").asDouble());

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("statistika", statistics);
                response.put("timestamp", new java.util.Date());

                return response;
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Nema podataka o kapacitetima vozila");
                error.put("timestamp", new java.util.Date());
                return error;
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Greška pri dobavljanju statistike kapaciteta: " + e.getMessage());
            error.put("timestamp", new java.util.Date());
            return error;
        }
    }
}
