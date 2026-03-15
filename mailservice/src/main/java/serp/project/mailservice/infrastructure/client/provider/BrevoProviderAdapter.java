/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.client.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import serp.project.mailservice.core.domain.dto.provider.ProviderSendResult;
import serp.project.mailservice.core.domain.entity.EmailAttachmentEntity;
import serp.project.mailservice.core.domain.entity.EmailEntity;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.port.client.IEmailProviderPort;
import serp.project.mailservice.kernel.property.BrevoProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.email.brevo.enable", havingValue = "true")
public class BrevoProviderAdapter implements IEmailProviderPort {
    
    private final WebClient brevoWebClient;
    private final BrevoProperties brevoProperties;
    private final ObjectMapper objectMapper;
    
    private static final String SEND_EMAIL_ENDPOINT = "/v3/smtp/email";
    private static final String ACCOUNT_ENDPOINT = "/v3/account";
    private static final int TIMEOUT_SECONDS = 30;

    @Override
    public ProviderSendResult sendEmail(EmailEntity email) {
        return sendEmailInternal(email);
    }

    @Override
    public String getProviderName() {
        return EmailProvider.BREVO.name();
    }

    @Override
    public EmailProvider getProviderType() {
        return EmailProvider.BREVO;
    }
    
    @Override
    public boolean isHealthy() {
        try {
            String response = brevoWebClient.get()
                    .uri(ACCOUNT_ENDPOINT)
                    .header("api-key", brevoProperties.getApiKey())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorResume(e -> Mono.just(""))
                    .block();
            
            boolean isHealthy = response != null && !response.isEmpty();
            
            if (isHealthy) {
                log.debug("Brevo provider health check passed");
            } else {
                log.warn("Brevo provider health check failed");
            }
            
            return isHealthy;
            
        } catch (Exception e) {
            log.error("Brevo provider health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    private ProviderSendResult sendEmailInternal(EmailEntity email) {
        long startTime = System.currentTimeMillis();

        try {
            List<EmailAttachmentEntity> attachments = email.getAttachments();

            Map<String, Object> requestBody = buildBrevoRequest(email, attachments);

            ResponseEntity<String> responseEntity = brevoWebClient.post()
                    .uri(SEND_EMAIL_ENDPOINT)
                    .header("api-key", brevoProperties.getApiKey())
                    .bodyValue(requestBody)
                    .retrieve()
                    .toEntity(String.class)
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .block();

            long responseTime = System.currentTimeMillis() - startTime;
            String rawResponseBody = responseEntity != null ? responseEntity.getBody() : null;
            Integer statusCode = responseEntity != null ? responseEntity.getStatusCode().value() : null;
            String brevoMessageId = extractBrevoMessageId(rawResponseBody);

            log.info("Email sent successfully via Brevo. MessageId: {}, BrevoMessageId: {}, ResponseTime: {}ms", 
                    email.getMessageId(), brevoMessageId, responseTime);

            return ProviderSendResult.success(
                    EmailProvider.BREVO,
                    email.getMessageId(),
                    brevoMessageId,
                    responseTime,
                    statusCode,
                    rawResponseBody);

        } catch (WebClientResponseException e) {
            long responseTime = System.currentTimeMillis() - startTime;

            log.error("Failed to send email via Brevo. MessageId: {}, Status: {}, Error: {}",
                    email.getMessageId(), e.getStatusCode(), e.getResponseBodyAsString(), e);

            return ProviderSendResult.failure(
                    EmailProvider.BREVO,
                    email.getMessageId(),
                    responseTime,
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString(),
                    e.getClass().getSimpleName(),
                    e.getResponseBodyAsString(),
                    List.of());
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;

            log.error("Failed to send email via Brevo. MessageId: {}, Error: {}", 
                    email.getMessageId(), e.getMessage(), e);

            return ProviderSendResult.failure(
                    EmailProvider.BREVO,
                    email.getMessageId(),
                    responseTime,
                    null,
                    e.getMessage(),
                    e.getClass().getSimpleName(),
                    null,
                    List.of());
        }
    }

    private String extractBrevoMessageId(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }

        try {
            JsonNode responseNode = objectMapper.readTree(responseBody);
            if (!responseNode.has("messageId")) {
                return null;
            }
            String value = responseNode.get("messageId").asText();
            return value == null || value.isBlank() ? null : value;
        } catch (Exception e) {
            log.warn("Cannot parse Brevo response body for messageId: {}", e.getMessage());
            return null;
        }
    }
    
    private Map<String, Object> buildBrevoRequest(EmailEntity email, List<EmailAttachmentEntity> attachments) 
            throws IOException {
        Map<String, Object> request = new HashMap<>();
        
        Map<String, String> sender = new HashMap<>();
        sender.put("email", email.getFromEmail());
        if (email.getFromName() != null && !email.getFromName().isEmpty()) {
            sender.put("name", email.getFromName());
        }
        request.put("sender", sender);
        
        List<Map<String, String>> toList = new ArrayList<>();
        if (email.getToEmails() != null) {
            for (String toEmail : email.getToEmails()) {
                Map<String, String> recipient = new HashMap<>();
                recipient.put("email", toEmail);
                toList.add(recipient);
            }
        }
        request.put("to", toList);
        
        if (email.getCcEmails() != null && !email.getCcEmails().isEmpty()) {
            List<Map<String, String>> ccList = new ArrayList<>();
            for (String ccEmail : email.getCcEmails()) {
                Map<String, String> recipient = new HashMap<>();
                recipient.put("email", ccEmail);
                ccList.add(recipient);
            }
            request.put("cc", ccList);
        }
        
        if (email.getBccEmails() != null && !email.getBccEmails().isEmpty()) {
            List<Map<String, String>> bccList = new ArrayList<>();
            for (String bccEmail : email.getBccEmails()) {
                Map<String, String> recipient = new HashMap<>();
                recipient.put("email", bccEmail);
                bccList.add(recipient);
            }
            request.put("bcc", bccList);
        }
        
        request.put("subject", email.getSubject());
        
        if (email.getIsHtml() != null && email.getIsHtml()) {
            request.put("htmlContent", email.getBody());
        } else {
            request.put("textContent", email.getBody());
        }
        
        if (email.getReplyTo() != null && !email.getReplyTo().isEmpty()) {
            Map<String, String> replyTo = new HashMap<>();
            replyTo.put("email", email.getReplyTo());
            request.put("replyTo", replyTo);
        }
        
        if (attachments != null && !attachments.isEmpty()) {
            List<Map<String, String>> attachmentList = new ArrayList<>();
            for (EmailAttachmentEntity attachment : attachments) {
                File file = new File(attachment.getFilePath());
                if (file.exists()) {
                    byte[] fileContent = Files.readAllBytes(file.toPath());
                    String base64Content = Base64.getEncoder().encodeToString(fileContent);
                    
                    Map<String, String> att = new HashMap<>();
                    att.put("content", base64Content);
                    att.put("name", attachment.getOriginalFilename());
                    attachmentList.add(att);
                } else {
                    log.warn("Attachment file not found: {}", attachment.getFilePath());
                }
            }
            if (!attachmentList.isEmpty()) {
                request.put("attachment", attachmentList);
            }
        }
        
        // Tags (for tracking)
        if (email.getType() != null) {
            request.put("tags", List.of(email.getType().name()));
        }
        
        return request;
    }
}
