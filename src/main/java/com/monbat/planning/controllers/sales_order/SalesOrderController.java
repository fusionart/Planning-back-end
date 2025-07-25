package com.monbat.planning.controllers.sales_order;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monbat.planning.services.MapToSalesOrderDto;
import com.monbat.planning.services.utils.ODataModule;
import com.monbat.vdm.namespaces.opapisalesordersrv0001.SalesOrderHeader;
import com.monbat.vdm.namespaces.opapisalesordersrv0001.SalesOrderItem;
import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.monbat.planning.controllers.constants.SapApiConstants.*;

@RestController
@RequestMapping("/api/sap")
public class SalesOrderController implements Serializable {
    @Autowired
    MapToSalesOrderDto mapToSalesOrderDto;

    private static final Logger logger = LoggerFactory.getLogger(SalesOrderController.class);

    private final ObjectMapper objectMapper;

    public SalesOrderController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @RequestMapping(value = "/getSalesOrders", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSalesOrders(@RequestParam String username,
                                            @RequestParam String password,
                                            @RequestParam LocalDateTime reqDelDateBegin,
                                            @RequestParam LocalDateTime reqDelDateEnd) {
        Base64 base64 = new Base64();
        try {
            HttpDestination destination = DefaultDestination.builder()
                    .property("Name", "mydestination")
                    .property("URL", SALES_ORDER_URL)
                    .property("Type", "HTTP")
                    .property("Authentication", "BasicAuthentication")
                    .property("User", new String(base64.decode(username.getBytes())))
                    .property("Password", new String(base64.decode(password.getBytes())))
                    .property("trustAll", "true")
                    .build().asHttp();

            CloseableHttpClient httpClient = (CloseableHttpClient) HttpClientAccessor.getHttpClient(destination);

            URI uri = new URIBuilder(SALES_ORDER_URL + SALES_ORDER_MAIN_GET)
                    .addParameter("$format", "json")
//                    .addParameter("$top", "20")
                    .addParameter("$expand", "to_Item")
                    .addParameter("$filter", "OverallTotalDeliveryStatus eq 'A' and " +
                            "RequestedDeliveryDate gt datetime'" + reqDelDateBegin + "' and " +
                            "RequestedDeliveryDate lt datetime'"+ reqDelDateEnd + "'")
                    .addParameter("sap-client", SAP_CLIENT)
                    .build();

            HttpGet request = new HttpGet(uri);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode != 200) {
                    logger.error("Failed to retrieve data. Status code: {}", statusCode);
                    return ResponseEntity.status(statusCode).body("Error: " + statusCode);
                }

                String jsonResponse = EntityUtils.toString(response.getEntity());
                JsonNode rootNode = objectMapper.readTree(jsonResponse);
                JsonNode resultsNode = rootNode.path("d").path("results");

                List<SalesOrderHeader> ordersList = new ArrayList<>();
                objectMapper.registerModule(new ODataModule());
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                for (JsonNode headerNode : resultsNode) {
                    try {
                        // Create a copy of the header node without navigation properties
                        ObjectNode modifiedHeaderNode = objectMapper.createObjectNode();
                        headerNode.fields().forEachRemaining(field -> {
                            if (!field.getKey().startsWith("to_")) {
                                modifiedHeaderNode.set(field.getKey(), field.getValue());
                            }
                        });

                        // Deserialize the main header first
                        SalesOrderHeader header = objectMapper.treeToValue(modifiedHeaderNode, SalesOrderHeader.class);

                        // Process to_Item if present
                        if (headerNode.has("to_Item")) {
                            JsonNode itemsNode = headerNode.get("to_Item");

                            if (itemsNode.has("results")) {
                                // More robust deserialization of items
                                List<SalesOrderItem> items = Arrays.asList(objectMapper.treeToValue(
                                        itemsNode.get("results"),
                                        SalesOrderItem[].class
                                ));

                                // Set items using reflection (if no setter available)
                                try {
                                    Field toItemField = SalesOrderHeader.class.getDeclaredField("toItem");
                                    toItemField.setAccessible(true);
                                    toItemField.set(header, items);
                                } catch (Exception e) {
                                    logger.warn("Failed to set toItem via reflection", e);
                                }
                            }
                            // Optional: Handle deferred case
                            else if (itemsNode.has("__deferred")) {
                                logger.debug("Deferred items found, URI: {}", itemsNode.get("__deferred").get("uri"));
                            }
                        }

                        ordersList.add(header);
                    } catch (Exception e) {
                        logger.error("Error processing order header", e);
                        // Optionally add error handling or continue to next item
                    }
                }

                return ResponseEntity.ok(mapToSalesOrderDto.salesOrderList(ordersList));
            }
        } catch (Exception e) {
            logger.error("Error in getSalesOrders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
