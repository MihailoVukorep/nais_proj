package com.example.demo.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class RouteRequest {


    private String start;

    private String end;

    private Double kolicinaKg;

    public RouteRequest() {}

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public Double getKolicinaKg() {
        return kolicinaKg;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public void setKolicinaKg(Double kolicinaKg) {
        this.kolicinaKg = kolicinaKg;
    }
}
