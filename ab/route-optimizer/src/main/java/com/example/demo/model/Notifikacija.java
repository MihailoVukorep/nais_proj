package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.*;

import java.time.Instant;

import static org.springframework.data.neo4j.core.schema.Relationship.Direction.OUTGOING;

@Node("Notifikacija")
public class Notifikacija {
    @Id
    @GeneratedValue
    @Setter
    @Getter
    private Long id;

    @Setter
    @Getter
    private String poruka;

    @Setter
    @Getter
    private Instant datum;

    @Setter
    @Getter
    private Boolean procitana;

    @Setter
    @Getter
    @Relationship(type = "SENT_TO", direction = OUTGOING)
    private User korisnik;
}