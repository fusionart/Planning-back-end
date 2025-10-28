package com.monbat.planning.repositories;

import com.monbat.planning.models.entities.ProductionSupervisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionSupervisorRepository extends JpaRepository<ProductionSupervisor, Long> {
}
