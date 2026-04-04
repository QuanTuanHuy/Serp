package com.example.ttcrs.dto.request;

import com.example.ttcrs.constant.RequestStatus;
import com.example.ttcrs.constant.RequestType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO chứa các tham số filter, phân trang và sắp xếp
 * cho API GET /api/v1/requests (dành cho Dispatcher).
 *
 * Tất cả các trường đều là optional.
 * Nếu không truyền thì không áp dụng điều kiện lọc tương ứng.
 */
@Getter
@Setter
public class RequestFilterDTO {

    // -------------------------------------------------------------------------
    // Filter fields
    // -------------------------------------------------------------------------

    /**
     * Lọc theo một hoặc nhiều trạng thái.
     * Ví dụ: ?statuses=PENDING&statuses=PLANNED
     * Nếu null hoặc rỗng → lấy tất cả trạng thái.
     */
    private List<RequestStatus> statuses;

    /**
     * Lọc theo loại request (OF, IF, OE, IE).
     * Ví dụ: ?type=OF
     */
    private RequestType type;

    /**
     * Lọc theo mã location nguồn (src_location_code).
     * Ví dụ: ?srcLocationCode=PORT_HCM
     */
    private String srcLocationCode;

    /**
     * Lọc theo mã location đích (dest_location_code).
     * Ví dụ: ?destLocationCode=DEPOT_HN
     */
    private String destLocationCode;

    /**
     * Lọc request được tạo từ ngày này (inclusive).
     * Ví dụ: ?createdFrom=2025-01-01T00:00:00
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdFrom;

    /**
     * Lọc request được tạo đến ngày này (inclusive).
     * Ví dụ: ?createdTo=2025-12-31T23:59:59
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdTo;

    // -------------------------------------------------------------------------
    // Pagination & Sorting fields
    // -------------------------------------------------------------------------

    /**
     * Số trang, bắt đầu từ 0 (zero-based).
     * Default: 0
     */
    private int page = 0;

    /**
     * Số bản ghi trên một trang.
     * Default: 20
     */
    private int size = 20;

    /**
     * Tên trường dùng để sắp xếp (phải là tên Java field trong RequestEntity).
     * Default: "createdAt"
     */
    private String sortBy = "createdAt";

    /**
     * Chiều sắp xếp: "asc" hoặc "desc" (case-insensitive).
     * Default: "desc"
     */
    private String sortDirection = "desc";
}
