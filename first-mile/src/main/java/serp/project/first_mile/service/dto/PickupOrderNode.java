package serp.project.first_mile.service.dto;

import java.time.LocalDateTime;

public record PickupOrderNode(
        Long orderId,
        String orderCode,
        String customerOrderCode,
        String senderName,
        String senderPhone,
        Double latitude,
        Double longitude,
        double weight,
        double volume,
        LocalDateTime pickupTimeStart,
        LocalDateTime pickupTimeEnd
) {
}