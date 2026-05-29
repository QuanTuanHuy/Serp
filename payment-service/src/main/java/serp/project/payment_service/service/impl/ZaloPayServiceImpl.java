package serp.project.payment_service.service.impl;

import serp.project.payment_service.config.ZaloPayConfig;
import serp.project.payment_service.dto.webhook.FirstMilePaymentConfirmedWebhookRequest;
import serp.project.payment_service.dto.transaction.TransactionHistoryFilterRequest;
import serp.project.payment_service.dto.transaction.TransactionHistoryResponse;
import serp.project.payment_service.dto.zalopay.*;
import serp.project.payment_service.entity.ZaloPayTransaction;
import serp.project.payment_service.entity.ZaloPayRefundTransaction;
import serp.project.payment_service.kafka.PaymentKafkaMessagePublisher;
import serp.project.payment_service.enums.ZaloPaySubReturnCode;
import serp.project.payment_service.repository.ZaloPayTransactionRepository;
import serp.project.payment_service.repository.ZaloPayRefundTransactionRepository;
import serp.project.payment_service.repository.specification.TransactionSpecification;
import serp.project.payment_service.service.ZaloPayService;
import serp.project.payment_service.service.WebhookEventService;
import serp.project.payment_service.util.ZaloPayHMACUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation của ZaloPayService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZaloPayServiceImpl implements ZaloPayService {
    
    private final ZaloPayConfig zaloPayConfig;
    private final ObjectMapper objectMapper;
    private final ZaloPayTransactionRepository transactionRepository;
    private final ZaloPayRefundTransactionRepository refundTransactionRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final PaymentKafkaMessagePublisher kafkaMessagePublisher;
    private final WebhookEventService webhookEventService;

    @Value("${app.notification.default-tenant-id:1}")
    private Long defaultTenantId;
    
    private static final String DATE_FORMAT = "yyMMdd";
    
    private String buildKafkaKey(String appUser, String id) {
        String u = (appUser == null || appUser.isBlank()) ? "unknown" : appUser.trim();
        String v = (id == null || id.isBlank()) ? "unknown" : id.trim();
        return u + ":" + v;
    }
    
    @Override
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        try {
            // Generate app_trans_id với format: yyMMdd_timestamp
            String appTransId = generateAppTransId();
            
            // Lấy thời gian hiện tại (milliseconds)
            long appTime = System.currentTimeMillis();
            
            // Convert items sang JSON string
            String itemJson = objectMapper.writeValueAsString(request.getItems());
            
            // Xử lý embed_data
            String embedDataJson = buildEmbedData(request.getEmbedData(), request);
            
            // Tạo HMAC input theo format: app_id|app_trans_id|app_user|amount|app_time|embed_data|item
            String hmacInput = String.format("%d|%s|%s|%d|%d|%s|%s",
                    zaloPayConfig.getAppId(),
                    appTransId,
                    request.getAppUser(),
                    request.getAmount(),
                    appTime,
                    embedDataJson,
                    itemJson
            );
            
            // Tạo MAC
            String mac = ZaloPayHMACUtil.computeHmacSHA256(hmacInput, zaloPayConfig.getKey1());
            
            log.info("Creating ZaloPay order - AppTransId: {}, Amount: {}, MAC Input: {}", 
                    appTransId, request.getAmount(), hmacInput);
            
            // Build request gửi tới ZaloPay
            ZaloPayOrderRequest zaloPayRequest = ZaloPayOrderRequest.builder()
                    .appId(zaloPayConfig.getAppId())
                    .appUser(request.getAppUser())
                    .appTransId(appTransId)
                    .appTime(appTime)
                    .amount(request.getAmount())
                    .item(itemJson)
                    .description(request.getDescription())
                    .embedData(embedDataJson)
                    .bankCode(request.getBankCode())
                    .mac(mac)
                    .callbackUrl(zaloPayConfig.getCallbackUrl())
                    .expireDurationSeconds(
                            request.getExpireDurationSeconds() != null 
                                    ? request.getExpireDurationSeconds() 
                                    : zaloPayConfig.getExpireDurationSeconds()
                    )
                    .title(request.getTitle())
                    .phone(request.getPhone())
                    .email(request.getEmail())
                    .address(request.getAddress())
                    .subAppId(request.getSubAppId())
                    .build();
            
            // Gọi API ZaloPay
            ZaloPayOrderResponse zaloPayResponse = callZaloPayCreateOrderAPI(zaloPayRequest);
            
            // Lưu transaction vào database
            saveTransaction(appTransId, request, itemJson, embedDataJson, zaloPayResponse);
            
            // Xử lý response
            return buildCreateOrderResponse(appTransId, zaloPayResponse);
            
        } catch (JsonProcessingException e) {
            log.error("Error processing JSON: {}", e.getMessage(), e);
            return CreateOrderResponse.builder()
                    .status("FAILED")
                    .message("Lỗi xử lý dữ liệu JSON")
                    .build();
        } catch (Exception e) {
            log.error("Error creating ZaloPay order: {}", e.getMessage(), e);
            return CreateOrderResponse.builder()
                    .status("FAILED")
                    .message("Lỗi tạo đơn hàng: " + e.getMessage())
                    .build();
        }
    }
    
    @Override
    @Transactional
    public String handleCallback(ZaloPayCallbackRequest callbackRequest) {
        try {
            log.info("=== ZALOPAY CALLBACK RECEIVED ===");
            log.info("Type: {}, MAC: {}", callbackRequest.getType(), callbackRequest.getMac());
            log.debug("Data: {}", callbackRequest.getData());
            
            // Bước 1: Verify MAC để đảm bảo callback đến từ ZaloPay
            String computedMac = ZaloPayHMACUtil.computeHmacSHA256(
                    callbackRequest.getData(), 
                    zaloPayConfig.getKey2()
            );
            
            if (!computedMac.equalsIgnoreCase(callbackRequest.getMac())) {
                log.error("INVALID MAC - Computed: {}, Received: {}",
                        computedMac, callbackRequest.getMac());
                return buildCallbackResponse(-1, "mac not equal");
            }
            
            log.info("MAC verified successfully");
            
            // Bước 2: Parse callback data
            ZaloPayCallbackData callbackData = objectMapper.readValue(
                    callbackRequest.getData(), 
                    ZaloPayCallbackData.class
            );
            
            log.info("Parsed callback data - AppTransId: {}, ZpTransId: {}, Amount: {}, Channel: {}", 
                    callbackData.getAppTransId(), 
                    callbackData.getZpTransId(),
                    callbackData.getAmount(),
                    callbackData.getChannel());
            
            // Bước 3: Kiểm tra idempotency - nếu đã xử lý rồi thì return code = 2
            ZaloPayTransaction existingTransaction = transactionRepository
                    .findByAppTransId(callbackData.getAppTransId())
                    .orElse(null);
            
            if (existingTransaction != null && 
                existingTransaction.getStatus() == ZaloPayTransaction.TransactionStatus.SUCCESS) {
                log.warn("Duplicate callback - Transaction already processed: {}",
                        callbackData.getAppTransId());
                // Theo đặc tả ZaloPay: return_code = 2 khi trùng mã giao dịch
                return buildCallbackResponse(2, "success");
            }
            
            // Bước 4: Cập nhật transaction trong database
            log.info("Processing payment success for AppTransId: {}", callbackData.getAppTransId());
            updateTransactionOnCallback(callbackData);
            
            // TODO: Xử lý business logic khi thanh toán thành công
            // - Cập nhật trạng thái order
            // - Gửi notification cho user
            // - Trigger các service khác (order, inventory, etc.)
            
            log.info("Callback processed successfully - AppTransId: {}", callbackData.getAppTransId());
            return buildCallbackResponse(1, "success");
            
        } catch (JsonProcessingException e) {
            log.error("JSON parsing error in callback: {}", e.getMessage(), e);
            // Return 0 để ZaloPay callback lại (tối đa 3 lần)
            return buildCallbackResponse(0, "Invalid JSON format: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error handling ZaloPay callback: {}", e.getMessage(), e);
            // Return 0 để ZaloPay callback lại (tối đa 3 lần)
            return buildCallbackResponse(0, e.getMessage());
        }
    }
    
    /**
     * Generate app_trans_id theo format: yyMMdd_timestamp
     * QUAN TRỌNG: Sử dụng TimeZone GMT+7 (Vietnam) theo yêu cầu của ZaloPay
     */
    private String generateAppTransId() {
        // Sử dụng Calendar với GMT+7 timezone giống như code mẫu ZaloPay
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setCalendar(cal);
        String datePrefix = sdf.format(cal.getTimeInMillis());
        
        // Sử dụng random ID để tránh trùng lặp trong cùng 1 millisecond
        Random rand = new Random();
        int randomId = rand.nextInt(1000000);
        
        return String.format("%s_%d", datePrefix, randomId);
    }
    
    /**
     * Build embed_data JSON string
     */
    private String buildEmbedData(EmbedData embedData, CreateOrderRequest request) throws JsonProcessingException {
        if (embedData == null) {
            embedData = new EmbedData();
        }
        
        // Set redirect URL mặc định nếu chưa có
        if (embedData.getRedirectUrl() == null || embedData.getRedirectUrl().isEmpty()) {
            embedData.setRedirectUrl(zaloPayConfig.getRedirectUrl());
        }

        Map<String, Object> merchantInfo = readMerchantInfo(embedData.getMerchantInfo());
        if (request.getTenantId() != null) {
            merchantInfo.put("tenantId", request.getTenantId());
        }
        if (request.getActorId() != null) {
            merchantInfo.put("actorId", request.getActorId());
        }
        if (request.getUserId() != null) {
            merchantInfo.put("userId", request.getUserId());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            merchantInfo.put("email", request.getEmail().trim());
        }
        if (!merchantInfo.isEmpty()) {
            embedData.setMerchantInfo(objectMapper.writeValueAsString(merchantInfo));
        }
        
        return objectMapper.writeValueAsString(embedData);
    }
    
    /**
     * Gọi ZaloPay Create Order API
     */
    private ZaloPayOrderResponse callZaloPayCreateOrderAPI(ZaloPayOrderRequest request) {
        try {
            // Sử dụng application/x-www-form-urlencoded
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // Convert request object sang form data
            MultiValueMap<String, String> formData = convertToFormData(request);
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
            
            log.info("Calling ZaloPay API: {}", zaloPayConfig.getCreateOrderUrl());
            
            ResponseEntity<ZaloPayOrderResponse> response = restTemplate.exchange(
                    zaloPayConfig.getCreateOrderUrl(),
                    HttpMethod.POST,
                    entity,
                    ZaloPayOrderResponse.class
            );
            
            ZaloPayOrderResponse responseBody = response.getBody();
            
            log.info("ZaloPay API response - ReturnCode: {}, Message: {}", 
                    responseBody != null ? responseBody.getReturnCode() : null,
                    responseBody != null ? responseBody.getReturnMessage() : null);
            
            return responseBody;
            
        } catch (RestClientException e) {
            log.error("Error calling ZaloPay API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call ZaloPay API", e);
        }
    }
    
    /**
     * Convert request object sang form data
     */
    private MultiValueMap<String, String> convertToFormData(ZaloPayOrderRequest request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        
        formData.add("app_id", String.valueOf(request.getAppId()));
        formData.add("app_user", request.getAppUser());
        formData.add("app_trans_id", request.getAppTransId());
        formData.add("app_time", String.valueOf(request.getAppTime()));
        formData.add("amount", String.valueOf(request.getAmount()));
        formData.add("item", request.getItem());
        formData.add("description", request.getDescription());
        formData.add("embed_data", request.getEmbedData());
        formData.add("mac", request.getMac());
        
        // Optional fields
        if (request.getBankCode() != null && !request.getBankCode().isEmpty()) {
            formData.add("bank_code", request.getBankCode());
        }
        if (request.getCallbackUrl() != null && !request.getCallbackUrl().isEmpty()) {
            formData.add("callback_url", request.getCallbackUrl());
        }
        if (request.getExpireDurationSeconds() != null) {
            formData.add("expire_duration_seconds", String.valueOf(request.getExpireDurationSeconds()));
        }
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            formData.add("title", request.getTitle());
        }
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            formData.add("phone", request.getPhone());
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            formData.add("email", request.getEmail());
        }
        if (request.getAddress() != null && !request.getAddress().isEmpty()) {
            formData.add("address", request.getAddress());
        }
        if (request.getSubAppId() != null && !request.getSubAppId().isEmpty()) {
            formData.add("sub_app_id", request.getSubAppId());
        }
        
        return formData;
    }
    
    /**
     * Build CreateOrderResponse từ ZaloPay response
     */
    private CreateOrderResponse buildCreateOrderResponse(String appTransId, ZaloPayOrderResponse zaloPayResponse) {
        if (zaloPayResponse == null) {
            return CreateOrderResponse.builder()
                    .status("FAILED")
                    .message("Không nhận được response từ ZaloPay")
                    .build();
        }
        
        boolean isSuccess = zaloPayResponse.getReturnCode() != null && zaloPayResponse.getReturnCode() == 1;
        
        return CreateOrderResponse.builder()
                .appTransId(appTransId)
                .orderUrl(zaloPayResponse.getOrderUrl())
                .zpTransToken(zaloPayResponse.getZpTransToken())
                .qrCode(zaloPayResponse.getQrCode())
                .status(isSuccess ? "SUCCESS" : "FAILED")
                .message(zaloPayResponse.getReturnMessage())
                .errorCode(zaloPayResponse.getSubReturnCode())
                .build();
    }
    
    /**
     * Build callback response JSON
     */
    private String buildCallbackResponse(int returnCode, String returnMessage) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("return_code", returnCode);
            response.put("return_message", returnMessage);
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            log.error("Error building callback response: {}", e.getMessage(), e);
            return "{\"return_code\": 0, \"return_message\": \"Error\"}";
        }
    }
    
    /**
     * Lưu transaction vào database
     */
    private void saveTransaction(String appTransId, CreateOrderRequest request, 
                                 String itemJson, String embedDataJson, 
                                 ZaloPayOrderResponse zaloPayResponse) {
        try {
            boolean isSuccess = zaloPayResponse != null && 
                              zaloPayResponse.getReturnCode() != null && 
                              zaloPayResponse.getReturnCode() == 1;
            
            ZaloPayTransaction transaction = ZaloPayTransaction.builder()
                    .appTransId(appTransId)
                    .appUser(request.getAppUser())
                    .amount(request.getAmount())
                    .description(request.getDescription())
                    .items(itemJson)
                    .embedData(embedDataJson)
                    .bankCode(request.getBankCode())
                    .zpTransToken(zaloPayResponse != null ? zaloPayResponse.getZpTransToken() : null)
                    .orderUrl(zaloPayResponse != null ? zaloPayResponse.getOrderUrl() : null)
                    .qrCode(zaloPayResponse != null ? zaloPayResponse.getQrCode() : null)
                    .status(isSuccess ? ZaloPayTransaction.TransactionStatus.PENDING 
                                     : ZaloPayTransaction.TransactionStatus.FAILED)
                    .returnCode(zaloPayResponse != null ? zaloPayResponse.getReturnCode() : null)
                    .returnMessage(zaloPayResponse != null ? zaloPayResponse.getReturnMessage() : null)
                    .email(request.getEmail())  // Lưu email từ request
                    .title(request.getTitle())  // Lưu title từ request
                    .build();
            
            transactionRepository.save(transaction);
            log.info("Saved transaction to database: {}", appTransId);
            
            // Nếu tạo order thất bại, gửi thông báo
            if (!isSuccess) {
                String failureReason = zaloPayResponse != null ? 
                        zaloPayResponse.getReturnMessage() : "Không thể kết nối đến ZaloPay";
                sendPaymentFailedNotification(transaction, failureReason);
            }
            
        } catch (Exception e) {
            log.error("Error saving transaction: {}", e.getMessage(), e);
            // Không throw exception để không block flow tạo đơn hàng
        }
    }
    
    /**
     * Cập nhật transaction khi nhận callback
     */
    private void updateTransactionOnCallback(ZaloPayCallbackData callbackData) {
        try {
            ZaloPayTransaction transaction = transactionRepository
                    .findByAppTransId(callbackData.getAppTransId())
                    .orElseThrow(() -> new RuntimeException(
                            "Transaction not found: " + callbackData.getAppTransId()));
            
            // Gửi thông báo thanh toán thành công TRƯỚC KHI update status (để check PENDING)
            sendPaymentSuccessNotification(transaction);
            
            // Cập nhật thông tin từ callback
            transaction.setZpTransId(callbackData.getZpTransId());
            transaction.setStatus(ZaloPayTransaction.TransactionStatus.SUCCESS);
            transaction.setChannel(callbackData.getChannel());
            transaction.setUserFeeAmount(callbackData.getUserFeeAmount());
            transaction.setDiscountAmount(callbackData.getDiscountAmount());
            
            // Convert server_time từ milliseconds sang LocalDateTime
            if (callbackData.getServerTime() != null) {
                transaction.setPaidAt(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(callbackData.getServerTime()),
                        ZoneId.systemDefault()
                ));
            }
            
            transactionRepository.save(transaction);
            sendFirstMilePaymentConfirmedWebhook(transaction);
            
            log.info("Updated transaction on callback: {}", callbackData.getAppTransId());
            
        } catch (Exception e) {
            log.error("Error updating transaction on callback: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update transaction", e);
        }
    }
    
    /**
     * Gửi thông báo thanh toán thành công qua Kafka envelope.
     */
    @Override
    public void sendPaymentSuccessNotification(ZaloPayTransaction transaction) {
        try {
            if (transaction.getStatus() != ZaloPayTransaction.TransactionStatus.PENDING) {
                log.info("Transaction {} not in PENDING status (current: {}). Skip sending success notification.",
                        transaction.getAppTransId(), transaction.getStatus());
                return;
            }

            String email = resolveEmail(transaction);
            if (email == null || email.isBlank()) {
                log.warn("No email found in transaction: {}. Skip sending success notification.",
                        transaction.getAppTransId());
                return;
            }

            NotificationContext context = resolveNotificationContext(transaction);
            String title = resolveTitle(transaction);
            String key = buildKafkaKey(transaction.getAppUser(), transaction.getAppTransId());

            Map<String, Object> metadata = baseMetadata(transaction);
            metadata.put("status", "SUCCESS");
            metadata.put("paidAt", LocalDateTime.now().toString());

            kafkaMessagePublisher.publishEmailRequested(
                    key,
                    context.tenantId(),
                    context.actorId(),
                    "PAYMENT_TRANSACTION",
                    transaction.getAppTransId(),
                    buildEmailPayload(
                            email,
                            "Thanh toán thành công - " + title,
                            buildPaymentMessageBody(title, transaction.getAppTransId(), transaction.getAmount(), null, true),
                            "PAYMENT_SUCCESS",
                            "MEDIUM",
                            metadata
                    )
            );

            if (context.userId() != null) {
                kafkaMessagePublisher.publishUserNotificationRequested(
                        key,
                        context.tenantId(),
                        context.actorId(),
                        "PAYMENT_TRANSACTION",
                        transaction.getAppTransId(),
                        buildInAppNotificationPayload(
                                context.userId(),
                                context.tenantId(),
                                "Thanh toán thành công",
                                "Đơn hàng " + title + " đã thanh toán thành công.",
                                "SUCCESS",
                                "MEDIUM",
                                transaction.getAppTransId(),
                                metadata
                        )
                );
            }

            log.info("Payment success notification published for transaction={} tenantId={}",
                    transaction.getAppTransId(), context.tenantId());
        } catch (Exception e) {
            log.error("Failed to send payment success notification for transaction: {}. Error: {}",
                    transaction.getAppTransId(), e.getMessage(), e);
        }
    }

    /**
     * Gửi thông báo thanh toán thất bại qua Kafka envelope.
     */
    @Override
    public void sendPaymentFailedNotification(ZaloPayTransaction transaction, String failureReason) {
        try {
            String email = resolveEmail(transaction);
            if (email == null || email.isBlank()) {
                log.warn("No email found in transaction: {}. Skip sending failed notification.",
                        transaction.getAppTransId());
                return;
            }

            NotificationContext context = resolveNotificationContext(transaction);
            String title = resolveTitle(transaction);
            String key = buildKafkaKey(transaction.getAppUser(), transaction.getAppTransId());

            Map<String, Object> metadata = baseMetadata(transaction);
            metadata.put("status", "FAILED");
            metadata.put("failureReason", failureReason);
            metadata.put("failedAt", LocalDateTime.now().toString());

            kafkaMessagePublisher.publishEmailRequested(
                    key,
                    context.tenantId(),
                    context.actorId(),
                    "PAYMENT_TRANSACTION",
                    transaction.getAppTransId(),
                    buildEmailPayload(
                            email,
                            "Thanh toán thất bại - " + title,
                            buildPaymentMessageBody(title, transaction.getAppTransId(), transaction.getAmount(), failureReason, false),
                            "PAYMENT_FAILED",
                            "HIGH",
                            metadata
                    )
            );

            if (context.userId() != null) {
                kafkaMessagePublisher.publishUserNotificationRequested(
                        key,
                        context.tenantId(),
                        context.actorId(),
                        "PAYMENT_TRANSACTION",
                        transaction.getAppTransId(),
                        buildInAppNotificationPayload(
                                context.userId(),
                                context.tenantId(),
                                "Thanh toán thất bại",
                                "Đơn hàng " + title + " thanh toán thất bại: " + failureReason,
                                "ERROR",
                                "HIGH",
                                transaction.getAppTransId(),
                                metadata
                        )
                );
            }

            log.info("Payment failed notification published for transaction={} tenantId={}",
                    transaction.getAppTransId(), context.tenantId());
        } catch (Exception e) {
            log.error("Failed to send payment failed notification for transaction: {}. Error: {}",
                    transaction.getAppTransId(), e.getMessage(), e);
        }
    }

    private String resolveEmail(ZaloPayTransaction transaction) {
        if (transaction.getEmail() != null && !transaction.getEmail().isBlank()) {
            return transaction.getEmail().trim();
        }
        return extractEmailFromEmbedData(transaction.getEmbedData());
    }

    /**
     * Trích xuất email từ embed_data JSON.
     */
    private String extractEmailFromEmbedData(String embedDataJson) {
        try {
            Map<String, Object> embedData = readEmbedData(embedDataJson);
            if (embedData.isEmpty()) {
                return null;
            }
            Object directEmail = embedData.get("email");
            if (directEmail instanceof String value && !value.isBlank()) {
                return value.trim();
            }
            Map<String, Object> merchantInfo = extractMerchantInfo(embedData);
            Object merchantEmail = merchantInfo.get("email");
            if (merchantEmail instanceof String value && !value.isBlank()) {
                return value.trim();
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to extract email from embed_data: {}", e.getMessage());
            return null;
        }
    }

    private String resolveTitle(ZaloPayTransaction transaction) {
        if (transaction.getTitle() == null || transaction.getTitle().isBlank()) {
            return "Đơn hàng #" + transaction.getAppTransId();
        }
        return transaction.getTitle();
    }

    private void sendFirstMilePaymentConfirmedWebhook(ZaloPayTransaction transaction) {
        try {
            if (transaction == null || transaction.getAppTransId() == null || transaction.getAppTransId().isBlank()) {
                return;
            }
            if (transaction.getAppUser() == null || transaction.getAppUser().isBlank()) {
                log.warn("Missing appUser(orderCode). Skip first-mile webhook for appTransId={}", transaction.getAppTransId());
                return;
            }

            NotificationContext context = resolveNotificationContext(transaction);
            FirstMilePaymentConfirmedWebhookRequest webhookRequest = FirstMilePaymentConfirmedWebhookRequest.builder()
                    .appTransId(transaction.getAppTransId())
                    .orderCode(transaction.getAppUser())
                    .tenantId(context.tenantId())
                    .amount(transaction.getAmount())
                    .paidAt(transaction.getPaidAt() != null ? transaction.getPaidAt() : LocalDateTime.now())
                    .gatewayCode("zalopay")
                    .gatewayTransactionId(
                            transaction.getZpTransId() == null
                                    ? null
                                    : String.valueOf(transaction.getZpTransId())
                    )
                    .build();

            webhookEventService.enqueueOrderPaymentConfirmedWebhook(webhookRequest);
        } catch (Exception ex) {
            log.error("Failed to enqueue first-mile payment webhook appTransId={}", transaction.getAppTransId(), ex);
        }
    }

    private NotificationContext resolveNotificationContext(ZaloPayTransaction transaction) {
        Map<String, Object> embedData = readEmbedData(transaction.getEmbedData());
        Map<String, Object> merchantInfo = extractMerchantInfo(embedData);

        Long tenantId = firstNonNull(
                tryParseLong(merchantInfo.get("tenantId")),
                tryParseLong(embedData.get("tenantId")),
                defaultTenantId
        );
        Long actorId = firstNonNull(
                tryParseLong(merchantInfo.get("actorId")),
                tryParseLong(embedData.get("actorId"))
        );
        Long userId = firstNonNull(
                tryParseLong(merchantInfo.get("userId")),
                tryParseLong(embedData.get("userId")),
                tryParseLong(transaction.getAppUser())
        );
        return new NotificationContext(tenantId, actorId, userId);
    }

    private Map<String, Object> readEmbedData(String embedDataJson) {
        try {
            if (embedDataJson == null || embedDataJson.isBlank()) {
                return Collections.emptyMap();
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> embedData = objectMapper.readValue(embedDataJson, Map.class);
            return embedData != null ? embedData : Collections.emptyMap();
        } catch (Exception e) {
            log.warn("Failed to parse embed_data JSON: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<String, Object> extractMerchantInfo(Map<String, Object> embedData) {
        Object rawMerchantInfo = embedData.get("merchantinfo");
        if (rawMerchantInfo == null) {
            rawMerchantInfo = embedData.get("merchantInfo");
        }
        return readMerchantInfo(rawMerchantInfo);
    }

    private Map<String, Object> readMerchantInfo(Object rawMerchantInfo) {
        try {
            if (rawMerchantInfo == null) {
                return new HashMap<>();
            }
            if (rawMerchantInfo instanceof Map<?, ?> sourceMap) {
                Map<String, Object> data = new HashMap<>();
                sourceMap.forEach((key, value) -> data.put(String.valueOf(key), value));
                return data;
            }
            if (rawMerchantInfo instanceof String text && !text.isBlank()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = objectMapper.readValue(text, Map.class);
                return data != null ? new HashMap<>(data) : new HashMap<>();
            }
        } catch (Exception e) {
            log.warn("Failed to parse merchantinfo: {}", e.getMessage());
        }
        return new HashMap<>();
    }

    private Map<String, Object> baseMetadata(ZaloPayTransaction transaction) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("appUser", transaction.getAppUser());
        metadata.put("appTransId", transaction.getAppTransId());
        metadata.put("amount", transaction.getAmount());
        metadata.put("description", transaction.getDescription());
        metadata.put("sourceService", "payment-service");
        return metadata;
    }

    private Map<String, Object> buildEmailPayload(String email, String subject, String body,
                                                  String type, String priority, Map<String, Object> metadata) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("toEmails", List.of(email));
        payload.put("subject", subject);
        payload.put("body", body);
        payload.put("isHtml", false);
        payload.put("type", type);
        payload.put("priority", priority);
        payload.put("provider", "JAVA_MAIL");
        payload.put("metadata", metadata);
        return payload;
    }

    private Map<String, Object> buildInAppNotificationPayload(Long userId, Long tenantId, String title,
                                                              String message, String type, String priority,
                                                              String sourceEventId,
                                                              Map<String, Object> metadata) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", userId);
        payload.put("tenantId", tenantId);
        payload.put("title", title);
        payload.put("message", message);
        payload.put("type", type);
        payload.put("category", "PAYMENT");
        payload.put("priority", priority);
        payload.put("sourceService", "payment-service");
        payload.put("sourceEventId", sourceEventId);
        payload.put("deliveryChannels", List.of("IN_APP"));
        payload.put("metadata", metadata);
        return payload;
    }

    private String buildPaymentMessageBody(String title, String appTransId, Long amount,
                                           String failureReason, boolean success) {
        if (success) {
            return "Đơn hàng " + title + " (mã " + appTransId + ") đã thanh toán thành công với số tiền "
                    + amount + " VND.";
        }
        return "Đơn hàng " + title + " (mã " + appTransId + ") thanh toán thất bại."
                + (failureReason != null && !failureReason.isBlank() ? " Lý do: " + failureReason : "");
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Long tryParseLong(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        if (rawValue instanceof Number number) {
            return number.longValue();
        }
        if (rawValue instanceof String text && !text.isBlank()) {
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private record NotificationContext(Long tenantId, Long actorId, Long userId) {
    }
    
    @Override
    public QueryOrderResponse queryOrderStatus(QueryOrderRequest request) {
        try {
            String appTransId = request.getAppTransId();
            
            log.info("Querying order status - AppTransId: {}", appTransId);
            
            // Tạo HMAC input theo format: app_id|app_trans_id|key1
            String hmacInput = String.format("%d|%s|%s",
                    zaloPayConfig.getAppId(),
                    appTransId,
                    zaloPayConfig.getKey1()
            );
            
            // Tạo MAC
            String mac = ZaloPayHMACUtil.computeHmacSHA256(hmacInput, zaloPayConfig.getKey1());
            
            log.debug("Query MAC Input: {}", hmacInput);
            
            // Build request gửi tới ZaloPay
            ZaloPayQueryRequest zaloPayRequest = ZaloPayQueryRequest.builder()
                    .appId(zaloPayConfig.getAppId())
                    .appTransId(appTransId)
                    .mac(mac)
                    .build();
            
            // Gọi API ZaloPay
            ZaloPayQueryResponse zaloPayResponse = callZaloPayQueryOrderAPI(zaloPayRequest);
            
            // Cập nhật transaction trong database nếu có thay đổi
            updateTransactionFromQuery(appTransId, zaloPayResponse);
            
            // Xử lý response
            return buildQueryOrderResponse(appTransId, zaloPayResponse);
            
        } catch (Exception e) {
            log.error("Error querying order status: {}", e.getMessage(), e);
            return QueryOrderResponse.builder()
                    .appTransId(request.getAppTransId())
                    .status("ERROR")
                    .message("Lỗi truy vấn trạng thái: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Gọi ZaloPay Query Order API
     */
    private ZaloPayQueryResponse callZaloPayQueryOrderAPI(ZaloPayQueryRequest request) {
        try {
            // Sử dụng application/x-www-form-urlencoded
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // Convert request object sang form data
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("app_id", String.valueOf(request.getAppId()));
            formData.add("app_trans_id", request.getAppTransId());
            formData.add("mac", request.getMac());
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
            
            log.info("Calling ZaloPay Query API: {}", zaloPayConfig.getQueryOrderUrl());
            
            ResponseEntity<ZaloPayQueryResponse> response = restTemplate.exchange(
                    zaloPayConfig.getQueryOrderUrl(),
                    HttpMethod.POST,
                    entity,
                    ZaloPayQueryResponse.class
            );
            
            ZaloPayQueryResponse responseBody = response.getBody();
            
            log.info("ZaloPay Query API response - ReturnCode: {}, Message: {}", 
                    responseBody != null ? responseBody.getReturnCode() : null,
                    responseBody != null ? responseBody.getReturnMessage() : null);
            
            return responseBody;
            
        } catch (RestClientException e) {
            log.error("Error calling ZaloPay Query API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call ZaloPay Query API", e);
        }
    }
    
    /**
     * Build QueryOrderResponse từ ZaloPay response
     */
    private QueryOrderResponse buildQueryOrderResponse(String appTransId, ZaloPayQueryResponse zaloPayResponse) {
        if (zaloPayResponse == null) {
            return QueryOrderResponse.builder()
                    .appTransId(appTransId)
                    .status("ERROR")
                    .message("Không nhận được response từ ZaloPay")
                    .errorCategory("SYSTEM")
                    .canRetry(true)
                    .build();
        }
        
        // Map return_code sang status
        String status;
        String errorCategory = null;
        Boolean canRetry = null;
        String errorMessage = null;
        String errorNote = null;
        
        switch (zaloPayResponse.getReturnCode()) {
            case 1 -> status = "SUCCESS";
            case 2 -> {
                status = "FAILED";
                // Xử lý sub_return_code để cung cấp thông tin chi tiết về lỗi
                if (zaloPayResponse.getSubReturnCode() != null) {
                    ZaloPaySubReturnCode subCode = ZaloPaySubReturnCode.fromCode(
                            zaloPayResponse.getSubReturnCode()
                    );
                    
                    errorMessage = subCode.getDescription();
                    errorNote = subCode.getNote();
                    canRetry = subCode.shouldRetry();
                    
                    // Xác định error category
                    if (subCode.isUserActionable()) {
                        errorCategory = "USER";
                    } else if (subCode.isMerchantError()) {
                        errorCategory = "MERCHANT";
                    } else if (subCode.isSystemError()) {
                        errorCategory = "SYSTEM";
                    } else {
                        errorCategory = "UNKNOWN";
                    }
                    
                    log.warn("Payment query failed - AppTransId: {}, SubCode: {}, Category: {}, Description: {}", 
                            appTransId, zaloPayResponse.getSubReturnCode(), errorCategory, errorMessage);
                }
            }
            case 3 -> {
                status = "PENDING";
                // Kiểm tra is_processing để xác định chính xác trạng thái
                if (Boolean.TRUE.equals(zaloPayResponse.getIsProcessing())) {
                    status = "PROCESSING";
                }
            }
            default -> {
                status = "UNKNOWN";
                errorCategory = "SYSTEM";
                log.error("Unknown return_code from ZaloPay: {} for AppTransId: {}", 
                        zaloPayResponse.getReturnCode(), appTransId);
            }
        }

        return QueryOrderResponse.builder()
                .appTransId(appTransId)
                .zpTransId(zaloPayResponse.getZpTransId())
                .status(status)
                .message(zaloPayResponse.getReturnMessage())
                .amount(zaloPayResponse.getAmount())
                .discountAmount(zaloPayResponse.getDiscountAmount())
                .isProcessing(zaloPayResponse.getIsProcessing())
                .errorCode(zaloPayResponse.getSubReturnCode())
                .errorMessage(errorMessage != null ? errorMessage : zaloPayResponse.getSubReturnMessage())
                .errorNote(errorNote)
                .canRetry(canRetry)
                .errorCategory(errorCategory)
                .build();
    }
    
    /**
     * Cập nhật transaction từ query response
     */
    private void updateTransactionFromQuery(String appTransId, ZaloPayQueryResponse queryResponse) {
        try {
            if (queryResponse == null) {
                return;
            }
            
            ZaloPayTransaction transaction = transactionRepository
                    .findByAppTransId(appTransId)
                    .orElse(null);
            
            if (transaction == null) {
                log.warn("Transaction not found for query update: {}", appTransId);
                return;
            }
            
            // Chỉ update nếu status hiện tại là PENDING (tránh ghi đè status đã xử lý)
            if (transaction.getStatus() != ZaloPayTransaction.TransactionStatus.PENDING) {
                log.info("Transaction {} already processed with status: {}. Skip update.", 
                        appTransId, transaction.getStatus());
                return;
            }
            
            // Xử lý theo return_code từ ZaloPay
            // 1 = SUCCESS, 2 = FAILED, 3 = PENDING
            switch (queryResponse.getReturnCode()) {
                case 1: // Thanh toán thành công
                    // Gửi thông báo TRƯỚC KHI update status
                    sendPaymentSuccessNotification(transaction);
                    
                    transaction.setZpTransId(queryResponse.getZpTransId());
                    transaction.setStatus(ZaloPayTransaction.TransactionStatus.SUCCESS);
                    transaction.setDiscountAmount(queryResponse.getDiscountAmount());
                    transaction.setPaidAt(LocalDateTime.now());
                    transactionRepository.save(transaction);
                    sendFirstMilePaymentConfirmedWebhook(transaction);
                    
                    log.info("Updated transaction from query to SUCCESS: {}", appTransId);
                    break;
                    
                case 2: // Thanh toán thất bại
                    // Xử lý chi tiết lỗi từ sub_return_code
                    String failureReason = queryResponse.getReturnMessage();
                    
                    if (queryResponse.getSubReturnCode() != null) {
                        ZaloPaySubReturnCode subCode = ZaloPaySubReturnCode.fromCode(
                                queryResponse.getSubReturnCode()
                        );
                        
                        // Tạo thông báo lỗi chi tiết
                        failureReason = String.format("%s - %s", 
                                subCode.getDescription(), 
                                subCode.getNote());
                        
                        log.warn("Payment failed with detailed error - AppTransId: {}, SubCode: {} ({}), Category: {}, Retry: {}", 
                                appTransId, 
                                queryResponse.getSubReturnCode(),
                                subCode.name(),
                                subCode.isMerchantError() ? "MERCHANT" : 
                                    (subCode.isUserActionable() ? "USER" : 
                                        (subCode.isSystemError() ? "SYSTEM" : "UNKNOWN")),
                                subCode.shouldRetry());
                    }
                    
                    // Gửi thông báo TRƯỚC KHI update status
                    sendPaymentFailedNotification(transaction, failureReason);
                    
                    transaction.setStatus(ZaloPayTransaction.TransactionStatus.FAILED);
                    transaction.setReturnCode(queryResponse.getReturnCode());
                    transaction.setReturnMessage(failureReason);
                    
                    // Lưu thêm thông tin về sub_return_code để trace
                    if (queryResponse.getSubReturnCode() != null) {
                        transaction.setSubReturnCode(queryResponse.getSubReturnCode());
                        transaction.setSubReturnMessage(queryResponse.getSubReturnMessage());
                    }
                    
                    transactionRepository.save(transaction);
                    
                    log.info("Updated transaction from query to FAILED: {}", appTransId);
                    break;
                    
                case 3: // Vẫn đang xử lý
                    log.info("Transaction {} still PENDING. No update needed.", appTransId);
                    break;
                    
                default:
                    log.warn("Unknown return code {} for transaction: {}", 
                            queryResponse.getReturnCode(), appTransId);
            }
            
        } catch (Exception e) {
            log.error("Error updating transaction from query: {}", e.getMessage(), e);
            // Không throw exception để không block query response
        }
    }
    
    @Override
    public GetBankListResponse getBankList() {
        try {
            log.info("Getting bank list from ZaloPay");
            
            // Lấy thời gian hiện tại (milliseconds)
            long reqTime = System.currentTimeMillis();
            
            // Tạo HMAC input theo format: appid|reqtime
            String hmacInput = String.format("%d|%d",
                    zaloPayConfig.getAppId(),
                    reqTime
            );
            
            // Tạo MAC
            String mac = ZaloPayHMACUtil.computeHmacSHA256(hmacInput, zaloPayConfig.getKey1());
            
            log.debug("GetBankList MAC Input: {}", hmacInput);
            
            // Gọi API ZaloPay
            GetBankListResponse response = callZaloPayGetBankListAPI(reqTime, mac);
            
            if (response != null && response.getReturnCode() != null && response.getReturnCode() == 1) {
                log.info("Get bank list successfully - Total PMCs: {}",
                        response.getBanks() != null ? response.getBanks().size() : 0);
            } else {
                log.warn("Get bank list failed - Code: {}, Message: {}",
                        response != null ? response.getReturnCode() : null,
                        response != null ? response.getReturnMessage() : null);
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error getting bank list: {}", e.getMessage(), e);
            return GetBankListResponse.builder()
                    .returnCode(0)
                    .returnMessage("Lỗi lấy danh sách ngân hàng: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Gọi ZaloPay Get Bank List API
     */
    private GetBankListResponse callZaloPayGetBankListAPI(long reqTime, String mac) {
        try {
            // Sử dụng application/x-www-form-urlencoded
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // Convert request object sang form data
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("appid", String.valueOf(zaloPayConfig.getAppId()));
            formData.add("reqtime", String.valueOf(reqTime));
            formData.add("mac", mac);
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
            
            String getBankListUrl = zaloPayConfig.getGetBankListUrl();
            log.info("Calling ZaloPay Get Bank List API: {}", getBankListUrl);
            
            ResponseEntity<GetBankListResponse> response = restTemplate.exchange(
                    getBankListUrl,
                    HttpMethod.POST,
                    entity,
                    GetBankListResponse.class
            );
            
            GetBankListResponse responseBody = response.getBody();
            
            log.info("ZaloPay Get Bank List API response - ReturnCode: {}, Message: {}", 
                    responseBody != null ? responseBody.getReturnCode() : null,
                    responseBody != null ? responseBody.getReturnMessage() : null);
            
            return responseBody;
            
        } catch (RestClientException e) {
            log.error("Error calling ZaloPay Get Bank List API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call ZaloPay Get Bank List API", e);
        }
    }
    
    @Override
    @Transactional
    public RefundResponse refundOrder(RefundRequest request) {
        try {
            log.info("Processing refund request - TransactionId: {}, Amount: {}", 
                    request.getZpTransId(), request.getAmount());
            
            // Tìm original transaction để lấy thông tin
            // Hỗ trợ cả appTransId (format: yymmdd_xxxxxx) và zpTransId (số Long thuần túy)
            ZaloPayTransaction originalTransaction = null;
            String actualZpTransId = request.getZpTransId();
            
            if (request.getZpTransId() != null && request.getZpTransId().contains("_")) {
                // Đây là appTransId, tìm bằng appTransId và lấy zpTransId thực
                log.info("Input is appTransId format, searching by appTransId: {}", request.getZpTransId());
                originalTransaction = transactionRepository
                        .findByAppTransId(request.getZpTransId())
                        .orElse(null);
                
                if (originalTransaction != null && originalTransaction.getZpTransId() != null) {
                    actualZpTransId = String.valueOf(originalTransaction.getZpTransId());
                    log.info("Found transaction, actual zpTransId: {}", actualZpTransId);
                } else {
                    log.error("Transaction not found or zpTransId is null for appTransId: {}", request.getZpTransId());
                    return RefundResponse.builder()
                            .status("FAILED")
                            .message("Không tìm thấy giao dịch gốc hoặc giao dịch chưa được thanh toán")
                            .zpTransId(request.getZpTransId())
                            .build();
                }
            } else {
                // Đây là zpTransId thuần túy
                try {
                    originalTransaction = transactionRepository
                            .findByZpTransId(Long.parseLong(request.getZpTransId()))
                            .orElse(null);
                } catch (NumberFormatException e) {
                    log.error("Invalid zpTransId format: {}", request.getZpTransId());
                    return RefundResponse.builder()
                            .status("FAILED")
                            .message("Mã giao dịch không hợp lệ: " + request.getZpTransId())
                            .zpTransId(request.getZpTransId())
                            .build();
                }
            }
            
            if (originalTransaction == null) {
                log.warn("Original transaction not found for TransactionId: {}", request.getZpTransId());
            }
            
            // Cập nhật request với zpTransId thực để gửi tới ZaloPay
            request.setZpTransId(actualZpTransId);
            
            // Generate m_refund_id theo format: yymmdd_appid_xxxx
            String mRefundId = generateMRefundId();
            
            // Lấy thời gian hiện tại (milliseconds)
            long timestamp = System.currentTimeMillis();
            
            // Tạo HMAC input theo documentation ZaloPay
            String hmacInput;
            if (request.getRefundFeeAmount() != null && request.getRefundFeeAmount() > 0) {
                // Có phí hoàn: app_id|zp_trans_id|amount|refund_fee_amount|description|timestamp
                hmacInput = String.format("%d|%s|%d|%d|%s|%d",
                        zaloPayConfig.getAppId(),
                        request.getZpTransId(),
                        request.getAmount(),
                        request.getRefundFeeAmount(),
                        request.getDescription(),
                        timestamp
                );
            } else {
                // Không có phí hoàn: app_id|zp_trans_id|amount|description|timestamp
                hmacInput = String.format("%d|%s|%d|%s|%d",
                        zaloPayConfig.getAppId(),
                        request.getZpTransId(),
                        request.getAmount(),
                        request.getDescription(),
                        timestamp
                );
            }
            
            // Tạo MAC
            String mac = ZaloPayHMACUtil.computeHmacSHA256(hmacInput, zaloPayConfig.getKey1());
            
            log.info("Refund request - MRefundId: {}, MAC Input: {}", mRefundId, hmacInput);
            
            // Build request gửi tới ZaloPay
            ZaloPayRefundRequest zaloPayRequest = ZaloPayRefundRequest.builder()
                    .appId(zaloPayConfig.getAppId())
                    .mRefundId(mRefundId)
                    .zpTransId(request.getZpTransId())
                    .amount(request.getAmount())
                    .refundFeeAmount(request.getRefundFeeAmount())
                    .timestamp(timestamp)
                    .description(request.getDescription())
                    .mac(mac)
                    .build();
            
            // Gọi API ZaloPay
            ZaloPayRefundResponse zaloPayResponse = callZaloPayRefundAPI(zaloPayRequest);
            
            // Lưu refund transaction vào database
            saveRefundTransaction(mRefundId, request, originalTransaction, zaloPayResponse);
            
            // Xử lý response
            return buildRefundResponse(mRefundId, request, zaloPayResponse);
            
        } catch (Exception e) {
            log.error("Error processing refund: {}", e.getMessage(), e);
            return RefundResponse.builder()
                    .status("FAILED")
                    .message("Lỗi xử lý hoàn tiền: " + e.getMessage())
                    .zpTransId(request.getZpTransId())
                    .build();
        }
    }
    
    /**
     * Generate m_refund_id theo format: yymmdd_appid_xxxx
     */
    private String generateMRefundId() {
        // Lấy thời gian hiện tại theo GMT+7
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        sdf.setCalendar(cal);
        String datePrefix = sdf.format(cal.getTimeInMillis());

        // Sinh số ngẫu nhiên 10 chữ số (0 -> 9999999999)
        Random rand = new Random();
        long randomId = Math.abs(rand.nextLong() % 1_000_000_0000L);
        String randomStr = String.format("%010d", randomId);

        // Trả về kết quả đúng format yêu cầu
        return String.format("%s_%d_%s", datePrefix, zaloPayConfig.getAppId(), randomStr);
    }
    
    /**
     * Gọi ZaloPay Refund API
     */
    private ZaloPayRefundResponse callZaloPayRefundAPI(ZaloPayRefundRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<ZaloPayRefundRequest> entity = new HttpEntity<>(request, headers);
            
            log.info("Calling ZaloPay Refund API: {}", zaloPayConfig.getRefundUrl());
            log.debug("Request payload: appId={}, mRefundId={}, zpTransId={}, amount={}, description={}", 
                    request.getAppId(), request.getMRefundId(), request.getZpTransId(), 
                    request.getAmount(), request.getDescription());
            
            ResponseEntity<ZaloPayRefundResponse> response = restTemplate.exchange(
                    zaloPayConfig.getRefundUrl(),
                    HttpMethod.POST,
                    entity,
                    ZaloPayRefundResponse.class
            );
            
            ZaloPayRefundResponse responseBody = response.getBody();
            
            log.info("ZaloPay Refund API response - ReturnCode: {}, SubReturnCode: {}, Message: {}", 
                    responseBody != null ? responseBody.getReturnCode() : null,
                    responseBody != null ? responseBody.getSubReturnCode() : null,
                    responseBody != null ? responseBody.getReturnMessage() : null);
            
            return responseBody;
            
        } catch (RestClientException e) {
            log.error("Error calling ZaloPay Refund API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call ZaloPay Refund API", e);
        }
    }
    
    /**
     * Lưu refund transaction vào database
     */
    private void saveRefundTransaction(String mRefundId, RefundRequest request,
                                       ZaloPayTransaction originalTransaction,
                                       ZaloPayRefundResponse zaloPayResponse) {
        try {
            ZaloPayRefundTransaction.RefundStatus status;
            
            if (zaloPayResponse != null && zaloPayResponse.getReturnCode() != null) {
                status = switch (zaloPayResponse.getReturnCode()) {
                    case 1 -> ZaloPayRefundTransaction.RefundStatus.SUCCESS;
                    case 2 -> ZaloPayRefundTransaction.RefundStatus.FAILED;
                    case 3 -> ZaloPayRefundTransaction.RefundStatus.PROCESSING;
                    default -> ZaloPayRefundTransaction.RefundStatus.FAILED;
                };
            } else {
                status = ZaloPayRefundTransaction.RefundStatus.FAILED;
            }
            
            ZaloPayRefundTransaction refundTransaction = ZaloPayRefundTransaction.builder()
                    .mRefundId(mRefundId)
                    .zpTransId(request.getZpTransId())
                    .appTransId(originalTransaction != null ? originalTransaction.getAppTransId() : null)
                    .amount(request.getAmount())
                    .refundFeeAmount(request.getRefundFeeAmount())
                    .description(request.getDescription())
                    .status(status)
                    .returnCode(zaloPayResponse != null ? zaloPayResponse.getReturnCode() : null)
                    .returnMessage(zaloPayResponse != null ? zaloPayResponse.getReturnMessage() : null)
                    .subReturnCode(zaloPayResponse != null ? zaloPayResponse.getSubReturnCode() : null)
                    .subReturnMessage(zaloPayResponse != null ? zaloPayResponse.getSubReturnMessage() : null)
                    .refundId(zaloPayResponse != null ? zaloPayResponse.getRefundId() : null)
                    .refundedAt(status == ZaloPayRefundTransaction.RefundStatus.SUCCESS ? 
                            LocalDateTime.now() : null)
                    .build();
            
            refundTransactionRepository.save(refundTransaction);
            
            log.info("Saved refund transaction to database: {}", mRefundId);
            
        } catch (Exception e) {
            log.error("Error saving refund transaction: {}", e.getMessage(), e);
            // Không throw exception để không block flow hoàn tiền
        }
    }
    
    /**
     * Build RefundResponse từ ZaloPay response
     */
    private RefundResponse buildRefundResponse(String mRefundId, RefundRequest request,
                                               ZaloPayRefundResponse zaloPayResponse) {
        if (zaloPayResponse == null) {
            return RefundResponse.builder()
                    .mRefundId(mRefundId)
                    .zpTransId(request.getZpTransId())
                    .amount(request.getAmount())
                    .status("FAILED")
                    .message("Không nhận được response từ ZaloPay")
                    .errorCategory("SYSTEM")
                    .canRetry(true)
                    .build();
        }
        
        // Map return_code sang status
        String status;
        String errorCategory = null;
        Boolean canRetry = null;
        String errorMessage = null;
        String errorNote = null;
        
        switch (zaloPayResponse.getReturnCode()) {
            case 1 -> status = "SUCCESS";
            case 2 -> {
                status = "FAILED";
                // Xử lý sub_return_code để cung cấp thông tin chi tiết về lỗi
                if (zaloPayResponse.getSubReturnCode() != null) {
                    ZaloPaySubReturnCode subCode = ZaloPaySubReturnCode.fromCode(
                            zaloPayResponse.getSubReturnCode()
                    );
                    
                    errorMessage = subCode.getDescription();
                    errorNote = subCode.getNote();
                    canRetry = subCode.shouldRetry();
                    
                    // Xác định error category
                    if (subCode.isUserActionable()) {
                        errorCategory = "USER";
                    } else if (subCode.isMerchantError()) {
                        errorCategory = "MERCHANT";
                    } else if (subCode.isSystemError()) {
                        errorCategory = "SYSTEM";
                    } else {
                        errorCategory = "UNKNOWN";
                    }
                    
                    log.warn("Refund failed - MRefundId: {}, SubCode: {}, Category: {}, Description: {}", 
                            mRefundId, zaloPayResponse.getSubReturnCode(), errorCategory, errorMessage);
                }
            }
            case 3 -> status = "PROCESSING";
            default -> {
                status = "UNKNOWN";
                errorCategory = "SYSTEM";
                log.error("Unknown return_code from ZaloPay Refund: {} for MRefundId: {}", 
                        zaloPayResponse.getReturnCode(), mRefundId);
            }
        }

        return RefundResponse.builder()
                .mRefundId(mRefundId)
                .zpTransId(request.getZpTransId())
                .refundId(zaloPayResponse.getRefundId())
                .amount(request.getAmount())
                .status(status)
                .message(zaloPayResponse.getReturnMessage())
                .errorCode(zaloPayResponse.getSubReturnCode())
                .errorMessage(errorMessage != null ? errorMessage : zaloPayResponse.getSubReturnMessage())
                .errorNote(errorNote)
                .canRetry(canRetry)
                .errorCategory(errorCategory)
                .build();
    }
    
    @Override
    public QueryRefundResponse queryRefundStatus(QueryRefundRequest request) {
        try {
            String mRefundId = request.getMRefundId();
            
            log.info("Querying refund status - MRefundId: {}", mRefundId);
            
            // Lấy thời gian hiện tại (milliseconds)
            long timestamp = System.currentTimeMillis();
            
            // Tạo HMAC input theo format: app_id|m_refund_id|timestamp
            String hmacInput = String.format("%d|%s|%d",
                    zaloPayConfig.getAppId(),
                    mRefundId,
                    timestamp
            );
            
            // Tạo MAC
            String mac = ZaloPayHMACUtil.computeHmacSHA256(hmacInput, zaloPayConfig.getKey1());
            
            log.debug("Query Refund MAC Input: {}", hmacInput);
            
            // Build request gửi tới ZaloPay
            ZaloPayQueryRefundRequest zaloPayRequest = ZaloPayQueryRefundRequest.builder()
                    .appId(zaloPayConfig.getAppId())
                    .mRefundId(mRefundId)
                    .timestamp(timestamp)
                    .mac(mac)
                    .build();
            
            // Gọi API ZaloPay
            ZaloPayQueryRefundResponse zaloPayResponse = callZaloPayQueryRefundAPI(zaloPayRequest);
            
            // Cập nhật refund transaction trong database nếu có thay đổi
            updateRefundTransactionFromQuery(mRefundId, zaloPayResponse);
            
            // Xử lý response
            return buildQueryRefundResponse(mRefundId, zaloPayResponse);
            
        } catch (Exception e) {
            log.error("Error querying refund status: {}", e.getMessage(), e);
            return QueryRefundResponse.builder()
                    .mRefundId(request.getMRefundId())
                    .status("ERROR")
                    .message("Lỗi truy vấn trạng thái hoàn tiền: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Gọi ZaloPay Query Refund API
     */
    private ZaloPayQueryRefundResponse callZaloPayQueryRefundAPI(ZaloPayQueryRefundRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<ZaloPayQueryRefundRequest> entity = new HttpEntity<>(request, headers);
            
            log.info("Calling ZaloPay Query Refund API: {}", zaloPayConfig.getQueryRefundUrl());
            
            ResponseEntity<ZaloPayQueryRefundResponse> response = restTemplate.exchange(
                    zaloPayConfig.getQueryRefundUrl(),
                    HttpMethod.POST,
                    entity,
                    ZaloPayQueryRefundResponse.class
            );
            
            ZaloPayQueryRefundResponse responseBody = response.getBody();
            
            log.info("ZaloPay Query Refund API response - ReturnCode: {}, Message: {}", 
                    responseBody != null ? responseBody.getReturnCode() : null,
                    responseBody != null ? responseBody.getReturnMessage() : null);
            
            return responseBody;
            
        } catch (RestClientException e) {
            log.error("Error calling ZaloPay Query Refund API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call ZaloPay Query Refund API", e);
        }
    }
    
    /**
     * Build QueryRefundResponse từ ZaloPay response
     */
    private QueryRefundResponse buildQueryRefundResponse(String mRefundId, 
                                                         ZaloPayQueryRefundResponse zaloPayResponse) {
        if (zaloPayResponse == null) {
            return QueryRefundResponse.builder()
                    .mRefundId(mRefundId)
                    .status("ERROR")
                    .message("Không nhận được response từ ZaloPay")
                    .errorCategory("SYSTEM")
                    .canRetry(true)
                    .build();
        }
        
        // Map return_code sang status
        String status;
        String errorCategory = null;
        Boolean canRetry = null;
        String errorMessage = null;
        String errorNote = null;
        
        switch (zaloPayResponse.getReturnCode()) {
            case 1 -> status = "SUCCESS";
            case 2 -> {
                status = "FAILED";
                // Xử lý sub_return_code để cung cấp thông tin chi tiết về lỗi
                if (zaloPayResponse.getSubReturnCode() != null) {
                    ZaloPaySubReturnCode subCode = ZaloPaySubReturnCode.fromCode(
                            zaloPayResponse.getSubReturnCode()
                    );
                    
                    errorMessage = subCode.getDescription();
                    errorNote = subCode.getNote();
                    canRetry = subCode.shouldRetry();
                    
                    // Xác định error category
                    if (subCode.isUserActionable()) {
                        errorCategory = "USER";
                    } else if (subCode.isMerchantError()) {
                        errorCategory = "MERCHANT";
                    } else if (subCode.isSystemError()) {
                        errorCategory = "SYSTEM";
                    } else {
                        errorCategory = "UNKNOWN";
                    }
                    
                    log.warn("Query refund failed - MRefundId: {}, SubCode: {}, Category: {}, Description: {}", 
                            mRefundId, zaloPayResponse.getSubReturnCode(), errorCategory, errorMessage);
                }
            }
            case 3 -> status = "PROCESSING";
            default -> {
                // Kiểm tra các trường hợp đặc biệt
                if (zaloPayResponse.getSubReturnCode() != null && 
                    zaloPayResponse.getSubReturnCode() == -1) {
                    status = "PENDING";  // Hoàn tiền chờ phê duyệt
                } else {
                    status = "UNKNOWN";
                    errorCategory = "SYSTEM";
                    log.error("Unknown return_code from ZaloPay Query Refund: {} for MRefundId: {}", 
                            zaloPayResponse.getReturnCode(), mRefundId);
                }
            }
        }

        return QueryRefundResponse.builder()
                .mRefundId(mRefundId)
                .status(status)
                .message(zaloPayResponse.getReturnMessage())
                .errorCode(zaloPayResponse.getSubReturnCode())
                .errorMessage(errorMessage != null ? errorMessage : zaloPayResponse.getSubReturnMessage())
                .errorNote(errorNote)
                .canRetry(canRetry)
                .errorCategory(errorCategory)
                .build();
    }
    
    /**
     * Cập nhật refund transaction từ query response
     */
    private void updateRefundTransactionFromQuery(String mRefundId, 
                                                  ZaloPayQueryRefundResponse queryResponse) {
        try {
            if (queryResponse == null) {
                return;
            }
            
            ZaloPayRefundTransaction refundTransaction = refundTransactionRepository
                    .findBymRefundId(mRefundId)
                    .orElse(null);
            
            if (refundTransaction == null) {
                log.warn("Refund transaction not found for query update: {}", mRefundId);
                return;
            }
            
            // Chỉ update nếu status hiện tại là PROCESSING (tránh ghi đè status đã xử lý)
            if (refundTransaction.getStatus() != ZaloPayRefundTransaction.RefundStatus.PROCESSING) {
                log.info("Refund transaction {} already processed with status: {}. Skip update.", 
                        mRefundId, refundTransaction.getStatus());
                return;
            }
            
            // Xử lý theo return_code từ ZaloPay
            // 1 = SUCCESS, 2 = FAILED, 3 = PROCESSING
            switch (queryResponse.getReturnCode()) {
                case 1: // Hoàn tiền thành công
                    refundTransaction.setStatus(ZaloPayRefundTransaction.RefundStatus.SUCCESS);
                    refundTransaction.setRefundedAt(LocalDateTime.now());
                    refundTransactionRepository.save(refundTransaction);
                    
                    log.info("Updated refund transaction from query to SUCCESS: {}", mRefundId);
                    
                    // Gửi thông báo thành công
                    ZaloPayTransaction originalTransaction = transactionRepository
                            .findByZpTransId(Long.parseLong(refundTransaction.getZpTransId()))
                            .orElse(null);
                    sendRefundSuccessNotification(refundTransaction, originalTransaction);
                    break;
                    
                case 2: // Hoàn tiền thất bại
                    String failureReason = queryResponse.getReturnMessage();
                    
                    if (queryResponse.getSubReturnCode() != null) {
                        ZaloPaySubReturnCode subCode = ZaloPaySubReturnCode.fromCode(
                                queryResponse.getSubReturnCode()
                        );
                        
                        // Tạo thông báo lỗi chi tiết
                        failureReason = String.format("%s - %s", 
                                subCode.getDescription(), 
                                subCode.getNote());
                        
                        log.warn("Refund failed with detailed error - MRefundId: {}, SubCode: {} ({}), Category: {}, Retry: {}", 
                                mRefundId, 
                                queryResponse.getSubReturnCode(),
                                subCode.name(),
                                subCode.isMerchantError() ? "MERCHANT" : 
                                    (subCode.isUserActionable() ? "USER" : 
                                        (subCode.isSystemError() ? "SYSTEM" : "UNKNOWN")),
                                subCode.shouldRetry());
                    }
                    
                    refundTransaction.setStatus(ZaloPayRefundTransaction.RefundStatus.FAILED);
                    refundTransaction.setReturnCode(queryResponse.getReturnCode());
                    refundTransaction.setReturnMessage(failureReason);
                    
                    // Lưu thêm thông tin về sub_return_code để trace
                    if (queryResponse.getSubReturnCode() != null) {
                        refundTransaction.setSubReturnCode(queryResponse.getSubReturnCode());
                        refundTransaction.setSubReturnMessage(queryResponse.getSubReturnMessage());
                    }
                    
                    refundTransactionRepository.save(refundTransaction);
                    
                    log.info("Updated refund transaction from query to FAILED: {}", mRefundId);
                    
                    // Gửi thông báo thất bại
                    ZaloPayTransaction originalTxn = transactionRepository
                            .findByZpTransId(Long.parseLong(refundTransaction.getZpTransId()))
                            .orElse(null);
                    sendRefundFailedNotification(refundTransaction, originalTxn, failureReason);
                    break;
                    
                case 3: // Vẫn đang xử lý
                    log.info("Refund transaction {} still PROCESSING. No update needed.", mRefundId);
                    break;
                    
                default:
                    log.warn("Unknown return code {} for refund transaction: {}", 
                            queryResponse.getReturnCode(), mRefundId);
            }
            
        } catch (Exception e) {
            log.error("Error updating refund transaction from query: {}", e.getMessage(), e);
            // Không throw exception để không block query response
        }
    }
    
    @Override
    public void sendRefundSuccessNotification(ZaloPayRefundTransaction refundTransaction,
                                              ZaloPayTransaction originalTransaction) {
        try {
            if (originalTransaction == null) {
                log.warn("Original transaction not found for refund: {}. Skip sending notification.", 
                        refundTransaction.getMRefundId());
                return;
            }
            
            // Lấy email từ original transaction
            String email = originalTransaction.getEmail();
            
            if (email == null || email.isEmpty()) {
                email = extractEmailFromEmbedData(originalTransaction.getEmbedData());
            }
            
            if (email == null || email.isEmpty()) {
                log.warn("No email found for refund: {}. Skip sending notification.", 
                        refundTransaction.getMRefundId());
                return;
            }

            NotificationContext context = resolveNotificationContext(originalTransaction);
            String title = resolveTitle(originalTransaction);
            String key = buildKafkaKey(originalTransaction.getAppUser(), refundTransaction.getMRefundId());

            Map<String, Object> metadata = baseMetadata(originalTransaction);
            metadata.put("status", "SUCCESS");
            metadata.put("mRefundId", refundTransaction.getMRefundId());
            metadata.put("zpTransId", refundTransaction.getZpTransId());
            metadata.put("refundAmount", refundTransaction.getAmount());
            metadata.put("refundFeeAmount", refundTransaction.getRefundFeeAmount());
            metadata.put("refundedAt", refundTransaction.getRefundedAt() != null
                    ? refundTransaction.getRefundedAt().toString()
                    : LocalDateTime.now().toString());

            kafkaMessagePublisher.publishEmailRequested(
                    key,
                    context.tenantId(),
                    context.actorId(),
                    "PAYMENT_REFUND",
                    refundTransaction.getMRefundId(),
                    buildEmailPayload(
                            email,
                            "Hoàn tiền thành công - " + title,
                            "Yêu cầu hoàn tiền " + refundTransaction.getMRefundId() + " đã được xử lý thành công.",
                            "REFUND_SUCCESS",
                            "MEDIUM",
                            metadata
                    )
            );

            if (context.userId() != null) {
                kafkaMessagePublisher.publishUserNotificationRequested(
                        key,
                        context.tenantId(),
                        context.actorId(),
                        "PAYMENT_REFUND",
                        refundTransaction.getMRefundId(),
                        buildInAppNotificationPayload(
                                context.userId(),
                                context.tenantId(),
                                "Hoàn tiền thành công",
                                "Yêu cầu hoàn tiền " + refundTransaction.getMRefundId() + " đã thành công.",
                                "SUCCESS",
                                "MEDIUM",
                                refundTransaction.getMRefundId(),
                                metadata
                        )
                );
            }

            log.info("Refund success notification published for mRefundId={} tenantId={}",
                    refundTransaction.getMRefundId(), context.tenantId());
        } catch (Exception e) {
            log.error("Failed to send refund success notification for: {}. Error: {}", 
                    refundTransaction.getMRefundId(), e.getMessage(), e);
            // Không throw exception để không block flow chính
        }
    }
    
    @Override
    public void sendRefundFailedNotification(ZaloPayRefundTransaction refundTransaction,
                                             ZaloPayTransaction originalTransaction,
                                             String failureReason) {
        try {
            if (originalTransaction == null) {
                log.warn("Original transaction not found for refund: {}. Skip sending notification.", 
                        refundTransaction.getMRefundId());
                return;
            }
            
            // Lấy email từ original transaction
            String email = originalTransaction.getEmail();
            
            if (email == null || email.isEmpty()) {
                email = extractEmailFromEmbedData(originalTransaction.getEmbedData());
            }
            
            if (email == null || email.isEmpty()) {
                log.warn("No email found for refund: {}. Skip sending notification.", 
                        refundTransaction.getMRefundId());
                return;
            }

            NotificationContext context = resolveNotificationContext(originalTransaction);
            String title = resolveTitle(originalTransaction);
            String key = buildKafkaKey(originalTransaction.getAppUser(), refundTransaction.getMRefundId());

            Map<String, Object> metadata = baseMetadata(originalTransaction);
            metadata.put("status", "FAILED");
            metadata.put("mRefundId", refundTransaction.getMRefundId());
            metadata.put("zpTransId", refundTransaction.getZpTransId());
            metadata.put("refundAmount", refundTransaction.getAmount());
            metadata.put("failureReason", failureReason);
            metadata.put("failedAt", LocalDateTime.now().toString());

            kafkaMessagePublisher.publishEmailRequested(
                    key,
                    context.tenantId(),
                    context.actorId(),
                    "PAYMENT_REFUND",
                    refundTransaction.getMRefundId(),
                    buildEmailPayload(
                            email,
                            "Hoàn tiền thất bại - " + title,
                            "Yêu cầu hoàn tiền " + refundTransaction.getMRefundId() + " thất bại: " + failureReason,
                            "REFUND_FAILED",
                            "HIGH",
                            metadata
                    )
            );

            if (context.userId() != null) {
                kafkaMessagePublisher.publishUserNotificationRequested(
                        key,
                        context.tenantId(),
                        context.actorId(),
                        "PAYMENT_REFUND",
                        refundTransaction.getMRefundId(),
                        buildInAppNotificationPayload(
                                context.userId(),
                                context.tenantId(),
                                "Hoàn tiền thất bại",
                                "Yêu cầu hoàn tiền " + refundTransaction.getMRefundId()
                                        + " thất bại: " + failureReason,
                                "ERROR",
                                "HIGH",
                                refundTransaction.getMRefundId(),
                                metadata
                        )
                );
            }

            log.info("Refund failed notification published for mRefundId={} tenantId={}",
                    refundTransaction.getMRefundId(), context.tenantId());
        } catch (Exception e) {
            log.error("Failed to send refund failed notification for: {}. Error: {}", 
                    refundTransaction.getMRefundId(), e.getMessage(), e);
            // Không throw exception để không block flow chính
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public TransactionHistoryResponse getTransactionHistory(TransactionHistoryFilterRequest filter) {
        try {
            // Validate và set default values cho pagination
            int page = filter.getPage() != null && filter.getPage() >= 0 ? filter.getPage() : 0;
            int size = filter.getSize() != null && filter.getSize() > 0 && filter.getSize() <= 100 
                    ? filter.getSize() : 20;
            
            // Validate sort field
            String sortBy = validateAndGetSortField(filter.getSortBy());
            
            // Validate sort direction
            Sort.Direction direction = "ASC".equalsIgnoreCase(filter.getSortDirection()) 
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            
            // Tạo Pageable
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            // Tạo Specification từ filter
            Specification<ZaloPayTransaction> spec = TransactionSpecification.buildSpecification(filter);
            
            log.info("Fetching transaction history - Page: {}, Size: {}, SortBy: {}, Direction: {}, Filters: {}", 
                    page, size, sortBy, direction, filter);
            
            // Query với specification và pagination
            Page<ZaloPayTransaction> transactionPage = transactionRepository.findAll(spec, pageable);
            
            // Convert sang DTO
            List<TransactionHistoryResponse.TransactionDetailDTO> transactionDTOs = transactionPage
                    .getContent()
                    .stream()
                    .map(TransactionHistoryResponse.TransactionDetailDTO::fromEntity)
                    .collect(Collectors.toList());
            
            // Build response
            TransactionHistoryResponse response = TransactionHistoryResponse.builder()
                    .transactions(transactionDTOs)
                    .totalElements(transactionPage.getTotalElements())
                    .totalPages(transactionPage.getTotalPages())
                    .currentPage(transactionPage.getNumber())
                    .pageSize(transactionPage.getSize())
                    .hasNext(transactionPage.hasNext())
                    .hasPrevious(transactionPage.hasPrevious())
                    .build();
            
            log.info("Transaction history fetched successfully - Total elements: {}, Total pages: {}, Current page: {}", 
                    response.getTotalElements(), response.getTotalPages(), response.getCurrentPage());
            
            return response;
            
        } catch (Exception e) {
            log.error("Error fetching transaction history: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi lấy lịch sử giao dịch: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate và trả về sort field hợp lệ
     */
    private String validateAndGetSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "createdAt";
        }
        
        // Danh sách các field được phép sort
        List<String> allowedFields = Arrays.asList("createdAt", "amount", "status", "paidAt", "updatedAt");
        
        if (allowedFields.contains(sortBy)) {
            return sortBy;
        }
        
        log.warn("Invalid sort field: {}. Using default 'createdAt'", sortBy);
        return "createdAt";
    }
}
