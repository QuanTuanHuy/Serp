/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.dto.ApiResponse;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.request.AssignPostOfficeHubRequest;
import serp.project.first_mile.dto.request.CreatePostOfficeRequest;
import serp.project.first_mile.dto.request.PostOfficeFilterRequest;
import serp.project.first_mile.dto.request.PostOfficeImportDTO;
import serp.project.first_mile.dto.request.UpdatePostOfficeRequest;
import serp.project.first_mile.dto.response.ImportHistoryResponse;
import serp.project.first_mile.dto.response.PostOfficeGeocodeBatchResponse;
import serp.project.first_mile.dto.response.PostOfficeResponse;
import serp.project.first_mile.dto.response.ValidateImportFileDTO;
import serp.project.first_mile.enums.PostOfficeStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.exception.MessageService;
import serp.project.first_mile.kernel.utils.AuthUtils;
import serp.project.first_mile.service.PostOfficeService;

@RestController
@RequestMapping("/api/v1/post-offices")
@RequiredArgsConstructor
@Slf4j
public class PostOfficeController {
    private final PostOfficeService postOfficeService;
    private final MessageService messageService;
    private final AuthUtils authUtils;

    @GetMapping
    public ApiResponse<PageResponse<PostOfficeResponse>> getPostOffices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(name = "province_code", required = false) String provinceCode,
            @RequestParam(name = "ward_code", required = false) String wardCode,
            @RequestParam(required = false) PostOfficeStatus status,
            @RequestParam(name = "has_location", required = false) Boolean hasLocation,
            @RequestParam(name = "min_service_radius_m", required = false) Integer minServiceRadiusM,
            @RequestParam(name = "max_service_radius_m", required = false) Integer maxServiceRadiusM,
            @RequestParam(name = "min_daily_capacity", required = false) Integer minDailyCapacity,
            @RequestParam(name = "max_daily_capacity", required = false) Integer maxDailyCapacity,
            @RequestParam(name = "min_current_load", required = false) Integer minCurrentLoad,
            @RequestParam(name = "max_current_load", required = false) Integer maxCurrentLoad,
            @RequestParam(name = "min_priority", required = false) Integer minPriority,
            @RequestParam(name = "max_priority", required = false) Integer maxPriority,
            @RequestParam(name = "hub_id", required = false) Long hubId
    ) {
        PostOfficeFilterRequest filterRequest = PostOfficeFilterRequest.builder()
                .keyword(keyword)
                .code(code)
                .name(name)
                .provinceCode(provinceCode)
                .wardCode(wardCode)
                .status(status)
                .hasLocation(hasLocation)
                .minServiceRadiusM(minServiceRadiusM)
                .maxServiceRadiusM(maxServiceRadiusM)
                .minDailyCapacity(minDailyCapacity)
                .maxDailyCapacity(maxDailyCapacity)
                .minCurrentLoad(minCurrentLoad)
                .maxCurrentLoad(maxCurrentLoad)
                .minPriority(minPriority)
                .maxPriority(maxPriority)
                .hubId(hubId)
                .build();

        return ApiResponse.<PageResponse<PostOfficeResponse>>builder()
                .message(messageService.getMessage("success.post_offices.list"))
                .result(postOfficeService.getPostOffices(page, size, filterRequest))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PostOfficeResponse> getPostOfficeById(@PathVariable Long id) {
        return ApiResponse.<PostOfficeResponse>builder()
                .message(messageService.getMessage("success.post_offices.detail"))
                .result(postOfficeService.getPostOfficeById(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<PostOfficeResponse> createPostOffice(@Valid @RequestBody CreatePostOfficeRequest request) {
        return ApiResponse.<PostOfficeResponse>builder()
                .message(messageService.getMessage("success.post_offices.create"))
                .result(postOfficeService.createPostOffice(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<PostOfficeResponse> updatePostOffice(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostOfficeRequest request
    ) {
        return ApiResponse.<PostOfficeResponse>builder()
                .message(messageService.getMessage("success.post_offices.update"))
                .result(postOfficeService.updatePostOffice(id, request))
                .build();
    }

    @PutMapping("/{id}/hub")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<PostOfficeResponse> assignPostOfficeHub(
            @PathVariable Long id,
            @RequestBody(required = false) AssignPostOfficeHubRequest request
    ) {
        return ApiResponse.<PostOfficeResponse>builder()
                .message(messageService.getMessage("success.post_offices.hub.assign"))
                .result(postOfficeService.assignPostOfficeHub(id, request))
                .build();
    }

    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<PostOfficeResponse> uploadPostOfficeImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        return ApiResponse.<PostOfficeResponse>builder()
                .message(messageService.getMessage("success.post_offices.image.upload"))
                .result(postOfficeService.uploadImage(id, file))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<Void> deletePostOffice(@PathVariable Long id) {
        postOfficeService.deletePostOffice(id);
        return ApiResponse.<Void>builder()
                .message(messageService.getMessage("success.post_offices.delete"))
                .build();
    }

    @PutMapping("/{id}/location/geocode")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<PostOfficeResponse> updatePostOfficeLocationByGeocode(@PathVariable Long id) {
        return ApiResponse.<PostOfficeResponse>builder()
                .message(messageService.getMessage("success.post_offices.geocode.single"))
                .result(postOfficeService.updatePostOfficeLocationByGeocode(id))
                .build();
    }

    @PutMapping("/location/geocode-null")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<PostOfficeGeocodeBatchResponse> updateNullLocationsByGeocode(
            @RequestParam(defaultValue = "50") int batch
    ) {
        return ApiResponse.<PostOfficeGeocodeBatchResponse>builder()
                .message(messageService.getMessage("success.post_offices.geocode.batch"))
                .result(postOfficeService.updatePostOfficesWithNullLocationByGeocode(batch))
                .build();
    }

    @GetMapping("/template")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ResponseEntity<byte[]> exportTemplate() {
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
        log.info("REST request to export Post Office Template Excel for tenant {}", tenantId);

        byte[] excelData = postOfficeService.exportTemplate();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=post_office_template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }

    @PostMapping("/validate")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ValidateImportFileDTO<PostOfficeImportDTO> validateFile(
            @RequestParam("file") MultipartFile file
    ) {
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
        log.info("REST request to validate Post Office import file for tenant {}", tenantId);
        return postOfficeService.validateImportFile(file, tenantId);
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ImportHistoryResponse importFile(
            @RequestParam("file") MultipartFile file
    ) {
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
        log.info("REST request to import Post Office file for tenant {}", tenantId);
        return postOfficeService.importPostOfficesAsync(file, tenantId);
    }
}
