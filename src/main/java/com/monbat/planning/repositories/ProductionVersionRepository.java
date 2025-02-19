package com.monbat.planning.repositories;

import com.monbat.planning.models.entities.ProductionVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProductionVersionRepository extends JpaRepository<ProductionVersion, Long> {
    ProductionVersion findFirstByMaterialAndProductionVersionNumber(String material, int productionVersionNumber);

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE PRODUCTION_VERSION", nativeQuery = true)
    void truncateTable();
}
