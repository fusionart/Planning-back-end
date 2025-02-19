package com.monbat.planning.models.entities;

import com.monbat.planning.models.converters.ActivityTypeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "routings")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class  Routing extends BaseEntity{
    @Column(name = "routing_group", nullable = false)
    private String routingGroup;
    @Column(name = "routing_group_counter", nullable = false)
    private Integer routingGroupCounter;
    @Column(nullable = false)
    //@Enumerated(EnumType.STRING)
    private Integer plant;
    @Column(name = "work_center", nullable = false)
    private String workCenter;
    @Column(nullable = false)
    private String description;
    @Column(name = "base_quantity", nullable = false)
    private Integer baseQuantity;

    @Column(name = "setup_time")
    @Convert(converter = ActivityTypeConverter.class)
    private ActivityType setupTime;
    @Column(name = "machine_time")
    @Convert(converter = ActivityTypeConverter.class)
    private ActivityType machineTime;
    @Column(name = "labor_time")
    @Convert(converter = ActivityTypeConverter.class)
    private ActivityType laborTime;
    @Column(name = "natural_gas")
    @Convert(converter = ActivityTypeConverter.class)
    private ActivityType naturalGas;
    @Column()
    @Convert(converter = ActivityTypeConverter.class)
    private ActivityType electricity;
    @Column()
    @Convert(converter = ActivityTypeConverter.class)
    private ActivityType water;
}
