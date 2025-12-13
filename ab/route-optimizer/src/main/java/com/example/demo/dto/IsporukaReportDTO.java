package com.example.demo.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IsporukaReportDTO {
    private Long id;
    private String brojIsporuke;
    private String status;
    private Double kolicinaKg;
    @Nullable
    private LocalDate datumPolaska;
    @Nullable
    private LocalDate datumDolaska;
    private Long routeId;
    private String routeName;

    public IsporukaReportDTO(Long id, String status, Double kolicinaKg,
                             LocalDate datumPolaska, LocalDate datumDolaska) {
        this.id = id;
        this.status = status;
        this.kolicinaKg = kolicinaKg;
        this.datumPolaska = datumPolaska;
        this.datumDolaska = datumDolaska;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public LocalDate getDatumDolaska() {
        return datumDolaska;
    }

    public void setDatumDolaska(LocalDate datumDolaska) {
        this.datumDolaska = datumDolaska;
    }

    public LocalDate getDatumPolaska() {
        return datumPolaska;
    }

    public void setDatumPolaska(LocalDate datumPolaska) {
        this.datumPolaska = datumPolaska;
    }

    public Double getKolicinaKg() {
        return kolicinaKg;
    }

    public void setKolicinaKg(Double kolicinaKg) {
        this.kolicinaKg = kolicinaKg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBrojIsporuke() {
        return brojIsporuke;
    }

    public void setBrojIsporuke(String brojIsporuke) {
        this.brojIsporuke = brojIsporuke;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}