package com.example.demo.controller;

import com.example.demo.dto.RouteRequest;
import com.example.demo.model.Route;
import com.example.demo.service.RouteService;
//import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
//@RequiredArgsConstructor
public class RouteController {

    @Autowired
    private RouteService service;

    @PostMapping("/optimize")
    public ResponseEntity<?> optimize(@RequestBody RouteRequest req) {
        //return ResponseEntity.ok(service.findOptimalRoute(req));
    }

    @GetMapping
    public List<Route> getAll() {
        return service.getAllRoutes();
    }

    @GetMapping("/{id}")
    public Route getOne(@PathVariable Long id) {
        return service.getById(id);
    }
}

