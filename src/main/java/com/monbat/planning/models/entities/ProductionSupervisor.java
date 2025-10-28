package com.monbat.planning.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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
}
