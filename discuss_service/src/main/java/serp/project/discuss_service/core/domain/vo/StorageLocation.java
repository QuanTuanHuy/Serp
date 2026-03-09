/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Storage location value object
 */

package serp.project.discuss_service.core.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.discuss_service.core.domain.enums.StorageProvider;

/**
 * Value object representing a file's location in storage.
 * Immutable and used to track where files are stored.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class StorageLocation {

    /**
     * The storage provider (S3, GCS, AZURE_BLOB, LOCAL)
     */
    private StorageProvider provider;

    /**
     * The bucket/container name
     */
    private String bucket;

    /**
     * The object key/path within the bucket
     */
    private String key;

    /**
     * The public or presigned URL to access the file
     */
    private String url;

    /**
     * Create a new storage location for S3-compatible storage
     */
    public static StorageLocation ofS3(String bucket, String key, String url) {
        return StorageLocation.builder()
                .provider(StorageProvider.S3)
                .bucket(bucket)
                .key(key)
                .url(url)
                .build();
    }

    /**
     * Create a new storage location for S3-compatible storage without URL
     * (URL will be generated later via presigned URL)
     */
    public static StorageLocation ofS3(String bucket, String key) {
        return StorageLocation.builder()
                .provider(StorageProvider.S3)
                .bucket(bucket)
                .key(key)
                .build();
    }

    /**
     * Get the full path (bucket + key)
     */
    public String getFullPath() {
        return bucket + "/" + key;
    }

    @Override
    public String toString() {
        return String.format("StorageLocation{provider=%s, bucket='%s', key='%s'}", 
                provider, bucket, key);
    }
}
