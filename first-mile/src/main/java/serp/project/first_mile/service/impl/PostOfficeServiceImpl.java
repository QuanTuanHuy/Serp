/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.first_mile.caller.GeocodeCaller;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.Province;
import serp.project.first_mile.domain.Ward;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.request.CreatePostOfficeRequest;
import serp.project.first_mile.dto.request.UpdatePostOfficeRequest;
import serp.project.first_mile.dto.response.PostOfficeGeocodeBatchResponse;
import serp.project.first_mile.dto.response.PostOfficeResponse;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.mapper.PostOfficeMapper;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.ProvinceRepository;
import serp.project.first_mile.repository.WardRepository;
import serp.project.first_mile.kernel.utils.AuthUtils;
import serp.project.first_mile.service.PostOfficeService;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostOfficeServiceImpl implements PostOfficeService {
    private static final int MAX_GEO_BATCH = 200;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private final PostOfficeRepository postOfficeRepository;
    private final ProvinceRepository provinceRepository;
    private final WardRepository wardRepository;
    private final AuthUtils authUtils;
    private final GeocodeCaller geocodeCaller;

    @Override
    public PageResponse<PostOfficeResponse> getPostOffices(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<PostOffice> postOfficePage;
        if (keyword == null || keyword.isBlank()) {
            postOfficePage = postOfficeRepository.findAll(pageable);
        } else {
            postOfficePage = postOfficeRepository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(
                    keyword,
                    keyword,
                    pageable
            );
        }

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
        Long tenantId = getCurrentTenantIdOrThrow();

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
        postOffice.setTenantId(getCurrentTenantIdOrThrow());
        PostOffice updatedPostOffice = postOfficeRepository.save(postOffice);
        return PostOfficeMapper.toResponse(updatedPostOffice);
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

        Long currentTenantId = getCurrentTenantIdOrThrow();
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

    private PostOffice getPostOfficeOrThrow(Long id) {
        return postOfficeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_NOT_FOUND));
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

    private Long getCurrentTenantIdOrThrow() {
        return authUtils.getCurrentTenantId().orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
    }

    private boolean isCurrentTenant(PostOffice postOffice) {
        Long currentTenantId = getCurrentTenantIdOrThrow();
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

        GeocodeCaller.GeoPoint geoPoint = geocodeCaller.searchFirst(addressQuery).orElse(null);
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
