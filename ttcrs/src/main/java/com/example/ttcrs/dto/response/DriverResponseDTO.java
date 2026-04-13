package com.example.ttcrs.dto.response;

import com.example.ttcrs.constant.DriverStatus;
import com.example.ttcrs.entity.DriverEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DriverResponseDTO {

    private Long id;
    private String name;
    private DriverStatus status;
    private LocalDateTime createdStamp;

    public static DriverResponseDTO fromEntity(DriverEntity e) {
        if (e == null) return null;
        return DriverResponseDTO.builder()
                .id(e.getId())
                .name(e.getName())
                .status(e.getStatus())
                .createdStamp(e.getCreatedStamp())
                .build();
    }
}
