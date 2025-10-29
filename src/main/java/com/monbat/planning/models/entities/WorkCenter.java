package com.monbat.planning.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "work_center")
@Getter
@Setter
public class WorkCenter extends BaseEntity {
    @Column(name = "work_center")
    private String workCenter;
    @Column
    private String description;
    @Column
    private String plant;

    @ManyToOne()
    @JoinColumn(name = "productionSupervisor_id", nullable = false)
    @JsonIgnore
    private ProductionSupervisor productionSupervisor;
}
