package com.monbat.planning.repositories;

import com.monbat.planning.models.entities.BatteryQuantity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BatteryQuantityRepository extends JpaRepository<BatteryQuantity, Long> {
    List<BatteryQuantity> findAllByStorageLocation(int storageLocation);
    List<BatteryQuantity> findByBatteryCodeStartingWith(String prefix);


    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE QUANTITIES", nativeQuery = true)
    void truncateTable();
}
