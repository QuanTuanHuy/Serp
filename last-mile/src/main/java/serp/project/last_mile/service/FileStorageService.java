/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.last_mile.service;

import serp.project.last_mile.dto.request.FileUploadRequest;
import serp.project.last_mile.dto.response.FileUploadResponse;

public interface FileStorageService {
    FileUploadResponse upload(FileUploadRequest request);
}
