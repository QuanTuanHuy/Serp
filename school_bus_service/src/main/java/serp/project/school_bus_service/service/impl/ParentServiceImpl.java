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
import org.springframework.context.annotation.Lazy;
import serp.project.school_bus_service.service.ISchoolBusUserService;
import serp.project.school_bus_service.entity.SchoolBusUserEntity;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.Set;

@Service
public class ParentServiceImpl extends AbstractBaseService<ParentProfileEntity, Long> implements IParentService {

    private final ParentProfileRepository parentProfileRepository;
    private final SchoolBusMapper mapper;
    private final IAuditLogService auditLogService;
    private final MessageCommon messageCommon;
    private final ISchoolBusUserService schoolBusUserService;


    public ParentServiceImpl(
            ParentProfileRepository parentProfileRepository,
            SchoolBusMapper mapper,
            IAuditLogService auditLogService,
            MessageCommon messageCommon,
            @Lazy ISchoolBusUserService schoolBusUserService) {
        this.parentProfileRepository = parentProfileRepository;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
        this.messageCommon = messageCommon;
        this.schoolBusUserService = schoolBusUserService;
    }


    @Override
    protected BaseRepository<ParentProfileEntity, Long> getRepository() {
        return parentProfileRepository;
    }

    @Override
    public PageResponse<ParentProfileResponse> getParents(ParentProfileParamsRequest params, Long tenantId) {
        PageResponse<ParentProfileResponse> page = PageResponse.from(parentProfileRepository.findAll(
                BaseSpecification.tenantActiveWithKeyword(tenantId,
                        params == null ? null : params.getKeyword(),
                        "fullName", "phone", "email", "address"),
                PageableUtils.from(params,
                        Set.of("id", "fullName", "email", "createdAt", "updatedAt"), "fullName")),
                mapper::toParentProfileResponse);

        // Enrich with SchoolBusUser details
        java.util.List<Long> schoolBusUserIds = page.getItems().stream()
                .map(ParentProfileResponse::getUserId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        if (!schoolBusUserIds.isEmpty()) {
            Map<Long, SchoolBusUserEntity> userMap = schoolBusUserService.findAllByIds(schoolBusUserIds).stream()
                    .collect(Collectors.toMap(
                            SchoolBusUserEntity::getId,
                            java.util.function.Function.identity()
                    ));
            page.getItems().forEach(r -> {
                Long internalId = r.getUserId();
                r.setSchoolBusUserId(internalId);
                SchoolBusUserEntity u = userMap.get(internalId);
                if (u != null) {
                    r.setAccountUserId(u.getAccountUserId());
                    r.setUserId(u.getAccountUserId()); // Map to accountUserId for UI select dropdown compatibility
                    r.setUser(mapper.toSchoolBusUserResponse(u));
                }
            });
        }
        return page;
    }

    @Override
    public ParentProfileResponse getParentResponse(Long id, Long tenantId) {
        ParentProfileEntity entity = getParent(id, tenantId);
        ParentProfileResponse response = mapper.toParentProfileResponse(entity);
        if (response.getUserId() != null) {
            Long internalId = response.getUserId();
            response.setSchoolBusUserId(internalId);
            schoolBusUserService.findById(internalId)
                    .ifPresent(u -> {
                        response.setAccountUserId(u.getAccountUserId());
                        response.setUserId(u.getAccountUserId()); // Map to accountUserId for UI select dropdown compatibility
                        response.setUser(mapper.toSchoolBusUserResponse(u));
                    });
        }
        return response;
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
        
        // Return enriched response
        ParentProfileResponse response = mapper.toParentProfileResponse(saved);
        if (saved.getUserId() != null) {
            Long internalId = saved.getUserId();
            response.setSchoolBusUserId(internalId);
            schoolBusUserService.findById(internalId)
                    .ifPresent(u -> {
                        response.setAccountUserId(u.getAccountUserId());
                        response.setUserId(u.getAccountUserId());
                        response.setUser(mapper.toSchoolBusUserResponse(u));
                    });
        }
        return response;
    }

    @Override
    @Transactional
    public ParentProfileResponse updateParent(Long id, ParentProfileUpsertRequest request, Long tenantId, Long actorId) {
        ParentProfileEntity parent = getParent(id, tenantId);
        parent.markUpdated(actor(actorId));
        applyParent(parent, request);
        ParentProfileEntity saved = parentProfileRepository.save(parent);
        auditLogService.log(tenantId, actorId, "ParentProfile", saved.getId(), "UPDATE", "Updated parent profile");
        
        // Return enriched response
        ParentProfileResponse response = mapper.toParentProfileResponse(saved);
        if (saved.getUserId() != null) {
            Long internalId = saved.getUserId();
            response.setSchoolBusUserId(internalId);
            schoolBusUserService.findById(internalId)
                    .ifPresent(u -> {
                        response.setAccountUserId(u.getAccountUserId());
                        response.setUserId(u.getAccountUserId());
                        response.setUser(mapper.toSchoolBusUserResponse(u));
                    });
        }
        return response;
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
        if (request.getAccountUserId() != null) {
            SchoolBusUserEntity schoolBusUser = schoolBusUserService.getRequiredByAccountUserId(request.getAccountUserId());
            parent.setUserId(schoolBusUser.getId());
        } else {
            parent.setUserId(null);
        }
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

    @Override
    @Transactional
    public void syncProfile(SchoolBusUserEntity user, boolean hasRole) {
        Optional<ParentProfileEntity> existingOpt = parentProfileRepository.findByTenantIdAndUserIdAndIsDeletedFalse(user.getTenantId(), user.getId());
        if (hasRole) {
            ParentProfileEntity profile;
            if (existingOpt.isPresent()) {
                profile = existingOpt.get();
            } else {
                profile = new ParentProfileEntity();
                profile.setTenantId(user.getTenantId());
                profile.setUserId(user.getId());
                profile.setIsDeleted(false);
                profile.markCreated(user.getTenantId(), "SYSTEM");
            }
            profile.setFullName(user.getFullName());
            profile.setPhone(user.getPhoneNumber());
            profile.setEmail(user.getEmail());
            profile.setIsActive(true);
            profile.markUpdated("SYSTEM");
            parentProfileRepository.save(profile);
        } else {
            if (existingOpt.isPresent()) {
                ParentProfileEntity profile = existingOpt.get();
                profile.setIsActive(false);
                profile.markUpdated("SYSTEM");
                parentProfileRepository.save(profile);
            }
        }
    }
}
