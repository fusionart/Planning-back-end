package com.monbat.planning.controllers.production_order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monbat.planning.models.production_order.ProductionOrderDto;
import com.monbat.planning.models.production_order.ProductionOrderWrapper;
import com.monbat.planning.services.MapToProductionOrderDto;
import com.monbat.vdm.namespaces.opapiproductionorder2srv0001.ProductionOrderComponents;
import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.exception.DestinationAccessException;
import com.sap.cloud.sdk.datamodel.odata.client.exception.ODataException;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import static com.monbat.planning.controllers.constants.SapApiConstants.*;

@RestController
@RequestMapping("/api/sap")
public class ProductionOrderController implements Serializable {
    @Autowired
    private MapToProductionOrderDto mapToProductionOrderDto;

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ProductionOrderController.class);

    @RequestMapping( value = "/getProductionOrders", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE )
    public ResponseEntity<?> getProductionOrders(@RequestParam String username,
                                                @RequestParam String password,
                                                @RequestParam LocalDateTime reqDelDateBegin,
                                                @RequestParam LocalDateTime reqDelDateEnd) {

        Base64 base64 = new Base64();
        try {
            HttpDestination destination = DefaultDestination.builder()
                    .property("Name", "mydestination")
                    .property("URL", PRODUCTION_ORDER_URL)
                    .property("Type", "HTTP")
                    .property("Authentication", "BasicAuthentication")
                    .property("User", new String(base64.decode(username.getBytes())))
                    .property("Password", new String(base64.decode(password.getBytes())))
                    .property("trustAll", "true")
                    .build().asHttp();

            // Step 2: Create an HTTP Client
            CloseableHttpClient httpClient = (CloseableHttpClient) HttpClientAccessor.getHttpClient(destination);

            URI uri = new URIBuilder(PRODUCTION_ORDER_URL + PRODUCTION_ORDER_MAIN_GET)
                    .addParameter("$format", "json")
//                    .addParameter("$top", "500")
                    .addParameter("$expand", "to_ProductionOrderOperation")
                    .addParameter("$filter", "MfgOrderScheduledStartDate gt datetime'" + reqDelDateBegin + "'")
                    .addParameter("sap-client", SAP_CLIENT)
                    .build();

            // Step 3: Execute the Request
            HttpGet request = new HttpGet(uri);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode != 200) {
                    System.err.println("Failed to retrieve data. Status code: " + statusCode);
                    return ResponseEntity.status(statusCode).body("Error: " + statusCode);
                }

                // Convert response entity to JSON string
                String jsonResponse = EntityUtils.toString(response.getEntity());

                // Deserialize JSON using ObjectMapper
                ObjectMapper objectMapper = new ObjectMapper();
                ProductionOrderWrapper ordersWrapper = objectMapper.readValue(jsonResponse, ProductionOrderWrapper.class);

                // Extract the list of production orders
                List<ProductionOrderComponents> ordersList = ordersWrapper.getD().getResults();

                List<ProductionOrderDto> productionOrderDtoList = mapToProductionOrderDto.productionOrderList(ordersList);

                return ResponseEntity.ok(productionOrderDtoList);
            }
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
