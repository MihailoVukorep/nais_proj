package com.example.demo.controller;

import com.example.demo.dto.RouteRequest;
import com.example.demo.model.Route;
import com.example.demo.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @PostMapping
    public ResponseEntity<?> createRoute(@RequestBody RouteRequest routeRequest) {
        try {
            Route route = routeService.createRoute(routeRequest);
            return ResponseEntity.ok(Map.of(
                    "message", "Ruta uspešno kreirana sa svim vezama",
                    "route", route
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri kreiranju rute",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRouteById(@PathVariable Long id) {
        try {
            Route route = routeService.getRouteById(id);
            if (route != null) {
                return ResponseEntity.ok(route);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri dobavljanju rute",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllRoutes() {
        try {
            List<Route> routes = routeService.getAllRoutes();
            return ResponseEntity.ok(routes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri dobavljanju ruta",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getRoutesByStatus(@PathVariable String status) {
        try {
            List<Route> routes = routeService.getRoutesByStatus(status);
            return ResponseEntity.ok(routes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri dobavljanju ruta po statusu",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/start/{startLocation}")
    public ResponseEntity<?> getRoutesByStartLocation(@PathVariable String startLocation) {
        try {
            List<Route> routes = routeService.getRoutesByStartLocation(startLocation);
            return ResponseEntity.ok(routes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri dobavljanju ruta po početnoj lokaciji",
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoute(@PathVariable Long id, @RequestBody RouteRequest route) {
        try {
            Route updatedRoute = routeService.updateRoute(id, route);
            return ResponseEntity.ok(updatedRoute);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri ažuriranju rute",
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoute(@PathVariable Long id) {
        try {
            routeService.deleteRoute(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Ruta uspešno obrisana"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri brisanju rute",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/distance-less-than")
    public ResponseEntity<?> getRoutesByDistanceLessThan(@RequestParam Double maxDistance) {
        try {
            List<Route> routes = routeService.getRoutesByDistanceLessThan(maxDistance);
            return ResponseEntity.ok(routes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri dobavljanju ruta po dužini",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/shortest")
    public ResponseEntity<?> getShortestRoutesBetween(
            @RequestParam String startLocation,
            @RequestParam String endLocation) {
        try {
            List<Route> routes = routeService.getShortestRoutesBetween(startLocation, endLocation);
            return ResponseEntity.ok(routes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Greška pri dobavljanju najkraćih ruta",
                    "message", e.getMessage()
            ));
        }
    }
}