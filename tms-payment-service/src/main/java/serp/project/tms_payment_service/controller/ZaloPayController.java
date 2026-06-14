package serp.project.tms_payment_service.controller;

import serp.project.tms_payment_service.dto.zalopay.CreateOrderRequest;
import serp.project.tms_payment_service.dto.zalopay.CreateOrderResponse;
import serp.project.tms_payment_service.dto.zalopay.GetBankListResponse;
import serp.project.tms_payment_service.dto.zalopay.QueryOrderRequest;
import serp.project.tms_payment_service.dto.zalopay.QueryOrderResponse;
import serp.project.tms_payment_service.dto.zalopay.QueryRefundRequest;
import serp.project.tms_payment_service.dto.zalopay.QueryRefundResponse;
import serp.project.tms_payment_service.dto.zalopay.RefundRequest;
import serp.project.tms_payment_service.dto.zalopay.RefundResponse;
import serp.project.tms_payment_service.dto.zalopay.ZaloPayCallbackRequest;
import serp.project.tms_payment_service.service.ZaloPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API liên quan đến ZaloPay
 * 
 * IMPORTANT: Base path không có /payment prefix vì API Gateway sẽ rewrite:
 * Gateway URL: /payment/api/v1/payments/zalopay/*
 * → Forward to: /v1/payments/zalopay/*
 */
@Slf4j
@RestController
@RequestMapping("/v1/payments/zalopay")
@RequiredArgsConstructor
@Tag(name = "ZaloPay Payment", description = "APIs tích hợp cổng thanh toán ZaloPay")
public class ZaloPayController {
    
    private final ZaloPayService zaloPayService;
    
    /**
     * API tạo đơn hàng thanh toán ZaloPay
     * 
     * @param request Thông tin đơn hàng
     * @return Response chứa URL thanh toán và thông tin đơn hàng
     */
    @Operation(
            summary = "Tạo đơn hàng thanh toán ZaloPay",
            description = "Tạo đơn hàng mới trên ZaloPay và nhận URL thanh toán, QR code để khách hàng thực hiện thanh toán. **Yêu cầu xác thực JWT.**",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tạo đơn hàng thành công",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateOrderResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu đầu vào không hợp lệ"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Lỗi server khi tạo đơn hàng"
            )
    })
    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        
        log.info("Received create order request - User: {}, Amount: {}", 
                request.getAppUser(), request.getAmount());
        
        try {
            CreateOrderResponse response = zaloPayService.createOrder(request);
            
            if ("SUCCESS".equals(response.getStatus())) {
                log.info("Order created successfully - AppTransId: {}", response.getAppTransId());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Order creation failed - Message: {}, ErrorCode: {}", 
                        response.getMessage(), response.getErrorCode());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in create order endpoint: {}", e.getMessage(), e);
            CreateOrderResponse errorResponse = CreateOrderResponse.builder()
                    .status("FAILED")
                    .message("Lỗi hệ thống: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Callback endpoint để ZaloPay gửi kết quả thanh toán
     * 
     * @param callbackRequest Callback data từ ZaloPay
     * @return Response xác nhận đã nhận callback
     */
    @Operation(
            summary = "Callback từ ZaloPay",
            description = "Endpoint nhận callback từ ZaloPay khi thanh toán hoàn tất. Endpoint này được ZaloPay gọi tự động."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Xử lý callback thành công",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Callback data không hợp lệ"
            )
    })
    @PostMapping(value = "/callback", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleCallback(@RequestBody ZaloPayCallbackRequest callbackRequest) {
        
        log.info("Received ZaloPay callback - Type: {}", callbackRequest.getType());
        
        try {
            String response = zaloPayService.handleCallback(callbackRequest);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error handling callback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"return_code\": 0, \"return_message\": \"Error handling callback\"}");
        }
    }
    
    /**
     * API truy vấn trạng thái đơn hàng
     * 
     * @param request Request chứa app_trans_id
     * @return Response chứa trạng thái đơn hàng
     */
    @Operation(
            summary = "Truy vấn trạng thái đơn hàng",
            description = "Query trạng thái đơn hàng từ ZaloPay. Sử dụng API này để check trạng thái khi callback bị miss hoặc để verify thanh toán. **Yêu cầu xác thực JWT.**",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Truy vấn thành công",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = QueryOrderResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu đầu vào không hợp lệ"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Lỗi server khi truy vấn"
            )
    })
    @PostMapping("/query-order")
    public ResponseEntity<QueryOrderResponse> queryOrderStatus(
            @Valid @RequestBody QueryOrderRequest request) {
        
        log.info("Received query order request - AppTransId: {}", request.getAppTransId());
        
        try {
            QueryOrderResponse response = zaloPayService.queryOrderStatus(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in query order endpoint: {}", e.getMessage(), e);
            QueryOrderResponse errorResponse = QueryOrderResponse.builder()
                    .appTransId(request.getAppTransId())
                    .status("ERROR")
                    .message("Lỗi hệ thống: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * API kiểm tra health check
     */
    @Operation(
            summary = "Health check",
            description = "Kiểm tra trạng thái hoạt động của ZaloPay service"
    )
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("{\"status\": \"UP\", \"service\": \"ZaloPay Payment Service\"}");
    }
    
    /**
     * API lấy danh sách ngân hàng được hỗ trợ
     * 
     * @return Response chứa danh sách ngân hàng theo từng payment method category
     */
    @Operation(
            summary = "Lấy danh sách ngân hàng được hỗ trợ",
            description = "Lấy danh sách tất cả các ngân hàng và phương thức thanh toán được ZaloPay hỗ trợ. " +
                         "Danh sách được phân loại theo PMCID: 36=Visa/Master/JCB, 37=Bank Account, 38=ZaloPay, 39=ATM, 41=Visa/Master Debit"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lấy danh sách thành công",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GetBankListResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Lỗi server khi lấy danh sách"
            )
    })
    @GetMapping("/banks")
    public ResponseEntity<GetBankListResponse> getBankList() {
        log.info("Received get bank list request");
        
        try {
            GetBankListResponse response = zaloPayService.getBankList();
            
            if (response != null && response.getReturnCode() != null && response.getReturnCode() == 1) {
                log.info("Get bank list successfully - Total PMCs: {}", 
                        response.getBanks() != null ? response.getBanks().size() : 0);
                return ResponseEntity.ok(response);
            } else {
                log.warn("Get bank list failed - Code: {}, Message: {}", 
                        response != null ? response.getReturnCode() : null,
                        response != null ? response.getReturnMessage() : null);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in get bank list endpoint: {}", e.getMessage(), e);
            GetBankListResponse errorResponse = GetBankListResponse.builder()
                    .returnCode(0)
                    .returnMessage("Lỗi hệ thống: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * API thực hiện hoàn tiền giao dịch
     * 
     * @param request Request chứa thông tin hoàn tiền
     * @return Response chứa kết quả hoàn tiền
     */
    @Operation(
            summary = "Hoàn tiền giao dịch",
            description = "Thực hiện hoàn tiền cho giao dịch đã thanh toán thành công trên ZaloPay. " +
                         "Cần cung cấp zp_trans_id của giao dịch gốc, số tiền hoàn và lý do hoàn tiền. " +
                         "**Yêu cầu xác thực JWT.**",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Yêu cầu hoàn tiền được xử lý",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RefundResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu đầu vào không hợp lệ"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Lỗi server khi xử lý hoàn tiền"
            )
    })
    @PostMapping("/refund")
    public ResponseEntity<RefundResponse> refundOrder(
            @Valid @RequestBody RefundRequest request) {
        
        log.info("Received refund request - ZpTransId: {}, Amount: {}", 
                request.getZpTransId(), request.getAmount());
        
        try {
            RefundResponse response = zaloPayService.refundOrder(request);
            
            if ("SUCCESS".equals(response.getStatus()) || "PROCESSING".equals(response.getStatus())) {
                log.info("Refund request processed - MRefundId: {}, Status: {}", 
                        response.getMRefundId(), response.getStatus());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Refund failed - Message: {}, ErrorCode: {}", 
                        response.getMessage(), response.getErrorCode());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in refund endpoint: {}", e.getMessage(), e);
            RefundResponse errorResponse = RefundResponse.builder()
                    .zpTransId(request.getZpTransId())
                    .status("FAILED")
                    .message("Lỗi hệ thống: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * API truy vấn trạng thái hoàn tiền
     * 
     * @param request Request chứa m_refund_id
     * @return Response chứa trạng thái hoàn tiền
     */
    @Operation(
            summary = "Truy vấn trạng thái hoàn tiền",
            description = "Query trạng thái của yêu cầu hoàn tiền từ ZaloPay. " +
                         "Sử dụng API này để kiểm tra trạng thái hoàn tiền khi status là PROCESSING hoặc để verify kết quả hoàn tiền. " +
                         "**Yêu cầu xác thực JWT.**",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Truy vấn thành công",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = QueryRefundResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu đầu vào không hợp lệ"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Lỗi server khi truy vấn"
            )
    })
    @PostMapping("/query-refund")
    public ResponseEntity<QueryRefundResponse> queryRefundStatus(
            @Valid @RequestBody QueryRefundRequest request) {
        
        log.info("Received query refund request - MRefundId: {}", request.getMRefundId());
        
        try {
            QueryRefundResponse response = zaloPayService.queryRefundStatus(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in query refund endpoint: {}", e.getMessage(), e);
            QueryRefundResponse errorResponse = QueryRefundResponse.builder()
                    .mRefundId(request.getMRefundId())
                    .status("ERROR")
                    .message("Lỗi hệ thống: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
