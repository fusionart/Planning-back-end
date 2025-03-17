package com.monbat.planning.controllers.production_order;

import com.google.gson.Gson;
import com.monbat.planning.services.utils.SSLUtils;
import com.monbat.vdm.namespaces.opapiproductionorder2srv0001.ProductionOrder;
import com.monbat.vdm.services.DefaultOPAPIPRODUCTIONORDER2SRV0001Service;
import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.datamodel.odata.client.exception.ODataException;

import com.sap.cloud.sdk.cloudplatform.connectivity.exception.DestinationAccessException;
import com.sap.cloud.sdk.datamodel.odata.helper.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    private static final String URL = "https://vhmotqs4ci.sap.monbat" +
            ".com:44300/sap/opu/odata/sap/API_PRODUCTION_ORDER_2_SRV";
    private static final String USER_NAME = "niliev";
    private static final String PASSWORD = "21Zaq12wsx!";


    // TODO: uncomment the lines below and insert your API key, if you are using the sandbox service
//    private static final String APIKEY_HEADER = "apikey";
//    private static final String SANDBOX_APIKEY = "";

    @RequestMapping( value = "/getProductionOrders", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE )
    public ResponseEntity<?> getProductionOrders() {

        // DESTINATION 3:  Destination to a SAP S/4HANA Cloud (public edition) tenant
        // Uncomment this section to test with actual SAP S/4HANA Cloud
        try {
            final HttpDestination destination = DefaultDestination.builder()
                    .property("Name", "mydestination")
                    .property("URL", URL)
                    .property("Type", "HTTP")
                    .property("Authentication", "BasicAuthentication")
                    .property("User", USER_NAME)
                    .property("Password", PASSWORD)
                    .property("trustAll", "true")
                    .build().asHttp();

            final List<ProductionOrder> productionOrders =
                    new DefaultOPAPIPRODUCTIONORDER2SRV0001Service()
                            .getAllProductionOrder_2()
                            .select(ProductionOrder.ALL_FIELDS)
                            .filter(ProductionOrder.ORDER_IS_RELEASED.eq(""))
                            .orderBy(ProductionOrder.PLANNED_START_DATE, Order.ASC)
                            .top(200)
                            // TODO: uncomment the line below, if you are using the sandbox service
//                        .withHeader(APIKEY_HEADER, SANDBOX_APIKEY)
                            .executeRequest(destination);

            logger.info("Found {} production orders(s).", productionOrders.size());

            return ResponseEntity.ok( new Gson().toJson(productionOrders));
        } catch (final DestinationAccessException e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to fetch destination.");
        } catch (final ODataException e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to fetch production orders.");
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Unexpected error occurred while fetching production orders.");
        }
    }
}
