/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.mailservice.core.domain.entity.EmailEntity;
import serp.project.mailservice.core.domain.enums.EmailPriority;
import serp.project.mailservice.core.domain.enums.EmailStatus;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService implements IEmailService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @Override
    public void validateEmail(EmailEntity email) {
        log.debug("Validating email: {}", email.getMessageId());

        if (email.getFromEmail() == null || email.getFromEmail().isBlank()) {
            throw new IllegalArgumentException("From email is required");
        }
        if (!isValidEmail(email.getFromEmail())) {
            throw new IllegalArgumentException("Invalid from email format: " + email.getFromEmail());
        }

        if (email.getToEmails() == null || email.getToEmails().isEmpty()) {
            throw new IllegalArgumentException("At least one recipient email is required");
        }
        for (String toEmail : email.getToEmails()) {
            if (!isValidEmail(toEmail)) {
                throw new IllegalArgumentException("Invalid recipient email format: " + toEmail);
            }
        }

        if (email.getCcEmails() != null) {
            for (String ccEmail : email.getCcEmails()) {
                if (!isValidEmail(ccEmail)) {
                    throw new IllegalArgumentException("Invalid CC email format: " + ccEmail);
                }
            }
        }

        if (email.getBccEmails() != null) {
            for (String bccEmail : email.getBccEmails()) {
                if (!isValidEmail(bccEmail)) {
                    throw new IllegalArgumentException("Invalid BCC email format: " + bccEmail);
                }
            }
        }

        if (email.getSubject() == null || email.getSubject().isBlank()) {
            throw new IllegalArgumentException("Email subject is required");
        }

        if ((email.getBody() == null || email.getBody().isBlank()) && email.getTemplateId() == null) {
            throw new IllegalArgumentException("Either email body or template ID must be provided");
        }

        log.debug("Email validation successful: {}", email.getMessageId());
    }

    @Override
    public void enrichEmail(EmailEntity email) {
        log.debug("Enriching email: {}", email.getMessageId());

        if (email.getMessageId() == null || email.getMessageId().isBlank()) {
            email.setMessageId(generateMessageId());
            log.debug("Generated message ID: {}", email.getMessageId());
        }

        if (email.getPriority() == null) {
            email.setPriority(EmailPriority.NORMAL);
            log.debug("Set default priority: NORMAL");
        }

        if (email.getIsHtml() == null) {
            email.setIsHtml(true);
            log.debug("Set default isHtml: true");
        }

        if (email.getMaxRetries() == null) {
            email.setMaxRetries(3);
            log.debug("Set default maxRetries: 3");
        }
        if (email.getRetryCount() == null) {
            email.setRetryCount(0);
        }

        if (email.getStatus() == null) {
            email.setStatus(EmailStatus.PENDING);
            log.debug("Set initial status: PENDING");
        }

        LocalDateTime now = LocalDateTime.now();
        if (email.getCreatedAt() == null) {
            email.setCreatedAt(now);
        }
        if (email.getUpdatedAt() == null) {
            email.setUpdatedAt(now);
        }

        log.debug("Email enrichment complete: {}", email.getMessageId());
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    private String generateMessageId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public boolean isHighPriority(EmailEntity email) {
        return EmailPriority.HIGH.equals(email.getPriority());
    }
}
