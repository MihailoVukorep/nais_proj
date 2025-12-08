package com.example.demo.service;

import com.example.demo.model.Location;
import com.example.demo.model.Road;
import com.example.demo.repository.LocationRepository;
import com.example.demo.repository.RoadCustomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class RoadService {

    @Autowired
    private RoadCustomRepository roadCustomRepository;

    @Autowired
    private LocationRepository locationRepository;

    public Road createRoad(Long fromLocationId, Long toLocationId,
                           Double distanceKm, Double durationHours) {
        try {
            // Proveri da li lokacije postoje
            Location fromLocation = locationRepository.findById(fromLocationId).orElse(null);
            Location toLocation = locationRepository.findById(toLocationId).orElse(null);

            if (fromLocation == null || toLocation == null) {
                throw new RuntimeException("Jedna od lokacija ne postoji");
            }

            // Kreiraj road relaciju
            return roadCustomRepository.createRoad(fromLocationId, toLocationId,
                    distanceKm, durationHours);
        } catch (Exception e) {
            throw new RuntimeException("Greška pri kreiranju puta: " + e.getMessage());
        }
    }

    public List<Road> getRoadsBetween(Long fromLocationId, Long toLocationId) {
        return roadCustomRepository.findRoadsBetween(fromLocationId, toLocationId);
    }

    public List<Road> getRoadsFromLocation(Long locationId) {
        return roadCustomRepository.findRoadsFromLocation(locationId);
    }

    public List<Road> getRoadsToLocation(Long locationId) {
        return roadCustomRepository.findRoadsToLocation(locationId);
    }

    public Road updateRoadAttributes(Long roadId, Double distanceKm, Double durationHours,
                                     Boolean blocked, String reason) {
        return roadCustomRepository.updateRoadAttributes(roadId, distanceKm,
                durationHours, blocked, reason);
    }

    public Road blockRoad(Long roadId, String reason) {
        return roadCustomRepository.updateRoadAttributes(roadId, null, null, true, reason);
    }

    public Road unblockRoad(Long roadId) {
        return roadCustomRepository.updateRoadAttributes(roadId, null, null, false, null);
    }

    public boolean deleteRoad(Long roadId) {
        return roadCustomRepository.deleteRoad(roadId);
    }

    public List<Road> findBlockedRoads() {
        return roadCustomRepository.findBlockedRoads();
    }

    public List<Road> findShortestPath(Long fromLocationId, Long toLocationId) {
        return roadCustomRepository.findShortestPath(fromLocationId, toLocationId);
    }
}