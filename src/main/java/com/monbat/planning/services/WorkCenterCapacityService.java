package com.monbat.planning.services;

import com.monbat.planning.models.entities.WorkCenterCapacity;

import java.util.List;

public interface WorkCenterCapacityService {
    WorkCenterCapacity getWorkCenterData(String workCenter);
    List<Object> getAllWorkCentersAsObject();
    List<WorkCenterCapacity> getAllWorkCenters();
}
