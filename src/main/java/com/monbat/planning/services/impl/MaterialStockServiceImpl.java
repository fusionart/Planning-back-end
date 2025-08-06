package com.monbat.planning.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monbat.planning.services.MaterialStockService;
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
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.monbat.planning.controllers.constants.SapApiConstants.*;

@Service
public class MaterialStockServiceImpl implements MaterialStockService {
    private static final Logger logger = LoggerFactory.getLogger(MaterialStockServiceImpl.class);

    private final ObjectMapper objectMapper;

    public MaterialStockServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public double getMaterialStock(String username, String password, String material) {
        Base64 base64 = new Base64();
        List<MaterialStock> materialStockList = new ArrayList<>();
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
                String jsonResponse = EntityUtils.toString(response.getEntity());
                JsonNode rootNode = objectMapper.readTree(jsonResponse);
                JsonNode resultsNode = rootNode.path("d").path("results");

                for (JsonNode headerNode : resultsNode) {
                    MaterialStock header = objectMapper.treeToValue(headerNode, MaterialStock.class);
                    materialStockList.add(header);
                }
            }
        } catch (Exception e) {
            logger.error("Error in getSalesOrders: ", e);

        }
        return sumQuantity(materialStockList);
    }

    private double sumQuantity(List<MaterialStock> materialStockList) {
        double total = 0;
        for (MaterialStock materialStock : materialStockList){
            assert materialStock.getMatlWrhsStkQtyInMatlBaseUnit() != null;
            total += materialStock.getMatlWrhsStkQtyInMatlBaseUnit().doubleValue();
        }
        return total;
    }
}
