package com.example.ttcrs.dto.response;

import com.example.ttcrs.constant.LocationType;
import com.example.ttcrs.entity.LocationEntity;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO trả về thông tin tối thiểu của một Location cho dropdown.
 */
@Getter
@Builder
public class LocationResponseDTO {

    private Long id;
    private String locationCode;
    private LocationType type;

    /**
     * Chuyển đổi từ {@link LocationEntity} sang {@link LocationResponseDTO}.
     */
    public static LocationResponseDTO fromEntity(LocationEntity entity) {
        if (entity == null) return null;
        return LocationResponseDTO.builder()
                .id(entity.getId())
                .locationCode(entity.getLocationCode())
                .type(entity.getType())
                .build();
    }
}
