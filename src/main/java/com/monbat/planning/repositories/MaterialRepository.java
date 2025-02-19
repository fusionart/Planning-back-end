package com.monbat.planning.repositories;

import com.monbat.planning.models.entities.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE MATERIALS", nativeQuery = true)
    void truncateTable();

    Material findFirstByMaterial(String material);

    List<Material> findByCurringTimeNotOrLeadTimeOffsetNot(int curringTime, int leadTimeOffset);

    List<Material> findByKilosForEachNot(int kilosForEach);
}
