package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.*;

import java.time.Instant;

@Node("Upozorenje")
public class Upozorenje {

    @Id
    @GeneratedValue
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String tip;

    @Getter
    @Setter
    private String poruka;

    @Getter
    @Setter
    private Instant vreme;
    @Relationship(type = "ABOUT", direction = Relationship.Direction.OUTGOING)
    private Isporuka isporuka;
}
