package com.example.demo.controller;

import com.example.demo.service.ComplexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/complex")
public class ComplexController {

    @Autowired
    private ComplexService complexService;

    // Pronadji vozaca sa najvise obavljenih voznji, isporuka i ukupne kolicine isporuka
    @GetMapping("/vozac")
    public ResponseEntity<?> getVozac() {
        try {
            return ResponseEntity.ok(complexService.pronadjiVozaca());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri dobavljanju vozaca",
                    "message", e.getMessage()
            ));
        }
    }

    // Postavi status rampe na 'zauzeto' i isporuke na 'spremna'
    @PutMapping("/update-status")
    public ResponseEntity<?> updateRampaAndIsporukaStatus() {
        try {
            Map<String, Object> result = complexService.updateRampaAndIsporukaStatus();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Došlo je do greške prilikom izmene statusa.",
                    "detalji", e.getMessage()
            ));
        }
    }

    //Koliko puta je svaki razlog blokade puta zabeležen
    @GetMapping("/blokade")
    public ResponseEntity<?> getRoadBlockageStatistics() {
        try {
            Map<String, Object> result = complexService.getRoadBlockageStatistics();

            Boolean success = (Boolean) result.get("success");
            if (Boolean.TRUE.equals(success)) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Interna greška servera: " + e.getMessage(),
                    "timestamp", new java.util.Date()
            ));
        }
    }

    //Statistika kapaciteta vozila
    @GetMapping("/vozila")
    public ResponseEntity<?> getVehicleCapacityStatistics() {
        try {
            Map<String, Object> result = complexService.getVehicleCapacityStatistics();

            Boolean success = (Boolean) result.get("success");
            if (Boolean.TRUE.equals(success)) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Interna greška servera: " + e.getMessage(),
                    "timestamp", new java.util.Date()
            ));
        }
    }
}
