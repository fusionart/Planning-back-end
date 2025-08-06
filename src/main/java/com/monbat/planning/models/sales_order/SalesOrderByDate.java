package com.monbat.planning.models.sales_order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SalesOrderByDate {
    private String reqDlvWeek;
    private List<SalesOrderMain> salesOrderMainList;
}
