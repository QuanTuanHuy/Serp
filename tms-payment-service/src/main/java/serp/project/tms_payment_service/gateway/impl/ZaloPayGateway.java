package serp.project.tms_payment_service.gateway.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import serp.project.tms_payment_service.dto.payment.PaymentBankDTO;
import serp.project.tms_payment_service.dto.payment.PaymentBankListResponse;
import serp.project.tms_payment_service.dto.payment.PaymentCallbackRequest;
import serp.project.tms_payment_service.dto.payment.PaymentCreateOrderRequest;
import serp.project.tms_payment_service.dto.payment.PaymentCreateOrderResponse;
import serp.project.tms_payment_service.dto.payment.PaymentEmbedData;
import serp.project.tms_payment_service.dto.payment.PaymentOrderItem;
import serp.project.tms_payment_service.dto.payment.PaymentQueryOrderRequest;
import serp.project.tms_payment_service.dto.payment.PaymentQueryOrderResponse;
import serp.project.tms_payment_service.dto.payment.PaymentQueryRefundRequest;
import serp.project.tms_payment_service.dto.payment.PaymentQueryRefundResponse;
import serp.project.tms_payment_service.dto.payment.PaymentRefundRequest;
import serp.project.tms_payment_service.dto.payment.PaymentRefundResponse;
import serp.project.tms_payment_service.dto.zalopay.BankDTO;
import serp.project.tms_payment_service.dto.zalopay.CreateOrderRequest;
import serp.project.tms_payment_service.dto.zalopay.CreateOrderResponse;
import serp.project.tms_payment_service.dto.zalopay.EmbedData;
import serp.project.tms_payment_service.dto.zalopay.GetBankListResponse;
import serp.project.tms_payment_service.dto.zalopay.OrderItem;
import serp.project.tms_payment_service.dto.zalopay.QueryOrderRequest;
import serp.project.tms_payment_service.dto.zalopay.QueryOrderResponse;
import serp.project.tms_payment_service.dto.zalopay.QueryRefundRequest;
import serp.project.tms_payment_service.dto.zalopay.QueryRefundResponse;
import serp.project.tms_payment_service.dto.zalopay.RefundRequest;
import serp.project.tms_payment_service.dto.zalopay.RefundResponse;
import serp.project.tms_payment_service.dto.zalopay.ZaloPayCallbackRequest;
import serp.project.tms_payment_service.gateway.PaymentGateway;
import serp.project.tms_payment_service.service.ZaloPayService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ZaloPayGateway implements PaymentGateway {

    public static final String CODE = "zalopay";

    private final ZaloPayService zaloPayService;

    @Override
    public String gatewayCode() {
        return CODE;
    }

    @Override
    public String displayName() {
        return "ZaloPay";
    }

    @Override
    public PaymentCreateOrderResponse createOrder(PaymentCreateOrderRequest request) {
        CreateOrderResponse response = zaloPayService.createOrder(toZaloCreateOrderRequest(request));
        return toPaymentCreateOrderResponse(response);
    }

    @Override
    public String handleCallback(PaymentCallbackRequest callbackRequest) {
        ZaloPayCallbackRequest request = ZaloPayCallbackRequest.builder()
                .data(callbackRequest.getData())
                .mac(callbackRequest.getMac())
                .type(callbackRequest.getType())
                .build();
        return zaloPayService.handleCallback(request);
    }

    @Override
    public PaymentQueryOrderResponse queryOrderStatus(PaymentQueryOrderRequest request) {
        QueryOrderResponse response = zaloPayService.queryOrderStatus(
                QueryOrderRequest.builder().appTransId(request.getAppTransId()).build()
        );
        return toPaymentQueryOrderResponse(response);
    }

    @Override
    public PaymentBankListResponse getBankList() {
        GetBankListResponse response = zaloPayService.getBankList();
        return toPaymentBankListResponse(response);
    }

    @Override
    public PaymentRefundResponse refundOrder(PaymentRefundRequest request) {
        RefundResponse response = zaloPayService.refundOrder(RefundRequest.builder()
                .zpTransId(request.getZpTransId())
                .amount(request.getAmount())
                .refundFeeAmount(request.getRefundFeeAmount())
                .description(request.getDescription())
                .build());
        return toPaymentRefundResponse(response);
    }

    @Override
    public PaymentQueryRefundResponse queryRefundStatus(PaymentQueryRefundRequest request) {
        QueryRefundResponse response = zaloPayService.queryRefundStatus(QueryRefundRequest.builder()
                .mRefundId(request.getMRefundId())
                .build());
        return toPaymentQueryRefundResponse(response);
    }

    private CreateOrderRequest toZaloCreateOrderRequest(PaymentCreateOrderRequest request) {
        List<OrderItem> items = request.getItems() == null
                ? Collections.emptyList()
                : request.getItems().stream()
                .map(this::toZaloOrderItem)
                .toList();

        return CreateOrderRequest.builder()
                .appUser(request.getAppUser())
                .amount(request.getAmount())
                .description(request.getDescription())
                .items(items)
                .bankCode(request.getBankCode())
                .expireDurationSeconds(request.getExpireDurationSeconds())
                .embedData(toZaloEmbedData(request.getEmbedData()))
                .title(request.getTitle())
                .phone(request.getPhone())
                .email(request.getEmail())
                .tenantId(request.getTenantId())
                .actorId(request.getActorId())
                .userId(request.getUserId())
                .address(request.getAddress())
                .subAppId(request.getSubAppId())
                .build();
    }

    private OrderItem toZaloOrderItem(PaymentOrderItem item) {
        return OrderItem.builder()
                .itemId(item.getItemId())
                .itemName(item.getItemName())
                .itemPrice(item.getItemPrice())
                .itemQuantity(item.getItemQuantity())
                .build();
    }

    private EmbedData toZaloEmbedData(PaymentEmbedData embedData) {
        if (embedData == null) {
            return null;
        }
        return EmbedData.builder()
                .redirectUrl(embedData.getRedirectUrl())
                .merchantInfo(embedData.getMerchantInfo())
                .promotionInfo(embedData.getPromotionInfo())
                .columnInfo(embedData.getColumnInfo())
                .preferredPaymentMethod(embedData.getPreferredPaymentMethod())
                .zlpPaymentId(embedData.getZlpPaymentId())
                .build();
    }

    private PaymentCreateOrderResponse toPaymentCreateOrderResponse(CreateOrderResponse response) {
        PaymentCreateOrderResponse dto = new PaymentCreateOrderResponse();
        BeanUtils.copyProperties(response, dto);
        return dto;
    }

    private PaymentQueryOrderResponse toPaymentQueryOrderResponse(QueryOrderResponse response) {
        PaymentQueryOrderResponse dto = new PaymentQueryOrderResponse();
        BeanUtils.copyProperties(response, dto);
        return dto;
    }

    private PaymentBankListResponse toPaymentBankListResponse(GetBankListResponse response) {
        if (response == null) {
            return null;
        }
        Map<String, List<PaymentBankDTO>> mappedBanks = response.getBanks() == null
                ? Collections.emptyMap()
                : response.getBanks().entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() == null
                                ? Collections.emptyList()
                                : entry.getValue().stream().map(this::toPaymentBankDTO).toList()
                ));
        return PaymentBankListResponse.builder()
                .returnCode(response.getReturnCode())
                .returnMessage(response.getReturnMessage())
                .banks(mappedBanks)
                .build();
    }

    private PaymentBankDTO toPaymentBankDTO(BankDTO bankDTO) {
        if (bankDTO == null) {
            return null;
        }
        return PaymentBankDTO.builder()
                .bankCode(bankDTO.getBankCode())
                .name(bankDTO.getName())
                .displayOrder(bankDTO.getDisplayOrder())
                .pmcId(bankDTO.getPmcId())
                .minAmount(bankDTO.getMinAmount())
                .maxAmount(bankDTO.getMaxAmount())
                .build();
    }

    private PaymentRefundResponse toPaymentRefundResponse(RefundResponse response) {
        PaymentRefundResponse dto = new PaymentRefundResponse();
        BeanUtils.copyProperties(response, dto);
        return dto;
    }

    private PaymentQueryRefundResponse toPaymentQueryRefundResponse(QueryRefundResponse response) {
        PaymentQueryRefundResponse dto = new PaymentQueryRefundResponse();
        BeanUtils.copyProperties(response, dto);
        return dto;
    }
}
