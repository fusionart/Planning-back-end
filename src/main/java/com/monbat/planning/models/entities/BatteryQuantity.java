package com.monbat.planning.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "quantities")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BatteryQuantity extends BaseEntity {
    @Column
    private String batteryCode;
    @Column
    private Integer quantity;
    @Column
    private Integer productionPlant;
    @Column
    private Integer storageLocation;
    @Column
    private String batch;
}
