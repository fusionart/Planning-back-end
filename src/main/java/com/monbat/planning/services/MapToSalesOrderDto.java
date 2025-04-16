package com.monbat.planning.services;

import com.monbat.planning.models.dto.sap.sales_order.SalesOrderDto;
import com.monbat.vdm.namespaces.apisalesordersrv.SalesOrderHeader;

import java.util.List;

public interface MapToSalesOrderDto {
    List<SalesOrderDto> salesOrderList(List<SalesOrderHeader> salesOrderComponentsList);
}
