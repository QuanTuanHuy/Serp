package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.ParentProfileParamsRequest;
import serp.project.school_bus_service.dto.request.ParentProfileUpsertRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.ParentProfileResponse;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.service.IParentService;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.ParentProfileEntity;
import serp.project.school_bus_service.repository.ParentProfileRepository;
import serp.project.school_bus_service.shared.base.specification.BaseSpecification;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import serp.project.school_bus_service.shared.pagination.PageableUtils;

import java.util.Set;

@Service
public class ParentServiceImpl extends AbstractBaseService<ParentProfileEntity, Long> implements IParentService {

    private final ParentProfileRepository parentProfileRepository;
    private final SchoolBusMapper mapper;
    private final IAuditLogService auditLogService;
    private final MessageCommon messageCommon;


    public ParentServiceImpl(
    ParentProfileRepository parentProfileRepository,
                                 SchoolBusMapper mapper,
                                 IAuditLogService auditLogService,
                                 MessageCommon messageCommon) {
        this.parentProfileRepository = parentProfileRepository;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
        this.messageCommon = messageCommon;
    }


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
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new AppException(AppErrorCode.Parent.PHONE_REQUIRED, messageCommon.getMessage(AppErrorCode.Parent.PHONE_REQUIRED));
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new AppException(AppErrorCode.Parent.EMAIL_INVALID, messageCommon.getMessage(AppErrorCode.Parent.EMAIL_INVALID));
        }
        parent.setUserId(request.getUserId());
        parent.setFullName(request.getFullName());
        parent.setPhone(request.getPhone());
        parent.setEmail(request.getEmail());
        parent.setAddress(request.getAddress());
        parent.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    @Override
    public long countByTenant(Long tenantId) {
        return parentProfileRepository.countByTenantIdAndIsDeletedFalse(tenantId);
    }
}
