package com.monbat.planning.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "production_version")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductionVersion extends BaseEntity{
    @Column(nullable = false)
    private String material;
    //@Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Integer plant;
    @Column(name = "production_version_number", nullable = false)
    private Integer productionVersionNumber;
    @Column(name = "routing_group", nullable = false)
    private String routingGroup;
    @Column(name = "routing_group_counter", nullable = false)
    private Integer routingGroupCounter;
    @Column
    private String description;
}
