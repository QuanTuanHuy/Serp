/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.last_mile.ui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import serp.project.last_mile.dto.ApiResponse;
import serp.project.last_mile.dto.request.FileUploadRequest;
import serp.project.last_mile.dto.response.FileUploadResponse;
import serp.project.last_mile.exception.AppException;
import serp.project.last_mile.exception.ErrorCode;
import serp.project.last_mile.exception.MessageService;
import serp.project.last_mile.kernel.utils.AuthUtils;
import serp.project.last_mile.service.FileStorageService;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
@Slf4j
public class FileStorageController {

    private final AuthUtils authUtils;
    private final FileStorageService fileStorageService;
    private final MessageService messageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "service", defaultValue = "last-mile") String serviceName,
            @RequestParam(name = "folder", defaultValue = "files") String folder,
            @RequestParam(name = "isPublic", defaultValue = "true") boolean isPublic
    ) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        Long uploaderId = authUtils.getCurrentUserId().orElse(null);

        log.info(
                "REST request to upload file for tenant={}, service={}, folder={}, fileName={}",
                tenantId,
                serviceName,
                folder,
                file != null ? file.getOriginalFilename() : null
        );

        FileUploadResponse result = fileStorageService.upload(FileUploadRequest.builder()
                .content(extractFileBytes(file))
                .originalFileName(file != null ? file.getOriginalFilename() : null)
                .contentType(file != null ? file.getContentType() : null)
                .serviceName(serviceName)
                .folder(folder)
                .tenantId(tenantId)
                .uploaderId(uploaderId)
                .publicFile(isPublic)
                .build());

        return ApiResponse.<FileUploadResponse>builder()
                .message(messageService.getMessage("success.files.upload"))
                .result(result)
                .build();
    }

    private byte[] extractFileBytes(MultipartFile file) {
        if (file == null) {
            throw new AppException(ErrorCode.FILE_UPLOAD_EMPTY);
        }

        try {
            return file.getBytes();
        } catch (IOException exception) {
            log.error("Read multipart file failed", exception);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}
