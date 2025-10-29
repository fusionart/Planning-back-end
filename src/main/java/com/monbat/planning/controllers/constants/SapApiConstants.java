package com.monbat.planning.controllers.constants;

public class SapApiConstants {
    //Production order
    public static final String PRODUCTION_ORDER_URL = "https://vhmotds4ci.sap.monbat" +
            ".com:44300/sap/opu/odata/sap/API_PRODUCTION_ORDER_2_SRV/";

    public static final String PRODUCTION_ORDER_MAIN_GET = "A_ProductionOrder_2";
    public static final String CONVERT_PLANNED_ORDER = "ConvertPlndOrder";

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

    //Business partner
    public static final String BUSINESS_PARTNER_URL = "https://vhmotds4ci.sap.monbat" +
            ".com:44300/sap/opu/odata/sap/API_BUSINESS_PARTNER/";
    public static final String BUSINESS_PARTNER_MAIN_GET = "A_BusinessPartner";

    //Product
    public static final String PRODUCT_URL = "https://vhmotds4ci.sap.monbat" +
            ".com:44300/sap/opu/odata/sap/API_PRODUCT_SRV/";
    public static final String PRODUCT_MAIN_GET = "A_Product";

    public static final String PRODUCTION_VERSIONS_URL = "https://vhmotds4ci.sap.monbat" +
        ".com:44300/sap/opu/odata4/sap/api_production_version/srvd_a2x/sap/productionversion/0001/ProductionVersion";

    //General
    public static final String TOP = "?$top=";
    public static final String SAP_CLIENT = "200";
    public static final String SAP_MAIN = "https://vhmotds4ci.sap.monbat.com:44300";

}
