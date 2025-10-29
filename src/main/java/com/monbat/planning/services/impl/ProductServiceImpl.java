package com.monbat.planning.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monbat.planning.services.ProductService;
import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.net.URI;

import static com.monbat.planning.controllers.constants.SapApiConstants.*;

@Service
public class ProductServiceImpl implements ProductService {
    @Override
    public String getProductDescription(String username, String password, String material) {
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

            URI uri = new URIBuilder(PRODUCT_URL + PRODUCT_MAIN_GET+ "('" + material + "')")
                    .addParameter("$format", "json")
                    .addParameter("$expand", "to_Description")
                    .addParameter("sap-client", SAP_CLIENT)
                    .build();

            HttpGet request = new HttpGet(uri);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode != 200) {
                    throw new RuntimeException("Failed to retrieve production orders. Status code: " + statusCode);
                }

                String jsonResponse = EntityUtils.toString(response.getEntity());
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(jsonResponse);
                JsonNode dNode = rootNode.path("d");

                JsonNode descriptionsNode = dNode.path("to_Description").path("results");
                String bgDescription = "";

                for (JsonNode descNode : descriptionsNode) {
                    if ("BG".equals(descNode.path("Language").asText())) {
                        bgDescription = descNode.path("ProductDescription").asText();
                        break;
                    }
                }

                return bgDescription;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving production orders: " + e.getMessage(), e);
        }
    }
}
