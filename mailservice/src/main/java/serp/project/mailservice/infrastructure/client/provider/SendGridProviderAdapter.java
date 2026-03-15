/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.client.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import serp.project.mailservice.core.domain.dto.provider.ProviderErrorDetail;
import serp.project.mailservice.core.domain.dto.provider.ProviderSendResult;
import serp.project.mailservice.core.domain.entity.EmailAttachmentEntity;
import serp.project.mailservice.core.domain.entity.EmailEntity;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.port.client.IEmailProviderPort;
import serp.project.mailservice.core.port.store.IEmailAttachmentPort;
import serp.project.mailservice.kernel.property.SendGridProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.email.send-grid.enable", havingValue = "true", matchIfMissing = true)
public class SendGridProviderAdapter implements IEmailProviderPort {

    private final WebClient sendGridWebClient;
    private final IEmailAttachmentPort emailAttachmentPort;
    private final SendGridProperties sendGridProperties;
    private final ObjectMapper objectMapper;

    private static final String SEND_EMAIL_ENDPOINT = "/v3/mail/send";
    private static final String SCOPES_ENDPOINT = "/v3/scopes";
    private static final int TIMEOUT_SECONDS = 30;
    private static final String MIME_TYPE_TEXT_HTML = "text/html";
    private static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
    private static final String DISPOSITION_ATTACHMENT = "attachment";
    private static final String SENDGRID_MESSAGE_ID_HEADER = "X-Message-Id";

    @Override
    public ProviderSendResult sendEmail(EmailEntity email) {
        return sendEmailInternal(email);
    }

    @Override
    public ProviderSendResult sendHtmlEmail(EmailEntity email) {
        return sendEmailInternal(email);
    }

    @Override
    public String getProviderName() {
        return EmailProvider.SEND_GRID.name();
    }

    @Override
    public EmailProvider getProviderType() {
        return EmailProvider.SEND_GRID;
    }

    @Override
    public boolean isHealthy() {
        try {
            String response = sendGridWebClient.get()
                    .uri(SCOPES_ENDPOINT)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorResume(e -> Mono.just(""))
                    .block();

            boolean isHealthy = response != null && !response.isEmpty();

            if (isHealthy) {
                log.debug("SendGrid provider health check passed");
            } else {
                log.warn("SendGrid provider health check failed");
            }

            return isHealthy;

        } catch (Exception e) {
            log.error("SendGrid provider health check failed: {}", e.getMessage());
            return false;
        }
    }

    private ProviderSendResult sendEmailInternal(EmailEntity email) {
        long startTime = System.currentTimeMillis();

        try {
            List<EmailAttachmentEntity> attachments = null;
            if (email.getId() != null) {
                attachments = emailAttachmentPort.findByEmailId(email.getId());
            }

            SendGridMailSendRequest requestBody = buildSendGridRequest(email, attachments);

            ResponseEntity<Void> responseEntity = sendGridWebClient.post()
                    .uri(SEND_EMAIL_ENDPOINT)
                    .bodyValue(requestBody)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .block();

            long responseTime = System.currentTimeMillis() - startTime;
            Integer statusCode = responseEntity != null ? responseEntity.getStatusCode().value() : null;
            String providerMessageId = extractProviderMessageId(responseEntity);

            log.info("Email sent successfully via SendGrid. MessageId: {}, ProviderMessageId: {}, ResponseTime: {}ms",
                    email.getMessageId(), providerMessageId, responseTime);

            return ProviderSendResult.success(
                    EmailProvider.SEND_GRID,
                    email.getMessageId(),
                    providerMessageId,
                    responseTime,
                    statusCode,
                    null);

        } catch (WebClientResponseException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            String rawResponseBody = e.getResponseBodyAsString();
            List<ProviderErrorDetail> errorDetails = parseSendGridErrorDetails(rawResponseBody);
            String errorMessage = resolveSendGridErrorMessage(errorDetails, rawResponseBody, e.getMessage());

            log.error("Failed to send email via SendGrid. MessageId: {}, Status: {}, Error: {}",
                    email.getMessageId(), e.getStatusCode(), rawResponseBody, e);

            return ProviderSendResult.failure(
                    EmailProvider.SEND_GRID,
                    email.getMessageId(),
                    responseTime,
                    e.getStatusCode().value(),
                    errorMessage,
                    e.getClass().getSimpleName(),
                    rawResponseBody,
                    errorDetails);

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;

            log.error("Failed to send email via SendGrid. MessageId: {}, Error: {}",
                    email.getMessageId(), e.getMessage(), e);

            return ProviderSendResult.failure(
                    EmailProvider.SEND_GRID,
                    email.getMessageId(),
                    responseTime,
                    null,
                    e.getMessage(),
                    e.getClass().getSimpleName(),
                    null,
                    List.of());
        }
    }

    private SendGridMailSendRequest buildSendGridRequest(EmailEntity email, List<EmailAttachmentEntity> attachments)
            throws IOException {
        SendGridPersonalization personalization = new SendGridPersonalization(
                toSendGridRecipients(email.getToEmails()),
                toSendGridRecipients(email.getCcEmails()),
                toSendGridRecipients(email.getBccEmails()),
                email.getSubject(),
                null);

        String fromEmail = resolveFromEmail(email);
        SendGridEmailAddress fromAddress = new SendGridEmailAddress(fromEmail, resolveFromName(email));

        SendGridEmailAddress replyToAddress = null;
        if (email.getReplyTo() != null && !email.getReplyTo().isBlank()) {
            replyToAddress = new SendGridEmailAddress(email.getReplyTo(), null);
        }

        SendGridContent content = new SendGridContent(
                Boolean.TRUE.equals(email.getIsHtml()) ? MIME_TYPE_TEXT_HTML : MIME_TYPE_TEXT_PLAIN,
                email.getBody());

        List<SendGridAttachment> sendGridAttachments = toSendGridAttachments(attachments);
        List<String> categories = email.getType() != null ? List.of(email.getType().name()) : null;
        Map<String, String> customArgs = buildSendGridCustomArgs(email);

        return new SendGridMailSendRequest(
                List.of(personalization),
                fromAddress,
                replyToAddress,
                List.of(content),
                sendGridAttachments,
                categories,
                customArgs);
    }

    private String resolveFromEmail(EmailEntity email) {
        if (sendGridProperties.getFrom() != null && !sendGridProperties.getFrom().isBlank()) {
            return sendGridProperties.getFrom();
        }
        return email.getFromEmail();
    }

    private String resolveFromName(EmailEntity email) {
        if (sendGridProperties.getFromName() != null && !sendGridProperties.getFromName().isBlank()) {
            return sendGridProperties.getFromName();
        }
        return email.getFromName();
    }

    private List<SendGridEmailAddress> toSendGridRecipients(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return null;
        }

        List<SendGridEmailAddress> recipients = new ArrayList<>();
        for (String email : emails) {
            recipients.add(new SendGridEmailAddress(email, null));
        }
        return recipients;
    }

    private List<SendGridAttachment> toSendGridAttachments(List<EmailAttachmentEntity> attachments) throws IOException {
        if (attachments == null || attachments.isEmpty()) {
            return null;
        }

        List<SendGridAttachment> sendGridAttachments = new ArrayList<>();
        for (EmailAttachmentEntity attachment : attachments) {
            File file = new File(attachment.getFilePath());
            if (!file.exists()) {
                log.warn("Attachment file not found: {}", attachment.getFilePath());
                continue;
            }

            byte[] fileContent = Files.readAllBytes(file.toPath());
            String base64Content = Base64.getEncoder().encodeToString(fileContent);

            sendGridAttachments.add(new SendGridAttachment(
                    base64Content,
                    attachment.getOriginalFilename(),
                    attachment.getContentType(),
                    DISPOSITION_ATTACHMENT,
                    null));
        }

        return sendGridAttachments.isEmpty() ? null : sendGridAttachments;
    }

    private Map<String, String> buildSendGridCustomArgs(EmailEntity email) {
        if (email.getMetadata() == null || email.getMetadata().isEmpty()) {
            return null;
        }

        Map<String, String> customArgs = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : email.getMetadata().entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null) {
                continue;
            }
            customArgs.put(entry.getKey(), String.valueOf(entry.getValue()));
        }

        return customArgs.isEmpty() ? null : customArgs;
    }

    private String extractProviderMessageId(ResponseEntity<Void> responseEntity) {
        if (responseEntity == null) {
            return null;
        }

        String messageId = responseEntity.getHeaders().getFirst(SENDGRID_MESSAGE_ID_HEADER);
        if (messageId == null || messageId.isBlank()) {
            return null;
        }
        return messageId;
    }

    private List<ProviderErrorDetail> parseSendGridErrorDetails(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return List.of();
        }

        try {
            SendGridErrorResponse errorResponse = objectMapper.readValue(responseBody, SendGridErrorResponse.class);
            if (errorResponse == null || errorResponse.errors() == null || errorResponse.errors().isEmpty()) {
                return List.of();
            }

            List<ProviderErrorDetail> result = new ArrayList<>();
            for (SendGridErrorItem errorItem : errorResponse.errors()) {
                result.add(new ProviderErrorDetail(
                        errorItem.message(),
                        errorItem.field(),
                        errorItem.help(),
                        errorItem.id()));
            }

            return result;
        } catch (Exception e) {
            log.warn("Cannot parse SendGrid error response body: {}", e.getMessage());
            return List.of();
        }
    }

    private String resolveSendGridErrorMessage(
            List<ProviderErrorDetail> errorDetails,
            String rawResponseBody,
            String fallbackMessage) {
        if (errorDetails != null && !errorDetails.isEmpty()) {
            String firstMessage = errorDetails.getFirst().message();
            if (firstMessage != null && !firstMessage.isBlank()) {
                return firstMessage;
            }
        }

        if (rawResponseBody != null && !rawResponseBody.isBlank()) {
            return rawResponseBody;
        }

        return fallbackMessage;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record SendGridMailSendRequest(
            List<SendGridPersonalization> personalizations,
            SendGridEmailAddress from,
            @JsonProperty("reply_to") SendGridEmailAddress replyTo,
            List<SendGridContent> content,
            List<SendGridAttachment> attachments,
            List<String> categories,
            @JsonProperty("custom_args") Map<String, String> customArgs) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record SendGridPersonalization(
            List<SendGridEmailAddress> to,
            List<SendGridEmailAddress> cc,
            List<SendGridEmailAddress> bcc,
            String subject,
            @JsonProperty("custom_args") Map<String, String> customArgs) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record SendGridEmailAddress(String email, String name) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record SendGridContent(String type, String value) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record SendGridAttachment(
            String content,
            String filename,
            String type,
            String disposition,
            @JsonProperty("content_id") String contentId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SendGridErrorResponse(List<SendGridErrorItem> errors) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SendGridErrorItem(String message, String field, Object help, String id) {
    }
}
