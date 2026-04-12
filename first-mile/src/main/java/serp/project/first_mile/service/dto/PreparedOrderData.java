package serp.project.first_mile.service.dto;

import java.util.List;

public record PreparedOrderData(
        List<PickupOrderNode> assignableOrders,
        List<UnassignedOrderState> initialUnassignedOrders
) {
}
