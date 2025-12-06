package com.example.demo.service;

import com.example.demo.dto.OptimalRouteResponse;
import com.example.demo.dto.RouteRequest;
import com.example.demo.model.Route;
import com.example.demo.model.Location;
import com.example.demo.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
//@RequiredArgsConstructor
public class RouteService {

    @Autowired
    private RouteRepository repo;

    public OptimalRouteResponse findOptimalRoute(RouteRequest req) {

        Map<String,Object> result = repo.findShortestPath(req.getStart(), req.getEnd());

        List<Long> nodeIds = (List<Long>) result.get("nodeIds");
        double cost = (double) result.get("totalCost");

        // Pretvaramo ID-ove u nazive čvorova
        List<String> nodeNames = nodeIds.stream().map(Object::toString).toList();

        return new OptimalRouteResponse(nodeNames, cost);
    }

    public List<Route> getAllRoutes() {
        return repo.findAll();
    }

    public Route getById(Long id) {
        return repo.findById(id).orElseThrow();
    }
}

