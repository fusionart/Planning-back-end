package com.monbat.planning.services;

import com.monbat.planning.models.entities.WorkCenter;

import java.util.List;

public interface WorkCenterService {
    List<WorkCenter> findByProductionSupervisor(String supervisorCode);
    List<WorkCenter> findAll();
    List<WorkCenter> findByPlant(String plant);
}
