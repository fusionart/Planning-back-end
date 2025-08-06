package com.monbat.planning.services;

import com.monbat.vdm.namespaces.opapisalesordersrv0001.SalesOrderHeader;

import java.time.LocalDateTime;
import java.util.List;

public interface SalesOrderService {
    List<SalesOrderHeader> getSalesOrdersItems(String username, String password,
                                               LocalDateTime reqDelDateBegin,
                                               LocalDateTime reqDelDateEnd);
}
