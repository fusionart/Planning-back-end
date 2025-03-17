package com.monbat.planning.controllers;

import com.monbat.planning.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static com.monbat.planning.utils.TypeConstants.*;

@RestController
@RequestMapping("/api/files")
public class ImportFileController {
    @Autowired
    private BomService bomService;
    @Autowired
    private MaterialService materialService;
    @Autowired
    private ProductionVersionService productionVersionService;
    @Autowired
    private ReadinessService readinessService;
    @Autowired
    private RoutingService routingService;
    @Autowired
    private BatteryQuantityService batteryQuantityService;


    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam("type") String type) throws IOException {
        String uploadId = UUID.randomUUID().toString();
        File theFile = multipartToFile(file, file.getOriginalFilename());
        switch (type) {
            case BOM:
                new Thread(() -> this.bomService.startImportBom(theFile, uploadId)).start();
                break;
            case MATERIAL:
                new Thread(() -> this.materialService.startImportMaterial(theFile, uploadId)).start();
                break;
            case PRODUCTION_VERSION:
                new Thread(() -> this.productionVersionService.startImportProductionVersions(theFile, uploadId)).start();
                break;
            case READINESS:
                new Thread(() -> this.readinessService.startImportReadiness(theFile, uploadId)).start();
                break;
            case ROUTING:
                new Thread(() -> this.routingService.startImportRouting(theFile, uploadId)).start();
                break;
            case QUANTITIES:
                new Thread(() -> this.batteryQuantityService.startBatteryQuantity(theFile, uploadId)).start();
                break;
            default:
                // code block
        }
        return ResponseEntity.ok(uploadId); // Return upload ID to client
    }

    @GetMapping("/progress/{uploadId}")
    public ResponseEntity<Integer> getProgress(@PathVariable String uploadId,
                                               @RequestParam("type") String type) {
        int progress = 0;
        return switch (type) {
            case BOM -> {
                progress = this.bomService.getProgress(uploadId);
                yield ResponseEntity.ok(progress);
            }
            case MATERIAL -> {
                progress = this.materialService.getProgress(uploadId);
                yield ResponseEntity.ok(progress);
            }
            case PRODUCTION_VERSION -> {
                progress = this.productionVersionService.getProgress(uploadId);
                yield ResponseEntity.ok(progress);
            }
            case READINESS -> {
                progress = this.readinessService.getProgress(uploadId);
                yield ResponseEntity.ok(progress);
            }
            case ROUTING -> {
                progress = this.routingService.getProgress(uploadId);
                yield ResponseEntity.ok(progress);
            }
            case QUANTITIES -> {
                progress = this.batteryQuantityService.getProgress(uploadId);
                yield ResponseEntity.ok(progress);
            }
            default -> null;
        };
    }

    private File multipartToFile(MultipartFile multipart, String fileName) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
        multipart.transferTo(convFile);
        return convFile;
    }
}
