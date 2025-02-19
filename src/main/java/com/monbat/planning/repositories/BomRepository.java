package com.monbat.planning.repositories;

import com.monbat.planning.models.entities.Bom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BomRepository extends JpaRepository<Bom, Long> {
    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE BOMS", nativeQuery = true)
    void truncateTable();



    List<Bom> findAllByMaterial(String material);
}
