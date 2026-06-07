/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.CreateHubRequest;
import serp.project.second_mile.dto.request.HubImportDTO;
import serp.project.second_mile.dto.request.HubFilterRequest;
import serp.project.second_mile.dto.request.UpdateHubRequest;
import serp.project.second_mile.dto.response.HubResponse;
import serp.project.second_mile.dto.response.ImportHistoryResponse;
import serp.project.second_mile.dto.response.ValidateImportFileDTO;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kernel.utils.ExcelTemplateUtils;
import serp.project.second_mile.kernel.utils.ImageContentTypeUtils;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.mapper.HubMapper;
import serp.project.second_mile.repository.HubRepository;
import serp.project.second_mile.repository.ProvinceRepository;
import serp.project.second_mile.repository.WardRepository;
import serp.project.second_mile.repository.projection.CodeNameProjection;
import serp.project.second_mile.repository.specification.HubSpecification;
import serp.project.second_mile.service.FileStorageService;
import serp.project.second_mile.service.HubImportExcelService;
import serp.project.second_mile.service.HubService;
import serp.project.second_mile.service.dto.request.FileUploadRequest;
import serp.project.second_mile.service.dto.response.FileUploadResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HubServiceImpl implements HubService {
    private static final String TEMPLATE_PATH = "excel/hub_template.xlsx";
    private static final String UNIT_SHEET_NAME = "Unit";
    private static final int START_ROW_INDEX = 1;
    private static final int PROVINCE_COLUMN_INDEX = 0;
    private static final int WARD_COLUMN_INDEX = 1;
    private static final String STORAGE_SERVICE_NAME = "second-mile";
    private static final String HUB_IMAGE_FOLDER = "hub-image";

    private final HubRepository hubRepository;
    private final ProvinceRepository provinceRepository;
    private final WardRepository wardRepository;
    private final SecondMileAccessUtils secondMileAccessUtils;
    private final FileStorageService fileStorageService;
    private final HubImportExcelService hubImportExcelService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HubResponse> getHubs(int page, int size, HubFilterRequest filterRequest) {
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        HubFilterRequest normalizedFilterRequest = normalizeFilterRequest(filterRequest);
        validateFilterRanges(normalizedFilterRequest);

        Page<Hub> hubPage = hubRepository.findAll(
                HubSpecification.byFilter(tenantId, normalizedFilterRequest),
                pageable
        );

        Page<HubResponse> mappedPage = hubPage.map(HubMapper::toResponse);

        return PageResponse.<HubResponse>builder()
                .items(mappedPage.getContent())
                .page(mappedPage.getNumber())
                .size(mappedPage.getSize())
                .totalElements(mappedPage.getTotalElements())
                .totalPages(mappedPage.getTotalPages())
                .hasNext(mappedPage.hasNext())
                .hasPrevious(mappedPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public HubResponse getHubById(Long id) {
        Hub hub = getHubOrThrow(id);
        return HubMapper.toResponse(hub);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportTemplate() {
        secondMileAccessUtils.getCurrentTenantIdOrThrow();

        List<CodeNameProjection> provinces = provinceRepository.findTemplateCodeNameList();
        List<CodeNameProjection> wards = wardRepository.findTemplateCodeNameList();

        try (InputStream inputStream = new ClassPathResource(TEMPLATE_PATH).getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet unitSheet = workbook.getSheet(UNIT_SHEET_NAME);
            if (unitSheet == null) {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }

            populateProvinceColumn(unitSheet, provinces);
            populateWardColumn(unitSheet, wards);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public ValidateImportFileDTO<HubImportDTO> validateImportFile(MultipartFile file) {
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        return hubImportExcelService.validateImportFile(file, tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportHistoryResponse importHubsAsync(MultipartFile file) {
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        return hubImportExcelService.importHubsAsync(file, tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HubResponse createHub(CreateHubRequest request) {
        validateGeoCoordinates(request.getLatitude(), request.getLongitude());
        validateWorkingTime(request.getWorkingStartTime(), request.getWorkingEndTime());
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();

        if (hubRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.HUB_CODE_EXISTED);
        }

        Hub hub = HubMapper.toEntity(request);
        hub.setTenantId(tenantId);
        Hub savedHub = hubRepository.save(hub);
        return HubMapper.toResponse(savedHub);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HubResponse updateHub(Long id, UpdateHubRequest request) {
        validateGeoCoordinates(request.getLatitude(), request.getLongitude());
        validateWorkingTime(request.getWorkingStartTime(), request.getWorkingEndTime());

        Hub hub = getHubOrThrow(id);
        validateTenantAccess(hub);

        if (!hub.getCode().equals(request.getCode()) && hubRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.HUB_CODE_EXISTED);
        }

        HubMapper.mapForUpdate(request, hub);
        hub.setTenantId(secondMileAccessUtils.getCurrentTenantIdOrThrow());
        Hub updatedHub = hubRepository.save(hub);
        return HubMapper.toResponse(updatedHub);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HubResponse uploadImage(Long id, MultipartFile file) {
        Hub hub = getHubOrThrow(id);
        validateTenantAccess(hub);

        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_UPLOAD_EMPTY);
        }

        String contentType = ImageContentTypeUtils.normalizeImageContentType(file.getContentType());

        try {
            FileUploadResponse uploadResponse = fileStorageService.upload(FileUploadRequest.builder()
                    .content(file.getBytes())
                    .originalFileName(file.getOriginalFilename())
                    .contentType(contentType)
                    .serviceName(STORAGE_SERVICE_NAME)
                    .folder(HUB_IMAGE_FOLDER)
                    .tenantId(hub.getTenantId())
                    .uploaderId(secondMileAccessUtils.getCurrentUserIdOrNull())
                    .publicFile(true)
                    .build());

            hub.setImageUrl(uploadResponse.getUrl());
            Hub updatedHub = hubRepository.save(hub);
            return HubMapper.toResponse(updatedHub);
        } catch (IOException exception) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteHub(Long id) {
        Hub hub = getHubOrThrow(id);
        validateTenantAccess(hub);
        hubRepository.delete(hub);
    }

    private Hub getHubOrThrow(Long id) {
        return hubRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.HUB_NOT_FOUND));
    }

    private HubFilterRequest normalizeFilterRequest(HubFilterRequest filterRequest) {
        if (filterRequest == null) {
            return HubFilterRequest.builder().build();
        }

        return HubFilterRequest.builder()
                .keyword(normalizeText(filterRequest.getKeyword()))
                .code(normalizeText(filterRequest.getCode()))
                .name(normalizeText(filterRequest.getName()))
                .hubType(filterRequest.getHubType())
                .provinceCode(normalizeText(filterRequest.getProvinceCode()))
                .wardCode(normalizeText(filterRequest.getWardCode()))
                .status(filterRequest.getStatus())
                .hasLocation(filterRequest.getHasLocation())
                .minLatitude(filterRequest.getMinLatitude())
                .maxLatitude(filterRequest.getMaxLatitude())
                .minLongitude(filterRequest.getMinLongitude())
                .maxLongitude(filterRequest.getMaxLongitude())
                .minDailyCapacity(filterRequest.getMinDailyCapacity())
                .maxDailyCapacity(filterRequest.getMaxDailyCapacity())
                .minCurrentLoad(filterRequest.getMinCurrentLoad())
                .maxCurrentLoad(filterRequest.getMaxCurrentLoad())
                .build();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private void validateFilterRanges(HubFilterRequest filterRequest) {
        validateCoordinateBounds(filterRequest);
        validateRange(filterRequest.getMinDailyCapacity(), filterRequest.getMaxDailyCapacity());
        validateRange(filterRequest.getMinCurrentLoad(), filterRequest.getMaxCurrentLoad());
    }

    private void validateCoordinateBounds(HubFilterRequest filterRequest) {
        validateLatitude(filterRequest.getMinLatitude());
        validateLatitude(filterRequest.getMaxLatitude());
        validateLongitude(filterRequest.getMinLongitude());
        validateLongitude(filterRequest.getMaxLongitude());
        validateCoordinateRange(filterRequest.getMinLatitude(), filterRequest.getMaxLatitude());
        validateCoordinateRange(filterRequest.getMinLongitude(), filterRequest.getMaxLongitude());
    }

    private void validateLatitude(Double value) {
        if (value != null && (!Double.isFinite(value) || value < -90.0 || value > 90.0)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void validateLongitude(Double value) {
        if (value != null && (!Double.isFinite(value) || value < -180.0 || value > 180.0)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void validateCoordinateRange(Double minValue, Double maxValue) {
        if (minValue != null && maxValue != null && minValue > maxValue) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void validateRange(Integer minValue, Integer maxValue) {
        if ((minValue != null && minValue < 0)
                || (maxValue != null && maxValue < 0)
                || (minValue != null && maxValue != null && minValue > maxValue)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void populateProvinceColumn(Sheet sheet, List<CodeNameProjection> provinces) {
        for (int i = 0; i < provinces.size(); i++) {
            CodeNameProjection province = provinces.get(i);
            ExcelTemplateUtils.setTextCellValue(
                    sheet,
                    START_ROW_INDEX + i,
                    PROVINCE_COLUMN_INDEX,
                    ExcelTemplateUtils.formatCodeAndName(province.getCode(), province.getName())
            );
        }
    }

    private void populateWardColumn(Sheet sheet, List<CodeNameProjection> wards) {
        for (int i = 0; i < wards.size(); i++) {
            CodeNameProjection ward = wards.get(i);
            ExcelTemplateUtils.setTextCellValue(
                    sheet,
                    START_ROW_INDEX + i,
                    WARD_COLUMN_INDEX,
                    ExcelTemplateUtils.formatCodeAndName(ward.getCode(), ward.getName())
            );
        }
    }

    private void validateGeoCoordinates(Double latitude, Double longitude) {
        if ((latitude == null && longitude != null) || (latitude != null && longitude == null)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void validateWorkingTime(LocalDateTime workingStartTime, LocalDateTime workingEndTime) {
        if ((workingStartTime == null) != (workingEndTime == null)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (workingStartTime != null && !workingEndTime.isAfter(workingStartTime)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private boolean isCurrentTenant(Hub hub) {
        Long currentTenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        return hub.getTenantId() != null && hub.getTenantId().equals(currentTenantId);
    }

    private void validateTenantAccess(Hub hub) {
        if (!isCurrentTenant(hub)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }
}
