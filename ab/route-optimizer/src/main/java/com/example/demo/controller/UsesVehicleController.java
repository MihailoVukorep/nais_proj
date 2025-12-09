package com.example.demo.controller;

import com.example.demo.service.UsesVehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/uses-vehicle")
public class UsesVehicleController {

    @Autowired
    private UsesVehicleService usesVehicleService;

    @PostMapping
    public ResponseEntity<?> create(@RequestParam Long isporukaId,
                                    @RequestParam Long voziloId) {
        //Object result = usesVehicleService.zauzmiVozilo(isporukaId, voziloId);
        usesVehicleService.zauzmiVozilo(isporukaId, voziloId);
        return ResponseEntity.ok("Veza napravljena");
    }

    @GetMapping("/isporuka/{isporukaId}")
    public ResponseEntity<?> getByIsporuka(@PathVariable Long isporukaId) {
        Object result = usesVehicleService.getVoziloForIsporuka(isporukaId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/vozilo/{voziloId}")
    public ResponseEntity<?> getByVozilo(@PathVariable Long voziloId) {
        Object result = usesVehicleService.getIsporukeForVozilo(voziloId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{isporukaId}")
    public ResponseEntity<?> update(@PathVariable Long isporukaId,
                                    @RequestParam Long noviVoziloId) {
        usesVehicleService.izmeniVozilo(isporukaId, noviVoziloId);
        return ResponseEntity.ok("Azurirano");
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam Long isporukaId,
                                    @RequestParam Long voziloId) {
        usesVehicleService.obrisiVozilo(isporukaId, voziloId);
        return ResponseEntity.ok().build();
    }
}