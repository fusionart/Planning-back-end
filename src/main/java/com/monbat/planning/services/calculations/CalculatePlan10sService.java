package com.monbat.planning.services.calculations;

import com.monbat.planning.models.entities.ProductionVersion;
import com.monbat.planning.models.entities.Readiness;
import com.monbat.planning.models.entities.Routing;
import com.monbat.planning.models.other.ReadinessByDate;
import com.monbat.planning.models.other.ReadinessByWeek;
import com.monbat.planning.models.other.ReadinessDetail;
import com.monbat.planning.models.other.ReadinessDetailWithDate;
import com.monbat.planning.services.ProductionVersionService;
import com.monbat.planning.services.ReadinessService;
import com.monbat.planning.services.RoutingService;
import com.monbat.planning.services.utils.ShowAlert;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.monbat.planning.utils.constants.Messages.MISSING_READINESS;

@Service
public class CalculatePlan10sService {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ReadinessService readinessService;
    @Autowired
    private RoutingService routingService;
    @Autowired
    private ProductionVersionService productionVersionService;

    private final HashMap<String, Routing> materialsWithRouting = new HashMap<>();

    private List<ReadinessByWeek> readinessByWeekList;

    private void loadDataFromDB() {
        List<String> allReadinessWeeks = readinessService.getDistinctReadinessWeek();
        List<Readiness> allReadinessByWeek;
        readinessByWeekList = new ArrayList<>();

        if (!allReadinessWeeks.isEmpty()) {

            for (String readinessWeek : allReadinessWeeks) {
                if (!Objects.equals(readinessWeek, "")) {
                    int currentEntryCounter = 0;
                    allReadinessByWeek = this.readinessService.getAllByByWeekOfReadiness(readinessWeek, 1000);

                    List<Date> allReadinessDatesByWeek = getDistinctReadinessDates(allReadinessByWeek);

                    List<ReadinessDetailWithDate> readinessDetailsList = new ArrayList<>();
                    for (Date readinessDate : allReadinessDatesByWeek) {
                        List<Readiness> filterByDate = allReadinessByWeek.stream()
                                .filter(object -> object.getDateOfReadiness().equals(readinessDate))
                                .toList();

                        for (Readiness readiness : filterByDate) {
                            ReadinessDetail readinessDetail = this.modelMapper.map(readiness, ReadinessDetail.class);
                            readinessDetail.setWorkCenter(getWorkCenter(readiness.getMaterial()));
                            ReadinessDetailWithDate readinessDetailWithDate =
                                    new ReadinessDetailWithDate(readinessDate, readinessDetail);
                            readinessDetailsList.add(readinessDetailWithDate);
                        }
                        currentEntryCounter++;
                    }

                    ReadinessByWeek readinessByWeek = new ReadinessByWeek();
                    readinessByWeek.put(readinessWeek, readinessDetailsList);
                    readinessByWeekList.add(readinessByWeek);
                }
            }
        }
    }

    private List<Date> getDistinctReadinessDates(List<Readiness> allReadinessByWeek) {
        return allReadinessByWeek.stream()
                .map(Readiness::getDateOfReadiness)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<ReadinessByWeek> getReadinessByWeek() {
        if (readinessByWeekList == null) {
            loadDataFromDB();
        }
        return readinessByWeekList;
    }

    private String getWorkCenter(String materialCode) {
        if (materialsWithRouting.containsKey(materialCode)) {
            return materialsWithRouting.get(materialCode).getWorkCenter();
        } else {
            ProductionVersion productionVersion;
            Routing routing;
            productionVersion = this.productionVersionService.getMaterialAndProductionVersionNumber(materialCode, 1000);
            if (productionVersion != null) {
                routing = this.routingService.getRoutingGroupAndRoutingGroupCounter(productionVersion.getRoutingGroup(), productionVersion.getRoutingGroupCounter());

                if (routing != null) {
                    materialsWithRouting.put(materialCode, routing);
                    return materialsWithRouting.get(materialCode).getWorkCenter();
                } else {
                    System.out.printf("Error in routing: %s | %s\n", materialCode, productionVersion.getRoutingGroup());
                }
            } else {
                System.out.printf("Error in production version: %s\n", materialCode);
                return "-";
            }
        }
        return null;
    }
}
