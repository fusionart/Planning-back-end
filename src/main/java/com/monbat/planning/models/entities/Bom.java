package com.monbat.planning.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "boms")
@Getter
@Setter
public class Bom extends BaseEntity{
    @Column
    private Integer plant;
    @Column
    private String material;
    @Column
    private Integer baseQuantity;
    @Column
    private String component;
    @Column
    private Double quantity;
    @Column
    private String componentUom;

}
