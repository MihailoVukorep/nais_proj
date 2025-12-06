package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.*;

@RelationshipProperties
public class Road {

    @RelationshipId
    private Long id;

    @TargetNode
    private Location destination;

    // property on relationship
    @Property("distance")
    private Double distanceKm;

    @Property("duration_hours")
    private Double durationHours;

    @Property("blocked")
    private Boolean blocked;

    @Property("reason")
    private String reason;

    public Road() {}

    public Road(Location destination, Double distanceKm, Double durationHours) {
        this.destination = destination;
        this.distanceKm = distanceKm;
        this.durationHours = durationHours;
        this.blocked = false;
        this.reason = null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Location getDestination() { return destination; }
    public void setDestination(Location destination) { this.destination = destination; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Double getDurationHours() { return durationHours; }
    public void setDurationHours(Double durationHours) { this.durationHours = durationHours; }

    public Boolean getBlocked() { return blocked; }
    public void setBlocked(Boolean blocked) { this.blocked = blocked; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

