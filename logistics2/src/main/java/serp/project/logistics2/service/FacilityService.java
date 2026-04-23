package serp.project.logistics2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import serp.project.logistics2.entity.FacilityEntity;
import serp.project.logistics2.repository.FacilityRepository;
import serp.project.logistics2.repository.specification.FacilitySpecification;
import serp.project.logistics2.util.PaginationUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityService {

    private final FacilityRepository facilityRepository;

    public Page<FacilityEntity> findFacilities(
            String query,
            String statusId,
            Long tenantId,
            int page,
            int size,
            String sortBy,
            String sortDirection) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        return facilityRepository.findAll(
                FacilitySpecification.satisfy(
                        query,
                        statusId,
                        tenantId),
                pageable);
    }

    public FacilityEntity getFacility(String facilityId, Long tenantId) {
        FacilityEntity facility = facilityRepository.findById(facilityId).orElse(null);
        if (facility == null || !facility.getTenantId().equals(tenantId)) {
            log.info("[FacilityService] Facility with ID {} not found or does not belong to tenantId {}",
                    facilityId,
                    tenantId);
            return null;
        }
        log.info("[FacilityService] Retrieved facility {} with ID {} for tenantId {}", facility.getName(),
                facilityId,
                tenantId);
        return facility;
    }

}
