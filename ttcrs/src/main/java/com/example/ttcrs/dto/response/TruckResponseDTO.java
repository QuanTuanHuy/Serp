package com.example.ttcrs.dto.response;

import com.example.ttcrs.constant.VehicleStatus;
import com.example.ttcrs.entity.TruckEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TruckResponseDTO {

    private Long id;
    private String code;
    private VehicleStatus status;
    private String currentLocationCode;
    private LocalDateTime createdStamp;

    public static TruckResponseDTO fromEntity(TruckEntity e) {
        if (e == null) return null;
        return TruckResponseDTO.builder()
                .id(e.getId())
                .code(e.getCode())
                .status(e.getStatus())
                .currentLocationCode(e.getCurrentLocationCode())
                .createdStamp(e.getCreatedStamp())
                .build();
    }
}
