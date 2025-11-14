package com.monbat.planning.services.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.monbat.planning.models.dto.PlannedOrderDto;
import com.monbat.planning.services.MapToPlannedOrderDto;
import com.monbat.planning.services.PlannedOrderService;
import com.monbat.vdm.namespaces.opapiplannedorderssrv0001.PlannedOrder;
import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
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

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
                    // Normalize all navigation properties that might be objects instead of arrays
                    ObjectNode normalizedNode = (ObjectNode) headerNode.deepCopy();

                    // List of all navigation properties that might need normalization
                    String[] navigationProperties = {
                            "to_PlannedOrderCapacity",
                            "to_PlannedOrderComponent",
                            "to_PlannedOrderOperation",
                            "to_PlannedOrderHeader"  // add any other navigation properties you have
                    };

                    for (String property : navigationProperties) {
                        JsonNode navNode = normalizedNode.get(property);
                        if (navNode != null && navNode.isObject()) {
                            ArrayNode arrayNode = objectMapper.createArrayNode();
                            arrayNode.add(navNode);
                            normalizedNode.set(property, arrayNode);
                        } else if (navNode == null || navNode.isNull()) {
                            normalizedNode.set(property, objectMapper.createArrayNode());
                        }
                    }

                    PlannedOrder header = objectMapper.treeToValue(normalizedNode, PlannedOrder.class);
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
    public List<PlannedOrderDto> getPlannedOrdersByProductionSupervisor(String username, String password, String productionSupervisor, LocalDateTime reqDelDateBegin, LocalDateTime reqDelDateEnd) {
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
                    .addParameter("$filter", "ProductionSupervisor eq '" + productionSupervisor + "' and " +
                            "PlndOrderPlannedStartDate gt datetime'" + reqDelDateBegin + "' and " +
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
                    // Normalize all navigation properties that might be objects instead of arrays
                    ObjectNode normalizedNode = (ObjectNode) headerNode.deepCopy();

                    // List of all navigation properties that might need normalization
                    String[] navigationProperties = {
                            "to_PlannedOrderCapacity",
                            "to_PlannedOrderComponent",
                            "to_PlannedOrderOperation",
                            "to_PlannedOrderHeader"  // add any other navigation properties you have
                    };

                    for (String property : navigationProperties) {
                        JsonNode navNode = normalizedNode.get(property);
                        if (navNode != null && navNode.isObject()) {
                            ArrayNode arrayNode = objectMapper.createArrayNode();
                            arrayNode.add(navNode);
                            normalizedNode.set(property, arrayNode);
                        } else if (navNode == null || navNode.isNull()) {
                            normalizedNode.set(property, objectMapper.createArrayNode());
                        }
                    }

                    PlannedOrder header = objectMapper.treeToValue(normalizedNode, PlannedOrder.class);
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
                    .addParameter("$expand", "to_PlannedOrderCapacity")
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
    public void dispatchPlannedOrder(String username, String password,
                                   String plannedOrder,
                                   LocalDateTime processingStartDateTime) {
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

            // Get capacity records for the planned order
            List<CapacityRecord> capacityRecords = getCapacityRecordsForPlannedOrder(httpClient, plannedOrder, username, password);

            if (!capacityRecords.isEmpty()) {
                String csrfToken = fetchCSRFToken(httpClient,
                        new String(base64.decode(username.getBytes())),
                        new String(base64.decode(password.getBytes())));

                // Schedule each capacity record using the SchedulePlannedOrderOperation endpoint
                for (CapacityRecord record : capacityRecords) {
                    schedulePlannedOrderOperation(httpClient, record, username, password, csrfToken,
                            processingStartDateTime);
                };
            } else {
                logger.warn("No capacity records found for Planned Order: {}", plannedOrder);
            }

        } catch (Exception e) {
            logger.error("Error in updatePlannedOrder: ", e);
            throw new RuntimeException("Error updating planned order: " + e.getMessage(), e);
        }
    }

    /**
     * Schedule a planned order operation using the SchedulePlannedOrderOperation endpoint
     * All parameters must be passed as URL query parameters (not in request body)
     */
    private void schedulePlannedOrderOperation(CloseableHttpClient httpClient,
                                               CapacityRecord record,
                                               String username,
                                               String password,
                                               String csrfToken,
                                               LocalDateTime processingStartDateTime) {
        try {
            // Decode credentials
            Base64 base64 = new Base64();
            String decodedUsername = new String(base64.decode(username.getBytes()));
            String decodedPassword = new String(base64.decode(password.getBytes()));
            String auth = decodedUsername + ":" + decodedPassword;
            String encodedAuth = new String(base64.encode(auth.getBytes(StandardCharsets.UTF_8)));

            // Build URI with all required parameters in the URL
            // CRITICAL: datetime and time values should NOT have surrounding single quotes in addParameter
            URIBuilder uriBuilder = new URIBuilder(PLANNED_ORDER_URL + "/SchedulePlannedOrderOperation")
                    .addParameter("PlannedOrder", "'" + record.getPlannedOrder() + "'")
                    .addParameter("CapacityRequirement", "'" + record.getCapacityRequirement() + "'")
                    .addParameter("CapacityRequirementItem", "'" + record.getCapacityRequirementItem() + "'")
                    .addParameter("CapacityRqmtItemCapacity", "'" + record.getCapacityRqmtItemCapacity() + "'")
                    .addParameter("OpSchedulingMode", "'F'")  // F = Forward scheduling
                    .addParameter("OpSchedulingStrategy", "'M'")  // M = Manual
                    .addParameter("OpSchedulingStatus", "'DISP'") // DISP = Dispatched
                    .addParameter("sap-client", SAP_CLIENT);

            // Add optional date/time parameters if provided
            if (processingStartDateTime != null) {
                // Format: datetime'2020-06-04T00:00:00' - the datetime prefix is part of the value
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                String startDateTime = processingStartDateTime.format(dateTimeFormatter);
                uriBuilder.addParameter("OpSchedldStartDate", "datetime'" + startDateTime + "'");

                // Format: time'PT08H00M00S' - the time prefix is part of the value
                String startTime = formatTimeForSAPDuration(processingStartDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                uriBuilder.addParameter("OpSchedldStartTime", "time'PT" + startTime + "'");
            }

            URI uri = uriBuilder.build();
            logger.debug("Scheduling planned order operation with URI: {}", uri);

            // Create POST request (function imports use POST)
            HttpPost postRequest = new HttpPost(uri);

            // Set required headers
            postRequest.setHeader("Content-Type", "application/json");
            postRequest.setHeader("Accept", "application/json");
            postRequest.setHeader("X-CSRF-Token", csrfToken);
            postRequest.setHeader("Authorization", "Basic " + encodedAuth);

            // Add If-Match header with ETag for optimistic locking (if available)
            if (StringUtils.isNotBlank(record.getEtag())) {
                postRequest.setHeader("If-Match", record.getEtag());
            }

            // Execute the request (no body needed - all params in URL)
            try (CloseableHttpResponse postResponse = httpClient.execute(postRequest)) {
                int statusCode = postResponse.getStatusLine().getStatusCode();

                if (statusCode >= 200 && statusCode < 300) {

                } else {
                    String errorResponse = EntityUtils.toString(postResponse.getEntity());

                    throw new RuntimeException("Failed to " + " capacity record. Status: " + statusCode + ", Response: " + errorResponse);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error " + " capacity record: " + e.getMessage(), e);
        }
    }

    @Override
    public void deallocatePlannedOrder(String username, String password,
                                     String plannedOrder,
                                     LocalDateTime processingStartDateTime) {
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

            // Get capacity records for the planned order
            List<CapacityRecord> capacityRecords = getCapacityRecordsForPlannedOrder(httpClient, plannedOrder, username, password);

            if (!capacityRecords.isEmpty()) {
                String csrfToken = fetchCSRFToken(httpClient,
                        new String(base64.decode(username.getBytes())),
                        new String(base64.decode(password.getBytes())));

                // Schedule each capacity record using the SchedulePlannedOrderOperation endpoint
                for (CapacityRecord record : capacityRecords) {
                    deallocatePlannedOrderOperation(httpClient, record, username, password, csrfToken,
                            processingStartDateTime);
                };
            } else {
                logger.warn("No capacity records found for Planned Order: {}", plannedOrder);
            }

        } catch (Exception e) {
            logger.error("Error in updatePlannedOrder: ", e);
            throw new RuntimeException("Error updating planned order: " + e.getMessage(), e);
        }
    }

    private void deallocatePlannedOrderOperation(CloseableHttpClient httpClient,
                                               CapacityRecord record,
                                               String username,
                                               String password,
                                               String csrfToken,
                                               LocalDateTime processingStartDateTime) {
        try {
            // Decode credentials
            Base64 base64 = new Base64();
            String decodedUsername = new String(base64.decode(username.getBytes()));
            String decodedPassword = new String(base64.decode(password.getBytes()));
            String auth = decodedUsername + ":" + decodedPassword;
            String encodedAuth = new String(base64.encode(auth.getBytes(StandardCharsets.UTF_8)));

            // Build URI with all required parameters in the URL
            // CRITICAL: datetime and time values should NOT have surrounding single quotes in addParameter
            URIBuilder uriBuilder = new URIBuilder(PLANNED_ORDER_URL + "/SchedulePlannedOrderOperation")
                    .addParameter("PlannedOrder", "'" + record.getPlannedOrder() + "'")
                    .addParameter("CapacityRequirement", "'" + record.getCapacityRequirement() + "'")
                    .addParameter("CapacityRequirementItem", "'" + record.getCapacityRequirementItem() + "'")
                    .addParameter("CapacityRqmtItemCapacity", "'" + record.getCapacityRqmtItemCapacity() + "'")
                    .addParameter("OpSchedulingMode", "'F'")  // F = Forward scheduling
                    .addParameter("OpSchedulingStrategy", "'M'")  // M = Manual
                    .addParameter("OpSchedulingStatus", "'DEA'") // DEA = Deallocated
                    .addParameter("sap-client", SAP_CLIENT);

            // Add optional date/time parameters if provided
            if (processingStartDateTime != null) {
                // Format: datetime'2020-06-04T00:00:00' - the datetime prefix is part of the value
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                String startDateTime = processingStartDateTime.format(dateTimeFormatter);
                uriBuilder.addParameter("OpSchedldStartDate", "datetime'" + startDateTime + "'");

                // Format: time'PT08H00M00S' - the time prefix is part of the value
                String startTime = formatTimeForSAPDuration(processingStartDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                uriBuilder.addParameter("OpSchedldStartTime", "time'PT" + startTime + "'");
            }

            URI uri = uriBuilder.build();
            logger.debug("Scheduling planned order operation with URI: {}", uri);

            // Create POST request (function imports use POST)
            HttpPost postRequest = new HttpPost(uri);

            // Set required headers
            postRequest.setHeader("Content-Type", "application/json");
            postRequest.setHeader("Accept", "application/json");
            postRequest.setHeader("X-CSRF-Token", csrfToken);
            postRequest.setHeader("Authorization", "Basic " + encodedAuth);

            // Add If-Match header with ETag for optimistic locking (if available)
            if (StringUtils.isNotBlank(record.getEtag())) {
                postRequest.setHeader("If-Match", record.getEtag());
            }

            // Execute the request (no body needed - all params in URL)
            try (CloseableHttpResponse postResponse = httpClient.execute(postRequest)) {
                int statusCode = postResponse.getStatusLine().getStatusCode();

                if (statusCode >= 200 && statusCode < 300) {

                } else {
                    String errorResponse = EntityUtils.toString(postResponse.getEntity());

                    throw new RuntimeException("Failed to " + " capacity record. Status: " + statusCode + ", Response: " + errorResponse);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error " + " capacity record: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to format time for SAP duration format (PT23H59M59S)
     * Used for OpSchedldStartTime and OpSchedldEndTime parameters
     * Returns format like: 08H00M00S (without PT prefix, that's added separately)
     */
    private String formatTimeForSAPDuration(String time) {
        try {
            String[] parts = time.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

            // Return format: 08H00M00S (PT prefix will be added when building the parameter)
            return String.format("%02dH%02dM%02dS", hours, minutes, seconds);
        } catch (Exception e) {
            logger.warn("Invalid time format: {}, using default", time);
            return "00H00M00S";
        }
    }

    /**
     * Get capacity records for a planned order
     */
    private List<CapacityRecord> getCapacityRecordsForPlannedOrder(CloseableHttpClient httpClient,
                                                                   String plannedOrder,
                                                                   String username,
                                                                   String password) {
        List<CapacityRecord> capacityRecords = new ArrayList<>();

        try {
            URI getUri = new URIBuilder(PLANNED_ORDER_URL + "/A_PlannedOrderCapacity")
                    .addParameter("$filter", "PlannedOrder eq '" + plannedOrder + "'")
                    .addParameter("$format", "json")
                    .addParameter("sap-client", SAP_CLIENT)
                    .build();

            HttpGet getRequest = new HttpGet(getUri);
            getRequest.setHeader("Accept", "application/json");

            // Add Basic Auth
            Base64 base64 = new Base64();
            String decodedUsername = new String(base64.decode(username.getBytes()));
            String decodedPassword = new String(base64.decode(password.getBytes()));
            String auth = decodedUsername + ":" + decodedPassword;
            String encodedAuth = new String(base64.encode(auth.getBytes(StandardCharsets.UTF_8)));
            getRequest.setHeader("Authorization", "Basic " + encodedAuth);

            try (CloseableHttpResponse getResponse = httpClient.execute(getRequest)) {
                int statusCode = getResponse.getStatusLine().getStatusCode();

                if (statusCode != 200) {
                    String errorResponse = EntityUtils.toString(getResponse.getEntity());
                    logger.error("Failed to retrieve capacity records. Status code: {}, Response: {}",
                            statusCode, errorResponse);
                    throw new RuntimeException("Failed to retrieve capacity records. Status code: " + statusCode);
                }

                String jsonResponse = EntityUtils.toString(getResponse.getEntity());
                JSONObject responseJson = new JSONObject(jsonResponse);
                JSONArray results = responseJson.getJSONObject("d").getJSONArray("results");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject capacity = results.getJSONObject(i);
                    CapacityRecord record = new CapacityRecord();
                    record.setPlannedOrder(plannedOrder);
                    record.setCapacityRequirement(capacity.getString("CapacityRequirement"));
                    record.setCapacityRequirementItem(capacity.getString("CapacityRequirementItem"));
                    record.setCapacityRqmtItemCapacity(capacity.getString("CapacityRqmtItemCapacity"));
                    record.setEtag(capacity.getJSONObject("__metadata").getString("etag"));

                    capacityRecords.add(record);
                }

                logger.info("Found {} capacity records for planned order: {}", capacityRecords.size(), plannedOrder);
            }
        } catch (Exception e) {
            logger.error("Error fetching capacity records for planned order {}: ", plannedOrder, e);
            throw new RuntimeException("Error fetching capacity records: " + e.getMessage(), e);
        }

        return capacityRecords;
    }

    @Override
    public void updatePlannedOrderQuantity(String username, String password, String plannedOrder,
                                           BigDecimal totalQuantity) {
        Base64 base64 = new Base64();
        CloseableHttpClient httpClient = null;

        try {
            String decodedUsername = new String(base64.decode(username.getBytes()));
            String decodedPassword = new String(base64.decode(password.getBytes()));

            HttpDestination destination = DefaultDestination.builder()
                    .property("Name", "mydestination")
                    .property("URL", PLANNED_ORDER_URL) // Assuming you have this constant
                    .property("Type", "HTTP")
                    .property("Authentication", "BasicAuthentication")
                    .property("User", decodedUsername)
                    .property("Password", decodedPassword)
                    .property("TrustAll", "true")
                    .build().asHttp();

            httpClient = (CloseableHttpClient) HttpClientAccessor.getHttpClient(destination);

            // Update planned order with new production version and total quantity
            updatePlannedOrderQuantityField(plannedOrder, httpClient, decodedUsername, decodedPassword, totalQuantity);

            logger.info("Planned order {} successfully updated", plannedOrder);

        } catch (Exception e) {
            logger.error("Error in updatePlannedOrder: ", e);
            throw new RuntimeException("Error updating planned order: " + e.getMessage(), e);
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

    @Override
    public void updatePlannedOrderProductionVersion(String username, String password, String plannedOrder, String productionVersion) {
        Base64 base64 = new Base64();
        CloseableHttpClient httpClient = null;

        try {
            String decodedUsername = new String(base64.decode(username.getBytes()));
            String decodedPassword = new String(base64.decode(password.getBytes()));

            HttpDestination destination = DefaultDestination.builder()
                    .property("Name", "mydestination")
                    .property("URL", PLANNED_ORDER_URL) // Assuming you have this constant
                    .property("Type", "HTTP")
                    .property("Authentication", "BasicAuthentication")
                    .property("User", decodedUsername)
                    .property("Password", decodedPassword)
                    .property("TrustAll", "true")
                    .build().asHttp();

            httpClient = (CloseableHttpClient) HttpClientAccessor.getHttpClient(destination);

            // Update planned order with new production version and total quantity
            updatePlannedOrderProductionVersionField(plannedOrder, httpClient, decodedUsername, decodedPassword,
                    productionVersion);

            logger.info("Planned order {} successfully updated", plannedOrder);

        } catch (Exception e) {
            logger.error("Error in updatePlannedOrder: ", e);
            throw new RuntimeException("Error updating planned order: " + e.getMessage(), e);
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

    private void updatePlannedOrderQuantityField(String plannedOrder,
                                          CloseableHttpClient httpClient,
                                          String decodedUsername,
                                          String decodedPassword,
                                          BigDecimal totalQuantity) {
        try {
            // First, read the planned order to get the ETag
            String plannedOrderUrl = PLANNED_ORDER_URL + "/A_PlannedOrder('" + plannedOrder + "')" +
                    "?sap-client=" + SAP_CLIENT;

            HttpGet getRequest = new HttpGet(plannedOrderUrl);
            getRequest.setHeader("Accept", "application/json");

            Base64 base64 = new Base64();
            String auth = decodedUsername + ":" + decodedPassword;
            String encodedAuth = new String(base64.encode(auth.getBytes(StandardCharsets.UTF_8)));
            getRequest.setHeader("Authorization", "Basic " + encodedAuth);

            //Get etag
            String etag = null;
            try (CloseableHttpResponse getResponse = httpClient.execute(getRequest)) {
                int statusCode = getResponse.getStatusLine().getStatusCode();

                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(getResponse.getEntity());
                    JSONObject plannedOrderData = new JSONObject(responseBody);

                    // Get ETag from metadata
                    if (plannedOrderData.has("d")) {
                        JSONObject data = plannedOrderData.getJSONObject("d");
                        if (data.has("__metadata")) {
                            JSONObject metadata = data.getJSONObject("__metadata");
                            if (metadata.has("etag")) {
                                etag = metadata.getString("etag");
                            }
                        }
                    }

                    logger.info("Successfully read planned order {}, ETag: {}", plannedOrder, etag);
                } else {
                    logger.error("Failed to read planned order {}. Status: {}", plannedOrder, statusCode);
                    throw new RuntimeException("Failed to read planned order: HTTP " + statusCode);
                }
            }

            // Now perform the PATCH operation to update the fields
            String csrfToken = fetchCSRFToken(httpClient, decodedUsername, decodedPassword);

            String patchUrl = PLANNED_ORDER_URL + "/A_PlannedOrder('" + plannedOrder + "')" +
                    "?sap-client=" + SAP_CLIENT;

            HttpPatch patchRequest = new HttpPatch(patchUrl);
            patchRequest.setHeader("Content-Type", "application/json");
            patchRequest.setHeader("Accept", "application/json");
            patchRequest.setHeader("X-CSRF-Token", csrfToken);
            patchRequest.setHeader("Authorization", "Basic " + encodedAuth);

            // CRITICAL: Add If-Match header with ETag from read operation
            if (etag != null && !etag.isEmpty()) {
                patchRequest.setHeader("If-Match", etag);
            } else {
                patchRequest.setHeader("If-Match", "*");
            }

            // Build the JSON payload with fields to update
            JSONObject updatePayload = new JSONObject();

            if (totalQuantity != null) {
                updatePayload.put("TotalQuantity", totalQuantity.toString());
            }

            StringEntity entity = new StringEntity(updatePayload.toString(), ContentType.APPLICATION_JSON);
            patchRequest.setEntity(entity);

            try (CloseableHttpResponse patchResponse = httpClient.execute(patchRequest)) {
                int statusCode = patchResponse.getStatusLine().getStatusCode();

                if (statusCode == 204) {
                    logger.info("Planned order {} successfully updated - Status 204 No Content", plannedOrder);
                } else {
                    String responseBody = EntityUtils.toString(patchResponse.getEntity());
                    logger.error("Failed to update planned order {}. Status: {}, Response: {}",
                            plannedOrder, statusCode, responseBody);
                    throw new RuntimeException("Failed to update planned order: HTTP " + statusCode);
                }
            }

        } catch (Exception e) {
            logger.error("Error updating planned order fields: ", e);
            throw new RuntimeException("Error updating planned order fields: " + e.getMessage(), e);
        }
    }

    private void updatePlannedOrderProductionVersionField(String plannedOrder,
                                                 CloseableHttpClient httpClient,
                                                 String decodedUsername,
                                                 String decodedPassword,
                                                 String productionVersion) {
        try {
            // First, read the planned order to get the ETag
            String plannedOrderUrl = PLANNED_ORDER_URL + "/A_PlannedOrder('" + plannedOrder + "')" +
                    "?sap-client=" + SAP_CLIENT;

            HttpGet getRequest = new HttpGet(plannedOrderUrl);
            getRequest.setHeader("Accept", "application/json");

            Base64 base64 = new Base64();
            String auth = decodedUsername + ":" + decodedPassword;
            String encodedAuth = new String(base64.encode(auth.getBytes(StandardCharsets.UTF_8)));
            getRequest.setHeader("Authorization", "Basic " + encodedAuth);

            //Get etag
            String etag = null;
            try (CloseableHttpResponse getResponse = httpClient.execute(getRequest)) {
                int statusCode = getResponse.getStatusLine().getStatusCode();

                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(getResponse.getEntity());
                    JSONObject plannedOrderData = new JSONObject(responseBody);

                    // Get ETag from metadata
                    if (plannedOrderData.has("d")) {
                        JSONObject data = plannedOrderData.getJSONObject("d");
                        if (data.has("__metadata")) {
                            JSONObject metadata = data.getJSONObject("__metadata");
                            if (metadata.has("etag")) {
                                etag = metadata.getString("etag");
                            }
                        }
                    }

                    logger.info("Successfully read planned order {}, ETag: {}", plannedOrder, etag);
                } else {
                    logger.error("Failed to read planned order {}. Status: {}", plannedOrder, statusCode);
                    throw new RuntimeException("Failed to read planned order: HTTP " + statusCode);
                }
            }

            // Now perform the PATCH operation to update the fields
            String csrfToken = fetchCSRFToken(httpClient, decodedUsername, decodedPassword);

            String patchUrl = PLANNED_ORDER_URL + "/A_PlannedOrder('" + plannedOrder + "')" +
                    "?sap-client=" + SAP_CLIENT;

            HttpPatch patchRequest = new HttpPatch(patchUrl);
            patchRequest.setHeader("Content-Type", "application/json");
            patchRequest.setHeader("Accept", "application/json");
            patchRequest.setHeader("X-CSRF-Token", csrfToken);
            patchRequest.setHeader("Authorization", "Basic " + encodedAuth);

            // CRITICAL: Add If-Match header with ETag from read operation
            if (etag != null && !etag.isEmpty()) {
                patchRequest.setHeader("If-Match", etag);
            } else {
                patchRequest.setHeader("If-Match", "*");
            }

            // Build the JSON payload with fields to update
            JSONObject updatePayload = new JSONObject();

            if (productionVersion != null && !productionVersion.trim().isEmpty()) {
                updatePayload.put("ProductionVersion", productionVersion);
            }

            StringEntity entity = new StringEntity(updatePayload.toString(), ContentType.APPLICATION_JSON);
            patchRequest.setEntity(entity);

            try (CloseableHttpResponse patchResponse = httpClient.execute(patchRequest)) {
                int statusCode = patchResponse.getStatusLine().getStatusCode();

                if (statusCode == 204) {
                    logger.info("Planned order {} successfully updated - Status 204 No Content", plannedOrder);
                } else {
                    String responseBody = EntityUtils.toString(patchResponse.getEntity());
                    logger.error("Failed to update planned order {}. Status: {}, Response: {}",
                            plannedOrder, statusCode, responseBody);
                    throw new RuntimeException("Failed to update planned order: HTTP " + statusCode);
                }
            }

        } catch (Exception e) {
            logger.error("Error updating planned order fields: ", e);
            throw new RuntimeException("Error updating planned order fields: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch CSRF token required for POST operations
     */
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

    /**
     * Capacity Record class to store capacity information
     */
    @Getter
    @Setter
    private static class CapacityRecord {
        private String plannedOrder;
        private String capacityRequirement;
        private String capacityRequirementItem;
        private String capacityRqmtItemCapacity;
        private String etag;
        private Boolean currentlyDispatched;
    }
}