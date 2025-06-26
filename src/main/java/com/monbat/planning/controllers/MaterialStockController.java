package com.monbat.planning.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monbat.vdm.namespaces.opapimaterialstocksrv.MaterialStock;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.monbat.planning.controllers.constants.SapApiConstants.*;

@RestController
@RequestMapping("/api/sap")
public class MaterialStockController implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(MaterialStockController.class);

    private final ObjectMapper objectMapper;

    public MaterialStockController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @RequestMapping(value = "/getMaterialStock", method = RequestMethod.GET, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMaterialStock(@RequestParam String username,
                                            @RequestParam String password,
                                            @RequestParam String material) {
        Base64 base64 = new Base64();
        try {
            HttpDestination destination = DefaultDestination.builder()
                    .property("Name", "mydestination")
                    .property("URL", MATERIAL_STOCK_URL)
                    .property("Type", "HTTP")
                    .property("Authentication", "BasicAuthentication")
                    .property("User", new String(base64.decode(username.getBytes())))
                    .property("Password", new String(base64.decode(password.getBytes())))
                    .property("trustAll", "true")
                    .build().asHttp();

            CloseableHttpClient httpClient = (CloseableHttpClient) HttpClientAccessor.getHttpClient(destination);

            URI uri = new URIBuilder(MATERIAL_STOCK_URL + String.format(MATERIAL_STOCK_ITEM_GET, material))
                    .addParameter("$format", "json")
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

                List<MaterialStock> materialStockList = new ArrayList<>();

                for (JsonNode headerNode : resultsNode) {
                    MaterialStock header = objectMapper.treeToValue(headerNode, MaterialStock.class);
                    materialStockList.add(header);
                }

                return ResponseEntity.ok(sumQuantity(materialStockList));
            }
        } catch (Exception e) {
            logger.error("Error in getSalesOrders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    private double sumQuantity(List<MaterialStock> materialStockList) {
        double total = 0;
        for (MaterialStock materialStock : materialStockList){
            total += materialStock.getMatlWrhsStkQtyInMatlBaseUnit().doubleValue();
        }
        return total;
    }
}
