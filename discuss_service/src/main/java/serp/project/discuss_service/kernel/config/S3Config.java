/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - S3 client configuration
 */

package serp.project.discuss_service.kernel.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.discuss_service.kernel.property.StorageProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Configuration for S3 client (works with MinIO and AWS S3)
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class S3Config {

    private final StorageProperties storageProperties;

    @Bean
    public S3Client s3Client() {
        StorageProperties.S3Properties s3Props = storageProperties.getS3();

        var builder = S3Client.builder()
                .region(Region.of(s3Props.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3Props.getAccessKey(), s3Props.getSecretKey())
                ));

        // For MinIO or custom S3-compatible endpoint
        if (StringUtils.hasText(s3Props.getEndpoint())) {
            builder.endpointOverride(URI.create(s3Props.getEndpoint()))
                    .forcePathStyle(true);  // Required for MinIO
            log.info("Configured S3 client with custom endpoint: {}", s3Props.getEndpoint());
        } else {
            log.info("Configured S3 client for AWS S3 in region: {}", s3Props.getRegion());
        }

        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        StorageProperties.S3Properties s3Props = storageProperties.getS3();

        var builder = S3Presigner.builder()
                .region(Region.of(s3Props.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3Props.getAccessKey(), s3Props.getSecretKey())
                ));

        // For MinIO or custom S3-compatible endpoint
        if (StringUtils.hasText(s3Props.getEndpoint())) {
            builder.endpointOverride(URI.create(s3Props.getEndpoint()));
            // Use path-style URLs for MinIO (bucket in path, not subdomain)
            // e.g., http://localhost:9000/bucket/key instead of http://bucket.localhost:9000/key
            builder.serviceConfiguration(software.amazon.awssdk.services.s3.S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build());
            log.info("Configured S3 presigner with path-style access for endpoint: {}", s3Props.getEndpoint());
        }

        return builder.build();
    }
}
