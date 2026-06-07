package com.example.ttcrs.controller;

import com.example.ttcrs.dto.request.request.CreateRequestDTO;
import com.example.ttcrs.dto.request.request.RequestFilterDTO;
import com.example.ttcrs.dto.request.request.UpdateRequestDTO;
import com.example.ttcrs.dto.request.request.UpdateRequestsStatusDTO;
import com.example.ttcrs.dto.request.transportplan.CreateTransportPlanInputDTO;
import com.example.ttcrs.dto.response.ApiResponse;
import com.example.ttcrs.dto.response.PageResponse;
import com.example.ttcrs.dto.response.RequestResponseDTO;
import com.example.ttcrs.service.RequestService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
@RequestMapping("/ttcrs/api/v1/dispatcher/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;
    private final ObjectMapper objectMapper;

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
        log.info("GET /api/v1/dispatcher/requests - filter: statuses={}, type={}, page={}, size={}",
                filter.getStatuses(), filter.getType(), filter.getPage(), filter.getSize());

        PageResponse<RequestResponseDTO> result = requestService.getRequests(filter);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // =========================================================================
    // POST /ttcrs/api/v1/dispatcher/requests
    // =========================================================================

    /**
     * Tạo mới một batch Request cho một Customer (dành cho Dispatcher).
     *
     * <p>Số request được tạo bằng {@code dto.quantity}.
     * Tất cả request có cùng thông tin ngoại trừ {@code id} và {@code createdAt}.
     *
     * @param dto tham số tạo request từ dispatcher
     * @return {@code 201 Created} với danh sách request vừa tạo
     */
    @PostMapping
    public ResponseEntity<ApiResponse<List<RequestResponseDTO>>> createRequests(
            @Valid @RequestBody CreateRequestDTO dto
    ) {
        log.info("POST /ttcrs/api/v1/dispatcher/requests - customerId={}, type={}, quantity={}",
                dto.getCustomerId(), dto.getType(), dto.getQuantity());

        List<RequestResponseDTO> created = requestService.createRequests(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Requests created successfully", created));
    }

    // =========================================================================
    // PUT /ttcrs/api/v1/dispatcher/requests/{id}
    // =========================================================================

    /**
     * Cập nhật thông tin một Request theo ID (chỉ áp dụng cho request PENDING).
     *
     * @param id  ID của request cần cập nhật
     * @param dto các trường cần thay đổi
     * @return {@code 200 OK} với request đã cập nhật
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RequestResponseDTO>> updateRequest(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequestDTO dto
    ) {
        log.info("PUT /ttcrs/api/v1/dispatcher/requests/{}", id);
        try {
            RequestResponseDTO updated = requestService.updateRequest(id, dto);
            return ResponseEntity.ok(ApiResponse.ok("Request updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    // =========================================================================
    // DELETE /ttcrs/api/v1/dispatcher/requests/{id}
    // =========================================================================

    /**
     * Xoá mềm một Request theo ID (chỉ áp dụng cho request PENDING).
     *
     * @param id ID của request cần xoá
     * @return {@code 204 No Content} nếu thành công
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRequest(@PathVariable Long id) {
        log.info("DELETE /ttcrs/api/v1/dispatcher/requests/{}", id);
        try {
            requestService.deleteRequest(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    // =========================================================================
    // PATCH /ttcrs/api/v1/dispatcher/requests/status
    // =========================================================================

    /**
     * Cập nhật hàng loạt trạng thái cho nhiều Request (dành cho Dispatcher).
     *
     * <p>Dùng trong luồng tạo Transport Plan:
     * <ul>
     *   <li>Click "Next" (Step 1 → Step 2): cập nhật các request đã chọn sang {@code PLANNED}.</li>
     *   <li>Click "Back" (Step 2 → Step 1): hoàn trả trạng thái về {@code PENDING}.</li>
     * </ul>
     *
     * @param dto danh sách ID request và trạng thái mới
     * @return {@code 200 OK} với danh sách request đã được cập nhật
     */
    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<List<RequestResponseDTO>>> updateRequestsStatus(
            @Valid @RequestBody UpdateRequestsStatusDTO dto
    ) {
        log.info("PATCH /ttcrs/api/v1/dispatcher/requests/status - requestIds={}, status={}",
                dto.getRequestIds(), dto.getStatus());

        List<RequestResponseDTO> updated = requestService.updateRequestsStatus(dto);

        return ResponseEntity.ok(ApiResponse.ok("Requests status updated successfully", updated));
    }

    // =========================================================================
    // POST /ttcrs/api/v1/dispatcher/requests/transport-plan
    // =========================================================================

    /**
     * Creates a transport plan from the dispatcher's resource/depot selections.
     *
     * <p>This is the final "Create Plan" submission (Step 2). The entire operation
     * runs inside a single {@code @Transactional} boundary on the service layer:
     * if anything fails, all changes are rolled back atomically.
     *
     * <p>Pre-condition: the included requests must already be in {@code PLANNED}
     * status (set by the "Next" click in Step 1).
     *
     * @param dto full plan input: request IDs, depot codes, resource IDs
     * @return {@code 201 Created} on success
     */
    @PostMapping("/transport-plan")
    public ResponseEntity<ApiResponse<Object>> createTransportPlan(
            @Valid @RequestBody CreateTransportPlanInputDTO dto
    ) {
        log.info("POST /ttcrs/api/v1/dispatcher/requests/transport-plan - requestIds={}", dto.getRequestIds());

        String resultJson = requestService.createTransportPlanInput(dto);

        try {
            Map<String, Object> resultMap = objectMapper.readValue(
                    resultJson, new TypeReference<Map<String, Object>>() {});
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Transport plan created successfully", resultMap));
        } catch (Exception e) {
            log.error("Failed to parse algorithm result JSON", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Algorithm completed but result could not be parsed"));
        }
    }
}
