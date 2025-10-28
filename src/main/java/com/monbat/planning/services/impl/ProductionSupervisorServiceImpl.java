package com.monbat.planning.services.impl;

import com.monbat.planning.models.entities.ProductionSupervisor;
import com.monbat.planning.repositories.ProductionSupervisorRepository;
import com.monbat.planning.services.ProductionSupervisorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductionSupervisorServiceImpl implements ProductionSupervisorService {
    @Autowired
    private ProductionSupervisorRepository productionSupervisorRepository;

    @Override
    public List<ProductionSupervisor> getProductionSupervisors() {
        return this.productionSupervisorRepository.findAll();
    }
}
