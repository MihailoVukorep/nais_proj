package com.example.demo.model;


import org.springframework.data.neo4j.core.schema.*;

@Node("Vozilo")
public class Vozilo {

    @Id @GeneratedValue
    private Long id;

    private String marka;
    private String model;
    private String registracija;
    private Double kapacitetKg;
    private String status; // slobodno, zauzeto, u_kvaru, na_servisu

    public Vozilo() {}

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMarka() { return marka; }
    public void setMarka(String marka) { this.marka = marka; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getRegistracija() { return registracija; }
    public void setRegistracija(String registracija) { this.registracija = registracija; }

    public Double getKapacitetKg() { return kapacitetKg; }
    public void setKapacitetKg(Double kapacitetKg) { this.kapacitetKg = kapacitetKg; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
