package serp.project.first_mile.service.dto;

import serp.project.first_mile.enums.OrderActorType;
import serp.project.first_mile.enums.TripStatus;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public record OrderActorScope(
        OrderActorType actorType,
        String customerCreatedBy,
        Set<String> managedOriginPostOfficeCodes,
        Long courierStaffId,
        Collection<TripStatus> courierVisibleTripStatuses
) {
    public static OrderActorScope admin() {
        return new OrderActorScope(OrderActorType.ADMIN, null, null, null, null);
    }

    public static OrderActorScope customer(String customerCreatedBy) {
        return new OrderActorScope(OrderActorType.CUSTOMER, customerCreatedBy, Set.of(), null, List.of());
    }

    public static OrderActorScope manager(Set<String> managedOriginPostOfficeCodes) {
        return new OrderActorScope(OrderActorType.MANAGER, null, managedOriginPostOfficeCodes, null, List.of());
    }

    public static OrderActorScope courier(Long courierStaffId, Collection<TripStatus> courierVisibleTripStatuses) {
        return new OrderActorScope(
                OrderActorType.COURIER,
                null,
                Set.of(),
                courierStaffId,
                courierVisibleTripStatuses == null ? List.of() : List.copyOf(courierVisibleTripStatuses)
        );
    }
}
