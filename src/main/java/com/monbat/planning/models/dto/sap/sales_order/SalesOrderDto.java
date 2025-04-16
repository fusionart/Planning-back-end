package com.monbat.planning.models.dto.sap.sales_order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SalesOrderDto {
    private String salesOrderNumber;
    private String soldToParty;
    private LocalDateTime requestedDeliveryDate;
    private List<ToItem> toItem;
}
