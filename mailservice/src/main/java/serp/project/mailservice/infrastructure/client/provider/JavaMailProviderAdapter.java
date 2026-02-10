/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.client.provider;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import serp.project.mailservice.core.domain.entity.EmailAttachmentEntity;
import serp.project.mailservice.core.domain.entity.EmailEntity;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.port.client.IEmailProviderPort;
import serp.project.mailservice.core.port.store.IEmailAttachmentPort;

import java.io.File;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.email.java-mail.enable", havingValue = "true")
public class JavaMailProviderAdapter implements IEmailProviderPort {
    
    private final JavaMailSender javaMailSender;
    private final IEmailAttachmentPort emailAttachmentPort;
    
    @Override
    public Map<String, Object> sendEmail(EmailEntity email) {
        return sendEmailInternal(email, false);
    }
    
    @Override
    public Map<String, Object> sendHtmlEmail(EmailEntity email) {
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
            javaMailSender.createMimeMessage();
            log.debug("JavaMail provider health check passed");
            return true;
        } catch (Exception e) {
            log.error("JavaMail provider health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    private Map<String, Object> sendEmailInternal(EmailEntity email, boolean forceHtml) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(email.getFromEmail());
            
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
            
            if (email.getReplyTo() != null && !email.getReplyTo().isEmpty()) {
                helper.setReplyTo(email.getReplyTo());
            }
            
            if (email.getId() != null) {
                List<EmailAttachmentEntity> attachments = emailAttachmentPort.findByEmailId(email.getId());
                if (attachments != null && !attachments.isEmpty()) {
                    for (EmailAttachmentEntity attachment : attachments) {
                        File file = new File(attachment.getFilePath());
                        if (file.exists()) {
                            helper.addAttachment(attachment.getOriginalFilename(), file);
                        } else {
                            log.warn("Attachment file not found: {}", attachment.getFilePath());
                        }
                    }
                }
            }
            
            javaMailSender.send(message);
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            result.put("success", true);
            result.put("provider", EmailProvider.JAVA_MAIL.name());
            result.put("messageId", email.getMessageId());
            result.put("sentAt", Instant.now().toEpochMilli());
            result.put("responseTimeMs", responseTime);
            
            log.info("Email sent successfully via JavaMail. MessageId: {}, ResponseTime: {}ms", 
                    email.getMessageId(), responseTime);
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            
            log.error("Failed to send email via JavaMail. MessageId: {}, Error: {}", 
                    email.getMessageId(), e.getMessage(), e);
            
            result.put("success", false);
            result.put("provider", EmailProvider.JAVA_MAIL.name());
            result.put("error", e.getMessage());
            result.put("errorClass", e.getClass().getSimpleName());
            result.put("responseTimeMs", responseTime);
        }
        
        return result;
    }
}
