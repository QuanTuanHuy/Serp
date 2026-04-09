package com.example.ttcrs.service;

import com.example.ttcrs.dto.response.LocationResponseDTO;
import com.example.ttcrs.repository.LocationRepository;
import com.example.ttcrs.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
