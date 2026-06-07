/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.ui.controller;

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
import serp.project.second_mile.dto.ApiResponse;
import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.AssignHubPostOfficeRequest;
import serp.project.second_mile.dto.request.CreateHubRequest;
import serp.project.second_mile.dto.request.HubImportDTO;
import serp.project.second_mile.dto.request.HubFilterRequest;
import serp.project.second_mile.dto.request.UpdateHubRequest;
import serp.project.second_mile.dto.response.HubPostOfficeMappingResponse;
import serp.project.second_mile.dto.response.HubResponse;
import serp.project.second_mile.dto.response.ImportHistoryResponse;
import serp.project.second_mile.dto.response.ValidateImportFileDTO;
import serp.project.second_mile.enums.HubStatus;
import serp.project.second_mile.enums.HubType;
import serp.project.second_mile.exception.MessageService;
import serp.project.second_mile.service.HubPostOfficeService;
import serp.project.second_mile.service.HubService;

@RestController
@RequestMapping("/api/v1/hubs")
@RequiredArgsConstructor
@Slf4j
public class HubController {
    private final HubService hubService;
    private final HubPostOfficeService hubPostOfficeService;
    private final MessageService messageService;

    @GetMapping
    public ApiResponse<PageResponse<HubResponse>> getHubs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(name = "hub_type", required = false) HubType hubType,
            @RequestParam(name = "province_code", required = false) String provinceCode,
            @RequestParam(name = "ward_code", required = false) String wardCode,
            @RequestParam(required = false) HubStatus status,
            @RequestParam(name = "has_location", required = false) Boolean hasLocation,
            @RequestParam(name = "min_latitude", required = false) Double minLatitude,
            @RequestParam(name = "max_latitude", required = false) Double maxLatitude,
            @RequestParam(name = "min_longitude", required = false) Double minLongitude,
            @RequestParam(name = "max_longitude", required = false) Double maxLongitude,
            @RequestParam(name = "min_daily_capacity", required = false) Integer minDailyCapacity,
            @RequestParam(name = "max_daily_capacity", required = false) Integer maxDailyCapacity,
            @RequestParam(name = "min_current_load", required = false) Integer minCurrentLoad,
            @RequestParam(name = "max_current_load", required = false) Integer maxCurrentLoad
    ) {
        HubFilterRequest filterRequest = HubFilterRequest.builder()
                .keyword(keyword)
                .code(code)
                .name(name)
                .hubType(hubType)
                .provinceCode(provinceCode)
                .wardCode(wardCode)
                .status(status)
                .hasLocation(hasLocation)
                .minLatitude(minLatitude)
                .maxLatitude(maxLatitude)
                .minLongitude(minLongitude)
                .maxLongitude(maxLongitude)
                .minDailyCapacity(minDailyCapacity)
                .maxDailyCapacity(maxDailyCapacity)
                .minCurrentLoad(minCurrentLoad)
                .maxCurrentLoad(maxCurrentLoad)
                .build();

        return ApiResponse.<PageResponse<HubResponse>>builder()
                .message(messageService.getMessage("success.hubs.list"))
                .result(hubService.getHubs(page, size, filterRequest))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<HubResponse> getHubById(@PathVariable Long id) {
        return ApiResponse.<HubResponse>builder()
                .message(messageService.getMessage("success.hubs.detail"))
                .result(hubService.getHubById(id))
                .build();
    }

    @GetMapping("/{id}/post-offices")
    public ApiResponse<PageResponse<HubPostOfficeMappingResponse>> listHubPostOffices(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.<PageResponse<HubPostOfficeMappingResponse>>builder()
                .message(messageService.getMessage("success.hubs.post_offices.list"))
                .result(hubPostOfficeService.listPostOfficesForHub(id, page, size))
                .build();
    }

    @PostMapping("/{id}/post-offices")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<HubPostOfficeMappingResponse> assignPostOfficeToHub(
            @PathVariable Long id,
            @Valid @RequestBody AssignHubPostOfficeRequest request
    ) {
        return ApiResponse.<HubPostOfficeMappingResponse>builder()
                .message(messageService.getMessage("success.hubs.post_offices.assign"))
                .result(hubPostOfficeService.assignPostOfficeToHub(id, request))
                .build();
    }

    @DeleteMapping("/{id}/post-offices/{postOfficeCode}")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<Void> removePostOfficeFromHub(
            @PathVariable Long id,
            @PathVariable String postOfficeCode
    ) {
        hubPostOfficeService.removePostOfficeFromHub(id, postOfficeCode);
        return ApiResponse.<Void>builder()
                .message(messageService.getMessage("success.hubs.post_offices.remove"))
                .build();
    }

    @GetMapping("/template")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ResponseEntity<byte[]> exportTemplate() {
        log.info("REST request to export Hub Template Excel");

        byte[] excelData = hubService.exportTemplate();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=hub_template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }

    @PostMapping("/validate")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ValidateImportFileDTO<HubImportDTO> validateFile(@RequestParam("file") MultipartFile file) {
        log.info("REST request to validate Hub import file");
        return hubService.validateImportFile(file);
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ImportHistoryResponse importFile(@RequestParam("file") MultipartFile file) {
        log.info("REST request to import Hub file");
        return hubService.importHubsAsync(file);
    }

    @PostMapping
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<HubResponse> createHub(@Valid @RequestBody CreateHubRequest request) {
        return ApiResponse.<HubResponse>builder()
                .message(messageService.getMessage("success.hubs.create"))
                .result(hubService.createHub(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<HubResponse> updateHub(
            @PathVariable Long id,
            @Valid @RequestBody UpdateHubRequest request
    ) {
        return ApiResponse.<HubResponse>builder()
                .message(messageService.getMessage("success.hubs.update"))
                .result(hubService.updateHub(id, request))
                .build();
    }

    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<HubResponse> uploadHubImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        return ApiResponse.<HubResponse>builder()
                .message(messageService.getMessage("success.hubs.image.upload"))
                .result(hubService.uploadImage(id, file))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<Void> deleteHub(@PathVariable Long id) {
        hubService.deleteHub(id);
        return ApiResponse.<Void>builder()
                .message(messageService.getMessage("success.hubs.delete"))
                .build();
    }
}
