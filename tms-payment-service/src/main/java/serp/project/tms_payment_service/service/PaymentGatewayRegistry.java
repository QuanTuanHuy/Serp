package serp.project.tms_payment_service.service;

import org.springframework.stereotype.Service;
import serp.project.tms_payment_service.exception.AppException;
import serp.project.tms_payment_service.exception.ErrorCode;
import serp.project.tms_payment_service.gateway.PaymentGateway;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class PaymentGatewayRegistry {

    private final Map<String, PaymentGateway> gatewaysByCode;

    public PaymentGatewayRegistry(List<PaymentGateway> gateways) {
        Map<String, PaymentGateway> mapping = new LinkedHashMap<>();
        for (PaymentGateway gateway : gateways) {
            String code = normalize(gateway.gatewayCode());
            if (code == null) {
                continue;
            }
            mapping.put(code, gateway);
        }
        this.gatewaysByCode = Map.copyOf(mapping);
    }

    public PaymentGateway resolve(String gatewayCode) {
        String normalized = normalize(gatewayCode);
        if (normalized == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        PaymentGateway gateway = gatewaysByCode.get(normalized);
        if (gateway == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        return gateway;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
