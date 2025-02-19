package com.monbat.planning.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "work_center_capacity")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkCenterCapacity extends BaseEntity{
    @Column
    private String workCenter;
    @Column
    private int averageCapacity;
    @Column
    private int shifts;
    @Column
    private int workDays;
    @Column
    private String description;
}
