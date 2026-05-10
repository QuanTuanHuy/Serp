package com.example.ttcrs.dto.request.request;

import com.example.ttcrs.constant.RequestType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * DTO nhận tham số khi Dispatcher tạo mới một batch request.
 */
@Getter
@Setter
public class CreateRequestDTO {

    /** ID của customer (user có role TTCRS_CUSTOMER) mà request được tạo hộ */
    @NotNull(message = "customerId không được để trống")
    private Long customerId;

    /** Loại request: OF | IF | OE | IE */
    @NotNull(message = "type không được để trống")
    private RequestType type;

    /** Mã location điểm đi */
    @NotBlank(message = "srcLocationCode không được để trống")
    private String srcLocationCode;

    /** Mã location điểm đến */
    @NotBlank(message = "destLocationCode không được để trống")
    private String destLocationCode;

    /**
     * Số lượng request cần tạo (batch).
     * Tối thiểu 1, tối đa 100 để tránh lạm dụng.
     */
    @NotNull(message = "quantity không được để trống")
    @Min(value = 1, message = "quantity phải >= 1")
    @Max(value = 100, message = "quantity không được vượt quá 100")
    private Integer quantity;

    /** Cân nặng (kg) — optional */
    private Double weight;

    /** Có cần cắt mooc (drop trailer) tại điểm đến không */
    private Boolean dropTrailerRequired = false;

    // -------------------------------------------------------------------------
    // Time window fields — điền tuỳ theo loại request
    // -------------------------------------------------------------------------

    /** Thời điểm sớm nhất có thể lấy hàng tại nguồn */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime earlyAtSrc;

    /** Thời điểm muộn nhất có thể lấy hàng tại nguồn */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lateAtSrc;

    /** Thời điểm sớm nhất có thể giao hàng tại đích */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime earlyAtDest;

    /** Thời điểm muộn nhất có thể giao hàng tại đích */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lateAtDest;
}
