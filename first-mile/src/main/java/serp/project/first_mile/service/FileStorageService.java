/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import serp.project.first_mile.dto.request.FileUploadRequest;
import serp.project.first_mile.dto.response.FileUploadResponse;

public interface FileStorageService {
    FileUploadResponse upload(FileUploadRequest request);
}
