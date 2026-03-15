/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.dto.provider;

import serp.project.mailservice.core.domain.enums.EmailProvider;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ProviderSendResult(
        boolean success,
        EmailProvider provider,
        String messageId,
        String providerMessageId,
        Long sentAt,
        Long responseTimeMs,
        Integer statusCode,
        String error,
        String errorClass,
        String rawResponseBody,
        List<ProviderErrorDetail> errorDetails) {

    public ProviderSendResult {
        errorDetails = errorDetails == null ? List.of() : List.copyOf(errorDetails);
    }

    public static ProviderSendResult success(
            EmailProvider provider,
            String messageId,
            String providerMessageId,
            Long responseTimeMs,
            Integer statusCode,
            String rawResponseBody) {
        return new ProviderSendResult(
                true,
                provider,
                messageId,
                providerMessageId,
                Instant.now().toEpochMilli(),
                responseTimeMs,
                statusCode,
                null,
                null,
                rawResponseBody,
                List.of());
    }

    public static ProviderSendResult failure(
            EmailProvider provider,
            String messageId,
            Long responseTimeMs,
            Integer statusCode,
            String error,
            String errorClass,
            String rawResponseBody,
            List<ProviderErrorDetail> errorDetails) {
        return new ProviderSendResult(
                false,
                provider,
                messageId,
                null,
                null,
                responseTimeMs,
                statusCode,
                error,
                errorClass,
                rawResponseBody,
                errorDetails);
    }

    public String resolveProviderMessageId(String fallbackMessageId) {
        if (providerMessageId != null && !providerMessageId.isBlank()) {
            return providerMessageId;
        }
        if (messageId != null && !messageId.isBlank()) {
            return messageId;
        }
        return fallbackMessageId;
    }

    public String resolveErrorMessage() {
        if (error != null && !error.isBlank()) {
            return error;
        }
        if (errorDetails != null && !errorDetails.isEmpty()) {
            ProviderErrorDetail firstError = errorDetails.getFirst();
            if (firstError.message() != null && !firstError.message().isBlank()) {
                return firstError.message();
            }
        }
        return "Unknown provider error";
    }

    public Map<String, Object> toPersistenceResponse() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", success);

        if (provider != null) {
            result.put("provider", provider.name());
        }
        if (messageId != null) {
            result.put("messageId", messageId);
        }
        if (providerMessageId != null) {
            result.put("providerMessageId", providerMessageId);
        }
        if (sentAt != null) {
            result.put("sentAt", sentAt);
        }
        if (responseTimeMs != null) {
            result.put("responseTimeMs", responseTimeMs);
        }
        if (statusCode != null) {
            result.put("statusCode", statusCode);
        }
        if (error != null) {
            result.put("error", error);
        }
        if (errorClass != null) {
            result.put("errorClass", errorClass);
        }
        if (rawResponseBody != null) {
            result.put("rawResponseBody", rawResponseBody);
        }
        if (!errorDetails.isEmpty()) {
            result.put("errors", errorDetails.stream()
                    .map(errorDetail -> {
                        Map<String, Object> detail = new LinkedHashMap<>();
                        detail.put("message", errorDetail.message());
                        detail.put("field", errorDetail.field());
                        detail.put("help", errorDetail.help());
                        detail.put("id", errorDetail.id());
                        return detail;
                    })
                    .toList());
        }

        return result;
    }
}
