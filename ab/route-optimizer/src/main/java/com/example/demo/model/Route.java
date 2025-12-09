package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.*;

import java.util.List;

@Node("Route")
public class Route {

    @Id
    @GeneratedValue
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    //private String startLocation;
    @Relationship(type = "STARTS_AT", direction = Relationship.Direction.OUTGOING)
    private Location startLocation;


    @Getter
    @Setter
    @Relationship(type = "ENDS_AT", direction = Relationship.Direction.OUTGOING)
    private Location endLocation;
    //private String endLocation;

    @Getter
    @Setter
    private Double distanceKm;

    @Getter
    @Setter
    private Double durationHours;

    @Getter
    @Setter
    @Relationship(type = "FOLLOWS_ROUTE", direction = Relationship.Direction.OUTGOING)
    private List<Location> path;

    @Getter
    @Setter
    @Property("status")
    private String status; // planirana/u_toku/odstupanje/zavrsena

    public Route() {}
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Double getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(Double durationHours) {
        this.durationHours = durationHours;
    }

    public List<Location> getPath() {
        return path;
    }

    public void setPath(List<Location> path) {
        this.path = path;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
