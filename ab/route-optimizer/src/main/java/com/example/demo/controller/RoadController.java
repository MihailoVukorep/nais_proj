package com.example.demo.controller;

import com.example.demo.model.Road;
import com.example.demo.service.RoadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roads")
public class RoadController {

    @Autowired
    private RoadService roadService;

    @PostMapping
    public ResponseEntity<?> createRoad(@RequestParam Long fromLocationId,
                                        @RequestParam Long toLocationId,
                                        @RequestParam Double distanceKm,
                                        @RequestParam Double durationHours) {
        try {
            Road road = roadService.createRoad(fromLocationId, toLocationId,
                    distanceKm, durationHours);
            return ResponseEntity.ok(road);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri kreiranju puta: " + e.getMessage());
        }
    }

    @GetMapping("/between")
    public ResponseEntity<?> getRoadsBetween(@RequestParam Long fromLocationId,
                                             @RequestParam Long toLocationId) {
        try {
            List<Road> roads = roadService.getRoadsBetween(fromLocationId, toLocationId);
            return ResponseEntity.ok(roads);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri dobavljanju puteva: " + e.getMessage());
        }
    }

    @GetMapping("/from/{locationId}")
    public ResponseEntity<?> getRoadsFromLocation(@PathVariable Long locationId) {
        try {
            List<Road> roads = roadService.getRoadsFromLocation(locationId);
            return ResponseEntity.ok(roads);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri dobavljanju puteva: " + e.getMessage());
        }
    }

    @GetMapping("/to/{locationId}")
    public ResponseEntity<?> getRoadsToLocation(@PathVariable Long locationId) {
        try {
            List<Road> roads = roadService.getRoadsToLocation(locationId);
            return ResponseEntity.ok(roads);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri dobavljanju puteva: " + e.getMessage());
        }
    }

    @PutMapping("/{roadId}")
    public ResponseEntity<?> updateRoad(@PathVariable Long roadId,
                                        @RequestParam(required = false) Double distanceKm,
                                        @RequestParam(required = false) Double durationHours,
                                        @RequestParam(required = false) Boolean blocked,
                                        @RequestParam(required = false) String reason) {
        try {
            Road updated = roadService.updateRoadAttributes(roadId, distanceKm,
                    durationHours, blocked, reason);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri ažuriranju puta: " + e.getMessage());
        }
    }

    @PatchMapping("/{roadId}/block")
    public ResponseEntity<?> blockRoad(@PathVariable Long roadId,
                                       @RequestParam String reason) {
        try {
            Map<String, Object> result = roadService.blockRoad(roadId, reason);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "error", "Greška pri blokiranju puta.",
                            "detalji", e.getMessage()
                    ));
        }
    }

    @PatchMapping("/{roadId}/unblock")
    public ResponseEntity<?> unblockRoad(@PathVariable Long roadId) {
        try {
            Map<String, Object> unblocked = roadService.unblockRoad(roadId);
            return ResponseEntity.ok(unblocked);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "error", "Greška pri blokiranju puta.",
                            "detalji", e.getMessage()
                    ));
        }
    }

    @DeleteMapping("/{roadId}")
    public ResponseEntity<?> deleteRoad(@PathVariable Long roadId) {
        try {
            boolean deleted = roadService.deleteRoad(roadId);
            if (deleted) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri brisanju puta: " + e.getMessage());
        }
    }

    @GetMapping("/blocked")
    public ResponseEntity<?> getBlockedRoads() {
        try {
            List<Road> blockedRoads = roadService.findBlockedRoads();
            return ResponseEntity.ok(blockedRoads);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri dobavljanju blokiranih puteva: " + e.getMessage());
        }
    }

    @GetMapping("/shortest")
    public ResponseEntity<?> getShortestPath(@RequestParam Long fromLocationId,
                                             @RequestParam Long toLocationId) {
        try {
            List<Road> shortestPath = roadService.findShortestPath(fromLocationId, toLocationId);
            return ResponseEntity.ok(shortestPath);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Greška pri pronalaženju najkraćeg puta: " + e.getMessage());
        }
    }
}