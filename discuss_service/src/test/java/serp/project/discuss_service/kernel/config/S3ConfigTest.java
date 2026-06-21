/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for S3Config
 */

package serp.project.discuss_service.kernel.config;

import org.junit.jupiter.api.Test;
import serp.project.discuss_service.kernel.property.StorageProperties;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class S3ConfigTest {

    @Test
    void s3Client_ShouldUseVirtualHostedStyleByDefault() {
        StorageProperties storageProperties = storageProperties(false);
        S3Config config = new S3Config(storageProperties);

        try (S3Client s3Client = config.s3Client()) {
            String url = s3Client.utilities()
                    .getUrl(GetUrlRequest.builder()
                            .bucket("discuss-attachments")
                            .key("tenant-1/file.txt")
                            .build())
                    .toString();

            assertEquals("http://discuss-attachments.localhost:9000/tenant-1/file.txt", url);
        }
    }

    @Test
    void s3Client_ShouldUsePathStyleWhenEnabled() {
        StorageProperties storageProperties = storageProperties(true);
        S3Config config = new S3Config(storageProperties);

        try (S3Client s3Client = config.s3Client()) {
            String url = s3Client.utilities()
                    .getUrl(GetUrlRequest.builder()
                            .bucket("discuss-attachments")
                            .key("tenant-1/file.txt")
                            .build())
                    .toString();

            assertEquals("http://localhost:9000/discuss-attachments/tenant-1/file.txt", url);
        }
    }

    private StorageProperties storageProperties(boolean pathStyleAccessEnabled) {
        StorageProperties storageProperties = new StorageProperties();
        StorageProperties.S3Properties s3Properties = storageProperties.getS3();

        s3Properties.setEndpoint("http://localhost:9000");
        s3Properties.setAccessKey("minioadmin");
        s3Properties.setSecretKey("minioadmin123");
        s3Properties.setBucket("discuss-attachments");
        s3Properties.setRegion("us-east-1");
        s3Properties.setPathStyleAccessEnabled(pathStyleAccessEnabled);

        return storageProperties;
    }
}
