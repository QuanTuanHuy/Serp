package com.example.ttcrs.dto.response;

import com.example.ttcrs.constant.ContainerSize;
import com.example.ttcrs.constant.VehicleStatus;
import com.example.ttcrs.entity.ContainerEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ContainerResponseDTO {

    private Long id;
    private String code;
    private ContainerSize size;
    private VehicleStatus status;
    private String currentLocationCode;
    private LocalDateTime createdStamp;

    public static ContainerResponseDTO fromEntity(ContainerEntity e) {
        if (e == null) return null;
        return ContainerResponseDTO.builder()
                .id(e.getId())
                .code(e.getCode())
                .size(e.getSize())
                .status(e.getStatus())
                .currentLocationCode(e.getCurrentLocationCode())
                .createdStamp(e.getCreatedStamp())
                .build();
    }
}
