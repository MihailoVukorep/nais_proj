package com.example.demo.service;

import com.example.demo.model.Isporuka;
import com.example.demo.repository.IsporukaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class IsporukaService {

    @Autowired
    private IsporukaRepository isporukaRepository;

    public Isporuka save(Isporuka isporuka) {
       /* if (isporuka.getDatumKreiranja() == null) {
            isporuka.setDatumKreiranja(Instant.now());
        }
        return isporukaRepository.save(isporuka);*/
        return isporukaRepository.createIsporuka(isporuka.getKolicinaKg(),
                isporuka.getStatus(),
                LocalDateTime.now());
    }

    public Isporuka findById(Long id) {
        Optional<Isporuka> isporuka = isporukaRepository.findById(id);
        return isporuka.orElse(null);
    }

    public List<Isporuka> findAll() {
        return isporukaRepository.findAll();
    }

    public List<Isporuka> findByStatus(String status) {
        return isporukaRepository.findByStatus(status);
    }

    public List<Isporuka> findByKolicinaGreaterThan(Double minKolicina) {
        return isporukaRepository.findByKolicinaKgGreaterThan(minKolicina);
    }

    public Isporuka update(Long id, Isporuka isporukaDetails) {
        Optional<Isporuka> optionalIsporuka = isporukaRepository.findById(id);
        if (optionalIsporuka.isPresent()) {
            Isporuka existingIsporuka = optionalIsporuka.get();
            existingIsporuka.setKolicinaKg(isporukaDetails.getKolicinaKg());
            existingIsporuka.setStatus(isporukaDetails.getStatus());
            existingIsporuka.setDatumPolaska(isporukaDetails.getDatumPolaska());
            existingIsporuka.setDatumDolaska(isporukaDetails.getDatumDolaska());
            existingIsporuka.setVozilo(isporukaDetails.getVozilo());
            existingIsporuka.setVozac(isporukaDetails.getVozac());
            existingIsporuka.setRoute(isporukaDetails.getRoute());
            return isporukaRepository.save(existingIsporuka);
        }
        return null;
    }

    public void delete(Long id) {
        isporukaRepository.deleteById(id);
    }

    public Isporuka promeniStatus(Long id, String noviStatus) {
        Optional<Isporuka> optionalIsporuka = isporukaRepository.findById(id);
        if (optionalIsporuka.isPresent()) {
            Isporuka isporuka = optionalIsporuka.get();
            isporuka.setStatus(noviStatus);

            if ("u_toku".equals(noviStatus) && isporuka.getDatumPolaska() == null) {
                //isporuka.setDatumPolaska(Instant.now());
                isporuka.setDatumPolaska(LocalDateTime.now());
            }

            if ("zavrsena".equals(noviStatus) && isporuka.getDatumDolaska() == null) {
                isporuka.setDatumDolaska(LocalDateTime.now());
            }

            return isporukaRepository.save(isporuka);
        }
        return null;
    }

    public List<Isporuka> findAktivneIsporuke() {
        return isporukaRepository.findAktivneIsporuke();
    }

    public List<Isporuka> findIsporukePoVozacu(Long vozacId) {
        return isporukaRepository.findByVozacId(vozacId);
    }

    public List<Isporuka> findIsporukePoVozilu(Long voziloId) {
        return isporukaRepository.findByVoziloId(voziloId);
    }
}