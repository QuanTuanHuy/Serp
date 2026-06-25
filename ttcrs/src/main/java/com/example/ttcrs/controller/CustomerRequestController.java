package com.example.ttcrs.controller;

import com.example.ttcrs.dto.request.request.RequestFilterDTO;
import com.example.ttcrs.dto.response.ApiResponse;
import com.example.ttcrs.dto.response.PageResponse;
import com.example.ttcrs.dto.response.RequestResponseDTO;
import com.example.ttcrs.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only API cho Customer — chỉ GET, không có tác vụ write.
 * Tự động lọc theo customerId lấy từ JWT claim "cid".
 */
@Slf4j
@RestController
@RequestMapping("/ttcrs/api/v1/customer/requests")
@RequiredArgsConstructor
public class CustomerRequestController {

    private final RequestService requestService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RequestResponseDTO>>> getRequests(
            @ModelAttribute RequestFilterDTO filter
    ) {
        log.info("GET /ttcrs/api/v1/customer/requests");
        PageResponse<RequestResponseDTO> result = requestService.getCustomerRequests(filter);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RequestResponseDTO>> getRequestById(@PathVariable Long id) {
        log.info("GET /ttcrs/api/v1/customer/requests/{}", id);
        try {
            return ResponseEntity.ok(ApiResponse.ok(requestService.getCustomerRequestById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }
}
