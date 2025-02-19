package com.monbat.planning.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "readiness")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Readiness extends BaseEntity implements Comparable<Readiness>{
    @Column()
    private Integer salesDocument;
    @Column()
    private String soldToParty;
    @Column()
    private String customerName;
    @Column()
    private Date dateOfReadiness;
    @Column()
    private String weekOfReadiness;
    @Column()
    private String reqDlvWeek;
    @Column()
    private String material;
    @Column()
    private Integer orderQuantity;
    @Column
    private Integer productionPlant;
    @Column
    private String batteryType;

    @Override
    public int compareTo(Readiness o) {
        return this.material.compareTo(o.material);
    }
}
