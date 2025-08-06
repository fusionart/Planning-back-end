package com.monbat.planning.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monbat.planning.models.production_order.ProductionOrderDto;
import com.monbat.planning.models.production_order.ProductionOrderWrapper;
import com.monbat.planning.services.MapToProductionOrderDto;
import com.monbat.planning.services.ProductionOrderService;
import com.monbat.vdm.namespaces.opapiproductionorder2srv0001.ProductionOrderComponents;
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
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import static com.monbat.planning.controllers.constants.SapApiConstants.*;

@Service
public class ProductionOrderServiceImpl implements ProductionOrderService {

    @Autowired
    private MapToProductionOrderDto mapToProductionOrderDto;

    private static final Logger logger = LoggerFactory.getLogger(ProductionOrderServiceImpl.class);

    @Override
    public List<ProductionOrderDto> getProductionOrders(String username, String password,
                                                        LocalDateTime reqDelDateBegin,
                                                        LocalDateTime reqDelDateEnd) {
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
                    logger.error("Failed to retrieve production orders. Status code: {}", statusCode);
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
}
