package serp.project.school_bus_service.infrastructure.store.repository;

import serp.project.school_bus_service.enums.SubscriptionStatus;
import serp.project.school_bus_service.enums.TripOption;
import serp.project.school_bus_service.infrastructure.store.model.StudentSubscriptionEntity;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;

import java.time.LocalDate;
import java.util.List;

public interface StudentSubscriptionRepository extends BaseRepository<StudentSubscriptionEntity, Long> {

    List<StudentSubscriptionEntity> findByStudentIdAndTenantIdAndIsDeletedFalse(Long studentId, Long tenantId);

    List<StudentSubscriptionEntity> findBySchoolIdAndTenantIdAndStatusAndIsDeletedFalse(
            Long schoolId,
            Long tenantId,
            SubscriptionStatus status);

    List<StudentSubscriptionEntity> findByStudentIdAndTripOptionAndTenantIdAndStatusAndIsDeletedFalse(
            Long studentId,
            TripOption tripOption,
            Long tenantId,
            SubscriptionStatus status);

    long countByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, SubscriptionStatus status);

    default boolean overlapsActiveSubscription(
            Long studentId,
            TripOption tripOption,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            Long tenantId,
            Long excludingId) {
        return findByStudentIdAndTripOptionAndTenantIdAndStatusAndIsDeletedFalse(
                studentId,
                tripOption,
                tenantId,
                SubscriptionStatus.ACTIVE)
                .stream()
                .filter(item -> excludingId == null || !item.getId().equals(excludingId))
                .anyMatch(item -> rangesOverlap(effectiveFrom, effectiveTo, item.getEffectiveFrom(), item.getEffectiveTo()));
    }

    private boolean rangesOverlap(LocalDate leftStart, LocalDate leftEnd, LocalDate rightStart, LocalDate rightEnd) {
        LocalDate safeLeftEnd = leftEnd == null ? LocalDate.MAX : leftEnd;
        LocalDate safeRightEnd = rightEnd == null ? LocalDate.MAX : rightEnd;
        return !safeLeftEnd.isBefore(rightStart) && !safeRightEnd.isBefore(leftStart);
    }
}

