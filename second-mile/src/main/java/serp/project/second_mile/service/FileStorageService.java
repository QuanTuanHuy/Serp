/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service;

import serp.project.second_mile.service.dto.request.FileUploadRequest;
import serp.project.second_mile.service.dto.response.FileUploadResponse;

public interface FileStorageService {
    FileUploadResponse upload(FileUploadRequest request);
}
