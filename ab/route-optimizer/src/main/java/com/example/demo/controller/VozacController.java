package com.example.demo.controller;

import com.example.demo.model.Vozac;
import com.example.demo.service.VozacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/vozaci")
public class VozacController {

    @Autowired
    private VozacService vozacService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Vozac vozac) {
        try {
            Vozac saved = vozacService.save(vozac);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Greška pri kreiranju vozača: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            List<Vozac> vozaci = vozacService.findAll();
            return ResponseEntity.ok(vozaci);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Greška pri dobavljanju vozača: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            Vozac vozac = vozacService.findById(id);
            if (vozac != null) {
                return ResponseEntity.ok(vozac);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri dobavljanju vozača: " + e.getMessage());
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getByStatus(@PathVariable String status) {
        try {
            List<Vozac> vozaci = vozacService.findByStatus(status);
            return ResponseEntity.ok(vozaci);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri dobavljanju vozača po statusu: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Vozac vozac) {
        try {
            Vozac updated = vozacService.update(id, vozac);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri ažuriranju vozača: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            Vozac vozac = vozacService.findById(id);
            if (vozac != null) {
                vozacService.delete(id);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri brisanju vozača: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Vozac updated = vozacService.promeniStatus(id, status);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri promeni statusa: " + e.getMessage());
        }
    }

    /*@PostMapping("/{id}/zakazi-vozaca")
    public ResponseEntity<?> increaseRides(@PathVariable Long id) {
        try {
            Vozac updated = vozacService.povecajBrojVoznji(id);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri povećanju broja vožnji: " + e.getMessage());
        }
    }*/
}