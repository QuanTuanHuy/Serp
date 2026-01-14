/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - File upload result value object
 */

package serp.project.discuss_service.core.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Value object representing the result of a file upload operation.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class FileUploadResult {

    /**
     * Whether the upload was successful
     */
    private boolean success;

    /**
     * The storage location where the file was uploaded
     */
    private StorageLocation storageLocation;

    /**
     * Error message if upload failed
     */
    private String errorMessage;

    /**
     * The content type of the uploaded file
     */
    private String contentType;

    /**
     * The size of the uploaded file in bytes
     */
    private Long fileSize;

    /**
     * Create a successful upload result
     */
    public static FileUploadResult success(StorageLocation location, String contentType, Long fileSize) {
        return FileUploadResult.builder()
                .success(true)
                .storageLocation(location)
                .contentType(contentType)
                .fileSize(fileSize)
                .build();
    }

    /**
     * Create a successful upload result with just location
     */
    public static FileUploadResult success(StorageLocation location) {
        return FileUploadResult.builder()
                .success(true)
                .storageLocation(location)
                .build();
    }

    /**
     * Create a failed upload result
     */
    public static FileUploadResult failure(String errorMessage) {
        return FileUploadResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * Check if the upload failed
     */
    public boolean isFailed() {
        return !success;
    }
}
