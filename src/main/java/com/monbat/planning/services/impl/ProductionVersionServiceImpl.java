package com.monbat.planning.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monbat.planning.models.dto.ProductionVersionDto;
import com.monbat.planning.models.entities.ProductionVersion;
import com.monbat.planning.repositories.ProductionVersionRepository;
import com.monbat.planning.services.ProductionVersionService;
import com.monbat.planning.utils.poi.ReadExcelFile;
import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.monbat.planning.controllers.constants.SapApiConstants.*;
import static com.monbat.planning.services.utils.columns.ProductionVersionFileColumns.*;

@Service
public class ProductionVersionServiceImpl implements ProductionVersionService {
    @Autowired
    private ProductionVersionRepository productionVersionRepository;
    @Autowired
    private ModelMapper modelMapper;
    private static final Logger logger = LoggerFactory.getLogger(ProductionVersionServiceImpl.class);

    private final Map<String, Integer> progressMap = new ConcurrentHashMap<>();

    @Override
    public boolean areImported() {
        return this.productionVersionRepository.count() > 0;
    }

    @Override
    public void importProductionVersions(File file, String uploadId) {
        if (file != null) {
            XSSFSheet sheet = ReadExcelFile.getWorksheet((File) file);
            //Clear the table before new import
            this.productionVersionRepository.truncateTable();

            for (Row row : sheet) {
                if (row.getRowNum() == 0 || row.getRowNum() == 1) {
                    continue;
                }

                ProductionVersionDto productionVersionDto = new ProductionVersionDto();

                productionVersionDto.setMaterial(row.getCell(MATERIAL_COLUMN).getStringCellValue());
                productionVersionDto.setPlant(Integer.parseInt(row.getCell(PLANT_COLUMN).getStringCellValue()));
                productionVersionDto.setProductionVersionNumber(Integer.parseInt(row.getCell(PRODUCTION_VERSION_NUMBER_COLUMN).getStringCellValue()));
                productionVersionDto.setRoutingGroup(row.getCell(ROUTING_GROUP_COLUMN).getStringCellValue());
                productionVersionDto.setRoutingGroupCounter(Integer.parseInt(row.getCell(ROUTING_GROUP_COUNTER_COLUMN).getStringCellValue()));
                productionVersionDto.setDescription(row.getCell(DESCRIPTION_COLUMN).getStringCellValue());

                this.productionVersionRepository.save(modelMapper.map(productionVersionDto, ProductionVersion.class));

                progressMap.put(uploadId, (row.getRowNum() * 100) / (sheet.getLastRowNum() - 1));
            }
        }
        progressMap.put(uploadId, 100);
    }

    @Override
    public int getProgress(String uploadId) {
        return progressMap.getOrDefault(uploadId, 0);
    }

    @Override
    public void startImportProductionVersions(File file, String uploadId) {
        importProductionVersions(file, uploadId);
    }

    @Override
    public void loadProductionVersionsFromSap(String username, String password) {
        Base64 base64 = new Base64();
        // Clear existing data or merge as needed
        this.productionVersionRepository.truncateTable();
        // Fetch data from SAP
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

            String nextUrl = PRODUCTION_VERSIONS_URL + "?$format=json&sap-client=" + SAP_CLIENT;

            do {
                URI uri = new URIBuilder(nextUrl).build();

                HttpGet request = new HttpGet(uri);
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int statusCode = response.getStatusLine().getStatusCode();

                    if (statusCode != 200) {
                        logger.error("Failed to retrieve production orders. Status code: {}", Optional.of(statusCode));
                        throw new RuntimeException("Failed to retrieve production orders. Status code: " + statusCode);
                    }

                    String jsonResponse = EntityUtils.toString(response.getEntity());
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(jsonResponse);
                    JsonNode resultsNode = rootNode.path("value");

                    saveProductionVersionsToDb(resultsNode);

                    JsonNode nextLinkNode = rootNode.path("@odata.nextLink");
                    if (nextLinkNode.isMissingNode() || nextLinkNode.asText().isEmpty()) {
                        nextUrl = null;
                    } else {
                        nextUrl = nextLinkNode.asText();
                        // If the nextLink is relative, you might need to prepend the base URL
                        if (nextUrl.startsWith("/")) {
                            nextUrl = SAP_MAIN + nextUrl;
                        }
                    }
                }
            } while (nextUrl != null);
        } catch (Exception e) {
            logger.error("Error in getProductionOrders: ", e);
            throw new RuntimeException("Error retrieving production orders: " + e.getMessage(), e);
        }
    }

    private void saveProductionVersionsToDb(JsonNode resultsNode) {
        List<ProductionVersion> productionVersionsList = new ArrayList<>();
        for (JsonNode versionNode : resultsNode) {
            ProductionVersion productionVersion = new ProductionVersion();

            productionVersion.setMaterial(versionNode.path("Material").asText());
            productionVersion.setPlant(versionNode.path("Plant").asInt());
            productionVersion.setProductionVersionNumber(versionNode.path("ProductionVersion").asInt());
            productionVersion.setRoutingGroup(versionNode.path("BillOfOperationsGroup").asText());
            productionVersion.setRoutingGroupCounter(versionNode.path("BillOfOperationsVariant").asInt());
            productionVersion.setDescription(versionNode.path("ProductionVersionText").asText());

            productionVersionsList.add(productionVersion);
        }

        if (!productionVersionsList.isEmpty()) {
            this.productionVersionRepository.saveAll(productionVersionsList);
        }
    }

    @Override
    public List<ProductionVersion> getAllProductionVersions() {
        return this.productionVersionRepository.findAll().stream().toList();
    }

    @Override
    public List<ProductionVersion> getProductionVersionsByMaterialAndPlant(String material, int plant) {
        return this.productionVersionRepository.findAllByMaterialAndPlant(material, plant);
    }

    @Override
    public ProductionVersion getMaterialAndProductionVersionNumber(String material, int productionVersionNumber) {
        return this.productionVersionRepository.findFirstByMaterialAndProductionVersionNumber(material, productionVersionNumber);
    }
}
