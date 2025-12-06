package com.example.demo.model;

import org.springframework.data.neo4j.core.schema.*;

@Node("Rampa")
public class Rampa {

    @Id @GeneratedValue
    private Long id;

    private String oznaka;
    private String status; // slobodna, zauzeta

    @Relationship(type = "ASSIGNED_TO", direction = Relationship.Direction.INCOMING)
    private Isporuka assignedIsporuka;

    public Rampa() {}

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOznaka() { return oznaka; }
    public void setOznaka(String oznaka) { this.oznaka = oznaka; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Isporuka getAssignedIsporuka() { return assignedIsporuka; }
    public void setAssignedIsporuka(Isporuka assignedIsporuka) { this.assignedIsporuka = assignedIsporuka; }
}
