package serp.project.first_mile.service.dto;

public record CourierResource(
        Long staffId,
        String code,
        String fullName,
        Integer maxStops
) {
}
