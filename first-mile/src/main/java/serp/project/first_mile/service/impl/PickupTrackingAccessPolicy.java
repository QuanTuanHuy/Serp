/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.first_mile.domain.Checkin;
import serp.project.first_mile.domain.Trip;
import serp.project.first_mile.enums.PostOfficeStaffRole;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.FirstMileAccessUtils;

import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
class PickupTrackingAccessPolicy {

    private static final String ACTOR_SCOPE_ADMIN_ALL = "ADMIN_ALL";
    private static final String ACTOR_SCOPE_MANAGER_SCOPED = "MANAGER_SCOPED";
    private static final String ACTOR_SCOPE_COURIER_SELF = "COURIER_SELF";

    private final FirstMileAccessUtils firstMileAccessUtils;

    Long normalizePositiveId(Long value) {
        if (value == null) {
            return null;
        }

        if (value <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        return value;
    }

    ScopeContext resolveScopeContext(Long tenantId, Long postOfficeId, Long courierStaffId) {
        if (firstMileAccessUtils.isAdmin()) {
            return new ScopeContext(
                    ACTOR_SCOPE_ADMIN_ALL,
                    postOfficeId == null ? null : Set.of(postOfficeId),
                    postOfficeId,
                    courierStaffId,
                    courierStaffId
            );
        }

        if (firstMileAccessUtils.isPostOfficerManager()) {
            Set<Long> managedPostOfficeIds = firstMileAccessUtils.getManagedPostOfficeIdsOrThrow(tenantId);

            if (postOfficeId != null && !managedPostOfficeIds.contains(postOfficeId)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            Set<Long> visiblePostOfficeIds = postOfficeId == null
                    ? managedPostOfficeIds
                    : Set.of(postOfficeId);

            return new ScopeContext(
                    ACTOR_SCOPE_MANAGER_SCOPED,
                    visiblePostOfficeIds,
                    postOfficeId,
                    courierStaffId,
                    courierStaffId
            );
        }

        if (firstMileAccessUtils.isCourier()) {
            Long currentCourierStaffId = firstMileAccessUtils.resolveCurrentStaffIdByRoleOrThrow(
                    tenantId,
                    PostOfficeStaffRole.COURIER
            );

            if (courierStaffId != null && !courierStaffId.equals(currentCourierStaffId)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            return new ScopeContext(
                    ACTOR_SCOPE_COURIER_SELF,
                    postOfficeId == null ? null : Set.of(postOfficeId),
                    postOfficeId,
                    currentCourierStaffId,
                    currentCourierStaffId
            );
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    boolean isTripInScope(Trip trip, ScopeContext scopeContext) {
        if (trip == null || trip.getId() == null) {
            return false;
        }

        if (scopeContext.visiblePostOfficeIds() != null
                && !scopeContext.visiblePostOfficeIds().contains(trip.getPostOfficeId())) {
            return false;
        }

        return scopeContext.effectiveCourierStaffId() == null
                || Objects.equals(scopeContext.effectiveCourierStaffId(), trip.getCourierStaffId());
    }

    void ensureCanViewPickupCheckin(Long tenantId, Trip trip, Checkin pickupCheckin) {
        if (firstMileAccessUtils.isAdmin()) {
            return;
        }

        if (firstMileAccessUtils.isPostOfficerManager()) {
            Set<Long> managedPostOfficeIds = firstMileAccessUtils.getManagedPostOfficeIdsOrThrow(tenantId);
            if (trip.getPostOfficeId() == null || !managedPostOfficeIds.contains(trip.getPostOfficeId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            return;
        }

        if (firstMileAccessUtils.isCourier()) {
            Long currentCourierStaffId = firstMileAccessUtils.resolveCurrentStaffIdByRoleOrThrow(
                    tenantId,
                    PostOfficeStaffRole.COURIER
            );
            if (!Objects.equals(currentCourierStaffId, pickupCheckin.getCourierStaffId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            return;
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    void ensureCanConfirmPostOfficeInbound(Long tenantId, Trip trip) {
        if (firstMileAccessUtils.isAdmin()) {
            return;
        }

        if (firstMileAccessUtils.isPostOfficerManager()) {
            Set<Long> managedPostOfficeIds = firstMileAccessUtils.getManagedPostOfficeIdsOrThrow(tenantId);
            if (trip.getPostOfficeId() == null || !managedPostOfficeIds.contains(trip.getPostOfficeId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            return;
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    void ensureCanManageTrip(Long tenantId, Trip trip) {
        if (firstMileAccessUtils.isAdmin()) {
            return;
        }

        if (firstMileAccessUtils.isPostOfficerManager()) {
            Set<Long> managedPostOfficeIds = firstMileAccessUtils.getManagedPostOfficeIdsOrThrow(tenantId);
            if (trip.getPostOfficeId() == null || !managedPostOfficeIds.contains(trip.getPostOfficeId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            return;
        }

        if (firstMileAccessUtils.isCourier()) {
            Long currentCourierStaffId = firstMileAccessUtils.resolveCurrentStaffIdByRoleOrThrow(
                    tenantId,
                    PostOfficeStaffRole.COURIER
            );
            if (!Objects.equals(currentCourierStaffId, trip.getCourierStaffId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            return;
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    record ScopeContext(
            String actorScope,
            Set<Long> visiblePostOfficeIds,
            Long selectedPostOfficeId,
            Long effectiveCourierStaffId,
            Long selectedCourierStaffId
    ) {
    }
}
