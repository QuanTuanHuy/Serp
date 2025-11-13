/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.mailservice.core.port.client.IFileStoragePort;
import serp.project.mailservice.kernel.property.AttachmentProperties;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocalFileStorageAdapter implements IFileStoragePort {

    private final AttachmentProperties attachmentProperties;

    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Override
    public String storeFile(InputStream inputStream, String filename, String contentType) {
        try {
            Path uploadPath = Paths.get(attachmentProperties.getStoragePath());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            String uniqueFilename = String.format("%s_%s_%s", timestamp, uuid, filename);

            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored successfully: {}", filePath);

            return filePath.toString();

        } catch (IOException e) {
            log.error("Failed to store file: {}", e.getMessage(), e);
            throw new RuntimeException("Could not store file: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream retrieveFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.error("File not found: {}", filePath);
                throw new FileNotFoundException("File not found: " + filePath);
            }

            return Files.newInputStream(path);

        } catch (IOException e) {
            log.error("Failed to retrieve file: {}", e.getMessage(), e);
            throw new RuntimeException("Could not retrieve file: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.warn("File not found for deletion: {}", filePath);
                return;
            }

            Files.deleteIfExists(path);
            log.info("File deleted successfully: {}", filePath);

        } catch (IOException e) {
            log.error("Error deleting file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean fileExists(String filePath) {
        Path path = Paths.get(filePath);
        return Files.exists(path);
    }

    @Override
    public long getFileSize(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return 0;
            }
            return Files.size(path);
        } catch (IOException e) {
            log.error("Failed to get file size: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public String calculateChecksum(InputStream inputStream) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            byte[] hashBytes = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            log.error("Failed to calculate checksum: {}", e.getMessage(), e);
            throw new RuntimeException("Could not calculate checksum: " + e.getMessage(), e);
        }
    }

    public int deleteExpiredFiles(LocalDateTime expirationDate) {
        int deletedCount = 0;

        try {
            Path storagePath = Paths.get(attachmentProperties.getStoragePath());

            if (!Files.exists(storagePath)) {
                log.warn("Storage path does not exist: {}", storagePath);
                return 0;
            }

            deletedCount = (int) Files.walk(storagePath)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            LocalDateTime fileTime = LocalDateTime.ofInstant(
                                    Files.getLastModifiedTime(path).toInstant(),
                                    java.time.ZoneId.systemDefault());
                            return fileTime.isBefore(expirationDate);
                        } catch (IOException e) {
                            log.error("Error checking file time: {}", e.getMessage());
                            return false;
                        }
                    })
                    .filter(path -> {
                        try {
                            Files.delete(path);
                            log.debug("Deleted expired file: {}", path);
                            return true;
                        } catch (IOException e) {
                            log.error("Failed to delete expired file: {}", e.getMessage());
                            return false;
                        }
                    })
                    .count();

            log.info("Deleted {} expired files older than {}", deletedCount, expirationDate);

        } catch (IOException e) {
            log.error("Error during expired files cleanup: {}", e.getMessage(), e);
        }

        return deletedCount;
    }

    public String getStoragePath() {
        return attachmentProperties.getStoragePath();
    }
}
