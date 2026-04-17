package com.example.ttcrs.service;

import com.example.ttcrs.dto.request.location.CreateLocationDTO;
import com.example.ttcrs.dto.request.location.UpdateLocationDTO;
import com.example.ttcrs.dto.response.LocationResponseDTO;
import com.example.ttcrs.entity.LocationEntity;
import com.example.ttcrs.repository.LocationRepository;
import com.example.ttcrs.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service xử lý business logic cho Location.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final AuthUtils authUtils;

    /**
     * Lấy tất cả location thuộc tenant của user hiện tại.
     *
     * @return danh sách {@link LocationResponseDTO}
     */
    public List<LocationResponseDTO> getLocationsForCurrentTenant() {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException(
                        "Không thể xác định tenant từ token. Vui lòng kiểm tra lại JWT."
                ));

        log.debug("Fetching locations for tenantId={}", tenantId);

        return locationRepository.findAllByTenantId(tenantId)
                .stream()
                .map(LocationResponseDTO::fromEntity)
                .toList();
    }

    /**
     * Tạo mới một Location cho tenant hiện tại.
     *
     * <p>Luồng xử lý:
     * <ol>
     *   <li>Lấy tenantId từ JWT.</li>
     *   <li>Kiểm tra locationCode chưa tồn tại.</li>
     *   <li>Lưu entity mới vào DB.</li>
     * </ol>
     *
     * @param dto tham số tạo location từ dispatcher
     * @return {@link LocationResponseDTO} vừa tạo
     * @throws IllegalArgumentException nếu locationCode đã tồn tại
     */
    @Transactional
    public LocationResponseDTO createLocation(CreateLocationDTO dto) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException(
                        "Không thể xác định tenant từ token. Vui lòng kiểm tra lại JWT."
                ));

        log.info("Creating location code={}, type={}, lat={}, lng={}, tenantId={}",
                dto.getLocationCode(), dto.getType(), dto.getLat(), dto.getLng(), tenantId);

        if (locationRepository.existsByLocationCode(dto.getLocationCode())) {
            throw new IllegalArgumentException(
                    "locationCode '" + dto.getLocationCode() + "' đã tồn tại."
            );
        }

        LocationEntity entity = LocationEntity.builder()
                .tenantId(tenantId)
                .locationCode(dto.getLocationCode())
                .type(dto.getType())
                .lat(dto.getLat())
                .lng(dto.getLng())
                .build();

        LocationEntity saved = locationRepository.save(entity);
        return LocationResponseDTO.fromEntity(saved);
    }

    /**
     * Cập nhật một Location theo ID.
     * Chỉ cho phép cập nhật type, lat, lng (không đổi locationCode vì là key tham chiếu).
     *
     * @param id  ID của location cần cập nhật
     * @param dto các trường cần thay đổi (null = giữ nguyên)
     * @return {@link LocationResponseDTO} sau khi cập nhật
     * @throws IllegalArgumentException nếu không tìm thấy hoặc không thuộc tenant hiện tại
     */
    @Transactional
    public LocationResponseDTO updateLocation(Long id, UpdateLocationDTO dto) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException(
                        "Không thể xác định tenant từ token. Vui lòng kiểm tra lại JWT."
                ));

        LocationEntity entity = locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + id));

        if (!entity.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Location not found: " + id);
        }

        log.info("Updating location id={}, tenantId={}", id, tenantId);

        if (dto.getType() != null)  entity.setType(dto.getType());
        if (dto.getLat()  != null)  entity.setLat(dto.getLat());
        if (dto.getLng()  != null)  entity.setLng(dto.getLng());

        return LocationResponseDTO.fromEntity(locationRepository.save(entity));
    }

    /**
     * Xoá mềm một Location theo ID (đặt isDeleted = true).
     *
     * @param id ID của location cần xoá
     * @throws IllegalArgumentException nếu không tìm thấy hoặc không thuộc tenant hiện tại
     */
    @Transactional
    public void deleteLocation(Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException(
                        "Không thể xác định tenant từ token. Vui lòng kiểm tra lại JWT."
                ));

        LocationEntity entity = locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + id));

        if (!entity.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Location not found: " + id);
        }

        log.info("Soft-deleting location id={}, tenantId={}", id, tenantId);
        entity.setIsDeleted(true);
        locationRepository.save(entity);
    }
}
