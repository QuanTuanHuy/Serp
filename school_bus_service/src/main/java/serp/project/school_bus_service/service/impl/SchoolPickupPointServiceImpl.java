package serp.project.school_bus_service.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.request.SchoolPickupPointUpsertRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.SchoolPickupPointResponse;
import serp.project.school_bus_service.service.IPickupPointService;
import serp.project.school_bus_service.service.ISchoolPickupPointService;
import serp.project.school_bus_service.service.ISchoolService;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.PickupPointEntity;
import serp.project.school_bus_service.entity.SchoolEntity;
import serp.project.school_bus_service.entity.SchoolPickupPointEntity;
import serp.project.school_bus_service.repository.SchoolPickupPointRepository;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;

import java.util.List;
import java.util.Optional;

@Service
public class SchoolPickupPointServiceImpl extends AbstractBaseService<SchoolPickupPointEntity, Long>
        implements ISchoolPickupPointService {

    private final SchoolPickupPointRepository repository;
    private final SchoolBusMapper mapper;
    private final ISchoolService schoolService;
    private final IPickupPointService pickupPointService;
    private final MessageCommon messageCommon;

    public SchoolPickupPointServiceImpl(
            SchoolPickupPointRepository repository,
            @Lazy SchoolBusMapper mapper,
            ISchoolService schoolService,
            IPickupPointService pickupPointService,
            MessageCommon messageCommon) {
        this.repository = repository;
        this.mapper = mapper;
        this.schoolService = schoolService;
        this.pickupPointService = pickupPointService;
        this.messageCommon = messageCommon;
    }

    @Override
    protected BaseRepository<SchoolPickupPointEntity, Long> getRepository() {
        return repository;
    }

    @Override
    public SchoolPickupPointEntity getSchoolPickupPoint(Long id, Long tenantId) {
        return findById(repository, id, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SchoolPickupPointResponse> getBySchool(Long schoolId, int page, int size, Long tenantId) {
        return PageResponse.from(
                repository.findBySchoolIdAndTenantIdAndIsDeletedFalse(
                        schoolId, tenantId, PageRequest.of(page, size, Sort.by("pickupPoint.name"))),
                mapper::toSchoolPickupPointResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolPickupPointResponse> getActiveBySchool(Long schoolId, Long tenantId) {
        return repository.findBySchoolIdAndTenantIdAndIsDeletedFalseAndIsActiveTrue(schoolId, tenantId)
                .stream().map(mapper::toSchoolPickupPointResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolPickupPointResponse> getAllActiveLinks(Long tenantId) {
        return repository.findByTenantIdAndIsDeletedFalseAndIsActiveTrue(tenantId)
                .stream().map(mapper::toSchoolPickupPointResponse).toList();
    }

    @Override
    @Transactional
    public SchoolPickupPointResponse link(Long schoolId, SchoolPickupPointUpsertRequest request,
            Long tenantId, Long actorId) {
        SchoolEntity school = schoolService.getSchool(schoolId, tenantId);
        PickupPointEntity pickupPoint = pickupPointService.getPickupPoint(request.getPickupPointId(), tenantId);

        repository.findBySchoolIdAndPickupPointIdAndTenantIdAndIsDeletedFalse(
                schoolId, request.getPickupPointId(), tenantId)
                .ifPresent(existing -> {
                    throw new AppException(AppErrorCode.SchoolPickupPoint.CONFLICT,
                            messageCommon.getMessage(AppErrorCode.SchoolPickupPoint.CONFLICT));
                });

        SchoolPickupPointEntity entity = new SchoolPickupPointEntity();
        entity.markCreated(tenantId, actor(actorId));
        entity.setSchool(school);
        entity.setPickupPoint(pickupPoint);
        applyFields(entity, request);
        SchoolPickupPointEntity saved = repository.save(entity);
        return mapper.toSchoolPickupPointResponse(saved);
    }

    @Override
    @Transactional
    public SchoolPickupPointResponse update(Long id, SchoolPickupPointUpsertRequest request,
            Long tenantId, Long actorId) {
        SchoolPickupPointEntity entity = repository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND,
                        messageCommon.getMessage("schoolPickupPoint.notFound")));
        entity.markUpdated(actor(actorId));
        applyFields(entity, request);
        SchoolPickupPointEntity saved = repository.save(entity);
        return mapper.toSchoolPickupPointResponse(saved);
    }

    @Override
    @Transactional
    public void unlink(Long schoolId, Long pickupPointId, Long tenantId, Long actorId) {
        SchoolPickupPointEntity entity = repository.findBySchoolIdAndPickupPointIdAndTenantIdAndIsDeletedFalse(
                schoolId, pickupPointId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND,
                        messageCommon.getMessage("schoolPickupPoint.linkNotFound")));
        entity.setIsDeleted(true);
        entity.setIsActive(false);
        entity.markUpdated(actor(actorId));
        repository.save(entity);
    }

    @Override
    public boolean isPickupPointLinkedToSchool(Long schoolId, Long pickupPointId, Long tenantId) {
        return repository.findBySchoolIdAndPickupPointIdAndTenantIdAndIsDeletedFalse(
                schoolId, pickupPointId, tenantId).isPresent();
    }

    @Override
    public Optional<SchoolPickupPointEntity> findLinkBySchoolAndPickupPoint(Long schoolId, Long pickupPointId, Long tenantId) {
        return repository.findBySchoolIdAndPickupPointIdAndTenantIdAndIsDeletedFalse(schoolId, pickupPointId, tenantId);
    }

    @Override
    public List<SchoolPickupPointEntity> getPickupPointLinksForSchools(List<Long> schoolIds, Long tenantId) {
        return repository.findBySchoolIdInAndTenantIdAndIsDeletedFalse(schoolIds, tenantId);
    }

    private void applyFields(SchoolPickupPointEntity entity, SchoolPickupPointUpsertRequest request) {
        if (request.getIsDefault() != null) {
            entity.setIsDefaultPoint(request.getIsDefault());
        }
        entity.setIsActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE);
    }
}
