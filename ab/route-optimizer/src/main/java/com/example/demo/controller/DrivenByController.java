package com.example.demo.controller;

import com.example.demo.service.DrivenByService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/driven-by")
public class DrivenByController {

    @Autowired
    private DrivenByService drivenByService;

    @PostMapping
    public ResponseEntity<?> create(@RequestParam Long isporukaId,
                                    @RequestParam Long vozacId) {
        try {
            drivenByService.assignDriverToDelivery(isporukaId, vozacId);
            return ResponseEntity.ok(Map.of(
                    "message", "Vozač uspešno dodeljen isporuci",
                    "isporukaId", isporukaId,
                    "vozacId", vozacId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri dodeli vozača",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/isporuka/{isporukaId}")
    public ResponseEntity<?> getByIsporuka(@PathVariable Long isporukaId) {
        try {
            return ResponseEntity.ok(drivenByService.getDriverOfDelivery(isporukaId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri dobavljanju vozača",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/vozac/{vozacId}")
    public ResponseEntity<?> getByVozac(@PathVariable Long vozacId) {
        try {
            return ResponseEntity.ok(drivenByService.getDeliveriesByDriver(vozacId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri dobavljanju isporuka",
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{isporukaId}")
    public ResponseEntity<?> update(@PathVariable Long isporukaId,
                                    @RequestParam Long noviVozacId) {
        try {
            drivenByService.changeDriver(isporukaId, noviVozacId);
            return ResponseEntity.ok(Map.of(
                    "message", "Vozač uspešno izmenjen",
                    "isporukaId", isporukaId,
                    "noviVozacId", noviVozacId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri izmeni vozača",
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam Long isporukaId) {
        try {
            drivenByService.removeDriverFromDelivery(isporukaId);
            return ResponseEntity.ok(Map.of(
                    "message", "Vozač uspešno uklonjen sa isporuke",
                    "isporukaId", isporukaId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri uklanjanju vozača",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/assign-by-experience/{isporukaId}")
    public ResponseEntity<?> assignByExperience(@PathVariable Long isporukaId) {
        try {
            drivenByService.assignDriverBasedOnExperience(isporukaId);
            return ResponseEntity.ok(Map.of(
                    "message", "Vozač dodeljen na osnovu iskustva",
                    "isporukaId", isporukaId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri dodeli vozača po iskustvu",
                    "message", e.getMessage()
            ));
        }
    }
}