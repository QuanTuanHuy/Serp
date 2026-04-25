package serp.project.first_mile.service.dto;

public record ManualOrderProductPayload(
        String name,
        Long value,
        Integer quantity,
        Double weightGram,
        Long productTypeId
) {
}
