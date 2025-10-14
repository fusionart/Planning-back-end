package com.monbat.planning.services.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monbat.planning.models.production_order.ProductionOrderDto;
import com.monbat.planning.models.production_order.ProductionOrderWrapper;
import com.monbat.planning.services.MapToProductionOrderDto;
import com.monbat.planning.services.PlannedOrderService;
import com.monbat.planning.services.ProductionOrderService;
import com.monbat.vdm.namespaces.opapiproductionorder2srv0001.ProductionOrderComponents;
import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.monbat.planning.controllers.constants.SapApiConstants.*;

@Service
public class ProductionOrderServiceImpl implements ProductionOrderService {

    @Autowired
    private MapToProductionOrderDto mapToProductionOrderDto;
    @Autowired
    private PlannedOrderService plannedOrderService;

    private static final Logger logger = LoggerFactory.getLogger(ProductionOrderServiceImpl.class);

    private final Base64 base64 = new Base64();
    private final ObjectMapper objectMapper;

    public ProductionOrderServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ProductionOrderDto> getProductionOrders(String username, String password,
                                                        LocalDateTime reqDelDateBegin,
                                                        LocalDateTime reqDelDateEnd) {

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

            URI uri = new URIBuilder(PRODUCTION_ORDER_URL + PRODUCTION_ORDER_MAIN_GET)
                    .addParameter("$format", "json")
                    .addParameter("$expand", "to_ProductionOrderOperation")
                    .addParameter("$filter", "MfgOrderScheduledStartDate gt datetime'" + reqDelDateBegin + "'")
                    .addParameter("sap-client", SAP_CLIENT)
                    .build();

            HttpGet request = new HttpGet(uri);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode != 200) {
                    logger.error("Failed to retrieve production orders. Status code: {}", Optional.of(statusCode));
                    throw new RuntimeException("Failed to retrieve production orders. Status code: " + statusCode);
                }

                String jsonResponse = EntityUtils.toString(response.getEntity());
                ObjectMapper objectMapper = new ObjectMapper();
                ProductionOrderWrapper ordersWrapper = objectMapper.readValue(jsonResponse, ProductionOrderWrapper.class);
                List<ProductionOrderComponents> ordersList = ordersWrapper.getD().getResults();

                return mapToProductionOrderDto.productionOrderList(ordersList);
            }
        } catch (Exception e) {
            logger.error("Error in getProductionOrders: ", e);
            throw new RuntimeException("Error retrieving production orders: " + e.getMessage(), e);
        }
    }

    @Override
    public void convertPlannedOrder(String username, String password, String plannedOrder,
                                    String manufacturingOrderType) {
        Base64 base64 = new Base64();
        CloseableHttpClient httpClient = null;

        try {
            String decodedUsername = new String(base64.decode(username.getBytes()));
            String decodedPassword = new String(base64.decode(password.getBytes()));

            HttpDestination destination = DefaultDestination.builder()
                    .property("Name", "mydestination")
                    .property("URL", PRODUCTION_ORDER_URL)
                    .property("Type", "HTTP")
                    .property("Authentication", "BasicAuthentication")
                    .property("User", decodedUsername)
                    .property("Password", decodedPassword)
                    .property("TrustAll", "true")
                    .build().asHttp();

            httpClient = (CloseableHttpClient) HttpClientAccessor.getHttpClient(destination);

            String ifMatchValue = null;
            try {
                ifMatchValue = this.plannedOrderService.getPlannedOrder(username, password, plannedOrder).getEtag();
            } catch (Exception e) {
                logger.warn("Could not fetch ETag for planned order {}: {}", plannedOrder, e.getMessage());
            }

            String csrfToken = fetchCSRFToken(httpClient, decodedUsername, decodedPassword);

            URI uri = new URIBuilder(PRODUCTION_ORDER_URL + CONVERT_PLANNED_ORDER)
                    .addParameter("PlannedOrder", "'" + plannedOrder + "'")
                    .addParameter("sap-client", SAP_CLIENT)
                    .build();

            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            request.setHeader("X-CSRF-Token", csrfToken);

            // Add Basic Auth header
            String auth = decodedUsername + ":" + decodedPassword;
            String encodedAuth = new String(base64.encode(auth.getBytes(StandardCharsets.UTF_8)));
            request.setHeader("Authorization", "Basic " + encodedAuth);

            // Add ETag if available (fetched BEFORE conversion)
            if (ifMatchValue != null && !ifMatchValue.isEmpty()) {
                request.setHeader("If-Match", ifMatchValue);
            }

            // Create request body (empty or with ManufacturingOrderType if needed)
            JSONObject requestBody = new JSONObject();
            if (manufacturingOrderType != null && !manufacturingOrderType.isEmpty()) {
                // Note: Check SAP documentation if ManufacturingOrderType should be in body or URL parameter
                // For some APIs it might need to be added as URL parameter instead
                requestBody.put("ManufacturingOrderType", manufacturingOrderType);
            }

            StringEntity entity = new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON);
            request.setEntity(entity);

            // Execute conversion request
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity responseEntity = response.getEntity();
                String responseBody = responseEntity != null ?
                        EntityUtils.toString(responseEntity) : "No response body";

                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode >= 200 && statusCode < 300) {
                    logger.info("Planned order {} successfully converted to manufacturing order type {}",
                            plannedOrder, manufacturingOrderType);
                    logger.debug("Response: {}", responseBody);

                    // Parse and extract the created production order number
                    try {
                        JSONObject responseJson = new JSONObject(responseBody);
                        String productionOrder = null;

                        // Try different response formats
                        if (responseJson.has("d")) {
                            JSONObject data = responseJson.getJSONObject("d");
                            if (data.has("ManufacturingOrder")) {
                                productionOrder = data.getString("ManufacturingOrder");
                            } else if (data.has("ProductionOrder")) {
                                productionOrder = data.getString("ProductionOrder");
                            }
                        } else if (responseJson.has("ManufacturingOrder")) {
                            productionOrder = responseJson.getString("ManufacturingOrder");
                        } else if (responseJson.has("ProductionOrder")) {
                            productionOrder = responseJson.getString("ProductionOrder");
                        }

                        if (productionOrder != null && !productionOrder.isEmpty()) {
                            logger.info("Successfully created Production Order: {}", productionOrder);
                        } else {
                            logger.info("Conversion successful but could not extract production order number from response");
                        }
                    } catch (Exception e) {
                        logger.warn("Could not parse production order from response: {}", e.getMessage());
                    }
                } else {
                    logger.error("Failed to convert planned order. Status code: {}", Optional.of(statusCode));
                    logger.error("Response body: {}", responseBody);
                    throw new RuntimeException("Failed to convert planned order. Status code: " + statusCode +
                            ". Response: " + responseBody);
                }
            }
        } catch (Exception e) {
            logger.error("Error in convertPlannedOrder: ", e);
            throw new RuntimeException("Error converting planned order: " + e.getMessage(), e);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    logger.warn("Error closing HTTP client", e);
                }
            }
        }
    }

    @Override
    public void updateProductionOrder(String username, String password, String manufacturingOrder) {
        Base64 base64 = new Base64();
        CloseableHttpClient httpClient = null;

        try {
            String decodedUsername = new String(base64.decode(username.getBytes()));
            String decodedPassword = new String(base64.decode(password.getBytes()));

            HttpDestination destination = DefaultDestination.builder()
                    .property("Name", "mydestination")
                    .property("URL", PRODUCTION_ORDER_URL)
                    .property("Type", "HTTP")
                    .property("Authentication", "BasicAuthentication")
                    .property("User", decodedUsername)
                    .property("Password", decodedPassword)
                    .property("TrustAll", "true")
                    .build().asHttp();

            httpClient = (CloseableHttpClient) HttpClientAccessor.getHttpClient(destination);

            // Just schedule the operations - skip all PATCH operations
            scheduleProductionOrderOperations(username, password, manufacturingOrder,
                    httpClient, decodedUsername, decodedPassword);

            logger.info("Production order {} operations successfully scheduled", manufacturingOrder);

        } catch (Exception e) {
            logger.error("Error in updateProductionOrder: ", e);
            throw new RuntimeException("Error updating production order: " + e.getMessage(), e);
        } finally {
            if (httpClient != null) {
                try {
                    //httpClient.close();
                } catch (Exception e) {
                    logger.warn("Error closing HTTP client", e);
                }
            }
        }
    }

    private void scheduleProductionOrderOperations(String username, String password,
                                                   String manufacturingOrder,
                                                   CloseableHttpClient httpClient,
                                                   String decodedUsername,
                                                   String decodedPassword) {
        try {
            String operationsUrl = PRODUCTION_ORDER_URL + "/A_ProductionOrder_2('" + manufacturingOrder + "')" +
                    "/to_ProductionOrderOperation?sap-client=" + SAP_CLIENT;

            HttpGet getRequest = new HttpGet(operationsUrl);
            getRequest.setHeader("Accept", "application/json");

            Base64 base64 = new Base64();
            String auth = decodedUsername + ":" + decodedPassword;
            String encodedAuth = new String(base64.encode(auth.getBytes(StandardCharsets.UTF_8)));
            getRequest.setHeader("Authorization", "Basic " + encodedAuth);

            try (CloseableHttpResponse getResponse = httpClient.execute(getRequest)) {
                String responseBody = EntityUtils.toString(getResponse.getEntity());
                JSONObject responseJson = new JSONObject(responseBody);
                JSONArray results = responseJson.getJSONObject("d").getJSONArray("results");

                String csrfToken = fetchCSRFToken(httpClient, decodedUsername, decodedPassword);

                for (int i = 0; i < results.length(); i++) {
                    JSONObject operation = results.getJSONObject(i);
                    String orderInternalBillOfOperations = operation.getString("OrderInternalBillOfOperations");
                    String orderIntBillOfOperationsItem = operation.getString("OrderIntBillOfOperationsItem");
                    String operationNumber = operation.getString("ManufacturingOrderOperation");

                    // Get ETag from metadata
                    String etag = null;
                    if (operation.has("__metadata")) {
                        JSONObject metadata = operation.getJSONObject("__metadata");
                        if (metadata.has("etag")) {
                            etag = metadata.getString("etag");
                        }
                    }

                    String currentScheduledStatus = operation.optString("OperationIsScheduled", "");

                    if ("X".equals(currentScheduledStatus)) {
                        logger.info("Operation {} is already scheduled", operationNumber);
                        continue;
                    }

                    // Prepare dates
                    LocalDateTime scheduledStartDateTime = LocalDateTime.now().plusHours(1);
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String startDate = scheduledStartDateTime.format(dateFormatter);
                    String startTime = String.format("PT%02dH%02dM%02dS",
                            scheduledStartDateTime.getHour(),
                            scheduledStartDateTime.getMinute(),
                            scheduledStartDateTime.getSecond());

                    String scheduleFunctionUrl = PRODUCTION_ORDER_URL + "/ScheduleProductionOrderOperation" +
                            "?ManufacturingOrder='" + manufacturingOrder + "'" +
                            "&OrderInternalBillOfOperations='" + orderInternalBillOfOperations + "'" +
                            "&OrderIntBillOfOperationsItem='" + orderIntBillOfOperationsItem + "'" +
                            "&OpSchedldStartDate=datetime'" + startDate + "T00:00:00'" +
                            "&OpSchedldStartTime=time'" + startTime + "'" +
                            "&OpSchedulingMode='F'" +
                            "&OpSchedulingStrategy='M'" +
                            "&OpSchedulingStatus='DISP'" +
                            "&sap-client=" + SAP_CLIENT;

                    HttpPost postRequest = new HttpPost(scheduleFunctionUrl);
                    postRequest.setHeader("Accept", "application/json");
                    postRequest.setHeader("X-CSRF-Token", csrfToken);
                    postRequest.setHeader("Authorization", "Basic " + encodedAuth);

                    // CRITICAL: Add If-Match header with ETag
                    if (etag != null && !etag.isEmpty()) {
                        postRequest.setHeader("If-Match", etag);
                    } else {
                        postRequest.setHeader("If-Match", "*");
                    }

                    logger.info("Scheduling operation {} for order {}", operationNumber, manufacturingOrder);
                    logger.debug("Using ETag: {}", etag);

                    try (CloseableHttpResponse scheduleResponse = httpClient.execute(postRequest)) {
                        int statusCode = scheduleResponse.getStatusLine().getStatusCode();
                        String scheduleResponseBody = EntityUtils.toString(scheduleResponse.getEntity());

                        System.out.println("Schedule Response Status: " + statusCode);
                        System.out.println("Schedule Response Body: " + scheduleResponseBody);

                        if (statusCode >= 200 && statusCode < 300) {
                            logger.info("Operation {} successfully scheduled - OperationIsScheduled set to X",
                                    operationNumber);
                        } else {
                            logger.error("Failed to schedule operation {}. Status: {}, Response: {}",
                                    operationNumber, statusCode, scheduleResponseBody);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error scheduling production order operations: ", e);
            throw new RuntimeException("Error scheduling production order operations: " + e.getMessage(), e);
        }
    }

    private void checkAvailableFunctions(CloseableHttpClient httpClient,
                                         String decodedUsername,
                                         String decodedPassword) {
        try {
            String metadataUrl = PRODUCTION_ORDER_URL + "/$metadata";

            HttpGet getRequest = new HttpGet(metadataUrl);
            getRequest.setHeader("Accept", "application/xml");

            Base64 base64 = new Base64();
            String auth = decodedUsername + ":" + decodedPassword;
            String encodedAuth = new String(base64.encode(auth.getBytes(StandardCharsets.UTF_8)));
            getRequest.setHeader("Authorization", "Basic " + encodedAuth);

            try (CloseableHttpResponse response = httpClient.execute(getRequest)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("Available functions:");
                // Look for FunctionImport tags related to scheduling
                System.out.println(responseBody);
            }
        } catch (Exception e) {
            logger.error("Error fetching metadata: ", e);
        }
    }

    @Override
    public ProductionOrderDto getProductionOrder(String username, String password, String productionOrder) {
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

            URI uri = new URIBuilder(PRODUCTION_ORDER_URL + PRODUCTION_ORDER_MAIN_GET + "('" + productionOrder + "')")
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

                List<ProductionOrderComponents> ordersList = new ArrayList<>();

                String jsonResponse = EntityUtils.toString(response.getEntity());
                JsonNode rootNode = objectMapper.readTree(jsonResponse);
                JsonNode resultsNode = rootNode.path("d");

                objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

                if (resultsNode.isArray()) {
                    for (JsonNode headerNode : resultsNode) {
                        ProductionOrderComponents header = objectMapper.treeToValue(headerNode, ProductionOrderComponents.class);
                        ordersList.add(header);
                    }
                } else {
                    ProductionOrderComponents header = objectMapper.treeToValue(resultsNode, ProductionOrderComponents.class);
                    ordersList.add(header);
                }

                return this.mapToProductionOrderDto.productionOrder(ordersList);
            }
        } catch (Exception e) {
            logger.error("Error in getPlannedOrders: ", e);
            throw new RuntimeException("Error retrieving planned orders: " + e.getMessage(), e);
        }
    }

    private String fetchCSRFToken(CloseableHttpClient httpClient, String username, String password) {
        try {
            URI uri = new URIBuilder(PLANNED_ORDER_URL + "/A_PlannedOrder")
                    .addParameter("sap-client", SAP_CLIENT)
                    .addParameter("$top", "1")
                    .build();

            HttpGet request = new HttpGet(uri);
            request.setHeader("X-CSRF-Token", "Fetch");
            request.setHeader("Accept", "application/json");

            // Add Basic Auth
            Base64 base64 = new Base64();
            String auth = username + ":" + password;
            String encodedAuth = new String(base64.encode(auth.getBytes(StandardCharsets.UTF_8)));
            request.setHeader("Authorization", "Basic " + encodedAuth);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String csrfToken = null;
                if (response.getFirstHeader("X-CSRF-Token") != null) {
                    csrfToken = response.getFirstHeader("X-CSRF-Token").getValue();
                }

                if (csrfToken == null || csrfToken.isEmpty()) {
                    throw new RuntimeException("Failed to fetch CSRF token");
                }

                logger.debug("CSRF Token fetched successfully");
                return csrfToken;
            }
        } catch (Exception e) {
            logger.error("Error fetching CSRF token: ", e);
            throw new RuntimeException("Failed to fetch CSRF token", e);
        }
    }

    private String formatDate(LocalDate date) {
        return date.toString(); // LocalDate.toString() returns ISO-8601 format (YYYY-MM-DD)
    }

    private String formatTime(LocalTime time) {
        return String.format("PT%02dH%02dM%02dS",
                time.getHour(),
                time.getMinute(),
                time.getSecond());
    }
}
