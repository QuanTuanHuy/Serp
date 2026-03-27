/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import java.time.LocalDateTime;

public record ProductTypeResponse(
        Long id,
        String code,
        String name,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy,
        Long tenantId
) {
}