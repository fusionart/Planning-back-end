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
import org.apache.http.client.utils.URIBuilder;
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
}
