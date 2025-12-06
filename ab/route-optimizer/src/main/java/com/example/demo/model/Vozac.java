package com.example.demo.model;

import org.springframework.data.neo4j.core.schema.*;

import java.util.List;

@Node("Vozac")
public class Vozac {

    @Id @GeneratedValue
    private Long id;

    private String email;
    private String ime;
    private String prezime;
    private Integer brVoznji;
    private String status; // slobodan, zauzet, na_odmoru

    public Vozac() {}

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getIme() { return ime; }
    public void setIme(String ime) { this.ime = ime; }

    public String getPrezime() { return prezime; }
    public void setPrezime(String prezime) { this.prezime = prezime; }

    public Integer getBrVoznji() { return brVoznji; }
    public void setBrVoznji(Integer brVoznji) { this.brVoznji = brVoznji; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
