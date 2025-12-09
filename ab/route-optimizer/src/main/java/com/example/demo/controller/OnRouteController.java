package com.example.demo.controller;

import com.example.demo.service.OnRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/on-route")
public class OnRouteController {

    @Autowired
    private OnRouteService onRouteService;

    @PostMapping
    public ResponseEntity<?> create(@RequestParam Long isporukaId,
                                    @RequestParam Long routeId) {
        try {
            onRouteService.assignRoute(isporukaId, routeId);
            return ResponseEntity.ok(Map.of(
                    "message", "Ruta uspešno dodeljena isporuci",
                    "isporukaId", isporukaId,
                    "routeId", routeId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri dodeli rute",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/isporuka/{isporukaId}")
    public ResponseEntity<?> getByIsporuka(@PathVariable Long isporukaId) {
        try {
            return ResponseEntity.ok(onRouteService.getRouteForIsporuka(isporukaId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri dobavljanju rute",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<?> getByRoute(@PathVariable Long routeId) {
        try {
            return ResponseEntity.ok(onRouteService.getIsporukeForRoute(routeId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri dobavljanju isporuka",
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{isporukaId}")
    public ResponseEntity<?> update(@PathVariable Long isporukaId,
                                    @RequestParam Long novaRouteId) {
        try {
            onRouteService.changeRoute(isporukaId, novaRouteId);
            return ResponseEntity.ok(Map.of(
                    "message", "Ruta uspešno izmenjena",
                    "isporukaId", isporukaId,
                    "novaRouteId", novaRouteId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri izmeni rute",
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam Long isporukaId,
                                    @RequestParam Long routeId) {
        try {
            onRouteService.removeRoute(isporukaId, routeId);
            return ResponseEntity.ok(Map.of(
                    "message", "Veza sa rutom uspešno uklonjena",
                    "isporukaId", isporukaId,
                    "routeId", routeId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri uklanjanju veze sa rutom",
                    "message", e.getMessage()
            ));
        }
    }
}