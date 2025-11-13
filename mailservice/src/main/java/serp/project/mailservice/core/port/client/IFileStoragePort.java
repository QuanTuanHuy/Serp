/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.port.client;

import java.io.InputStream;

public interface IFileStoragePort {
    String storeFile(InputStream inputStream, String filename, String contentType);

    InputStream retrieveFile(String filePath);

    void deleteFile(String filePath);

    boolean fileExists(String filePath);

    long getFileSize(String filePath);

    /**
     * Calculate file checksum (SHA-256)
     * 
     * @param inputStream File input stream
     * @return Checksum string
     */
    String calculateChecksum(InputStream inputStream);
}
