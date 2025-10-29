package com.monbat.planning.repositories;

import com.monbat.planning.models.entities.WorkCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkCenterRepository extends JpaRepository<WorkCenter, Long> {
    List<WorkCenter> findByProductionSupervisorSupervisor(String supervisor);
    @Query("SELECT wc FROM WorkCenter wc JOIN wc.productionSupervisor ps WHERE ps.supervisor = :supervisor")
    List<WorkCenter> findWorkCentersBySupervisorCode(@Param("supervisor") String supervisor);
    List<WorkCenter> findByPlant(String plant);
    WorkCenter findByWorkCenter(String workCenter);
}
