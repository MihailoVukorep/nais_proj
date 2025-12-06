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
}
