package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.application.dto.params.StudentSubscriptionParamsRequest;
import serp.project.school_bus_service.application.dto.request.BaseParamsRequest;
import serp.project.school_bus_service.application.dto.request.StudentSubscriptionUpsertRequest;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.StudentSubscriptionResponse;
import serp.project.school_bus_service.core.service.ICodeGeneratorService;
import serp.project.school_bus_service.core.service.IMasterDataService;
import serp.project.school_bus_service.core.service.IStudentSubscriptionService;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.SubscriptionStatus;
import serp.project.school_bus_service.enums.TripOption;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.PickupPointEntity;
import serp.project.school_bus_service.infrastructure.store.model.RequestStudentEntity;
import serp.project.school_bus_service.infrastructure.store.model.StudentEntity;
import serp.project.school_bus_service.infrastructure.store.model.StudentSubscriptionEntity;
import serp.project.school_bus_service.infrastructure.store.model.TransportRequestEntity;
import serp.project.school_bus_service.infrastructure.store.repository.StudentSubscriptionRepository;
import serp.project.school_bus_service.infrastructure.store.specification.BaseSpecification;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseService;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.kernel.shared.code.SchoolBusCode;
import serp.project.school_bus_service.kernel.shared.exception.AppErrorCode;
import serp.project.school_bus_service.kernel.shared.exception.AppException;
import serp.project.school_bus_service.kernel.shared.pagination.PageableUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StudentSubscriptionServiceImpl extends AbstractBaseService<StudentSubscriptionEntity, Long>
        implements IStudentSubscriptionService {

    private final StudentSubscriptionRepository subscriptionRepository;
    private final IMasterDataService masterDataService;
    private final ICodeGeneratorService codeGeneratorService;
    private final SchoolBusMapper mapper;

    @Override
    protected BaseRepository<StudentSubscriptionEntity, Long> getRepository() {
        return subscriptionRepository;
    }

    @Override
    public PageResponse<StudentSubscriptionResponse> getSubscriptions(StudentSubscriptionParamsRequest params,
            Long tenantId) {
        Specification<StudentSubscriptionEntity> spec = spec(tenantId, params == null ? null : params.getKeyword(),
                "subscriptionCode", "student.fullName", "school.name", "status", "tripOption");
        if (params != null && params.getSchoolId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("school").get("id"), params.getSchoolId()));
        }
        if (params != null && params.getStudentId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("student").get("id"), params.getStudentId()));
        }
        if (params != null && params.getStatus() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), parseStatus(params.getStatus())));
        }
        if (params != null && params.getTripOption() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tripOption"), parseTripOption(params.getTripOption())));
        }
        return PageResponse.from(subscriptionRepository.findAll(
                spec,
                pageable(params, Set.of("id", "subscriptionCode", "effectiveFrom", "effectiveTo", "status",
                        "createdAt", "updatedAt"), "createdAt")),
                mapper::toStudentSubscriptionResponse);
    }

    @Override
    public StudentSubscriptionResponse getSubscription(Long id, Long tenantId) {
        return mapper.toStudentSubscriptionResponse(findById(id, tenantId));
    }

    @Override
    public StudentSubscriptionEntity getSubscriptionEntity(Long id, Long tenantId) {
        return findById(id, tenantId);
    }

    @Override
    @Transactional
    public StudentSubscriptionResponse createSubscription(StudentSubscriptionUpsertRequest request, Long tenantId,
            Long actorId) {
        StudentSubscriptionEntity entity = new StudentSubscriptionEntity();
        entity.markCreated(tenantId, actor(actorId));
        entity.setSubscriptionCode(codeGeneratorService.generate(SchoolBusCode.SUBSCRIPTION.sequenceKey(),
                SchoolBusCode.SUBSCRIPTION.prefix(), tenantId, actorId));
        apply(entity, request, tenantId);
        StudentSubscriptionEntity saved = subscriptionRepository.save(entity);
        return mapper.toStudentSubscriptionResponse(saved);
    }

    @Override
    @Transactional
    public StudentSubscriptionResponse updateSubscription(Long id, StudentSubscriptionUpsertRequest request,
            Long tenantId, Long actorId) {
        StudentSubscriptionEntity entity = findById(id, tenantId);
        entity.markUpdated(actor(actorId));
        apply(entity, request, tenantId);
        return mapper.toStudentSubscriptionResponse(subscriptionRepository.save(entity));
    }

    @Override
    @Transactional
    public StudentSubscriptionResponse activateSubscription(Long id, Long tenantId, Long actorId) {
        return transition(id, tenantId, actorId, SubscriptionStatus.ACTIVE);
    }

    @Override
    @Transactional
    public StudentSubscriptionResponse pauseSubscription(Long id, Long tenantId, Long actorId) {
        return transition(id, tenantId, actorId, SubscriptionStatus.PAUSED);
    }

    @Override
    @Transactional
    public StudentSubscriptionResponse stopSubscription(Long id, Long tenantId, Long actorId) {
        return transition(id, tenantId, actorId, SubscriptionStatus.STOPPED);
    }

    @Override
    public List<StudentSubscriptionEntity> findEligibleSubscriptions(Long schoolId, RouteDirection direction,
            LocalDate serviceDate, Long tenantId) {
        return subscriptionRepository
                .findBySchoolIdAndTenantIdAndStatusAndIsDeletedFalse(schoolId, tenantId, SubscriptionStatus.ACTIVE)
                .stream()
                .filter(item -> !serviceDate.isBefore(item.getEffectiveFrom()))
                .filter(item -> item.getEffectiveTo() == null || !serviceDate.isAfter(item.getEffectiveTo()))
                .filter(item -> servesDate(item, serviceDate))
                .filter(item -> servesDirection(item, direction))
                .toList();
    }

    @Override
    @Transactional
    public StudentSubscriptionEntity createFromApprovedRequest(TransportRequestEntity request,
            RequestStudentEntity requestStudent, TripOption tripOption, Long tenantId, Long actorId) {
        StudentSubscriptionEntity entity = new StudentSubscriptionEntity();
        entity.markCreated(tenantId, actor(actorId));
        entity.setSubscriptionCode(codeGeneratorService.generate(SchoolBusCode.SUBSCRIPTION.sequenceKey(),
                SchoolBusCode.SUBSCRIPTION.prefix(), tenantId, actorId));

        StudentEntity student = requestStudent.getStudent();
        PickupPointEntity managedPoint = requestStudent.getPickupPoint() == null
                ? student.getPickupPoint()
                : requestStudent.getPickupPoint();

        if (subscriptionRepository.overlapsActiveSubscription(student.getId(), tripOption,
                request.getEffectiveFrom(), request.getEffectiveTo(), tenantId, null)) {
            throw new AppException(AppErrorCode.CONFLICT,
                    "Student already has an active overlapping subscription");
        }

        entity.setStudent(student);
        entity.setSchool(request.getSchool());
        entity.setPickupPoint(tripOption == TripOption.AFTERNOON ? null : managedPoint);
        entity.setDropoffPoint(tripOption == TripOption.MORNING ? null : managedPoint);
        entity.setTripOption(tripOption);
        entity.setMonday(Boolean.TRUE);
        entity.setTuesday(Boolean.TRUE);
        entity.setWednesday(Boolean.TRUE);
        entity.setThursday(Boolean.TRUE);
        entity.setFriday(Boolean.TRUE);
        entity.setSaturday(Boolean.FALSE);
        entity.setSunday(Boolean.FALSE);
        entity.setEffectiveFrom(request.getEffectiveFrom());
        entity.setEffectiveTo(request.getEffectiveTo());
        entity.setStatus(SubscriptionStatus.ACTIVE);
        entity.setSourceRequest(request);
        entity.setIsActive(Boolean.TRUE);
        return subscriptionRepository.save(entity);
    }

    private StudentSubscriptionResponse transition(Long id, Long tenantId, Long actorId, SubscriptionStatus status) {
        StudentSubscriptionEntity entity = findById(id, tenantId);
        entity.setStatus(status);
        entity.markUpdated(actor(actorId));
        return mapper.toStudentSubscriptionResponse(subscriptionRepository.save(entity));
    }

    private void apply(StudentSubscriptionEntity entity, StudentSubscriptionUpsertRequest request, Long tenantId) {
        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new AppException(AppErrorCode.INVALID_REQUEST, "effectiveTo must be greater than or equal to effectiveFrom");
        }
        StudentEntity student = masterDataService.getStudent(request.getStudentId(), tenantId);
        if (!student.getSchool().getId().equals(request.getSchoolId())) {
            throw new AppException(AppErrorCode.INVALID_REQUEST, "Subscription school must match student school");
        }
        TripOption tripOption = parseTripOption(request.getTripOption());
        SubscriptionStatus status = request.getStatus() == null
                ? SubscriptionStatus.ACTIVE
                : parseStatus(request.getStatus());
        if (status == SubscriptionStatus.ACTIVE && subscriptionRepository.overlapsActiveSubscription(
                student.getId(), tripOption, request.getEffectiveFrom(), request.getEffectiveTo(), tenantId,
                entity.getId())) {
            throw new AppException(AppErrorCode.CONFLICT, "Student already has an active overlapping subscription");
        }

        entity.setStudent(student);
        entity.setSchool(student.getSchool());
        entity.setPickupPoint(resolvePoint(request.getPickupPointId(), tenantId, student));
        entity.setDropoffPoint(resolvePoint(request.getDropoffPointId(), tenantId, student));
        entity.setTripOption(tripOption);
        entity.setMonday(Boolean.TRUE.equals(request.getMonday()));
        entity.setTuesday(Boolean.TRUE.equals(request.getTuesday()));
        entity.setWednesday(Boolean.TRUE.equals(request.getWednesday()));
        entity.setThursday(Boolean.TRUE.equals(request.getThursday()));
        entity.setFriday(Boolean.TRUE.equals(request.getFriday()));
        entity.setSaturday(Boolean.TRUE.equals(request.getSaturday()));
        entity.setSunday(Boolean.TRUE.equals(request.getSunday()));
        entity.setEffectiveFrom(request.getEffectiveFrom());
        entity.setEffectiveTo(request.getEffectiveTo());
        entity.setStatus(status);
        entity.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    private PickupPointEntity resolvePoint(Long pointId, Long tenantId, StudentEntity student) {
        if (pointId == null) {
            return null;
        }
        PickupPointEntity point = masterDataService.getPickupPoint(pointId, tenantId);
        if (!point.getSchool().getId().equals(student.getSchool().getId())) {
            throw new AppException(AppErrorCode.INVALID_REQUEST, "Pickup/dropoff point must belong to the student school");
        }
        return point;
    }

    private boolean servesDirection(StudentSubscriptionEntity entity, RouteDirection direction) {
        if (entity.getTripOption() == TripOption.ROUND_TRIP) {
            return true;
        }
        return direction == RouteDirection.OUTBOUND
                ? entity.getTripOption() == TripOption.MORNING
                : entity.getTripOption() == TripOption.AFTERNOON;
    }

    private boolean servesDate(StudentSubscriptionEntity entity, LocalDate serviceDate) {
        DayOfWeek day = serviceDate.getDayOfWeek();
        return switch (day) {
            case MONDAY -> Boolean.TRUE.equals(entity.getMonday());
            case TUESDAY -> Boolean.TRUE.equals(entity.getTuesday());
            case WEDNESDAY -> Boolean.TRUE.equals(entity.getWednesday());
            case THURSDAY -> Boolean.TRUE.equals(entity.getThursday());
            case FRIDAY -> Boolean.TRUE.equals(entity.getFriday());
            case SATURDAY -> Boolean.TRUE.equals(entity.getSaturday());
            case SUNDAY -> Boolean.TRUE.equals(entity.getSunday());
        };
    }

    private TripOption parseTripOption(String value) {
        try {
            return TripOption.valueOf(value == null ? "" : value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new AppException(AppErrorCode.INVALID_REQUEST, "Invalid tripOption: " + value);
        }
    }

    private SubscriptionStatus parseStatus(String value) {
        try {
            return SubscriptionStatus.valueOf(value == null ? "" : value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new AppException(AppErrorCode.INVALID_REQUEST, "Invalid subscription status: " + value);
        }
    }

    private Specification<StudentSubscriptionEntity> spec(Long tenantId, String keyword, String... fields) {
        return BaseSpecification.tenantActiveWithKeyword(tenantId, keyword, fields);
    }

    private Pageable pageable(BaseParamsRequest params, Set<String> allowedSorts, String defaultSortBy) {
        return PageableUtils.from(params, allowedSorts, defaultSortBy);
    }
}
