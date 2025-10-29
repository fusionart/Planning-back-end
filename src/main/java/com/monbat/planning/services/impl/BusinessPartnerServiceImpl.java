package com.monbat.planning.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monbat.planning.services.BusinessPartnerService;
import com.monbat.vdm.namespaces.sapicsmbusinesspartnerext.BusinessPartner;
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
import org.springframework.stereotype.Service;

import java.net.URI;

import static com.monbat.planning.controllers.constants.SapApiConstants.*;

@Service
public class BusinessPartnerServiceImpl implements BusinessPartnerService {
    private static final Logger logger = LoggerFactory.getLogger(BusinessPartnerServiceImpl.class);

    private final ObjectMapper objectMapper;

    public BusinessPartnerServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getBusinessPartnerName(String username, String password, String businessPartnerNumber) {
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

            URI uri = new URIBuilder(BUSINESS_PARTNER_URL + BUSINESS_PARTNER_MAIN_GET+ "('" + businessPartnerNumber + "')")
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
                JsonNode dNode = rootNode.path("d");

                BusinessPartner businessPartner = objectMapper.treeToValue(dNode, BusinessPartner.class);

                return businessPartner.getBusinessPartnerName();
            }
        } catch (Exception e) {
            logger.error("Error in getPlannedOrders: ", e);
            throw new RuntimeException("Error retrieving planned orders: " + e.getMessage(), e);
        }
    }
}
