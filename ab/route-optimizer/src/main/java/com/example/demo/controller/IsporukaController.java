package com.example.demo.controller;

import com.example.demo.model.Isporuka;
import com.example.demo.service.IsporukaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/isporuke")
public class IsporukaController {

    @Autowired
    private IsporukaService isporukaService;

    // CREATE
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Isporuka isporuka) {
        try {
            Isporuka saved = isporukaService.save(isporuka);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Greška pri kreiranju isporuke: " + e.getMessage());
        }
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            List<Isporuka> isporuke = isporukaService.findAll();
            return ResponseEntity.ok(isporuke);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Greška pri dobavljanju isporuka: " + e.getMessage());
        }
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            Isporuka isporuka = isporukaService.findById(id);
            if (isporuka != null) {
                return ResponseEntity.ok(isporuka);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri dobavljanju isporuke: " + e.getMessage());
        }
    }

    // READ BY STATUS
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getByStatus(@PathVariable String status) {
        try {
            List<Isporuka> isporuke = isporukaService.findByStatus(status);
            return ResponseEntity.ok(isporuke);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri dobavljanju isporuka po statusu: " + e.getMessage());
        }
    }

    // READ AKTIVNE
    @GetMapping("/aktivne")
    public ResponseEntity<?> getAktivne() {
        try {
            List<Isporuka> isporuke = isporukaService.findAktivneIsporuke();
            return ResponseEntity.ok(isporuke);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri dobavljanju aktivnih isporuka: " + e.getMessage());
        }
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Isporuka isporuka) {
        try {
            Isporuka updated = isporukaService.update(id, isporuka);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri ažuriranju isporuke: " + e.getMessage());
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            Isporuka isporuka = isporukaService.findById(id);
            if (isporuka != null) {
                isporukaService.delete(id);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri brisanju isporuke: " + e.getMessage());
        }
    }

    // CUSTOM: Promena statusa
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Isporuka updated = isporukaService.promeniStatus(id, status);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri promeni statusa isporuke: " + e.getMessage());
        }
    }

    // CUSTOM: Isporuke po vozaču
    @GetMapping("/vozac/{vozacId}")
    public ResponseEntity<?> getByVozac(@PathVariable Long vozacId) {
        try {
            List<Isporuka> isporuke = isporukaService.findIsporukePoVozacu(vozacId);
            return ResponseEntity.ok(isporuke);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri dobavljanju isporuka za vozača: " + e.getMessage());
        }
    }

    // CUSTOM: Isporuke po vozilu
    @GetMapping("/vozilo/{voziloId}")
    public ResponseEntity<?> getByVozilo(@PathVariable Long voziloId) {
        try {
            List<Isporuka> isporuke = isporukaService.findIsporukePoVozilu(voziloId);
            return ResponseEntity.ok(isporuke);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri dobavljanju isporuka za vozilo: " + e.getMessage());
        }
    }
}