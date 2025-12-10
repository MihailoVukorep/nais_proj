package com.example.demo.dto;

public class DjangoVoziloDTO {
    private Long id;
    private String marka;
    private String model;
    private String registracija;
    private Double kapacitet;
    private String status;

    public DjangoVoziloDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMarka() { return marka; }
    public void setMarka(String marka) { this.marka = marka; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getRegistracija() { return registracija; }
    public void setRegistracija(String registracija) { this.registracija = registracija; }
    public Double getKapacitet() { return kapacitet; }
    public void setKapacitet(Double kapacitet) { this.kapacitet = kapacitet; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
