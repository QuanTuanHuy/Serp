package com.example.ttcrs.dto.response;

import com.example.ttcrs.constant.VehicleStatus;
import com.example.ttcrs.entity.TrailerEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TrailerResponseDTO {

    private Long id;
    private String code;
    private VehicleStatus status;
    private String currentLocationCode;
    private LocalDateTime createdStamp;

    public static TrailerResponseDTO fromEntity(TrailerEntity e) {
        if (e == null) return null;
        return TrailerResponseDTO.builder()
                .id(e.getId())
                .code(e.getCode())
                .status(e.getStatus())
                .currentLocationCode(e.getCurrentLocationCode())
                .createdStamp(e.getCreatedStamp())
                .build();
    }
}
