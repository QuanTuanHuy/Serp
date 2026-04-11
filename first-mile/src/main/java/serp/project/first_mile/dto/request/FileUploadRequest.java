/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileUploadRequest {
    byte[] content;
    String originalFileName;
    String contentType;
    String serviceName;
    String folder;
    Long tenantId;
    Long uploaderId;

    @Builder.Default
    boolean publicFile = true;
}
