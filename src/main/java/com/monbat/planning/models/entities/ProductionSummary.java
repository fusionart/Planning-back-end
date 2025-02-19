package com.monbat.planning.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "production_summary")
@Getter
@Setter
public class ProductionSummary extends BaseEntity implements Comparable<ProductionSummary>{
    @Column
    private String workCenter;
    @Column
    private LocalDate scheduleStartDate;
    @Column
    private Integer shift;
    @Column
    private String material;
    @Column
    private Double targetQuantity;
    @Column
    private Double deliveredQuantity;
    @Column
    private String systemStatus;
    @Column
    private String calendarWeek;
    @Column
    private String productionVersion;

    public String getCalendarWeek() {
        return calendarWeek;
    }

    public String getWorkCenter() {
        return workCenter;
    }

    @Override
    public int compareTo(ProductionSummary o) {
        int weekComparison = this.getCalendarWeek().compareTo(o.getCalendarWeek());
        if (weekComparison != 0) {
            return weekComparison;
        }
        return this.getWorkCenter().compareTo(o.getWorkCenter());
    }
}
