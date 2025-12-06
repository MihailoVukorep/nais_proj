package com.example.demo.model;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node("Location")
public class Location {
    @Id @GeneratedValue
    @Getter
    @Setter
    private Long id;
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Double lat;

    @Getter
    @Setter
    private Double lon;

    @Getter
    @Setter
    @Relationship(type = "ROAD")
    private List<Road> roads;
}
