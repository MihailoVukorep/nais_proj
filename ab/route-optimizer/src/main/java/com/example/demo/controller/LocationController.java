package com.example.demo.controller;

import com.example.demo.model.Location;
import com.example.demo.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    @Autowired
    private LocationService locationService;

 /*   @PostMapping
    public ResponseEntity<?> create(@RequestParam(value = "name")String name,  //@RequestParam(value = "name", defaultValue = "")
                                    @RequestParam(value = "lat") Double lat,
                                    @RequestParam(value = "lon") Double lon){
        locationService.create(name, lat, lon);
        Location l = locationService.findByName(name);
        if(l != null){
            return ResponseEntity.ok("Lokacija je uspesno kreirana!");
        }
        return ResponseEntity.badRequest().body("Kreiranje neuspesno.");
    }*/
     @PostMapping
     public ResponseEntity<?> create(@RequestBody Location location) {
         try {
             Location saved = locationService.save(location);
             return ResponseEntity.ok("Lokacija je uspešno kreirana! ID: " + saved.getId());
         } catch (Exception e) {
             return ResponseEntity.badRequest().body("Kreiranje neuspešno: " + e.getMessage());
         }
     }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        Optional<Location> location = locationService.findById(id);
        if(location.isPresent()){
            locationService.delete(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Brisanje neuspesno.");
    }

    /*@PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestParam(value = "lat", defaultValue = "") Double lat,
                                    @RequestParam(value = "lon", defaultValue = "") Double lon){
        Location l = locationService.findById(id);
        if(l != null){
            locationService.update(id, lat, lon);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Azuriranje neuspesno.");
    }*/
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody Location location) {
        try {
            Optional<Location> l = locationService.findById(id);
            if(!l.isPresent()){
                return ResponseEntity.notFound().build();
            }

            location.setId(id);
            Location updated = locationService.save(location);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Ažuriranje neuspešno: " + e.getMessage());
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Optional<Location> location = locationService.findById(id);
        if(location.isPresent()){
            return ResponseEntity.ok(location);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(locationService.getAll());
    }
}
