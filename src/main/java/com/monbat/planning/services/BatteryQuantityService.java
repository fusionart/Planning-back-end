package com.monbat.planning.services;

import com.monbat.planning.models.entities.BatteryQuantity;

import java.io.File;
import java.util.List;

public interface BatteryQuantityService {
    void importBatteryQuantity(File file, String uploadId);
    void startBatteryQuantity(File file, String uploadId);
    List<BatteryQuantity> findAllBatteryQuantity();
    List<BatteryQuantity> getAllByStorageLocation(int storageLocation);
    List<BatteryQuantity> getAllByBatteryCodePrefix(int prefix);
    int getProgress(String uploadId);

}
