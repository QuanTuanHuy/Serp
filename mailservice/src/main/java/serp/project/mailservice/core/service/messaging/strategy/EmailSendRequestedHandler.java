/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.mailservice.core.service.messaging.strategy;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import serp.project.mailservice.core.domain.dto.message.KafkaEventContext;
import serp.project.mailservice.core.domain.dto.request.SendEmailRequest;
import serp.project.mailservice.core.exception.AppException;
import serp.project.mailservice.core.exception.KafkaNonRetryableException;
import serp.project.mailservice.core.usecase.EmailSendingUseCases;
import serp.project.mailservice.kernel.utils.JsonUtils;

@Component
public class EmailSendRequestedHandler implements IKafkaEventHandlerStrategy {

    private static final String EVENT_TYPE = "email.send.requested";

    private final EmailSendingUseCases emailSendingUseCases;
    private final JsonUtils jsonUtils;

    public EmailSendRequestedHandler(
            EmailSendingUseCases emailSendingUseCases,
            JsonUtils jsonUtils) {
        this.emailSendingUseCases = emailSendingUseCases;
        this.jsonUtils = jsonUtils;
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public void handle(JsonNode message, KafkaEventContext context) {
        Long tenantId = context.tenantId();
        if (tenantId == null) {
            throw new KafkaNonRetryableException("Kafka payload missing meta.tenantId for eventType=" + EVENT_TYPE);
        }

        JsonNode dataNode = message.path("data");
        if (dataNode.isMissingNode() || dataNode.isNull()) {
            throw new KafkaNonRetryableException("Kafka payload missing data section for eventType=" + EVENT_TYPE);
        }

        SendEmailRequest request = toSendEmailRequest(dataNode);
        Long actorId = extractActorId(message.path("meta").path("actorId"));

        try {
            emailSendingUseCases.sendEmail(request, tenantId, actorId);
        } catch (IllegalArgumentException ex) {
            throw new KafkaNonRetryableException("Invalid email request payload: " + ex.getMessage(), ex);
        } catch (AppException ex) {
            if (!isRetryable(ex)) {
                throw new KafkaNonRetryableException(ex.getMessage(), ex);
            }
            throw ex;
        }
    }

    private SendEmailRequest toSendEmailRequest(JsonNode dataNode) {
        try {
            SendEmailRequest request = jsonUtils.fromJson(jsonUtils.toJson(dataNode), SendEmailRequest.class);
            if (request == null) {
                throw new KafkaNonRetryableException("Kafka payload data section cannot be null for eventType=" + EVENT_TYPE);
            }
            return request;
        } catch (KafkaNonRetryableException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new KafkaNonRetryableException("Kafka payload data section is invalid for eventType=" + EVENT_TYPE, ex);
        }
    }

    private Long extractActorId(JsonNode actorNode) {
        if (actorNode.isNull() || actorNode.isMissingNode()) {
            return null;
        }

        if (actorNode.isNumber()) {
            return actorNode.asLong();
        }

        String actorId = actorNode.asText(null);
        if (actorId == null || actorId.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(actorId.trim());
        } catch (NumberFormatException ex) {
            throw new KafkaNonRetryableException("Kafka payload contains invalid meta.actorId", ex);
        }
    }

    private boolean isRetryable(AppException ex) {
        int code = ex.getCode();
        return code >= HttpStatus.INTERNAL_SERVER_ERROR.value()
                || code == HttpStatus.TOO_MANY_REQUESTS.value();
    }
}
