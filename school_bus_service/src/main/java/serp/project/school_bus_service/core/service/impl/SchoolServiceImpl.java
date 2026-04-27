package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.application.dto.params.SchoolParamsRequest;
import serp.project.school_bus_service.application.dto.request.SchoolUpsertRequest;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.SchoolResponse;
import serp.project.school_bus_service.core.service.IAuditLogService;
import serp.project.school_bus_service.core.service.ICodeGeneratorService;
import serp.project.school_bus_service.core.service.ISchoolService;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.SchoolEntity;
import serp.project.school_bus_service.infrastructure.store.repository.SchoolRepository;
import serp.project.school_bus_service.infrastructure.store.specification.BaseSpecification;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseService;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.kernel.shared.code.SchoolBusCode;
import serp.project.school_bus_service.kernel.shared.exception.AppErrorCode;
import serp.project.school_bus_service.kernel.shared.exception.AppException;
import serp.project.school_bus_service.kernel.shared.pagination.PageableUtils;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class SchoolServiceImpl extends AbstractBaseService<SchoolEntity, Long> implements ISchoolService {

    private final SchoolRepository schoolRepository;
    private final SchoolBusMapper mapper;
    private final IAuditLogService auditLogService;
    private final ICodeGeneratorService codeGeneratorService;

    @Override
    protected BaseRepository<SchoolEntity, Long> getRepository() {
        return schoolRepository;
    }

    @Override
    public PageResponse<SchoolResponse> getSchools(SchoolParamsRequest params, Long tenantId) {
        return PageResponse.from(schoolRepository.findAll(
                BaseSpecification.tenantActiveWithKeyword(tenantId,
                        params == null ? null : params.getKeyword(),
                        "name", "code", "address", "contactPhone", "contactEmail"),
                PageableUtils.from(params,
                        Set.of("id", "name", "code", "createdAt", "updatedAt"), "name")),
                mapper::toSchoolResponse);
    }

    @Override
    public SchoolResponse getSchoolResponse(Long id, Long tenantId) {
        return mapper.toSchoolResponse(getSchool(id, tenantId));
    }

    @Override
    public SchoolEntity getSchool(Long id, Long tenantId) {
        return findById(schoolRepository, id, tenantId);
    }

    @Override
    @Transactional
    public SchoolResponse createSchool(SchoolUpsertRequest request, Long tenantId, Long actorId) {
        SchoolEntity school = new SchoolEntity();
        school.markCreated(tenantId, actor(actorId));
        applySchool(school, request);
        school.setCode(codeGeneratorService.generate(
                SchoolBusCode.SCHOOL.sequenceKey(), SchoolBusCode.SCHOOL.prefix(), tenantId, actorId));
        SchoolEntity saved = schoolRepository.save(school);
        auditLogService.log(tenantId, actorId, "School", saved.getId(), "CREATE", "Created school master data");
        return mapper.toSchoolResponse(saved);
    }

    @Override
    @Transactional
    public SchoolResponse updateSchool(Long id, SchoolUpsertRequest request, Long tenantId, Long actorId) {
        SchoolEntity school = getSchool(id, tenantId);
        school.markUpdated(actor(actorId));
        applySchool(school, request);
        SchoolEntity saved = schoolRepository.save(school);
        auditLogService.log(tenantId, actorId, "School", saved.getId(), "UPDATE", "Updated school master data");
        return mapper.toSchoolResponse(saved);
    }

    @Override
    @Transactional
    public void deleteSchool(Long id, Long tenantId, Long actorId) {
        softDeleteById(schoolRepository, id, tenantId, actorId);
        auditLogService.log(tenantId, actorId, "School", id, "SOFT_DELETE", "Soft deleted school");
    }

    private void applySchool(SchoolEntity school, SchoolUpsertRequest request) {
        validateCoordinatePair(request.getLatitude(), request.getLongitude(), "school");
        school.setName(request.getName());
        school.setAddress(request.getAddress());
        school.setContactPhone(request.getContactPhone());
        school.setContactEmail(request.getContactEmail());
        school.setLatitude(request.getLatitude());
        school.setLongitude(request.getLongitude());
        school.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    private void validateCoordinatePair(Double latitude, Double longitude, String target) {
        if ((latitude == null) != (longitude == null)) {
            throw new AppException(
                    AppErrorCode.INVALID_REQUEST,
                    String.format("Both latitude and longitude are required when pinning a %s", target));
        }

        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new AppException(
                    AppErrorCode.INVALID_REQUEST,
                    String.format("Latitude for %s must be between -90 and 90", target));
        }

        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new AppException(
                    AppErrorCode.INVALID_REQUEST,
                    String.format("Longitude for %s must be between -180 and 180", target));
        }
    }
}
