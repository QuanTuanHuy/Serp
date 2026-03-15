/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.client.provider;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import serp.project.mailservice.core.domain.dto.provider.ProviderSendResult;
import serp.project.mailservice.core.domain.entity.EmailAttachmentEntity;
import serp.project.mailservice.core.domain.entity.EmailEntity;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.port.client.IEmailProviderPort;
import serp.project.mailservice.core.port.store.IEmailAttachmentPort;
import serp.project.mailservice.kernel.property.JavaMailProperties;

import java.io.File;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.email.java-mail.enabled", havingValue = "true")
public class JavaMailProviderAdapter implements IEmailProviderPort {

    private final JavaMailSender javaMailSender;
    private final IEmailAttachmentPort emailAttachmentPort;
    private final JavaMailProperties javaMailProperties;
    
    @Override
    public ProviderSendResult sendEmail(EmailEntity email) {
        return sendEmailInternal(email, false);
    }
    
    @Override
    public ProviderSendResult sendHtmlEmail(EmailEntity email) {
        return sendEmailInternal(email, true);
    }
    
    @Override
    public String getProviderName() {
        return EmailProvider.JAVA_MAIL.name();
    }

    @Override
    public EmailProvider getProviderType() {
        return EmailProvider.JAVA_MAIL;
    }
    
    @Override
    public boolean isHealthy() {
        try {
            if (javaMailSender instanceof JavaMailSenderImpl javaMailSenderImpl) {
                javaMailSenderImpl.testConnection();
            } else {
                javaMailSender.createMimeMessage();
            }
            log.debug("JavaMail provider health check passed");
            return true;
        } catch (Exception e) {
            log.error("JavaMail provider health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    private ProviderSendResult sendEmailInternal(EmailEntity email, boolean forceHtml) {
        long startTime = System.currentTimeMillis();

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            String defaultEncoding = StringUtils.hasText(javaMailProperties.getDefaultEncoding())
                    ? javaMailProperties.getDefaultEncoding()
                    : "UTF-8";
            MimeMessageHelper helper = new MimeMessageHelper(message, true, defaultEncoding);

            String fromEmail = resolveFromEmail(email);
            String fromName = resolveFromName(email);
            if (StringUtils.hasText(fromName)) {
                helper.setFrom(fromEmail, fromName);
            } else {
                helper.setFrom(fromEmail);
            }
            
            if (email.getToEmails() != null && !email.getToEmails().isEmpty()) {
                helper.setTo(email.getToEmails().toArray(new String[0]));
            }
            
            if (email.getCcEmails() != null && !email.getCcEmails().isEmpty()) {
                helper.setCc(email.getCcEmails().toArray(new String[0]));
            }
            
            if (email.getBccEmails() != null && !email.getBccEmails().isEmpty()) {
                helper.setBcc(email.getBccEmails().toArray(new String[0]));
            }
            
            helper.setSubject(email.getSubject());
            boolean isHtml = forceHtml || (email.getIsHtml() != null && email.getIsHtml());
            helper.setText(email.getBody(), isHtml);

            String replyTo = resolveReplyTo(email, fromEmail);
            if (StringUtils.hasText(replyTo)) {
                helper.setReplyTo(replyTo);
            }

            if (email.getId() != null) {
                List<EmailAttachmentEntity> attachments = emailAttachmentPort.findByEmailId(email.getId());
                if (attachments != null && !attachments.isEmpty()) {
                    for (EmailAttachmentEntity attachment : attachments) {
                        File file = new File(attachment.getFilePath());
                        if (file.exists()) {
                            if (StringUtils.hasText(attachment.getContentType())) {
                                helper.addAttachment(
                                        attachment.getOriginalFilename(),
                                        new FileSystemResource(file),
                                        attachment.getContentType());
                            } else {
                                helper.addAttachment(attachment.getOriginalFilename(), file);
                            }
                        } else {
                            log.warn("Attachment file not found: {}", attachment.getFilePath());
                        }
                    }
                }
            }
            
            javaMailSender.send(message);
            String providerMessageId = message.getMessageID();

            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Email sent successfully via JavaMail. MessageId: {}, ResponseTime: {}ms",
                    email.getMessageId(), responseTime);

            return ProviderSendResult.success(
                    EmailProvider.JAVA_MAIL,
                    email.getMessageId(),
                    providerMessageId,
                    responseTime,
                    null,
                    null);

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;

            String errorMessage = resolveProviderErrorMessage(e);
            log.error("Failed to send email via JavaMail. MessageId: {}, Error: {}",
                    email.getMessageId(), errorMessage, e);

            return ProviderSendResult.failure(
                    EmailProvider.JAVA_MAIL,
                    email.getMessageId(),
                    responseTime,
                    null,
                    errorMessage,
                    e.getClass().getSimpleName(),
                    null,
                    List.of());
        }
    }

    private String resolveProviderErrorMessage(Exception exception) {
        if (containsCause(exception, AuthenticationFailedException.class)) {
            return "JavaMail authentication failed. Check MAIL_USERNAME/MAIL_PASSWORD (use Gmail App Password)";
        }

        if (exception.getMessage() == null || exception.getMessage().isBlank()) {
            return exception.getClass().getSimpleName();
        }

        return exception.getMessage();
    }

    private boolean containsCause(Throwable throwable, Class<? extends Throwable> targetType) {
        Throwable current = throwable;
        while (current != null) {
            if (targetType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String resolveFromEmail(EmailEntity email) {
        if (StringUtils.hasText(javaMailProperties.getFrom())) {
            return javaMailProperties.getFrom();
        }
        if (StringUtils.hasText(email.getFromEmail())) {
            return email.getFromEmail();
        }
        throw new IllegalArgumentException("JavaMail sender address is required");
    }

    private String resolveFromName(EmailEntity email) {
        if (StringUtils.hasText(javaMailProperties.getFromName())) {
            return javaMailProperties.getFromName();
        }
        return email.getFromName();
    }

    private String resolveReplyTo(EmailEntity email, String resolvedFromEmail) {
        if (StringUtils.hasText(email.getReplyTo())) {
            return email.getReplyTo();
        }
        if (StringUtils.hasText(javaMailProperties.getReplyTo())) {
            return javaMailProperties.getReplyTo();
        }
        if (StringUtils.hasText(email.getFromEmail())
                && !email.getFromEmail().equalsIgnoreCase(resolvedFromEmail)) {
            return email.getFromEmail();
        }
        return null;
    }
}
