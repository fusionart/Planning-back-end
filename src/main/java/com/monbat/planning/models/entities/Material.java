package com.monbat.planning.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "materials")
@Getter
@Setter
public class Material extends BaseEntity{
    @Column
    private String material;
    @Column
    private Integer plant;
    @Column
    private String description;
    @Column
    private String materialType;
    @Column
    private String materialGroup;
    @Column
    private String uom;
    @Column
    private String externalMaterialGroup;
    @Column
    private Integer leadTimeOffset;
    @Column
    private Integer curringTime;
    @Column
    private Integer netWeight;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Material material1 = (Material) o;
        return Objects.equals(material, material1.material) && Objects.equals(plant, material1.plant) && Objects.equals(description, material1.description) && Objects.equals(materialType, material1.materialType) && Objects.equals(materialGroup, material1.materialGroup) && Objects.equals(uom, material1.uom) && Objects.equals(externalMaterialGroup, material1.externalMaterialGroup) && Objects.equals(leadTimeOffset, material1.leadTimeOffset) && Objects.equals(curringTime, material1.curringTime) && Objects.equals(netWeight, material1.netWeight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, plant, description, materialType, materialGroup, uom, externalMaterialGroup, leadTimeOffset, curringTime, netWeight);
    }
}
