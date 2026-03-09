/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Storage provider enumeration
 */

package serp.project.discuss_service.core.domain.enums;

import lombok.Getter;

/**
 * Represents the type of storage provider used for file storage.
 * Supports S3-compatible storage (MinIO, AWS S3) and future cloud providers.
 */
@Getter
public enum StorageProvider {
    S3("S3", "S3-Compatible Storage (MinIO/AWS S3)"),
    GCS("GCS", "Google Cloud Storage"),
    AZURE_BLOB("AZURE_BLOB", "Azure Blob Storage"),
    LOCAL("LOCAL", "Local File System");

    private final String code;
    private final String displayName;

    StorageProvider(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static StorageProvider fromCode(String code) {
        for (StorageProvider provider : values()) {
            if (provider.code.equalsIgnoreCase(code)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown storage provider: " + code);
    }

    /**
     * Check if this provider is S3-compatible (MinIO or AWS S3)
     */
    public boolean isS3Compatible() {
        return this == S3;
    }
}
