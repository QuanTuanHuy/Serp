package serp.project.payment_service.controller;

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
import serp.project.payment_service.dto.payment.PaymentBankListResponse;
import serp.project.payment_service.dto.payment.PaymentCallbackRequest;
import serp.project.payment_service.dto.payment.PaymentCreateOrderRequest;
import serp.project.payment_service.dto.payment.PaymentCreateOrderResponse;
import serp.project.payment_service.dto.payment.PaymentQueryOrderRequest;
import serp.project.payment_service.dto.payment.PaymentQueryOrderResponse;
import serp.project.payment_service.dto.payment.PaymentQueryRefundRequest;
import serp.project.payment_service.dto.payment.PaymentQueryRefundResponse;
import serp.project.payment_service.dto.payment.PaymentRefundRequest;
import serp.project.payment_service.dto.payment.PaymentRefundResponse;
import serp.project.payment_service.gateway.PaymentGateway;
import serp.project.payment_service.service.PaymentGatewayRegistry;

@Slf4j
@RestController
@RequestMapping("/v1/payments/{gateway}")
@RequiredArgsConstructor
@Tag(name = "Payment Gateway", description = "Generic APIs for configurable payment gateways")
public class PaymentGatewayController {

    private final PaymentGatewayRegistry paymentGatewayRegistry;

    @Operation(
            summary = "Create payment order by gateway",
            description = "Create payment order for selected gateway.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Create order success",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentCreateOrderResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping("/create-order")
    public ResponseEntity<PaymentCreateOrderResponse> createOrder(
            @PathVariable String gateway,
            @Valid @RequestBody PaymentCreateOrderRequest request) {
        PaymentGateway paymentGateway = paymentGatewayRegistry.resolve(gateway);
        log.info("Received create-order request: gateway={} user={} amount={}",
                paymentGateway.gatewayCode(),
                request.getAppUser(),
                request.getAmount());
        try {
            PaymentCreateOrderResponse response = paymentGateway.createOrder(request);
            if ("SUCCESS".equals(response.getStatus())) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("Error create-order gateway={}: {}", paymentGateway.gatewayCode(), e.getMessage(), e);
            PaymentCreateOrderResponse errorResponse = PaymentCreateOrderResponse.builder()
                    .status("FAILED")
                    .message("Lỗi hệ thống: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Gateway callback endpoint")
    @PostMapping(value = "/callback", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleCallback(
            @PathVariable String gateway,
            @RequestBody PaymentCallbackRequest callbackRequest) {
        PaymentGateway paymentGateway = paymentGatewayRegistry.resolve(gateway);
        try {
            return ResponseEntity.ok(paymentGateway.handleCallback(callbackRequest));
        } catch (Exception e) {
            log.error("Error callback gateway={}: {}", paymentGateway.gatewayCode(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"return_code\": 0, \"return_message\": \"Error handling callback\"}");
        }
    }

    @Operation(
            summary = "Query order status by gateway",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/query-order")
    public ResponseEntity<PaymentQueryOrderResponse> queryOrderStatus(
            @PathVariable String gateway,
            @Valid @RequestBody PaymentQueryOrderRequest request) {
        PaymentGateway paymentGateway = paymentGatewayRegistry.resolve(gateway);
        try {
            return ResponseEntity.ok(paymentGateway.queryOrderStatus(request));
        } catch (Exception e) {
            log.error("Error query-order gateway={}: {}", paymentGateway.gatewayCode(), e.getMessage(), e);
            PaymentQueryOrderResponse errorResponse = PaymentQueryOrderResponse.builder()
                    .appTransId(request.getAppTransId())
                    .status("ERROR")
                    .message("Lỗi hệ thống: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Gateway health check")
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck(@PathVariable String gateway) {
        PaymentGateway paymentGateway = paymentGatewayRegistry.resolve(gateway);
        return ResponseEntity.ok("{\"status\":\"UP\",\"gateway\":\"" + paymentGateway.displayName() + "\"}");
    }

    @Operation(summary = "Get bank list by gateway")
    @GetMapping("/banks")
    public ResponseEntity<PaymentBankListResponse> getBankList(@PathVariable String gateway) {
        PaymentGateway paymentGateway = paymentGatewayRegistry.resolve(gateway);
        try {
            PaymentBankListResponse response = paymentGateway.getBankList();
            if (response != null && response.getReturnCode() != null && response.getReturnCode() == 1) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            log.error("Error get-bank-list gateway={}: {}", paymentGateway.gatewayCode(), e.getMessage(), e);
            PaymentBankListResponse errorResponse = PaymentBankListResponse.builder()
                    .returnCode(0)
                    .returnMessage("Lỗi hệ thống: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(
            summary = "Refund by gateway",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/refund")
    public ResponseEntity<PaymentRefundResponse> refundOrder(
            @PathVariable String gateway,
            @Valid @RequestBody PaymentRefundRequest request) {
        PaymentGateway paymentGateway = paymentGatewayRegistry.resolve(gateway);
        try {
            PaymentRefundResponse response = paymentGateway.refundOrder(request);
            if ("SUCCESS".equals(response.getStatus()) || "PROCESSING".equals(response.getStatus())) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("Error refund gateway={}: {}", paymentGateway.gatewayCode(), e.getMessage(), e);
            PaymentRefundResponse errorResponse = PaymentRefundResponse.builder()
                    .zpTransId(request.getZpTransId())
                    .status("FAILED")
                    .message("Lỗi hệ thống: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(
            summary = "Query refund status by gateway",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/query-refund")
    public ResponseEntity<PaymentQueryRefundResponse> queryRefundStatus(
            @PathVariable String gateway,
            @Valid @RequestBody PaymentQueryRefundRequest request) {
        PaymentGateway paymentGateway = paymentGatewayRegistry.resolve(gateway);
        try {
            return ResponseEntity.ok(paymentGateway.queryRefundStatus(request));
        } catch (Exception e) {
            log.error("Error query-refund gateway={}: {}", paymentGateway.gatewayCode(), e.getMessage(), e);
            PaymentQueryRefundResponse errorResponse = PaymentQueryRefundResponse.builder()
                    .mRefundId(request.getMRefundId())
                    .status("ERROR")
                    .message("Lỗi hệ thống: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
