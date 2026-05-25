package com.example.ttcrs.dto.request.request;

import com.example.ttcrs.constant.ContainerSize;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * DTO for updating an existing Request (only allowed when status is PENDING).
 * All fields are optional — only non-null values are applied.
 */
@Getter
@Setter
public class UpdateRequestDTO {

    private String srcLocationCode;
    private String destLocationCode;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime earlyAtSrc;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lateAtSrc;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime earlyAtDest;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lateAtDest;

    private Double weight;
    private ContainerSize containerSize;
    private Boolean dropTrailerRequired;
    private String reason;
}
