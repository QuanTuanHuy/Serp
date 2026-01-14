/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Storage port interface for file operations
 */

package serp.project.discuss_service.core.port.client;

import serp.project.discuss_service.core.domain.enums.StorageProvider;
import serp.project.discuss_service.core.domain.vo.FileUploadResult;
import serp.project.discuss_service.core.domain.vo.StorageLocation;

import java.io.InputStream;
import java.time.Duration;

/**
 * Port interface for file storage operations.
 * Supports S3-compatible storage (MinIO, AWS S3) and can be extended for other providers.
 */
public interface IStoragePort {

    /**
     * Upload a file to storage
     *
     * @param inputStream The file content stream
     * @param fileName    The original file name
     * @param contentType The MIME type of the file
     * @param fileSize    The size of the file in bytes
     * @param tenantId    The tenant ID for path organization
     * @param channelId   The channel ID for path organization
     * @return FileUploadResult containing storage location or error
     */
    FileUploadResult upload(InputStream inputStream, String fileName, String contentType,
                            Long fileSize, Long tenantId, Long channelId);

    /**
     * Download a file from storage
     *
     * @param location The storage location of the file
     * @return InputStream of the file content
     */
    InputStream download(StorageLocation location);

    /**
     * Delete a file from storage
     *
     * @param location The storage location of the file
     * @return true if deleted successfully, false otherwise
     */
    boolean delete(StorageLocation location);

    /**
     * Generate a presigned URL for downloading a file
     *
     * @param location The storage location of the file
     * @param expiry   How long the URL should be valid
     * @return Presigned URL string
     */
    String generatePresignedUrl(StorageLocation location, Duration expiry);

    /**
     * Generate a presigned URL with default expiry (1 hour)
     *
     * @param location The storage location of the file
     * @return Presigned URL string
     */
    default String generatePresignedUrl(StorageLocation location) {
        return generatePresignedUrl(location, Duration.ofHours(1));
    }

    /**
     * Check if a file exists in storage
     *
     * @param location The storage location to check
     * @return true if the file exists, false otherwise
     */
    boolean exists(StorageLocation location);

    /**
     * Get the storage provider type
     *
     * @return The storage provider enum value
     */
    StorageProvider getProvider();

    /**
     * Get the configured bucket name
     *
     * @return The bucket name
     */
    String getBucket();

    /**
     * Ensure the bucket exists, create if not
     */
    void ensureBucketExists();
}
