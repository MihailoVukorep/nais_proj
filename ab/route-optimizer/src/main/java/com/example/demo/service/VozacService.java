package com.example.demo.service;

import com.example.demo.model.Vozac;
import com.example.demo.repository.VozacRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VozacService {

    @Autowired
    private VozacRepository vozacRepository;

    public Vozac save(Vozac vozac) {
        return vozacRepository.save(vozac);
    }

    public Vozac findById(Long id) {
        Optional<Vozac> vozac = vozacRepository.findById(id);
        return vozac.orElse(null);
    }

    public List<Vozac> findAll() {
        return vozacRepository.findAll();
    }

    public List<Vozac> findByStatus(String status) {
        return vozacRepository.findByStatus(status);
    }

    public List<Vozac> findByImeAndPrezime(String ime, String prezime) {
        return vozacRepository.findByImeAndPrezime(ime, prezime);
    }

    public Vozac update(Long id, Vozac vozacDetails) {
        Optional<Vozac> optionalVozac = vozacRepository.findById(id);
        if (optionalVozac.isPresent()) {
            Vozac existingVozac = optionalVozac.get();
            existingVozac.setIme(vozacDetails.getIme());
            existingVozac.setPrezime(vozacDetails.getPrezime());
            existingVozac.setBrVoznji(vozacDetails.getBrVoznji());
            existingVozac.setStatus(vozacDetails.getStatus());
            return vozacRepository.save(existingVozac);
        }
        return null;
    }

    public void delete(Long id) {
        //vozacRepository.deleteById(id);
        vozacRepository.deleteVozac(id);
    }

    public Vozac promeniStatus(Long id, String noviStatus) {
        Optional<Vozac> optionalVozac = vozacRepository.findById(id);
        if (optionalVozac.isPresent()) {
            Vozac vozac = optionalVozac.get();
            vozac.setStatus(noviStatus);
            return vozacRepository.save(vozac);
        }
        return null;
    }

    public Vozac povecajBrojVoznji(Long id) {
        Optional<Vozac> optionalVozac = vozacRepository.findById(id);
        if (optionalVozac.isPresent()) {
            Vozac vozac = optionalVozac.get();
            vozac.setBrVoznji(vozac.getBrVoznji() != null ? vozac.getBrVoznji() + 1 : 1);
            return vozacRepository.save(vozac);
        }
        return null;
    }
}