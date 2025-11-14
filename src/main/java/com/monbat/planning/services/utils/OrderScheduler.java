package com.monbat.planning.services.utils;

import com.monbat.planning.models.entities.NormalizedOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OrderScheduler {

    public LocalDateTime findNextAvailableSlot(List<NormalizedOrder> orders,
                                               LocalDate desiredDate, LocalTime desiredTime,
                                               long durationMinutes) {
        LocalDateTime desiredStart = LocalDateTime.of(desiredDate, desiredTime);

        // Check if desired slot is available
        if (isTimeSlotAvailable(orders, desiredStart, durationMinutes)) {
            return desiredStart;
        }

        // Find overlapping orders
        List<NormalizedOrder> overlappingOrders = findOverlappingOrders(orders, desiredStart, durationMinutes);

        if (overlappingOrders.isEmpty()) {
            return desiredStart;
        }

        // Find the end time of the last overlapping order
        LocalDateTime lastOverlapEnd = findLastOverlapEnd(overlappingOrders);

        // Start looking from the end of the last overlapping order
        return findFirstAvailableSlotFrom(orders, lastOverlapEnd, durationMinutes);
    }

    private boolean isTimeSlotAvailable(List<NormalizedOrder> orders,
                                        LocalDateTime start, long durationMinutes) {
        LocalDateTime end = start.plusMinutes(durationMinutes);
        return orders.stream()
                .noneMatch(order -> isOverlapping(order, start, end));
    }

    private List<NormalizedOrder> findOverlappingOrders(List<NormalizedOrder> orders,
                                                        LocalDateTime start, long durationMinutes) {
        LocalDateTime end = start.plusMinutes(durationMinutes);
        return orders.stream()
                .filter(order -> isOverlapping(order, start, end))
                .sorted(Comparator.comparing(order ->
                        LocalDateTime.of(order.getEndDate(), order.getEndTime())))
                .collect(Collectors.toList());
    }

    private LocalDateTime findLastOverlapEnd(List<NormalizedOrder> overlappingOrders) {
        return overlappingOrders.stream()
                .map(order -> LocalDateTime.of(order.getEndDate(), order.getEndTime()))
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.MIN);
    }

    private LocalDateTime findFirstAvailableSlotFrom(List<NormalizedOrder> orders,
                                                     LocalDateTime fromDateTime,
                                                     long durationMinutes) {
        // Get all orders that start after fromDateTime
        List<NormalizedOrder> futureOrders = orders.stream()
                .filter(order -> LocalDateTime.of(order.getStartDate(), order.getStartTime())
                        .isAfter(fromDateTime))
                .sorted(Comparator.comparing(order ->
                        LocalDateTime.of(order.getStartDate(), order.getStartTime())))
                .collect(Collectors.toList());

        // If no future orders, the slot right after fromDateTime is available
        if (futureOrders.isEmpty()) {
            return fromDateTime;
        }

        // Check gap between fromDateTime and first future order
        LocalDateTime firstFutureStart = LocalDateTime.of(
                futureOrders.get(0).getStartDate(),
                futureOrders.get(0).getStartTime());

        LocalDateTime potentialStart = fromDateTime;
        LocalDateTime potentialEnd = potentialStart.plusMinutes(durationMinutes);

        if (!potentialEnd.isAfter(firstFutureStart)) {
            return potentialStart;
        }

        // Check gaps between future orders
        for (int i = 0; i < futureOrders.size() - 1; i++) {
            NormalizedOrder current = futureOrders.get(i);
            NormalizedOrder next = futureOrders.get(i + 1);

            LocalDateTime currentEnd = LocalDateTime.of(current.getEndDate(), current.getEndTime());
            LocalDateTime nextStart = LocalDateTime.of(next.getStartDate(), next.getStartTime());

            if (currentEnd.isBefore(nextStart)) {
                potentialStart = currentEnd;
                potentialEnd = potentialStart.plusMinutes(durationMinutes);

                if (!potentialEnd.isAfter(nextStart)) {
                    return potentialStart;
                }
            }
        }

        // If no gaps found, return after the last order
        NormalizedOrder lastOrder = futureOrders.get(futureOrders.size() - 1);
        return LocalDateTime.of(lastOrder.getEndDate(), lastOrder.getEndTime());
    }

    private boolean isOverlapping(NormalizedOrder order, LocalDateTime start, LocalDateTime end) {
        LocalDateTime orderStart = LocalDateTime.of(order.getStartDate(), order.getStartTime());
        LocalDateTime orderEnd = LocalDateTime.of(order.getEndDate(), order.getEndTime());
        return start.isBefore(orderEnd) && end.isAfter(orderStart);
    }
}
