package com.monbat.planning.services.impl;

import com.monbat.planning.models.dto.PlannedOrderDto;
import com.monbat.planning.models.entities.NormalizedOrder;
import com.monbat.planning.models.production_order.ProductionOrderDto;
import com.monbat.planning.services.CapacityService;
import com.monbat.planning.services.PlannedOrderService;
import com.monbat.planning.services.ProductionOrderService;
import com.monbat.planning.services.utils.OrderScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CapacityServiceImpl implements CapacityService {
    @Autowired
    private PlannedOrderService plannedOrderService;
    @Autowired
    private ProductionOrderService productionOrderService;

    private final List<NormalizedOrder> normalizedOrders = new ArrayList<>();

    @Override
    public LocalDateTime getFreeCapacity(String username, String password, String manufacturingOrder,
                                boolean isProductionOrder, LocalDateTime scheduleTime) {
        String workCenter;
        String productionSupervisor;
        LocalDate desiredDate;
        LocalTime desiredTime;
        long durationMinutes;
        LocalDateTime reqDelDateBegin = LocalDateTime.of(scheduleTime.toLocalDate(), LocalTime.MIN);
        LocalDateTime reqDelDateEnd = LocalDateTime.of(scheduleTime.toLocalDate(), LocalTime.of(23, 59));;

        if (isProductionOrder) {
            ProductionOrderDto productionOrderDto = this.productionOrderService.getProductionOrder(username, password, manufacturingOrder);
            workCenter = productionOrderDto.getWorkCenter();
            productionSupervisor = productionOrderDto.getProductionSupervisor();
            desiredDate = productionOrderDto.getMfgOrderScheduledStartDate();
            desiredTime = productionOrderDto.getMfgOrderScheduledStartTime();
            durationMinutes =
                    Duration.between(desiredTime, productionOrderDto.getMfgOrderScheduledEndTime()).toMinutes();
        } else {
            PlannedOrderDto productionOrderDto = this.plannedOrderService.getPlannedOrder(username, password, manufacturingOrder);
            workCenter = productionOrderDto.getWorkCenter();
            productionSupervisor = productionOrderDto.getProductionSupervisor();
            desiredDate = productionOrderDto.getPlndOrderPlannedStartDate();
            desiredTime = productionOrderDto.getPlndOrderPlannedStartTime();
            durationMinutes = Duration.between(desiredTime, productionOrderDto.getPlndOrderPlannedEndTime()).toMinutes();
        }

        List<ProductionOrderDto> productionOrderDtoList =
                this.productionOrderService.getProductionOrdersByProductionSupervisor(username, password,
                        productionSupervisor, reqDelDateBegin, reqDelDateEnd);

        this.normalizeProductionOrders(productionOrderDtoList, workCenter);

        List<PlannedOrderDto> plannedOrderDtoList =
                this.plannedOrderService.getPlannedOrdersByProductionSupervisor(username, password, productionSupervisor, reqDelDateBegin, reqDelDateEnd);

        this.normalizePlannedOrders(plannedOrderDtoList, workCenter);

        List<NormalizedOrder> sortedList = normalizedOrders.stream()
                .sorted(Comparator.comparing(NormalizedOrder::getStartTime))
                .toList();

        LocalDateTime scheduleTimeToReturn;

        if (hasOverlappingOrder(sortedList, desiredDate, desiredTime)) {
            scheduleTimeToReturn = scheduleTime;
        } else {
            OrderScheduler orderScheduler = new OrderScheduler();
            scheduleTimeToReturn = orderScheduler.findNextAvailableSlot(sortedList, desiredDate, desiredTime, durationMinutes);
        }

        return scheduleTimeToReturn;
    }

    private void normalizeProductionOrders(List<ProductionOrderDto> productionOrderDtoList, String workCenter){
        if (productionOrderDtoList.isEmpty()){
            return;
        }

        for (ProductionOrderDto productionOrderDto : productionOrderDtoList){
            if (!productionOrderDto.getWorkCenter().equals(workCenter)){
                continue;
            }
            NormalizedOrder normalizedOrder = new NormalizedOrder();
            normalizedOrder.setMaterial(productionOrderDto.getMaterial());
            normalizedOrder.setStartDate(productionOrderDto.getMfgOrderScheduledStartDate());
            normalizedOrder.setEndDate(productionOrderDto.getMfgOrderScheduledEndDate());
            normalizedOrder.setStartTime(productionOrderDto.getMfgOrderScheduledStartTime());
            normalizedOrder.setEndTime(productionOrderDto.getMfgOrderScheduledEndTime());
            normalizedOrder.setDuration(Duration.between(normalizedOrder.getEndTime(),
                    normalizedOrder.getStartTime()).toMinutes());
            normalizedOrders.add(normalizedOrder);
        }
    }

    private void normalizePlannedOrders(List<PlannedOrderDto> plannedOrderDtoList, String workCenter){
        if (plannedOrderDtoList.isEmpty()){
            return;
        }

        for (PlannedOrderDto plannedOrderDto : plannedOrderDtoList) {
            if (!plannedOrderDto.getWorkCenter().equals(workCenter)) {
                continue;
            }
            NormalizedOrder normalizedOrder = new NormalizedOrder();
            normalizedOrder.setMaterial(plannedOrderDto.getMaterial());
            normalizedOrder.setStartDate(plannedOrderDto.getPlndOrderPlannedStartDate());
            normalizedOrder.setEndDate(plannedOrderDto.getPlndOrderPlannedEndDate());
            normalizedOrder.setStartTime(plannedOrderDto.getPlndOrderPlannedStartTime());
            normalizedOrder.setEndTime(plannedOrderDto.getPlndOrderPlannedEndTime());
            normalizedOrder.setDuration(Duration.between(normalizedOrder.getEndTime(),
                    normalizedOrder.getStartTime()).toMinutes());
            normalizedOrders.add(normalizedOrder);
        }
    }

    private boolean hasOverlappingOrder(List<NormalizedOrder> orders, LocalDate date, LocalTime time) {
        return orders.stream()
                .anyMatch(order -> isTimeInOrder(order, date, time));
    }

    private boolean isTimeInOrder(NormalizedOrder order, LocalDate date, LocalTime time) {
        // Check if the date is within the order's date range
        boolean dateInRange = !date.isBefore(order.getStartDate()) && !date.isAfter(order.getEndDate());

        if (!dateInRange) {
            return false;
        }

        // For single day orders
        if (order.getStartDate().equals(order.getEndDate())) {
            return !time.isBefore(order.getStartTime()) && !time.isAfter(order.getEndTime());
        }

        // For multi-day orders, handle start day, middle days, and end day separately
        if (date.equals(order.getStartDate())) {
            // On start date, check if time is after start time
            return !time.isBefore(order.getStartTime());
        } else if (date.equals(order.getEndDate())) {
            // On end date, check if time is before end time
            return !time.isAfter(order.getEndTime());
        } else {
            // For days in between, the entire day is occupied
            return true;
        }
    }
}
