package com.monbat.planning.repositories;

import com.monbat.planning.models.entities.Readiness;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ReadinessRepository extends JpaRepository<Readiness, Long> {
    List<Readiness> findAllByReqDlvWeekAndProductionPlant(String reqDlvWeek, int productionPlan);
    List<Readiness> findAllByWeekOfReadinessAndProductionPlant(String weekOfReadiness, int productionPlan);

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE READINESS", nativeQuery = true)
    void truncateTable();
}
