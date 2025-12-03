package serp.project.logistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.logistics.constant.EntityType;
import serp.project.logistics.dto.request.AddressCreationForm;
import serp.project.logistics.dto.request.FacilityCreationForm;
import serp.project.logistics.dto.request.FacilityUpdateForm;
import serp.project.logistics.entity.FacilityEntity;
import serp.project.logistics.exception.AppErrorCode;
import serp.project.logistics.exception.AppException;
import serp.project.logistics.repository.FacilityRepository;
import serp.project.logistics.repository.specification.FacilitySpecification;
import serp.project.logistics.util.IdUtils;
import serp.project.logistics.util.PaginationUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityService {

        private final FacilityRepository facilityRepository;
        private final AddressService addressService;

        @Transactional(rollbackFor = Exception.class)
        public void createFacility(FacilityCreationForm form, Long tenantId) {
                String facilityId = IdUtils.generateFacilityId();
                FacilityEntity facility = FacilityEntity.builder()
                                .id(facilityId)
                                .name(form.getName())
                                .statusId(form.getStatusId())
                                .isDefault(true)
                                .phone(form.getPhone())
                                .postalCode(form.getPostalCode())
                                .length(form.getLength())
                                .width(form.getWidth())
                                .height(form.getHeight())
                                .tenantId(tenantId)
                                .build();
                facilityRepository.save(facility);
                log.info("[FacilityService] Generated facility {} with ID {} for tenantId {}", form.getName(),
                                facilityId,
                                tenantId);

                AddressCreationForm addressForm = new AddressCreationForm();
                addressForm.setEntityId(facility.getId());
                addressForm.setEntityType(EntityType.FACILITY.value());
                addressForm.setAddressType(form.getAddressType());
                addressForm.setLatitude(form.getLatitude());
                addressForm.setLongitude(form.getLongitude());
                addressForm.setDefault(true);
                addressForm.setFullAddress(form.getFullAddress());
                addressService.createAddress(addressForm, tenantId);
                log.info("[FacilityService] Created address {} for facility ID {} and tenantId {}",
                                addressForm.getFullAddress(),
                                facilityId, tenantId);
        }

        @Transactional(rollbackFor = Exception.class)
        public void updateFacility(String facilityId, FacilityUpdateForm form, Long tenantId) {
                FacilityEntity facility = facilityRepository.findById(facilityId).orElse(null);
                if (facility == null || !facility.getTenantId().equals(tenantId)) {
                        throw new AppException(AppErrorCode.NOT_FOUND);
                }
                facility.setName(form.getName());
                facility.setPhone(form.getPhone());
                facility.setStatusId(form.getStatusId());
                facility.setDefault(form.isDefault());
                facility.setPostalCode(form.getPostalCode());
                facility.setLength(form.getLength());
                facility.setWidth(form.getWidth());
                facility.setHeight(form.getHeight());
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
