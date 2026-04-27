package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.application.dto.params.ParentProfileParamsRequest;
import serp.project.school_bus_service.application.dto.request.ParentProfileUpsertRequest;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.ParentProfileResponse;
import serp.project.school_bus_service.core.service.IAuditLogService;
import serp.project.school_bus_service.core.service.IParentService;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.ParentProfileEntity;
import serp.project.school_bus_service.infrastructure.store.repository.ParentProfileRepository;
import serp.project.school_bus_service.infrastructure.store.specification.BaseSpecification;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseService;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.kernel.shared.pagination.PageableUtils;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ParentServiceImpl extends AbstractBaseService<ParentProfileEntity, Long> implements IParentService {

    private final ParentProfileRepository parentProfileRepository;
    private final SchoolBusMapper mapper;
    private final IAuditLogService auditLogService;

    @Override
    protected BaseRepository<ParentProfileEntity, Long> getRepository() {
        return parentProfileRepository;
    }

    @Override
    public PageResponse<ParentProfileResponse> getParents(ParentProfileParamsRequest params, Long tenantId) {
        return PageResponse.from(parentProfileRepository.findAll(
                BaseSpecification.tenantActiveWithKeyword(tenantId,
                        params == null ? null : params.getKeyword(),
                        "fullName", "phone", "email", "address"),
                PageableUtils.from(params,
                        Set.of("id", "fullName", "email", "createdAt", "updatedAt"), "fullName")),
                mapper::toParentProfileResponse);
    }

    @Override
    public ParentProfileResponse getParentResponse(Long id, Long tenantId) {
        return mapper.toParentProfileResponse(getParent(id, tenantId));
    }

    @Override
    public ParentProfileEntity getParent(Long id, Long tenantId) {
        return findById(parentProfileRepository, id, tenantId);
    }

    @Override
    @Transactional
    public ParentProfileResponse createParent(ParentProfileUpsertRequest request, Long tenantId, Long actorId) {
        ParentProfileEntity parent = new ParentProfileEntity();
        parent.markCreated(tenantId, actor(actorId));
        applyParent(parent, request);
        ParentProfileEntity saved = parentProfileRepository.save(parent);
        auditLogService.log(tenantId, actorId, "ParentProfile", saved.getId(), "CREATE", "Created parent profile");
        return mapper.toParentProfileResponse(saved);
    }

    @Override
    @Transactional
    public ParentProfileResponse updateParent(Long id, ParentProfileUpsertRequest request, Long tenantId, Long actorId) {
        ParentProfileEntity parent = getParent(id, tenantId);
        parent.markUpdated(actor(actorId));
        applyParent(parent, request);
        ParentProfileEntity saved = parentProfileRepository.save(parent);
        auditLogService.log(tenantId, actorId, "ParentProfile", saved.getId(), "UPDATE", "Updated parent profile");
        return mapper.toParentProfileResponse(saved);
    }

    @Override
    @Transactional
    public void deleteParent(Long id, Long tenantId, Long actorId) {
        softDeleteById(parentProfileRepository, id, tenantId, actorId);
        auditLogService.log(tenantId, actorId, "ParentProfile", id, "SOFT_DELETE", "Soft deleted parent profile");
    }

    private void applyParent(ParentProfileEntity parent, ParentProfileUpsertRequest request) {
        parent.setUserId(request.getUserId());
        parent.setFullName(request.getFullName());
        parent.setPhone(request.getPhone());
        parent.setEmail(request.getEmail());
        parent.setAddress(request.getAddress());
        parent.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }
}
