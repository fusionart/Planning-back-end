package com.monbat.planning.controllers.constants;

public class SapApiConstants {
    //Credentials
    public static final String USER_NAME = "niliev";
    public static final String PASSWORD = "21Zaq12wsx!`";

    //Production order
    public static final String PRODUCTION_ORDER_URL = "https://vhmotds4ci.sap.monbat" +
            ".com:44300/sap/opu/odata/sap/API_PRODUCTION_ORDER_2_SRV/";

    public static final String PRODUCTION_ORDER_MAIN_GET = "A_ProductionOrder_2";

    //Sales order
    public static final String SALES_ORDER_URL = "https://vhmotds4ci.sap.monbat" +
            ".com:44300/sap/opu/odata/sap/API_SALES_ORDER_SRV/";
    public static final String SALES_ORDER_MAIN_GET = "A_SalesOrder";

    //General
    public static final String TOP = "?$top=";
    public static final String URL_SUFFIX = "&sap-client=200";

}
