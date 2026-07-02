/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.kernel.utils.AuthUtils;

import java.util.EnumSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OrderAccessPolicy {

    private static final Set<OrderStatus> EDITABLE_ORDER_STATUSES = EnumSet.of(OrderStatus.CREATED);

    private final AuthUtils authUtils;

    public String resolveCustomerCreatedByScope() {
        if (isTenantStaffReader()) {
            return null;
        }
        if (authUtils.hasAnyRole("TMS_CUSTOMER")) {
            return String.valueOf(getCurrentUserIdOrThrow());
        }
        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    public void validateCanReadOrder(Order order) {
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (isTenantStaffReader()) {
            return;
        }
        if (authUtils.hasAnyRole("TMS_CUSTOMER") && isCustomerOwner(order)) {
            return;
        }
        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    public void validateCanMutateOrder(Order order) {
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (authUtils.hasAnyRole("TMS_ADMIN")) {
            return;
        }
        if (authUtils.hasAnyRole("TMS_CUSTOMER") && isCustomerOwner(order)) {
            return;
        }
        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    public void ensurePostOfficeManager() {
        if (!authUtils.hasAnyRole("TMS_POSTOFFICER_MANAGER")) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    public Long getCurrentUserIdOrThrow() {
        return authUtils.getCurrentUserId().orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
    }

    public void ensureOrderEditable(Order order) {
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() == null || !EDITABLE_ORDER_STATUSES.contains(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_NOT_EDITABLE);
        }
    }

    private boolean isTenantStaffReader() {
        return authUtils.hasAnyRole(
                "TMS_ADMIN",
                "TMS_POSTOFFICER_MANAGER",
                "TMS_POSTOFFICER",
                "TMS_HUB_MANAGER",
                "TMS_HUB_EMPLOYEE"
        );
    }

    private boolean isCustomerOwner(Order order) {
        Long currentUserId = getCurrentUserIdOrThrow();
        return order != null
                && OrderTextUtils.hasText(order.getCreatedBy())
                && String.valueOf(currentUserId).equals(order.getCreatedBy());
    }
}
