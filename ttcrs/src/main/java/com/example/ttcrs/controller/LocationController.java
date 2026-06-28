package com.example.ttcrs.controller;

import com.example.ttcrs.dto.request.location.CreateLocationDTO;
import com.example.ttcrs.dto.request.location.UpdateLocationDTO;
import com.example.ttcrs.dto.response.ApiResponse;
import com.example.ttcrs.dto.response.LocationImportResult;
import com.example.ttcrs.dto.response.LocationResponseDTO;
import com.example.ttcrs.service.LocationImportService;
import com.example.ttcrs.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import java.io.IOException;
import java.util.List;

/**
 * Controller cho Location API (dành cho Dispatcher).
 *
 * <p>Base URL: {@code /ttcrs/api/v1/dispatcher/locations}
 */
@Slf4j
@RestController
@RequestMapping("/ttcrs/api/v1/dispatcher/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final LocationImportService locationImportService;

    /**
     * Lấy danh sách tất cả Location trong tenant của Dispatcher hiện tại.
     *
     * @return {@code 200 OK} với danh sách Location
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<LocationResponseDTO>>> getLocations() {
        log.info("GET /ttcrs/api/v1/dispatcher/locations");
        List<LocationResponseDTO> locations = locationService.getLocationsForCurrentTenant();
        return ResponseEntity.ok(ApiResponse.ok(locations));
    }

    /**
     * Tạo mới một Location cho tenant hiện tại.
     *
     * <p><b>Request body:</b>
     * <ul>
     *   <li>{@code locationCode} — mã định danh duy nhất (bắt buộc)</li>
     *   <li>{@code type}         — loại location (bắt buộc): PORT | WAREHOUSE | DEPOT_CONTAINER | DEPOT_TRUCK | DEPOT_TRAILER</li>
     *   <li>{@code lat}          — vĩ độ từ bản đồ (bắt buộc)</li>
     *   <li>{@code lng}          — kinh độ từ bản đồ (bắt buộc)</li>
     * </ul>
     *
     * @param dto tham số tạo location
     * @return {@code 201 Created} với location vừa tạo, hoặc {@code 409 Conflict} nếu locationCode đã tồn tại
     */
    @PostMapping
    public ResponseEntity<ApiResponse<LocationResponseDTO>> createLocation(
            @Valid @RequestBody CreateLocationDTO dto
    ) {
        log.info("POST /ttcrs/api/v1/dispatcher/locations - code={}, type={}", dto.getLocationCode(), dto.getType());
        try {
            LocationResponseDTO created = locationService.createLocation(dto);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Location created successfully", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Cập nhật một Location theo ID.
     *
     * @param id  ID của location cần cập nhật
     * @param dto các trường cần thay đổi
     * @return {@code 200 OK} với location đã cập nhật, hoặc {@code 404 Not Found}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LocationResponseDTO>> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLocationDTO dto
    ) {
        log.info("PUT /ttcrs/api/v1/dispatcher/locations/{}", id);
        try {
            LocationResponseDTO updated = locationService.updateLocation(id, dto);
            return ResponseEntity.ok(ApiResponse.ok("Location updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Xoá mềm một Location theo ID.
     *
     * @param id ID của location cần xoá
     * @return {@code 204 No Content} nếu thành công, hoặc {@code 404 Not Found}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLocation(@PathVariable Long id) {
        log.info("DELETE /ttcrs/api/v1/dispatcher/locations/{}", id);
        try {
            locationService.deleteLocation(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Import danh sách Location từ file Excel.
     *
     * <p>File Excel phải có các cột: Location Code, Type, Latitude, Longitude (có header).
     * Dòng không hợp lệ sẽ được báo lỗi cụ thể (dòng nào, thiếu trường gì).
     *
     * @param file file Excel (.xlsx hoặc .xls)
     * @return kết quả import gồm danh sách location đã tạo và các lỗi
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<LocationImportResult>> importLocations(
            @RequestParam("file") MultipartFile file
    ) {
        log.info("POST /ttcrs/api/v1/dispatcher/locations/import - file={}, size={}",
                file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File rỗng. Vui lòng chọn file Excel."));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File phải có định dạng .xlsx hoặc .xls"));
        }

        try {
            LocationImportResult result = locationImportService.importLocations(file);
            return ResponseEntity.ok(ApiResponse.ok("Import completed", result));
        } catch (IOException e) {
            log.error("Failed to read Excel file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Không thể đọc file Excel: " + e.getMessage()));
        }
    }

    /**
     * Tải template Excel để nhập danh sách Location.
     *
     * <p>Template chứa header và dropdown validation cho cột Type.
     *
     * @return file Excel template
     */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        log.info("GET /ttcrs/api/v1/dispatcher/locations/template");
        try {
            byte[] template = locationImportService.generateTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "location_import_template.xlsx");
            headers.setContentLength(template.length);
            return ResponseEntity.ok().headers(headers).body(template);
        } catch (IOException e) {
            log.error("Failed to generate template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
