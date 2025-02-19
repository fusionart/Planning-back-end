package com.monbat.planning.services;

import com.monbat.planning.models.entities.Routing;
import com.monbat.planning.repositories.RoutingRepository;
import org.modelmapper.ModelMapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface RoutingService {
    boolean areImported();
    void importRoutings(File file, String uploadId);
    void startImportRouting(File file, String uploadId);
    List<Routing> getAllRoutings();
    Routing getRoutingGroupAndRoutingGroupCounter(String routingGroup, int routingGroupCounter);
    int getProgress(String uploadId);
}
