/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.last_mile.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
