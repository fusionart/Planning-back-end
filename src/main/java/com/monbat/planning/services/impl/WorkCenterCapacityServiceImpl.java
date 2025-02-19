package com.monbat.planning.services.impl;

import com.monbat.planning.models.entities.WorkCenterCapacity;
import com.monbat.planning.repositories.WorkCenterCapacityRepository;
import com.monbat.planning.services.WorkCenterCapacityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class WorkCenterCapacityServiceImpl implements WorkCenterCapacityService {
    @Autowired
    private WorkCenterCapacityRepository workCenterCapacityRepository;

    @Override
    public WorkCenterCapacity getWorkCenterData(String workCenter) {
        return this.workCenterCapacityRepository.findByWorkCenter(workCenter);
    }

    @Override
    public List<Object> getAllWorkCentersAsObject() {
        return Collections.singletonList(this.workCenterCapacityRepository.findAll());
    }

    @Override
    public List<WorkCenterCapacity> getAllWorkCenters() {
        return this.workCenterCapacityRepository.findAll();
    }
}
