package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.AttendantProfileParamsRequest;
import serp.project.school_bus_service.dto.request.BusAttendantProfileUpsertRequest;
import serp.project.school_bus_service.dto.response.AttendantProfileResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.service.IAttendantService;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.BusAttendantProfileEntity;
import serp.project.school_bus_service.repository.BusAttendantProfileRepository;
import serp.project.school_bus_service.shared.base.specification.BaseSpecification;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import serp.project.school_bus_service.shared.pagination.PageableUtils;

import java.util.Set;

@Service
public class AttendantServiceImpl extends AbstractBaseService<BusAttendantProfileEntity, Long> implements IAttendantService {

    private static final Set<String> VALID_CREW_STATUSES = Set.of(
            "AVAILABLE", "ASSIGNED", "INACTIVE", "ON_LEAVE");

    private final BusAttendantProfileRepository busAttendantProfileRepository;
    private final SchoolBusMapper mapper;
    private final IAuditLogService auditLogService;
    private final MessageCommon messageCommon;


    public AttendantServiceImpl(
    BusAttendantProfileRepository busAttendantProfileRepository,
                                 SchoolBusMapper mapper,
                                 IAuditLogService auditLogService,
                                 MessageCommon messageCommon) {
        this.busAttendantProfileRepository = busAttendantProfileRepository;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
        this.messageCommon = messageCommon;
    }


    @Override
    protected BaseRepository<BusAttendantProfileEntity, Long> getRepository() {
        return busAttendantProfileRepository;
    }

    @Override
    public PageResponse<AttendantProfileResponse> getAttendants(AttendantProfileParamsRequest params, Long tenantId) {
        return PageResponse.from(busAttendantProfileRepository.findAll(
                BaseSpecification.tenantActiveWithKeyword(tenantId,
                        params == null ? null : params.getKeyword(),
                        "fullName", "phone", "status"),
                PageableUtils.from(params,
                        Set.of("id", "fullName", "status", "createdAt", "updatedAt"), "fullName")),
                mapper::toAttendantProfileResponse);
    }

    @Override
    public AttendantProfileResponse getAttendantResponse(Long id, Long tenantId) {
        return mapper.toAttendantProfileResponse(getAttendant(id, tenantId));
    }

    @Override
    public BusAttendantProfileEntity getAttendant(Long id, Long tenantId) {
        return findById(busAttendantProfileRepository, id, tenantId);
    }

    @Override
    @Transactional
    public AttendantProfileResponse createAttendant(BusAttendantProfileUpsertRequest request, Long tenantId, Long actorId) {
        BusAttendantProfileEntity attendant = new BusAttendantProfileEntity();
        attendant.markCreated(tenantId, actor(actorId));
        applyAttendant(attendant, request);
        BusAttendantProfileEntity saved = busAttendantProfileRepository.save(attendant);
        auditLogService.log(tenantId, actorId, "AttendantProfile", saved.getId(), "CREATE", "Created attendant profile");
        return mapper.toAttendantProfileResponse(saved);
    }

    @Override
    @Transactional
    public AttendantProfileResponse updateAttendant(Long id, BusAttendantProfileUpsertRequest request, Long tenantId, Long actorId) {
        BusAttendantProfileEntity attendant = getAttendant(id, tenantId);
        attendant.markUpdated(actor(actorId));
        applyAttendant(attendant, request);
        BusAttendantProfileEntity saved = busAttendantProfileRepository.save(attendant);
        auditLogService.log(tenantId, actorId, "AttendantProfile", saved.getId(), "UPDATE", "Updated attendant profile");
        return mapper.toAttendantProfileResponse(saved);
    }

    @Override
    @Transactional
    public void deleteAttendant(Long id, Long tenantId, Long actorId) {
        softDeleteById(busAttendantProfileRepository, id, tenantId, actorId);
        auditLogService.log(tenantId, actorId, "AttendantProfile", id, "SOFT_DELETE", "Soft deleted attendant profile");
    }

    private void applyAttendant(BusAttendantProfileEntity attendant, BusAttendantProfileUpsertRequest request) {
        if (request.getStatus() != null && !VALID_CREW_STATUSES.contains(request.getStatus().toUpperCase())) {
            throw new AppException(AppErrorCode.Attendant.INVALID_STATUS,
                    messageCommon.getMessage(AppErrorCode.Attendant.INVALID_STATUS, request.getStatus(), VALID_CREW_STATUSES));
        }
        attendant.setUserId(request.getUserId());
        attendant.setFullName(request.getFullName());
        attendant.setPhone(request.getPhone());
        attendant.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "AVAILABLE");
        attendant.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }
}
