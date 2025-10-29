package com.monbat.planning.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "production_supervisor")
@Getter
@Setter
public class ProductionSupervisor extends BaseEntity{
    @Column
    private String plant;
    @Column
    private String supervisor;
    @Column
    private String supervisorName;

    @OneToMany(mappedBy="productionSupervisor")
    @JsonIgnore
    private Set<WorkCenter> workCenters;
}
