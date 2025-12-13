package com.example.demo.dto;

import lombok.Data;

@Data
public class DriverAnalyticsDTO {

    //private Integer vozacId;
    private String ime;
    private String prezime;
    private Integer brVoznji;
    private Double avgRouteDistance;
    private Double score;

    public DriverAnalyticsDTO() {}
    /*public DriverAnalyticsDTO(Long vozacId, String ime, String prezime,
                              Long brVoznji, Double avgRouteDistance, Double score) {
        this.vozacId = vozacId;
        this.ime = ime;
        this.prezime = prezime;
        this.brVoznji = brVoznji;
        this.avgRouteDistance = avgRouteDistance;
        this.score = score;
    }

    public Long getVozacId() {
        return vozacId != null ? vozacId.longValue() : null;
    }

    public void setVozacId(Integer vozacId) {
        this.vozacId = vozacId;
    }*/

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public Integer getBrVoznji() {
        return brVoznji;
    }

    public void setBrVoznji(Integer brVoznji) {
        this.brVoznji = brVoznji;
    }

    public Double getAvgRouteDistance() {
        return avgRouteDistance;
    }

    public void setAvgRouteDistance(Double avgRouteDistance) {
        this.avgRouteDistance = avgRouteDistance;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
