package com.example.demo.model;

import org.springframework.data.neo4j.core.schema.*;

import java.time.Instant;

@Node("Isporuka")
public class Isporuka {

    @Id @GeneratedValue
    private Long id;

    private Double kolicinaKg;
    private String status; // aktivna, u_toku, spremna, zavrsena

    private Instant datumKreiranja;
    private Instant datumPolaska;
    private Instant datumDolaska;

    // veze
    @Relationship(type = "USES_VEHICLE", direction = Relationship.Direction.OUTGOING)
    private Vozilo vozilo;

    @Relationship(type = "DRIVEN_BY", direction = Relationship.Direction.OUTGOING)
    private Vozac vozac;

    @Relationship(type = "ON_ROUTE", direction = Relationship.Direction.OUTGOING)
    private Route route;

    public Isporuka() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getKolicinaKg() { return kolicinaKg; }
    public void setKolicinaKg(Double kolicinaKg) { this.kolicinaKg = kolicinaKg; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getDatumKreiranja() { return datumKreiranja; }
    public void setDatumKreiranja(Instant datumKreiranja) { this.datumKreiranja = datumKreiranja; }

    public Instant getDatumPolaska() { return datumPolaska; }
    public void setDatumPolaska(Instant datumPolaska) { this.datumPolaska = datumPolaska; }

    public Instant getDatumDolaska() { return datumDolaska; }
    public void setDatumDolaska(Instant datumDolaska) { this.datumDolaska = datumDolaska; }

    public Vozilo getVozilo() { return vozilo; }
    public void setVozilo(Vozilo vozilo) { this.vozilo = vozilo; }

    public Vozac getVozac() { return vozac; }
    public void setVozac(Vozac vozac) { this.vozac = vozac; }

    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }
}
