package com.example.ttcrs.dto.response;

import com.example.ttcrs.constant.LocationType;
import com.example.ttcrs.entity.LocationEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin của một Location.
 */
@Getter
@Builder
public class LocationResponseDTO {

    private Long id;
    private String locationCode;
    private LocationType type;
    private Double lat;
    private Double lng;
    private LocalDateTime createdStamp;

    /**
     * Chuyển đổi từ {@link LocationEntity} sang {@link LocationResponseDTO}.
     */
    public static LocationResponseDTO fromEntity(LocationEntity entity) {
        if (entity == null) return null;
        return LocationResponseDTO.builder()
                .id(entity.getId())
                .locationCode(entity.getLocationCode())
                .type(entity.getType())
                .lat(entity.getLat())
                .lng(entity.getLng())
                .createdStamp(entity.getCreatedStamp())
                .build();
    }
}
