package com.example.ttcrs.controller;

import com.example.ttcrs.dto.request.RequestFilterDTO;
import com.example.ttcrs.dto.response.ApiResponse;
import com.example.ttcrs.dto.response.PageResponse;
import com.example.ttcrs.dto.response.RequestResponseDTO;
import com.example.ttcrs.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller xử lý các API liên quan đến Request (vận đơn vận chuyển).
 *
 * <p>Phân quyền được xử lý tập trung qua API Gateway / Spring Security URL config,
 * không cần annotation {@code @PreAuthorize} ở đây.
 *
 * <p>Base URL: {@code /api/v1/requests}
 */
@Slf4j
@RestController
@RequestMapping("/ttcrs/api/v1/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    // =========================================================================
    // GET /api/v1/requests
    // =========================================================================

    /**
     * Lấy danh sách Request có phân trang và bộ lọc động (dành cho Dispatcher).
     *
     * <p><b>Query parameters (tất cả optional):</b>
     * <ul>
     *   <li>{@code statuses}       — List trạng thái, ví dụ: {@code ?statuses=PENDING&statuses=PLANNED}</li>
     *   <li>{@code type}           — Loại request: {@code OF | IF | OE | IE}</li>
     *   <li>{@code srcLocationCode}  — Mã location nguồn</li>
     *   <li>{@code destLocationCode} — Mã location đích</li>
     *   <li>{@code createdFrom}    — ISO datetime, ví dụ: {@code 2025-01-01T00:00:00}</li>
     *   <li>{@code createdTo}      — ISO datetime, ví dụ: {@code 2025-12-31T23:59:59}</li>
     *   <li>{@code page}           — Số trang (zero-based, mặc định: 0)</li>
     *   <li>{@code size}           — Số bản ghi/trang (mặc định: 20)</li>
     *   <li>{@code sortBy}         — Tên field sắp xếp (mặc định: {@code createdAt})</li>
     *   <li>{@code sortDirection}  — {@code asc} hoặc {@code desc} (mặc định: {@code desc})</li>
     * </ul>
     *
     * <p><b>Ví dụ Postman:</b>
     * <pre>
     * GET /api/v1/requests?statuses=PENDING&statuses=PLANNED&type=OF&page=0&size=10&sortBy=createdAt&sortDirection=desc
     * </pre>
     *
     * @param filter tham số lọc và phân trang (tự động bind từ query params)
     * @return {@code 200 OK} với danh sách Request đã phân trang
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RequestResponseDTO>>> getRequests(
            @ModelAttribute RequestFilterDTO filter
    ) {
        log.info("GET /api/v1/requests - filter: statuses={}, type={}, page={}, size={}",
                filter.getStatuses(), filter.getType(), filter.getPage(), filter.getSize());

        PageResponse<RequestResponseDTO> result = requestService.getRequests(filter);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
