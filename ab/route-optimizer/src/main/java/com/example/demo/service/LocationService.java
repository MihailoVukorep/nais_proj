package com.example.demo.service;

import com.example.demo.model.Location;
import com.example.demo.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    public Location save(Location l){ return locationRepository.save(l);}

    public void create(String name, Double lat, Double lon){
        locationRepository.createLocation(name, lat, lon);
    }
    public void update(Long id, Double lat, Double lon){
        locationRepository.updateLocation(id, lat, lon);
    }
    public List<Location> findNeighbors(String name){
        return locationRepository.neighborsOf(name);
    }
    public List<Location> getAll(){ return locationRepository.findAll();}
    public Location findByName(String name){ return locationRepository.findByName(name);}
    public Optional<Location> findById(Long id){ return locationRepository.findById(id);}
    public void delete(Long id){ locationRepository.deleteLocation(id);}

}
