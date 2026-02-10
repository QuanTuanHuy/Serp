/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.client.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
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
import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.email.send-grid.enable", havingValue = "true", matchIfMissing = true)
public class SendGridProviderAdapter implements IEmailProviderPort {

    private final WebClient sendGridWebClient;
    private final IEmailAttachmentPort emailAttachmentPort;
    private final SendGridProperties sendGridProperties;

    private static final String SEND_EMAIL_ENDPOINT = "/v3/mail/send";
    private static final String SCOPES_ENDPOINT = "/v3/scopes";
    private static final int TIMEOUT_SECONDS = 30;

    @Override
    public Map<String, Object> sendEmail(EmailEntity email) {
        return sendEmailInternal(email);
    }

    @Override
    public Map<String, Object> sendHtmlEmail(EmailEntity email) {
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

    private Map<String, Object> sendEmailInternal(EmailEntity email) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        try {
            List<EmailAttachmentEntity> attachments = null;
            if (email.getId() != null) {
                attachments = emailAttachmentPort.findByEmailId(email.getId());
            }

            Map<String, Object> requestBody = buildSendGridRequest(email, attachments);

            sendGridWebClient.post()
                    .uri(SEND_EMAIL_ENDPOINT)
                    .bodyValue(requestBody)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .block();

            long responseTime = System.currentTimeMillis() - startTime;

            result.put("success", true);
            result.put("provider", EmailProvider.SEND_GRID.name());
            result.put("messageId", email.getMessageId());
            result.put("sentAt", Instant.now().toEpochMilli());
            result.put("responseTimeMs", responseTime);

            log.info("Email sent successfully via SendGrid. MessageId: {}, ResponseTime: {}ms",
                    email.getMessageId(), responseTime);

        } catch (WebClientResponseException e) {
            long responseTime = System.currentTimeMillis() - startTime;

            log.error("Failed to send email via SendGrid. MessageId: {}, Status: {}, Error: {}",
                    email.getMessageId(), e.getStatusCode(), e.getResponseBodyAsString(), e);

            result.put("success", false);
            result.put("provider", EmailProvider.SEND_GRID.name());
            result.put("error", e.getResponseBodyAsString());
            result.put("errorClass", e.getClass().getSimpleName());
            result.put("responseTimeMs", responseTime);

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;

            log.error("Failed to send email via SendGrid. MessageId: {}, Error: {}",
                    email.getMessageId(), e.getMessage(), e);

            result.put("success", false);
            result.put("provider", EmailProvider.SEND_GRID.name());
            result.put("error", e.getMessage());
            result.put("errorClass", e.getClass().getSimpleName());
            result.put("responseTimeMs", responseTime);
        }

        return result;
    }

    private Map<String, Object> buildSendGridRequest(EmailEntity email, List<EmailAttachmentEntity> attachments)
            throws IOException {
        Map<String, Object> request = new HashMap<>();

        // personalizations
        List<Map<String, Object>> personalizations = new ArrayList<>();
        Map<String, Object> personalization = new HashMap<>();

        if (email.getToEmails() != null && !email.getToEmails().isEmpty()) {
            List<Map<String, String>> toList = new ArrayList<>();
            for (String toEmail : email.getToEmails()) {
                toList.add(Map.of("email", toEmail));
            }
            personalization.put("to", toList);
        }

        if (email.getCcEmails() != null && !email.getCcEmails().isEmpty()) {
            List<Map<String, String>> ccList = new ArrayList<>();
            for (String ccEmail : email.getCcEmails()) {
                ccList.add(Map.of("email", ccEmail));
            }
            personalization.put("cc", ccList);
        }

        if (email.getBccEmails() != null && !email.getBccEmails().isEmpty()) {
            List<Map<String, String>> bccList = new ArrayList<>();
            for (String bccEmail : email.getBccEmails()) {
                bccList.add(Map.of("email", bccEmail));
            }
            personalization.put("bcc", bccList);
        }

        personalization.put("subject", email.getSubject());
        personalizations.add(personalization);
        request.put("personalizations", personalizations);

        // from
        Map<String, String> from = new HashMap<>();
        from.put("email", sendGridProperties.getFrom());
        if (sendGridProperties.getFromName() != null && !sendGridProperties.getFromName().isEmpty()) {
            from.put("name", sendGridProperties.getFromName());
        }
        request.put("from", from);

        // reply_to
        if (email.getReplyTo() != null && !email.getReplyTo().isEmpty()) {
            request.put("reply_to", Map.of("email", email.getReplyTo()));
        }

        // content
        List<Map<String, String>> contentList = new ArrayList<>();
        if (email.getIsHtml() != null && email.getIsHtml()) {
            contentList.add(Map.of("type", "text/html", "value", email.getBody()));
        } else {
            contentList.add(Map.of("type", "text/plain", "value", email.getBody()));
        }
        request.put("content", contentList);

        // attachments
        if (attachments != null && !attachments.isEmpty()) {
            List<Map<String, String>> attachmentList = new ArrayList<>();
            for (EmailAttachmentEntity attachment : attachments) {
                File file = new File(attachment.getFilePath());
                if (file.exists()) {
                    byte[] fileContent = Files.readAllBytes(file.toPath());
                    String base64Content = Base64.getEncoder().encodeToString(fileContent);

                    Map<String, String> att = new HashMap<>();
                    att.put("content", base64Content);
                    att.put("filename", attachment.getOriginalFilename());
                    if (attachment.getContentType() != null) {
                        att.put("type", attachment.getContentType());
                    }
                    att.put("disposition", "attachment");
                    attachmentList.add(att);
                } else {
                    log.warn("Attachment file not found: {}", attachment.getFilePath());
                }
            }
            if (!attachmentList.isEmpty()) {
                request.put("attachments", attachmentList);
            }
        }

        // categories (for tracking)
        if (email.getType() != null) {
            request.put("categories", List.of(email.getType().name()));
        }

        return request;
    }
}
