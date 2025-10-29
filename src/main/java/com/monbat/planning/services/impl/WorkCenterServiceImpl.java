package com.monbat.planning.services.impl;

import com.monbat.planning.models.entities.WorkCenter;
import com.monbat.planning.repositories.WorkCenterRepository;
import com.monbat.planning.services.WorkCenterService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkCenterServiceImpl implements WorkCenterService {

    private static final Logger logger = LoggerFactory.getLogger(WorkCenterServiceImpl.class);

    @Autowired
    private WorkCenterRepository workCenterRepository;

    @Override
    @Transactional()
    public List<WorkCenter> findByProductionSupervisor(String supervisorCode) {
        logger.debug("Finding work centers for supervisor: {}", supervisorCode);

        // This uses the repository method that queries by the relationship
        List<WorkCenter> workCenters = workCenterRepository
                .findByProductionSupervisorSupervisor(supervisorCode);

        logger.debug("Found {} work centers for supervisor: {}",
                workCenters.size(), supervisorCode);

        return workCenters;
    }

    @Override
    @Transactional()
    public List<WorkCenter> findAll() {
        return workCenterRepository.findAll();
    }

    @Override
    @Transactional()
    public List<WorkCenter> findByPlant(String plant) {
        return workCenterRepository.findByPlant(plant);
    }
}
