package com.example.ttcrs.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Generic wrapper cho tất cả API response trong hệ thống ttcrs.
 *
 * Cấu trúc trả về:
 * <pre>
 * {
 *   "success": true,
 *   "message": "OK",
 *   "data": { ... }
 * }
 * </pre>
 *
 * @param <T> kiểu dữ liệu của trường {@code data}
 */
@Getter
@Builder
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    // -------------------------------------------------------------------------
    // Static factory methods
    // -------------------------------------------------------------------------

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("OK")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }
}
