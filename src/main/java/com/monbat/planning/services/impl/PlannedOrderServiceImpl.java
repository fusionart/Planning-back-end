package com.monbat.planning.services.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monbat.planning.models.dto.PlannedOrderDto;
import com.monbat.planning.services.MapToPlannedOrderDto;
import com.monbat.planning.services.PlannedOrderService;
import com.monbat.vdm.namespaces.opapiplannedorderssrv0001.PlannedOrder;
import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.monbat.planning.controllers.constants.SapApiConstants.*;

@Service
public class PlannedOrderServiceImpl implements PlannedOrderService {

    @Autowired
    private MapToPlannedOrderDto mapToPlannedOrderDto;

    private static final Logger logger = LoggerFactory.getLogger(PlannedOrderServiceImpl.class);
    private final ObjectMapper objectMapper;

    public PlannedOrderServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<PlannedOrderDto> getPlannedOrders(String username, String password,
                                                  LocalDateTime reqDelDateBegin,
                                                  LocalDateTime reqDelDateEnd) {
        Base64 base64 = new Base64();
        try {
            HttpDestination destination = DefaultDestination.builder()
                    .property("Name", "mydestination")
                    .property("URL", PLANNED_ORDER_URL)
                    .property("Type", "HTTP")
                    .property("Authentication", "BasicAuthentication")
                    .property("User", new String(base64.decode(username.getBytes())))
                    .property("Password", new String(base64.decode(password.getBytes())))
                    .property("trustAll", "true")
                    .build().asHttp();

            CloseableHttpClient httpClient = (CloseableHttpClient) HttpClientAccessor.getHttpClient(destination);

            URI uri = new URIBuilder(PLANNED_ORDER_URL + PLANNED_ORDER_MAIN_GET)
                    .addParameter("$format", "json")
                    .addParameter("$expand", "to_PlannedOrderCapacity")
                    .addParameter("$filter", "PlndOrderPlannedStartDate gt datetime'" + reqDelDateBegin + "' and " +
                            "PlndOrderPlannedEndDate lt datetime'"+ reqDelDateEnd + "'")
                    .addParameter("sap-client", SAP_CLIENT)
                    .build();

            HttpGet request = new HttpGet(uri);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode != 200) {
                    logger.error("Failed to retrieve planned orders. Status code: {}", statusCode);
                    throw new RuntimeException("Failed to retrieve planned orders. Status code: " + statusCode);
                }

                String jsonResponse = EntityUtils.toString(response.getEntity());
                JsonNode rootNode = objectMapper.readTree(jsonResponse);
                JsonNode resultsNode = rootNode.path("d").path("results");

                List<PlannedOrder> ordersList = new ArrayList<>();

                for (JsonNode headerNode : resultsNode) {
                    PlannedOrder header = objectMapper.treeToValue(headerNode, PlannedOrder.class);
                    ordersList.add(header);
                }

                return this.mapToPlannedOrderDto.getPlannedOrderList(ordersList);
            }
        } catch (Exception e) {
            logger.error("Error in getPlannedOrders: ", e);
            throw new RuntimeException("Error retrieving planned orders: " + e.getMessage(), e);
        }
    }

    @Override
    public PlannedOrderDto getPlannedOrder(String username, String password,
                                           String plannedOrder) {
        Base64 base64 = new Base64();
        try {
            HttpDestination destination = DefaultDestination.builder()
                    .property("Name", "mydestination")
                    .property("URL", PLANNED_ORDER_URL)
                    .property("Type", "HTTP")
                    .property("Authentication", "BasicAuthentication")
                    .property("User", new String(base64.decode(username.getBytes())))
                    .property("Password", new String(base64.decode(password.getBytes())))
                    .property("trustAll", "true")
                    .build().asHttp();

            String plannedOrderWithZeros = StringUtils.leftPad(plannedOrder, 10, "0");

            CloseableHttpClient httpClient = (CloseableHttpClient) HttpClientAccessor.getHttpClient(destination);

            URI uri = new URIBuilder(PLANNED_ORDER_URL + PLANNED_ORDER_MAIN_GET + "('" + plannedOrderWithZeros + "')")
                    .addParameter("$format", "json")
                    .addParameter("sap-client", SAP_CLIENT)
                    .build();

            HttpGet request = new HttpGet(uri);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode != 200) {
                    logger.error("Failed to retrieve planned orders. Status code: {}", statusCode);
                    throw new RuntimeException("Failed to retrieve planned orders. Status code: " + statusCode);
                }

                String jsonResponse = EntityUtils.toString(response.getEntity());
                JsonNode rootNode = objectMapper.readTree(jsonResponse);
                JsonNode resultsNode = rootNode.path("d");

                List<PlannedOrder> ordersList = new ArrayList<>();

                objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

                if (resultsNode.isArray()) {
                    for (JsonNode headerNode : resultsNode) {
                        PlannedOrder header = objectMapper.treeToValue(headerNode, PlannedOrder.class);
                        ordersList.add(header);
                    }
                } else {
                    PlannedOrder header = objectMapper.treeToValue(resultsNode, PlannedOrder.class);
                    ordersList.add(header);
                }

                return this.mapToPlannedOrderDto.getPlannedOrderList(ordersList).getFirst();
            }
        } catch (Exception e) {
            logger.error("Error in getPlannedOrders: ", e);
            throw new RuntimeException("Error retrieving planned orders: " + e.getMessage(), e);
        }
    }

    @Override
    public void updatePlannedOrder(String username, String password, String plannedOrder,
                                   boolean plannedOrderCapacityIsDsptchd, String opLtstSchedldProcgStrtDte,
                                   String opLtstSchedldProcgStrtTme, String opLtstSchedldTrdwnStrtDte,
                                   String opLtstSchedldTrdwnStrtTme) {
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

            CloseableHttpClient httpClient = (CloseableHttpClient) HttpClientAccessor.getHttpClient(destination);

            // First, get CSRF token and ETag by fetching the entity
            URI getUri = new URIBuilder(PLANNED_ORDER_URL + PLANNED_ORDER_MAIN_GET + "('" + plannedOrder + "')")
                    .addParameter("$format", "json")
                    .addParameter("sap-client", SAP_CLIENT)
                    .build();

            HttpGet getRequest = new HttpGet(getUri);
            getRequest.setHeader("X-CSRF-Token", "Fetch");

            String etag;
            String csrfToken;

            try (CloseableHttpResponse getResponse = httpClient.execute(getRequest)) {
                int statusCode = getResponse.getStatusLine().getStatusCode();

                if (statusCode != 200) {
                    String errorResponse = EntityUtils.toString(getResponse.getEntity());
                    logger.error("Failed to retrieve production order for update. Status code: {}, Response: {}",
                            statusCode, errorResponse);
                    throw new RuntimeException("Failed to retrieve production order. Status code: " + statusCode);
                }

                String jsonResponse = EntityUtils.toString(getResponse.getEntity());
                JsonNode rootNode = objectMapper.readTree(jsonResponse);
                JsonNode dataNode = rootNode.path("d");
                etag = dataNode.path("__metadata").path("etag").asText();

                // Get CSRF token from response header
                csrfToken = getResponse.getFirstHeader("X-CSRF-Token").getValue();

                logger.info("Retrieved ETag: {} and CSRF Token for Manufacturing Order: {}", etag, plannedOrder);
                logger.info("New values - PlannedOrderCapacityIsDsptchd: '{}', OpLtstSchedldProcgStrtDte: '{}', " +
                                "OpLtstSchedldProcgStrtTme: '{}', OpLtstSchedldTrdwnStrtDte: '{}', OpLtstSchedldTrdwnStrtTme: '{}'",
                        plannedOrderCapacityIsDsptchd, opLtstSchedldProcgStrtDte, opLtstSchedldProcgStrtTme,
                        opLtstSchedldTrdwnStrtDte, opLtstSchedldTrdwnStrtTme);
            }

            URI patchUri = new URIBuilder(PLANNED_ORDER_URL + PLANNED_ORDER_MAIN_GET + "('" + plannedOrder + "')")
                    .addParameter("sap-client", SAP_CLIENT)
                    .build();

            HttpPatch patchRequest = new HttpPatch(patchUri);
            patchRequest.setHeader("Content-Type", "application/json");
            patchRequest.setHeader("Accept", "application/json");
            patchRequest.setHeader("If-Match", etag);
            patchRequest.setHeader("X-CSRF-Token", csrfToken);
            patchRequest.setHeader("sap-client", SAP_CLIENT);

            // Build JSON payload dynamically based on non-null parameters
            StringBuilder payloadBuilder = new StringBuilder("{");
            boolean first = true;

            payloadBuilder.append(String.format("\"PlannedOrderCapacityIsDsptchd\": %s",
                    plannedOrderCapacityIsDsptchd));

            if (opLtstSchedldProcgStrtDte != null) {
                if (!first) payloadBuilder.append(", ");
                payloadBuilder.append(String.format("\"OpLtstSchedldProcgStrtDte\": \"%s\"",
                        opLtstSchedldProcgStrtDte));
                first = false;
            }
            if (opLtstSchedldProcgStrtTme != null) {
                if (!first) payloadBuilder.append(", ");
                payloadBuilder.append(String.format("\"OpLtstSchedldProcgStrtTme\": \"%s\"",
                        opLtstSchedldProcgStrtTme));
                first = false;
            }
            if (opLtstSchedldTrdwnStrtDte != null) {
                if (!first) payloadBuilder.append(", ");
                payloadBuilder.append(String.format("\"OpLtstSchedldTrdwnStrtDte\": \"%s\"",
                        opLtstSchedldTrdwnStrtDte));
                first = false;
            }
            if (opLtstSchedldTrdwnStrtTme != null) {
                if (!first) payloadBuilder.append(", ");
                payloadBuilder.append(String.format("\"OpLtstSchedldTrdwnStrtTme\": \"%s\"",
                        opLtstSchedldTrdwnStrtTme));
            }
            payloadBuilder.append("}");

            String payload = payloadBuilder.toString();
            patchRequest.setEntity(new StringEntity(payload, "UTF-8"));

            logger.info("Attempting to update Production Order fields for Manufacturing Order: {}", plannedOrder);
            logger.info("Payload: {}", payload);

            try (CloseableHttpResponse patchResponse = httpClient.execute(patchRequest)) {
                int statusCode = patchResponse.getStatusLine().getStatusCode();

                if (statusCode != 204 && statusCode != 200) {
                    String errorResponse = EntityUtils.toString(patchResponse.getEntity());
                    logger.error("Failed to update production order fields. Status code: {}, Response: {}",
                            statusCode, errorResponse);

                    if (errorResponse.contains("was not changed")) {
                        logger.warn("The order was not changed. This could mean:");
                        logger.warn("1. One or more field values are already set to the requested values");
                        logger.warn("2. The fields cannot be changed in the current order status");
                        logger.warn("3. The fields may be read-only or require different permissions");
                    }

                    throw new RuntimeException("Failed to update planned order fields. Status code: " + statusCode +
                            ", Response: " + errorResponse);
                }

                logger.info("Successfully updated Planned Order fields for Manufacturing Order '{}'", plannedOrder);
            }

        } catch (Exception e) {
            logger.error("Error in updateProductionOrderFields: ", e);
            throw new RuntimeException("Error updating planned order fields: " + e.getMessage(), e);
        }
    }
}
