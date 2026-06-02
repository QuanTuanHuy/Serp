/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.last_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileUploadResponse {
    String bucket;
    String objectKey;
    String fileName;
    String contentType;
    Long size;
    String url;
}
