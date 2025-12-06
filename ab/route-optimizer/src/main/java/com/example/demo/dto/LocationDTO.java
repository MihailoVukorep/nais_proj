package com.example.demo.dto;


public class LocationDTO {
    private String name;
    private Double lat;
    private Double lon;
    public LocationDTO(){}
    public LocationDTO(String name, Double lat, Double lon){this.name=name; this.lat=lat; this.lon=lon;}
    public String getName(){return name;} public void setName(String n){this.name=n;}
    public Double getLat(){return lat;} public void setLat(Double l){this.lat=l;}
    public Double getLon(){return lon;} public void setLon(Double lo){this.lon=lo;}
}