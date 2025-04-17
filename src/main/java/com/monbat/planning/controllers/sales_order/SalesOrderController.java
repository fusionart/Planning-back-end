package com.monbat.planning.controllers.sales_order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.monbat.planning.services.MapToSalesOrderDto;
import com.monbat.vdm.namespaces.apisalesordersrv.SalesOrderHeader;
import com.monbat.vdm.namespaces.apisalesordersrv.SalesOrderItem;
import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.monbat.planning.controllers.constants.SapApiConstants.*;

@RestController
@RequestMapping("/api/sap")
public class SalesOrderController implements Serializable {
    @Autowired
    MapToSalesOrderDto mapToSalesOrderDto;

    private static final Logger logger = LoggerFactory.getLogger(SalesOrderController.class);

    private final ObjectMapper objectMapper;

    public SalesOrderController() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.addMixIn(SalesOrderHeader.class, SalesOrderHeaderMixIn.class);
        this.objectMapper.addMixIn(SalesOrderItem.class, SalesOrderItemMixIn.class);
    }

    @RequestMapping(value = "/getSalesOrders", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSalesOrders() {
        try {
            HttpDestination destination = DefaultDestination.builder()
                    .property("Name", "mydestination")
                    .property("URL", SALES_ORDER_URL)
                    .property("Type", "HTTP")
                    .property("Authentication", "BasicAuthentication")
                    .property("User", USER_NAME)
                    .property("Password", PASSWORD)
                    .property("trustAll", "true")
                    .build().asHttp();

            CloseableHttpClient httpClient = (CloseableHttpClient) HttpClientAccessor.getHttpClient(destination);

            URI uri = new URIBuilder(SALES_ORDER_URL + SALES_ORDER_MAIN_GET)
                    .addParameter("$format", "json")
//                    .addParameter("$top", "20")
                    .addParameter("$expand", "to_Item")
                    .addParameter("$filter", "OverallTotalDeliveryStatus eq 'A' and " +
                            "RequestedDeliveryDate gt datetime'2025-01-01T00:00:00'")
                    .addParameter("sap-client", "200")
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

                for (JsonNode headerNode : resultsNode) {
                    // Handle to_Item if it has results
                    if (headerNode.has("to_Item") && headerNode.get("to_Item").has("results")) {
                        JsonNode itemsNode = headerNode.get("to_Item").get("results");
                        List<SalesOrderItem> items = objectMapper.readValue(
                                objectMapper.treeAsTokens(itemsNode),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, SalesOrderItem.class)
                        );

                        // Create a modified header node without to_Item
                        ObjectNode modifiedHeaderNode = objectMapper.createObjectNode();
                        headerNode.fields().forEachRemaining(field -> {
                            if (!field.getKey().equals("to_Item")) {
                                modifiedHeaderNode.set(field.getKey(), field.getValue());
                            }
                        });

                        // Deserialize the header
                        SalesOrderHeader header = objectMapper.treeToValue(modifiedHeaderNode, SalesOrderHeader.class);

                        // Set items using reflection
                        try {
                            Field toItemField = SalesOrderHeader.class.getDeclaredField("toItem");
                            toItemField.setAccessible(true);
                            toItemField.set(header, items);
                        } catch (Exception e) {
                            logger.warn("Failed to set toItem via reflection", e);
                        }

                        ordersList.add(header);
                    } else {
                        // If no items, just deserialize normally
                        SalesOrderHeader header = objectMapper.treeToValue(headerNode, SalesOrderHeader.class);
                        ordersList.add(header);
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
