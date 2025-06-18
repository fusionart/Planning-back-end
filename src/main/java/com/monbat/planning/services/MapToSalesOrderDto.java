package com.monbat.planning.services;

import com.monbat.planning.models.sales_order.SalesOrderDto;
import com.monbat.vdm.namespaces.opapisalesordersrv0001.SalesOrderHeader;

import java.util.List;

public interface MapToSalesOrderDto {
    List<SalesOrderDto> salesOrderList(List<SalesOrderHeader> salesOrderComponentsList);
}
