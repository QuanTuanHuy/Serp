/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.mapper;

import serp.project.second_mile.domain.HubPostOfficeMapping;
import serp.project.second_mile.dto.response.HubPostOfficeMappingResponse;

public final class HubPostOfficeMappingMapper {

    private HubPostOfficeMappingMapper() {
    }

    public static HubPostOfficeMappingResponse toResponse(HubPostOfficeMapping mapping) {
        return new HubPostOfficeMappingResponse(
                mapping.getId(),
                mapping.getHub() != null ? mapping.getHub().getId() : null,
                mapping.getPostOfficeCode(),
                mapping.getCreatedAt(),
                mapping.getUpdatedAt(),
                mapping.getTenantId()
        );
    }
}
