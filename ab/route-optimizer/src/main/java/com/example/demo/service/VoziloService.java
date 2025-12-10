package com.example.demo.service;

import com.example.demo.model.Vozilo;
import com.example.demo.repository.VoziloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VoziloService {

    @Autowired
    private VoziloRepository voziloRepository;

    public Vozilo save(Vozilo vozilo) {
        //return voziloRepository.save(vozilo);
        return voziloRepository.createVozilo(vozilo.getMarka(),
                vozilo.getModel(),
                vozilo.getRegistracija(),
                vozilo.getKapacitetKg(),
                vozilo.getStatus());
    }

    public Vozilo findById(Long id) {
        Optional<Vozilo> vozilo = voziloRepository.findById(id);
        return vozilo.orElse(null);
    }

    public List<Vozilo> findAll() {
        return voziloRepository.findAll();
    }

    public List<Vozilo> findByStatus(String status) {
        return voziloRepository.findByStatus(status);
    }

    public List<Vozilo> findByMarka(String marka) {
        return voziloRepository.findByMarka(marka);
    }
    public Vozilo findByMarkaModel(String marka, String model) {
        return voziloRepository.findByMarkaModel(marka,model).get(0);
    }

    public List<Vozilo> findByKapacitetGreaterThan(Double minKapacitet) {
        return voziloRepository.findByKapacitetKgGreaterThanEqual(minKapacitet);
    }

    public Vozilo update(Long id, Vozilo voziloDetails) {
        Optional<Vozilo> optionalVozilo = voziloRepository.findById(id);
        if (optionalVozilo.isPresent()) {
            /*Vozilo existingVozilo = optionalVozilo.get();
            existingVozilo.setMarka(voziloDetails.getMarka());
            existingVozilo.setModel(voziloDetails.getModel());
            existingVozilo.setRegistracija(voziloDetails.getRegistracija());
            existingVozilo.setKapacitetKg(voziloDetails.getKapacitetKg());
            existingVozilo.setStatus(voziloDetails.getStatus());
            return voziloRepository.save(existingVozilo);*/
            Vozilo vozilo = optionalVozilo.get();
            return voziloRepository.updateVozilo(vozilo.getId(),
                    vozilo.getMarka(),
                    vozilo.getModel(),
                    vozilo.getRegistracija(),
                    vozilo.getKapacitetKg(),
                    vozilo.getStatus());

        }
        return null;
    }

    public void delete(Long id) {
        voziloRepository.deleteById(id);
    }

    public Vozilo promeniStatus(Long id, String noviStatus) {
        Optional<Vozilo> optionalVozilo = voziloRepository.findById(id);
        if (optionalVozilo.isPresent()) {
            Vozilo vozilo = optionalVozilo.get();
            vozilo.setStatus(noviStatus);
            return voziloRepository.save(vozilo);
        }
        return null;
    }

    public List<Vozilo> findSlobodnaVozilaSaKapacitetom(Double minKapacitet) {
        return voziloRepository.findByStatusAndKapacitet("slobodno", minKapacitet);
    }
}