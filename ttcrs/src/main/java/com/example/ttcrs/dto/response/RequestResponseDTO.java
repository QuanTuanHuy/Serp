package com.example.ttcrs.dto.response;

import com.example.ttcrs.constant.ContainerSize;
import com.example.ttcrs.constant.RequestStatus;
import com.example.ttcrs.constant.RequestType;
import com.example.ttcrs.entity.RequestEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin của một Request trong danh sách.
 * Chỉ expose các trường cần thiết, không lộ internal fields.
 *
 * Sử dụng static method {@link #fromEntity(RequestEntity)} để convert.
 */
@Getter
@Builder
public class RequestResponseDTO {

    private Long id;
    private Long tenantId;
    private Long customerId;

    private String srcLocationCode;
    private String destLocationCode;

    private LocalDateTime earlyAtSrc;
    private LocalDateTime lateAtSrc;
    private LocalDateTime earlyAtDest;
    private LocalDateTime lateAtDest;

    private Double weight;
    private ContainerSize containerSize;
    private Boolean dropTrailerRequired;

    /** Lý do huỷ hoặc ghi chú bổ sung */
    private String reason;

    /** URL ảnh bằng chứng tại điểm đầu (nguồn) */
    private String evidenceAtSrc;

    /** URL ảnh bằng chứng tại điểm đích */
    private String evidenceAtDest;

    private RequestStatus status;
    private RequestType type;

    /** ID của transport plan đã được gán (null nếu chưa plan) */
    private Long transportPlanId;

    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedStamp;

    // -------------------------------------------------------------------------
    // Static mapper — không cần MapStruct dependency
    // -------------------------------------------------------------------------

    /**
     * Chuyển đổi từ {@link RequestEntity} sang {@link RequestResponseDTO}.
     *
     * @param entity RequestEntity từ database
     * @return DTO sẵn sàng trả về client
     */
    public static RequestResponseDTO fromEntity(RequestEntity entity) {
        if (entity == null) return null;

        return RequestResponseDTO.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .customerId(entity.getCustomerId())
                .srcLocationCode(entity.getSrcLocationCode())
                .destLocationCode(entity.getDestLocationCode())
                .earlyAtSrc(entity.getEarlyAtSrc())
                .lateAtSrc(entity.getLateAtSrc())
                .earlyAtDest(entity.getEarlyAtDest())
                .lateAtDest(entity.getLateAtDest())
                .weight(entity.getWeight())
                .containerSize(entity.getContainerSize())
                .dropTrailerRequired(entity.getDropTrailerRequired())
                .reason(entity.getReason())
                .evidenceAtSrc(entity.getEvidenceAtSrc())
                .evidenceAtDest(entity.getEvidenceAtDest())
                .status(entity.getStatus())
                .type(entity.getType())
                .transportPlanId(entity.getTransportPlanId())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .lastUpdatedStamp(entity.getLastUpdatedStamp())
                .build();
    }
}
