package com.example.demo.dto;

import java.util.List;

public class RouteRequest {
    private String start;
    private Double startLat;
    private Double startLon;
    private String end;
    private Double endLat;
    private Double endLon;
    private Double distanceKm;
    private Double durationHours;
    private List<String> path; // lista imena lokacija na putanji

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStart() { return start; }
    public void setStart(String start) { this.start = start; }

    public Double getStartLat() { return startLat; }
    public void setStartLat(Double startLat) { this.startLat = startLat; }

    public Double getStartLon() { return startLon; }
    public void setStartLon(Double startLon) { this.startLon = startLon; }

    public String getEnd() { return end; }
    public void setEnd(String end) { this.end = end; }

    public Double getEndLat() { return endLat; }
    public void setEndLat(Double endLat) { this.endLat = endLat; }

    public Double getEndLon() { return endLon; }
    public void setEndLon(Double endLon) { this.endLon = endLon; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Double getDurationHours() { return durationHours; }
    public void setDurationHours(Double durationHours) { this.durationHours = durationHours; }

    public List<String> getPath() { return path; }
    public void setPath(List<String> path) { this.path = path; }
}