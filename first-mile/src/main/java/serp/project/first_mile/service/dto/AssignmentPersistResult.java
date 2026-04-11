package serp.project.first_mile.service.dto;

import serp.project.first_mile.dto.response.PickupAssignmentResponse;

import java.util.List;
import java.util.Set;

public record AssignmentPersistResult(
        List<PickupAssignmentResponse.AssignedTripResponse> tripResponses,
        Set<Long> assignedOrderIds
) {
}
