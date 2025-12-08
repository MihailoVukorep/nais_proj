package com.example.demo.controller;

import com.example.demo.model.Vozilo;
import com.example.demo.service.VoziloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/vozila")
public class VoziloController {

    @Autowired
    private VoziloService voziloService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Vozilo vozilo) {
        try {
            Vozilo saved = voziloService.save(vozilo);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Greška pri kreiranju vozila: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            List<Vozilo> vozila = voziloService.findAll();
            return ResponseEntity.ok(vozila);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Greška pri dobavljanju vozila: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            Vozilo vozilo = voziloService.findById(id);
            if (vozilo != null) {
                return ResponseEntity.ok(vozilo);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri dobavljanju vozila: " + e.getMessage());
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getByStatus(@PathVariable String status) {
        try {
            List<Vozilo> vozila = voziloService.findByStatus(status);
            return ResponseEntity.ok(vozila);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri dobavljanju vozila po statusu: " + e.getMessage());
        }
    }

    @GetMapping("/slobodna")
    public ResponseEntity<?> getSlobodnaSaKapacitetom(@RequestParam(defaultValue = "0") Double minKapacitet) {
        try {
            List<Vozilo> vozila = voziloService.findSlobodnaVozilaSaKapacitetom(minKapacitet);
            return ResponseEntity.ok(vozila);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri dobavljanju slobodnih vozila: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Vozilo vozilo) {
        try {
            Vozilo updated = voziloService.update(id, vozilo);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri ažuriranju vozila: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            Vozilo vozilo = voziloService.findById(id);
            if (vozilo != null) {
                voziloService.delete(id);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri brisanju vozila: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Vozilo updated = voziloService.promeniStatus(id, status);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri promeni statusa vozila: " + e.getMessage());
        }
    }
}