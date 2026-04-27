/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.caller.GeocodeCaller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import serp.project.first_mile.caller.dto.GeoPoint;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.Province;
import serp.project.first_mile.domain.Ward;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.request.FileUploadRequest;
import serp.project.first_mile.dto.request.CreatePostOfficeRequest;
import serp.project.first_mile.dto.request.PostOfficeFilterRequest;
import serp.project.first_mile.dto.request.PostOfficeImportDTO;
import serp.project.first_mile.dto.request.UpdatePostOfficeRequest;
import serp.project.first_mile.dto.response.FileUploadResponse;
import serp.project.first_mile.dto.response.ImportHistoryResponse;
import serp.project.first_mile.dto.response.PostOfficeGeocodeBatchResponse;
import serp.project.first_mile.dto.response.PostOfficeResponse;
import serp.project.first_mile.dto.response.ValidateImportFileDTO;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.mapper.PostOfficeMapper;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.ProvinceRepository;
import serp.project.first_mile.repository.WardRepository;
import serp.project.first_mile.repository.projection.CodeNameProjection;
import serp.project.first_mile.repository.specification.PostOfficeSpecification;
import serp.project.first_mile.kernel.utils.ExcelTemplateUtils;
import serp.project.first_mile.kernel.utils.FirstMileAccessUtils;
import serp.project.first_mile.kernel.utils.ImageContentTypeUtils;
import serp.project.first_mile.service.FileStorageService;
import serp.project.first_mile.service.PostOfficeImportExcelService;
import serp.project.first_mile.service.PostOfficeService;

import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostOfficeServiceImpl implements PostOfficeService {
    private static final int MAX_GEO_BATCH = 200;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);
    private static final String TEMPLATE_PATH = "excel/post_office_template.xlsx";
    private static final String UNIT_SHEET_NAME = "Unit";
    private static final int START_ROW_INDEX = 1;
    private static final int PROVINCE_COLUMN_INDEX = 0;
    private static final int WARD_COLUMN_INDEX = 1;
    private static final String STORAGE_SERVICE_NAME = "first-mile";
    private static final String POST_OFFICE_IMAGE_FOLDER = "post-office-image";

    private final PostOfficeRepository postOfficeRepository;
    private final ProvinceRepository provinceRepository;
    private final WardRepository wardRepository;
    private final FirstMileAccessUtils firstMileAccessUtils;
    private final GeocodeCaller geocodeCaller;
    private final FileStorageService fileStorageService;
    private final PostOfficeImportExcelService postOfficeImportExcelService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostOfficeResponse> getPostOffices(int page, int size, PostOfficeFilterRequest filterRequest) {
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        PostOfficeFilterRequest normalizedFilterRequest = normalizeFilterRequest(filterRequest);
        validateFilterRanges(normalizedFilterRequest);

        Page<PostOffice> postOfficePage = postOfficeRepository.findAll(
                PostOfficeSpecification.byFilter(tenantId, normalizedFilterRequest),
                pageable
        );

        Page<PostOfficeResponse> mappedPage = postOfficePage.map(PostOfficeMapper::toResponse);

        return PageResponse.<PostOfficeResponse>builder()
                .items(mappedPage.getContent())
                .page(mappedPage.getNumber())
                .size(mappedPage.getSize())
                .totalElements(mappedPage.getTotalElements())
                .totalPages(mappedPage.getTotalPages())
                .hasNext(mappedPage.hasNext())
                .hasPrevious(mappedPage.hasPrevious())
                .build();
    }

    private PostOfficeFilterRequest normalizeFilterRequest(PostOfficeFilterRequest filterRequest) {
        if (filterRequest == null) {
            return PostOfficeFilterRequest.builder().build();
        }

        return PostOfficeFilterRequest.builder()
                .keyword(normalizeText(filterRequest.getKeyword()))
                .code(normalizeText(filterRequest.getCode()))
                .name(normalizeText(filterRequest.getName()))
                .provinceCode(normalizeText(filterRequest.getProvinceCode()))
                .wardCode(normalizeText(filterRequest.getWardCode()))
                .status(filterRequest.getStatus())
                .hasLocation(filterRequest.getHasLocation())
                .minServiceRadiusM(filterRequest.getMinServiceRadiusM())
                .maxServiceRadiusM(filterRequest.getMaxServiceRadiusM())
                .minDailyCapacity(filterRequest.getMinDailyCapacity())
                .maxDailyCapacity(filterRequest.getMaxDailyCapacity())
                .minCurrentLoad(filterRequest.getMinCurrentLoad())
                .maxCurrentLoad(filterRequest.getMaxCurrentLoad())
                .minPriority(filterRequest.getMinPriority())
                .maxPriority(filterRequest.getMaxPriority())
                .build();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private void validateFilterRanges(PostOfficeFilterRequest filterRequest) {
        validateRange(filterRequest.getMinServiceRadiusM(), filterRequest.getMaxServiceRadiusM());
        validateRange(filterRequest.getMinDailyCapacity(), filterRequest.getMaxDailyCapacity());
        validateRange(filterRequest.getMinCurrentLoad(), filterRequest.getMaxCurrentLoad());
        validateRange(filterRequest.getMinPriority(), filterRequest.getMaxPriority());
    }

    private void validateRange(Integer minValue, Integer maxValue) {
        if ((minValue != null && minValue < 0)
                || (maxValue != null && maxValue < 0)
                || (minValue != null && maxValue != null && minValue > maxValue)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    @Override
    public PostOfficeResponse getPostOfficeById(Long id) {
        PostOffice postOffice = getPostOfficeOrThrow(id);
        return PostOfficeMapper.toResponse(postOffice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostOfficeResponse createPostOffice(CreatePostOfficeRequest request) {
        validateGeoCoordinates(request.getLatitude(), request.getLongitude());
        validateAddress(request.getProvinceCode(), request.getWardCode());
        validateOperationalTimeline(
            request.getOperationalStartDate(),
            request.getOperationalEndDate(),
            request.getWorkingStartTime(),
            request.getWorkingEndTime());
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();

        if (postOfficeRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.POST_OFFICE_CODE_EXISTED);
        }

        PostOffice postOffice = PostOfficeMapper.toEntity(request);
        postOffice.setTenantId(tenantId);
        PostOffice savedPostOffice = postOfficeRepository.save(postOffice);
        return PostOfficeMapper.toResponse(savedPostOffice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostOfficeResponse updatePostOffice(Long id, UpdatePostOfficeRequest request) {
        validateGeoCoordinates(request.getLatitude(), request.getLongitude());
        validateAddress(request.getProvinceCode(), request.getWardCode());
        validateOperationalTimeline(
            request.getOperationalStartDate(),
            request.getOperationalEndDate(),
            request.getWorkingStartTime(),
            request.getWorkingEndTime());

        PostOffice postOffice = getPostOfficeOrThrow(id);
        validateTenantAccess(postOffice);

        if (!postOffice.getCode().equals(request.getCode()) && postOfficeRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.POST_OFFICE_CODE_EXISTED);
        }

        PostOfficeMapper.mapForUpdate(request, postOffice);
        postOffice.setTenantId(firstMileAccessUtils.getCurrentTenantIdOrThrow());
        PostOffice updatedPostOffice = postOfficeRepository.save(postOffice);
        return PostOfficeMapper.toResponse(updatedPostOffice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostOfficeResponse uploadImage(Long id, MultipartFile file) {
        PostOffice postOffice = getPostOfficeOrThrow(id);
        validateTenantAccess(postOffice);

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
                    .folder(POST_OFFICE_IMAGE_FOLDER)
                    .tenantId(postOffice.getTenantId())
                    .uploaderId(firstMileAccessUtils.getCurrentUserIdOrNull())
                    .publicFile(true)
                    .build());

            postOffice.setImageUrl(uploadResponse.getUrl());
            PostOffice updatedPostOffice = postOfficeRepository.save(postOffice);
            return PostOfficeMapper.toResponse(updatedPostOffice);
        } catch (IOException exception) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePostOffice(Long id) {
        PostOffice postOffice = getPostOfficeOrThrow(id);
        validateTenantAccess(postOffice);
        postOfficeRepository.delete(postOffice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostOfficeResponse updatePostOfficeLocationByGeocode(Long id) {
        PostOffice postOffice = getPostOfficeOrThrow(id);
        validateTenantAccess(postOffice);

        GeocodeUpdateResult result = updateLocationByGeocode(postOffice);
        if (result.updated()) {
            PostOffice updatedPostOffice = postOfficeRepository.save(postOffice);
            log.info(
                    "Geocode single update success: postOfficeId={}, code={}, lat={}, lon={}",
                    postOffice.getId(),
                    postOffice.getCode(),
                    result.latitude(),
                    result.longitude()
            );
            return PostOfficeMapper.toResponse(updatedPostOffice);
        }

        log.warn(
                "Geocode single update skipped: postOfficeId={}, code={}, reason={}, addressQuery={}",
                postOffice.getId(),
                postOffice.getCode(),
                result.reason(),
                result.addressQuery()
        );
        return PostOfficeMapper.toResponse(postOffice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostOfficeGeocodeBatchResponse updatePostOfficesWithNullLocationByGeocode(int batch) {
        if (batch <= 0 || batch > MAX_GEO_BATCH) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Long currentTenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
        Pageable pageable = PageRequest.of(0, batch, Sort.by(Sort.Direction.ASC, "id"));
        List<PostOffice> postOffices = postOfficeRepository.findByLocationIsNull(pageable).getContent();
        log.info(
                "Geocode batch started: requestedBatch={}, fetchedRows={}, currentTenantId={}",
                batch,
                postOffices.size(),
                currentTenantId
        );

        int updated = 0;
        int skippedOtherTenant = 0;
        int skippedNoResult = 0;
        int skippedInvalidCoordinates = 0;
        for (PostOffice postOffice : postOffices) {
            if (!isCurrentTenant(postOffice)) {
                skippedOtherTenant++;
                log.debug(
                        "Geocode batch skip: postOfficeId={}, code={}, reason=TENANT_MISMATCH, currentTenantId={}, postOfficeTenantId={}",
                        postOffice.getId(),
                        postOffice.getCode(),
                        currentTenantId,
                        postOffice.getTenantId()
                );
                continue;
            }

            GeocodeUpdateResult result = updateLocationByGeocode(postOffice);
            if (result.updated()) {
                postOfficeRepository.save(postOffice);
                updated++;
                log.info(
                        "Geocode batch updated: postOfficeId={}, code={}, lat={}, lon={}",
                        postOffice.getId(),
                        postOffice.getCode(),
                        result.latitude(),
                        result.longitude()
                );
                continue;
            }

            if (GeocodeSkipReason.NO_GEOCODE_RESULT.equals(result.reason())) {
                skippedNoResult++;
            } else if (GeocodeSkipReason.INVALID_COORDINATES.equals(result.reason())) {
                skippedInvalidCoordinates++;
            }

            log.warn(
                    "Geocode batch skip: postOfficeId={}, code={}, reason={}, addressQuery={}",
                    postOffice.getId(),
                    postOffice.getCode(),
                    result.reason(),
                    result.addressQuery()
            );
        }

        int skipped = postOffices.size() - updated;
        log.info(
                "Geocode batch finished: requestedBatch={}, fetchedRows={}, updated={}, skipped={}, skippedOtherTenant={}, skippedNoResult={}, skippedInvalidCoordinates={}",
                batch,
                postOffices.size(),
                updated,
                skipped,
                skippedOtherTenant,
                skippedNoResult,
                skippedInvalidCoordinates
        );

        return new PostOfficeGeocodeBatchResponse(batch, postOffices.size(), updated, skipped);
    }

    @Override
    public byte[] exportTemplate() {
        firstMileAccessUtils.getCurrentTenantIdOrThrow();

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
    public ValidateImportFileDTO<PostOfficeImportDTO> validateImportFile(MultipartFile file, Long tenantId) {
        return postOfficeImportExcelService.validateImportFile(file, tenantId);
    }

    @Override
    public ImportHistoryResponse importPostOfficesAsync(MultipartFile file, Long tenantId) {
        return postOfficeImportExcelService.importPostOfficesAsync(file, tenantId);
    }

    private PostOffice getPostOfficeOrThrow(Long id) {
        return postOfficeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_NOT_FOUND));
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

    private void validateAddress(String provinceCode, String wardCode) {
        provinceRepository.findByProvinceCode(provinceCode)
                .orElseThrow(() -> new AppException(ErrorCode.PROVINCE_NOT_FOUND));

        Ward ward = wardRepository.findByWardCode(wardCode)
                .orElseThrow(() -> new AppException(ErrorCode.WARD_NOT_FOUND));

        if (!provinceCode.equals(ward.getProvinceCode())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void validateGeoCoordinates(Double latitude, Double longitude) {
        if ((latitude == null && longitude != null) || (latitude != null && longitude == null)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void validateOperationalTimeline(
            LocalDate operationalStartDate,
            LocalDate operationalEndDate,
            LocalTime workingStartTime,
            LocalTime workingEndTime) {
        if (operationalStartDate != null && operationalEndDate != null
                && operationalEndDate.isBefore(operationalStartDate)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if ((workingStartTime == null) != (workingEndTime == null)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (workingStartTime != null && !workingEndTime.isAfter(workingStartTime)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private boolean isCurrentTenant(PostOffice postOffice) {
        Long currentTenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
        return postOffice.getTenantId() != null && postOffice.getTenantId().equals(currentTenantId);
    }

    private void validateTenantAccess(PostOffice postOffice) {
        if (!isCurrentTenant(postOffice)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private GeocodeUpdateResult updateLocationByGeocode(PostOffice postOffice) {
        String addressQuery = buildAddressQuery(postOffice);
        log.info(
                "Geocode lookup: postOfficeId={}, code={}, addressQuery={}",
                postOffice.getId(),
                postOffice.getCode(),
                addressQuery
        );

        GeoPoint geoPoint = geocodeCaller.searchFirst(addressQuery).orElse(null);
        if (geoPoint == null) {
            return new GeocodeUpdateResult(false, GeocodeSkipReason.NO_GEOCODE_RESULT, null, null, addressQuery);
        }

        Double latitude = geoPoint.latitude();
        Double longitude = geoPoint.longitude();
        if (latitude == null || longitude == null) {
            return new GeocodeUpdateResult(false, GeocodeSkipReason.INVALID_COORDINATES, latitude, longitude, addressQuery);
        }

        postOffice.setLocation(GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude)));
        return new GeocodeUpdateResult(true, GeocodeSkipReason.NONE, latitude, longitude, addressQuery);
    }

    private String buildAddressQuery(PostOffice postOffice) {
        String wardName = wardRepository.findByWardCode(postOffice.getWardCode())
                .map(Ward::getName)
                .orElse(postOffice.getWardCode());

        String provinceName = provinceRepository.findByProvinceCode(postOffice.getProvinceCode())
                .map(Province::getName)
                .orElse(postOffice.getProvinceCode());

        return String.format("%s, %s, %s", postOffice.getAddressDetail(), wardName, provinceName);
    }

    private record GeocodeUpdateResult(
            boolean updated,
            String reason,
            Double latitude,
            Double longitude,
            String addressQuery
    ) {
    }

    private static final class GeocodeSkipReason {
        private static final String NONE = "NONE";
        private static final String NO_GEOCODE_RESULT = "NO_GEOCODE_RESULT";
        private static final String INVALID_COORDINATES = "INVALID_COORDINATES";

        private GeocodeSkipReason() {
        }
    }
}
