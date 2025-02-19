package com.monbat.planning.repositories;

import com.monbat.planning.models.entities.Routing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RoutingRepository extends JpaRepository<Routing, Long> {
    Routing findFirstByRoutingGroupAndRoutingGroupCounter(String routingGroup, int routingGroupCounter);

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE ROUTINGS", nativeQuery = true)
    void truncateTable();
}
