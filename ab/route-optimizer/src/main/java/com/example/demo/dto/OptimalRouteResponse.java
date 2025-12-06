package com.example.demo.dto;

import com.example.demo.model.Route;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@AllArgsConstructor
public class OptimalRouteResponse {
    private List<String> nodeNames;
    private Double totalCost;
    private String recommendedDriverName;
    private Long recommendedDriverId;
    private Long recommendedVehicleId;

    public OptimalRouteResponse() {}

    public OptimalRouteResponse(List<String> nodeNames, Double totalCost) {
        this.nodeNames = nodeNames;
        this.totalCost = totalCost;
    }

    // getters / setters
    public List<String> getNodeNames() { return nodeNames; }
    public void setNodeNames(List<String> nodeNames) { this.nodeNames = nodeNames; }

    public Double getTotalCost() { return totalCost; }
    public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }

    public String getRecommendedDriverName() { return recommendedDriverName; }
    public void setRecommendedDriverName(String recommendedDriverName) { this.recommendedDriverName = recommendedDriverName; }

    public Long getRecommendedDriverId() { return recommendedDriverId; }
    public void setRecommendedDriverId(Long recommendedDriverId) { this.recommendedDriverId = recommendedDriverId; }

    public Long getRecommendedVehicleId() { return recommendedVehicleId; }
    public void setRecommendedVehicleId(Long recommendedVehicleId) { this.recommendedVehicleId = recommendedVehicleId; }
}