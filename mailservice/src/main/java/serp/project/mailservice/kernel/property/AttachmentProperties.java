/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.kernel.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app.email.attachments")
public class AttachmentProperties {
    private String storagePath = "./data/email-attachments";
    private Integer maxSizeMb = 25;
    private List<String> allowedExtensions = List.of("pdf", "doc", "docx", "xls", "xlsx", "txt", "jpg", "jpeg", "png",
            "gif", "zip");
    private Integer retentionDays = 7;
}
