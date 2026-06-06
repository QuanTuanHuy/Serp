package serp.project.payment_service.controller;

import serp.project.payment_service.dto.ApiResponse;
import serp.project.payment_service.dto.transaction.TransactionHistoryFilterRequest;
import serp.project.payment_service.dto.transaction.TransactionHistoryResponse;
import serp.project.payment_service.entity.ZaloPayTransaction;
import serp.project.payment_service.service.ZaloPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controller xử lý các API lịch sử giao dịch
 * 
 * IMPORTANT: Base path không có /payment prefix vì API Gateway sẽ rewrite:
 * Gateway URL: /payment/api/v1/transactions/*
 * → Forward to: /v1/transactions/*
 */
@Slf4j
@RestController
@RequestMapping("/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction History", description = "APIs quản lý lịch sử giao dịch thanh toán")
public class TransactionHistoryController {
    
    private final ZaloPayService zaloPayService;
    
    /**
     * API cho user xem lịch sử giao dịch của chính họ
     * 
     * @return Response chứa danh sách giao dịch với phân trang
     */
    @Operation(
            summary = "Xem lịch sử giao dịch của người dùng",
            description = "Lấy lịch sử tất cả giao dịch thanh toán của người dùng hiện tại với các bộ lọc. " +
                         "**Yêu cầu xác thực JWT.** User chỉ có thể xem giao dịch của chính mình.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lấy lịch sử giao dịch thành công",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chưa xác thực hoặc token không hợp lệ"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Lỗi server khi lấy lịch sử"
            )
    })
    @GetMapping("/my-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TransactionHistoryResponse>> getMyTransactionHistory(
            @Parameter(description = "Trạng thái giao dịch")
            @RequestParam(required = false) ZaloPayTransaction.TransactionStatus status,
            
            @Parameter(description = "Thời gian bắt đầu (format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            
            @Parameter(description = "Thời gian kết thúc (format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            
            @Parameter(description = "Số tiền tối thiểu (VNĐ)")
            @RequestParam(required = false) Long minAmount,
            
            @Parameter(description = "Số tiền tối đa (VNĐ)")
            @RequestParam(required = false) Long maxAmount,
            
            @Parameter(description = "Mã ngân hàng")
            @RequestParam(required = false) String bankCode,
            
            @Parameter(description = "Từ khóa tìm kiếm trong mô tả hoặc tiêu đề")
            @RequestParam(required = false) String searchKeyword,
            
            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") Integer page,
            
            @Parameter(description = "Số lượng bản ghi mỗi trang")
            @RequestParam(defaultValue = "20") Integer size,
            
            @Parameter(description = "Trường sắp xếp (createdAt, amount, status, paidAt)")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Hướng sắp xếp (ASC, DESC)")
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        try {
            // Lấy username từ JWT token
            String username = getCurrentUsername();
            
            if (username == null || username.isEmpty()) {
                log.error("Cannot extract username from JWT token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.<TransactionHistoryResponse>builder()
                                .code(401)
                                .message("Chưa xác thực hoặc token không hợp lệ")
                                .build());
            }
            
            log.info("User {} is fetching their transaction history", username);
            
            // Build filter request với username của user hiện tại
            TransactionHistoryFilterRequest filter = TransactionHistoryFilterRequest.builder()
                    .appUser(username)  // Force filter theo username của user hiện tại
                    .status(status)
                    .startDate(startDate)
                    .endDate(endDate)
                    .minAmount(minAmount)
                    .maxAmount(maxAmount)
                    .bankCode(bankCode)
                    .searchKeyword(searchKeyword)
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .build();
            
            TransactionHistoryResponse response = zaloPayService.getTransactionHistory(filter);
            
            log.info("Transaction history fetched successfully for user: {} - Total: {}", 
                    username, response.getTotalElements());
            
            return ResponseEntity.ok(ApiResponse.<TransactionHistoryResponse>builder()
                    .code(200)
                    .message("Lấy lịch sử giao dịch thành công")
                    .result(response)
                    .build());
            
        } catch (Exception e) {
            log.error("Error fetching user transaction history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<TransactionHistoryResponse>builder()
                            .code(500)
                            .message("Lỗi server khi lấy lịch sử giao dịch: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * API cho admin xem lịch sử giao dịch của tất cả user
     * 
     * @return Response chứa danh sách giao dịch với phân trang
     */
    @Operation(
            summary = "Xem lịch sử giao dịch của hệ thống (Admin)",
            description = "Lấy lịch sử tất cả giao dịch thanh toán trong hệ thống với các bộ lọc. " +
                         "**Yêu cầu quyền ADMIN.** Admin có thể xem giao dịch của tất cả user và filter theo username.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lấy lịch sử giao dịch thành công",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chưa xác thực hoặc token không hợp lệ"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Không có quyền truy cập (yêu cầu quyền ADMIN)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Lỗi server khi lấy lịch sử"
            )
    })
    @GetMapping("/admin/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TransactionHistoryResponse>> getSystemTransactionHistory(
            @Parameter(description = "Username của người dùng (filter theo user cụ thể)")
            @RequestParam(required = false) String appUser,
            
            @Parameter(description = "Trạng thái giao dịch")
            @RequestParam(required = false) ZaloPayTransaction.TransactionStatus status,
            
            @Parameter(description = "Thời gian bắt đầu (format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            
            @Parameter(description = "Thời gian kết thúc (format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            
            @Parameter(description = "Số tiền tối thiểu (VNĐ)")
            @RequestParam(required = false) Long minAmount,
            
            @Parameter(description = "Số tiền tối đa (VNĐ)")
            @RequestParam(required = false) Long maxAmount,
            
            @Parameter(description = "Mã ngân hàng")
            @RequestParam(required = false) String bankCode,
            
            @Parameter(description = "Từ khóa tìm kiếm trong mô tả hoặc tiêu đề")
            @RequestParam(required = false) String searchKeyword,
            
            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") Integer page,
            
            @Parameter(description = "Số lượng bản ghi mỗi trang")
            @RequestParam(defaultValue = "20") Integer size,
            
            @Parameter(description = "Trường sắp xếp (createdAt, amount, status, paidAt)")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Hướng sắp xếp (ASC, DESC)")
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        try {
            String adminUsername = getCurrentUsername();
            log.info("Admin {} is fetching system transaction history", adminUsername);
            
            // Build filter request - admin có thể filter theo bất kỳ username nào
            TransactionHistoryFilterRequest filter = TransactionHistoryFilterRequest.builder()
                    .appUser(appUser)  // Admin có thể xem của user cụ thể hoặc tất cả (null)
                    .status(status)
                    .startDate(startDate)
                    .endDate(endDate)
                    .minAmount(minAmount)
                    .maxAmount(maxAmount)
                    .bankCode(bankCode)
                    .searchKeyword(searchKeyword)
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .build();
            
            TransactionHistoryResponse response = zaloPayService.getTransactionHistory(filter);
            
            log.info("System transaction history fetched successfully by admin: {} - Total: {}", 
                    adminUsername, response.getTotalElements());
            
            return ResponseEntity.ok(ApiResponse.<TransactionHistoryResponse>builder()
                    .code(200)
                    .message("Lấy lịch sử giao dịch hệ thống thành công")
                    .result(response)
                    .build());
            
        } catch (Exception e) {
            log.error("Error fetching system transaction history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<TransactionHistoryResponse>builder()
                            .code(500)
                            .message("Lỗi server khi lấy lịch sử giao dịch: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * Lấy username từ JWT token
     */
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())
                    && authentication.getPrincipal() instanceof Jwt) {
                return authentication.getName();
            }
            return null;
        } catch (Exception e) {
            log.error("Error extracting username from JWT: {}", e.getMessage(), e);
            return null;
        }
    }
}
