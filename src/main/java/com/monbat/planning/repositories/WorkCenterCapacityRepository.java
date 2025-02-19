package com.monbat.planning.repositories;

import com.monbat.planning.models.entities.WorkCenterCapacity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkCenterCapacityRepository extends JpaRepository<WorkCenterCapacity, Long> {
    WorkCenterCapacity findByWorkCenter(String workCenter);
}
