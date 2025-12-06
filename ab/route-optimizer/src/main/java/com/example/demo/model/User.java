package com.example.demo.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("User")
public class User {
    @Id
    @GeneratedValue
    private Long id;

    private String email;
    private String ime;
    private String prezime;
    private String uloga; // DISPECER, VOZAC, ADMIN

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getIme() { return ime; }
    public void setIme(String ime) { this.ime = ime; }

    public String getPrezime() { return prezime; }
    public void setPrezime(String prezime) { this.prezime = prezime; }

    public String getUloga() { return uloga; }
    public void setUloga(String uloga) { this.uloga = uloga; }

}
