package serp.project.sales.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.sales.dto.request.FacilityCreationForm;
import serp.project.sales.dto.request.FacilityUpdateForm;
import serp.project.sales.entity.FacilityEntity;
import serp.project.sales.exception.AppErrorCode;
import serp.project.sales.exception.AppException;
import serp.project.sales.repository.AddressRepository;
import serp.project.sales.repository.FacilityRepository;
import serp.project.sales.repository.specification.FacilitySpecification;
import serp.project.sales.util.PaginationUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityService {

        private final FacilityRepository facilityRepository;
        private final AddressRepository addressRepository;

        @Transactional(rollbackFor = Exception.class)
        public void createFacility(FacilityCreationForm form, Long tenantId) {
                FacilityEntity facility = new FacilityEntity(form, tenantId);
                facilityRepository.save(facility);
                log.info("[FacilityService] Generated facility {} with ID {} for tenantId {}", form.getName(),
                                facility.getId(),
                                tenantId);

                addressRepository.save(facility.getAddress());
                log.info("[FacilityService] Created address {} for facility ID {} and tenantId {}",
                                facility.getAddress().getFullAddress(),
                                facility.getId(), tenantId);
        }

        @Transactional(rollbackFor = Exception.class)
        public void updateFacility(String facilityId, FacilityUpdateForm form, Long tenantId) {
                FacilityEntity facility = facilityRepository.findById(facilityId).orElse(null);
                if (facility == null || !facility.getTenantId().equals(tenantId)) {
                        throw new AppException(AppErrorCode.NOT_FOUND);
                }
                facility.update(form);
                facilityRepository.save(facility);
                log.info("[FacilityService] Updated facility {} with ID {} for tenantId {}", form.getName(), facilityId,
                                tenantId);
        }

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
