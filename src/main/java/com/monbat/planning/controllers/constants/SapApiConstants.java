package com.monbat.planning.controllers.constants;

public class SapApiConstants {
    //Production order
    public static final String PRODUCTION_ORDER_URL = "https://vhmotds4ci.sap.monbat" +
            ".com:44300/sap/opu/odata/sap/API_PRODUCTION_ORDER_2_SRV/";

    public static final String PRODUCTION_ORDER_MAIN_GET = "A_ProductionOrder_2";

    //Sales order
    public static final String SALES_ORDER_URL = "https://vhmotds4ci.sap.monbat" +
            ".com:44300/sap/opu/odata/sap/API_SALES_ORDER_SRV/";
    public static final String SALES_ORDER_MAIN_GET = "A_SalesOrder";

    //Planned order
    public static final String PLANNED_ORDER_URL = "https://vhmotds4ci.sap.monbat" +
            ".com:44300/sap/opu/odata/sap/API_PLANNED_ORDERS/";
    public static final String PLANNED_ORDER_MAIN_GET = "A_PlannedOrder";

    //Material sotck
    public static final String MATERIAL_STOCK_URL = "https://vhmotds4ci.sap.monbat" +
            ".com:44300/sap/opu/odata/sap/API_MATERIAL_STOCK_SRV/";
    public static final String MATERIAL_STOCK_ITEM_GET = "A_MaterialStock('%s')/to_MatlStkInAcctMod";
    public static final String MATERIAL_STOCK_ITEM_GET1 = "A_MatlStkInAcctMod";

    //General
    public static final String TOP = "?$top=";
    public static final String SAP_CLIENT = "200";

}
