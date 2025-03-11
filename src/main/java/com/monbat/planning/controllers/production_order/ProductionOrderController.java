package com.monbat.planning.controllers.production_order;

import com.google.gson.Gson;
import com.monbat.planning.services.utils.SSLUtils;
import com.monbat.vdm.namespaces.opapiproductionorder2srv0001.ProductionOrder;
import com.monbat.vdm.services.DefaultOPAPIPRODUCTIONORDER2SRV0001Service;
import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;

import com.sap.cloud.sdk.datamodel.odata.helper.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@RestController
@RequestMapping("/api/sap")
public class ProductionOrderController implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ProductionOrderController.class);


    private static final String APIKEY_HEADER = "apikey";
    private static final String SANDBOX_APIKEY = "xvvMomYGNAO2Vh76tRa6NxOudWTP1U6k";

    @RequestMapping( value = "/getProductionOrders", method = RequestMethod.GET )
    public String getProductionOrders() {

        // DESTINATION 1:  Destination to the local Mock Server
        // Uncomment this section to test with mock server
//        final HttpDestination destination = DefaultDestination.builder()
//                .property("Name", "mydestination")
//                .property("URL", "http://localhost:8081")
//                .property("Type", "HTTP")
//                .property("Authentication", "NoAuthentication")
//                .build().asHttp();

        // DESTINATION 2:  Destination to the api.sap.com Sandbox
        // Uncomment this section to test with sandbox in api.sap.com
        // final HttpDestination destination = DefaultDestination.builder()
        //                                         .property("Name", "mydestination")
        //                                         .property("URL", "https://sandbox.api.sap.com/s4hanacloud")
        //                                         .property("Type", "HTTP")
        //                                         .property("Authentication", "NoAuthentication")
        //                                         .build().asHttp();


        //!!!!Warning: This disables SSL security and should not be used in production.!!!!!
        SSLUtils.disableSSLValidation();
        // DESTINATION 3:  Destination to a SAP S/4HANA Cloud (public edition) tenant
        // Uncomment this section to test with actual SAP S/4HANA Cloud
        final HttpDestination destination = DefaultDestination.builder()
                                                 .property("Name", "mydestination")
                                                 .property("URL", "https://vhmotqs4ci.sap.monbat.com:44300/sap/opu/odata/sap/API_PRODUCTION_ORDER_2_SRV")
                                                 .property("Type", "HTTP")
                                                 .property("Authentication", "BasicAuthentication")
                                                 .property("User", "niliev")
                                                 .property("Password", "21Zaq12wsx!")
                                                 .build().asHttp();


        final List<ProductionOrder> productionOrders =
                new DefaultOPAPIPRODUCTIONORDER2SRV0001Service()
                        .getAllProductionOrder_2()
                        .select(ProductionOrder.ALL_FIELDS)
                        .filter(ProductionOrder.ORDER_IS_RELEASED.eq(""))
                        .orderBy(ProductionOrder.PLANNED_START_DATE, Order.ASC)
                        .top(200)
                        .withHeader(APIKEY_HEADER, SANDBOX_APIKEY)
                        .executeRequest(destination);

        logger.info("Found {} production orders(s).", productionOrders.size());

        return new Gson().toJson(productionOrders);
    }
}
