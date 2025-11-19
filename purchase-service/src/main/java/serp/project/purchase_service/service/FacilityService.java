package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import serp.project.purchase_service.constant.EntityType;
import serp.project.purchase_service.constant.FacilityStatus;
import serp.project.purchase_service.dto.request.AddressCreationForm;
import serp.project.purchase_service.dto.request.FacilityCreationForm;
import serp.project.purchase_service.dto.request.FacilityUpdateForm;
import serp.project.purchase_service.entity.AddressEntity;
import serp.project.purchase_service.entity.FacilityEntity;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;
import serp.project.purchase_service.repository.FacilityRepository;
import serp.project.purchase_service.repository.specification.FacilitySpecification;
import serp.project.purchase_service.util.IdUtils;
import serp.project.purchase_service.util.PaginationUtils;

@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final AddressService addressService;

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
                .weight(form.getWeight())
                .height(form.getHeight())
                .tenantId(tenantId)
                .build();
        facilityRepository.save(facility);

        AddressCreationForm addressForm = new AddressCreationForm();
        addressForm.setEntityId(facility.getId());
        addressForm.setEntityType(EntityType.FACILITY.value());
        addressForm.setAddressType(form.getAddressType());
        addressForm.setLatitude(form.getLatitude());
        addressForm.setLongitude(form.getLongitude());
        addressForm.setDefault(true);
        addressForm.setFullAddress(form.getFullAddress());
        addressService.createAddress(addressForm, tenantId);
    }

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
        facility.setWeight(form.getWeight());
        facility.setHeight(form.getHeight());
        facilityRepository.save(facility);
    }

    public Page<FacilityEntity> findFacilities(
            String query,
            String statusId,
            Long tenantId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        return facilityRepository.findAll(
                FacilitySpecification.satisfy(
                        query,
                        statusId,
                        tenantId
                ), pageable
        );
    }

    public FacilityEntity getFacility(String facilityId, Long tenantId) {
        FacilityEntity facility = facilityRepository.findById(facilityId).orElse(null);
        if (facility == null || !facility.getTenantId().equals(tenantId)) {
            return null;
        }
        return facility;
    }

}
