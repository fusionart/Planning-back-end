package com.monbat.planning.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActivityType {
    private String type;
    private Double value;
    private String uom;

    @Override
    public String toString() {
        return "Type: " + type + ", Value: " + value + ", UOM: " + uom;
    }
}
