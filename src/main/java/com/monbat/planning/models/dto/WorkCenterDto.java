package com.monbat.planning.models.dto;

import com.monbat.planning.models.entities.WorkCenter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkCenterDto {
    private String workCenter;
    private String description;
    private String plant;

    /**
     * Constructor from entity
     */
    public static WorkCenterDto fromEntity(WorkCenter entity) {
        if (entity == null) {
            return null;
        }
        return new WorkCenterDto(
                entity.getWorkCenter(),
                entity.getDescription(),
                entity.getPlant()
        );
    }
}
