package com.example.ttcrs.dto.request;

import com.example.ttcrs.constant.LocationType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhận tham số khi Dispatcher tạo mới một Location.
 */
@Getter
@Setter
public class CreateLocationDTO {

    /** Mã định danh duy nhất của location (tối đa 50 ký tự) */
    @NotBlank(message = "locationCode không được để trống")
    @Size(max = 50, message = "locationCode không được vượt quá 50 ký tự")
    private String locationCode;

    /** Loại location: PORT | WAREHOUSE | DEPOT_CONTAINER | DEPOT_TRUCK | DEPOT_TRAILER */
    @NotNull(message = "type không được để trống")
    private LocationType type;

    /** Vĩ độ (latitude) — lấy từ bản đồ khi người dùng click */
    @NotNull(message = "lat không được để trống")
    @DecimalMin(value = "-90.0", message = "lat phải >= -90")
    @DecimalMax(value = "90.0",  message = "lat phải <= 90")
    private Double lat;

    /** Kinh độ (longitude) — lấy từ bản đồ khi người dùng click */
    @NotNull(message = "lng không được để trống")
    @DecimalMin(value = "-180.0", message = "lng phải >= -180")
    @DecimalMax(value = "180.0",  message = "lng phải <= 180")
    private Double lng;
}
