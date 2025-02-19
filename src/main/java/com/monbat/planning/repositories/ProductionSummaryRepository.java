package com.monbat.planning.repositories;

import com.monbat.planning.models.entities.ProductionSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductionSummaryRepository extends JpaRepository<ProductionSummary, Long> {
    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE PRODUCTION_SUMMARY", nativeQuery = true)
    void truncateTable();

    List<ProductionSummary> findAllByCalendarWeek(String week);
}
