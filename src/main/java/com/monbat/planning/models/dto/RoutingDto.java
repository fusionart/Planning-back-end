package com.monbat.planning.models.dto;

import com.monbat.planning.models.entities.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoutingDto {
    private String routingGroup;
    private Integer routingGroupCounter;
    private Integer plant;
    private String workCenter;
    private String description;
    private Integer baseQuantity;

    private ActivityType setupTime;
    private ActivityType machineTime;
    private ActivityType laborTime;
    private ActivityType naturalGas;
    private ActivityType electricity;
    private ActivityType water;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoutingDto that = (RoutingDto) o;
        return Objects.equals(routingGroup, that.routingGroup) && Objects.equals(routingGroupCounter, that.routingGroupCounter) && Objects.equals(plant, that.plant) && Objects.equals(workCenter, that.workCenter) && Objects.equals(description, that.description) && Objects.equals(baseQuantity, that.baseQuantity) && Objects.equals(setupTime, that.setupTime) && Objects.equals(machineTime, that.machineTime) && Objects.equals(laborTime, that.laborTime) && Objects.equals(naturalGas, that.naturalGas) && Objects.equals(electricity, that.electricity) && Objects.equals(water, that.water);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routingGroup, routingGroupCounter, plant, workCenter, description, baseQuantity, setupTime, machineTime, laborTime, naturalGas, electricity, water);
    }
}
