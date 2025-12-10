package com.example.demo.dto;

public class CreateVoziloRequest {
    private String marka;
    private String model;
    private String registracija;
    private Double kapacitetKg;
    private String status;

    public CreateVoziloRequest() {}

    public String getMarka() {
        return marka;
    }

    public void setMarka(String marka) {
        this.marka = marka;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getRegistracija() {
        return registracija;
    }

    public void setRegistracija(String registracija) {
        this.registracija = registracija;
    }

    public Double getKapacitetKg() {
        return kapacitetKg;
    }

    public void setKapacitetKg(Double kapacitetKg) {
        this.kapacitetKg = kapacitetKg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
