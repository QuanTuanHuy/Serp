package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Component;
import serp.project.school_bus_service.entity.PickupPointEntity;
import serp.project.school_bus_service.service.IPickupPointService;
import serp.project.school_bus_service.service.ISchoolPickupPointService;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;

/**
 * Reusable validator to check that a pickup/drop-off point is allowed
 * for a given school within the N-N junction table.
 */
@Component
public class SchoolPickupPointValidator {

    private final IPickupPointService pickupPointService;
    private final ISchoolPickupPointService schoolPickupPointService;
    private final MessageCommon messageCommon;

    public SchoolPickupPointValidator(IPickupPointService pickupPointService,
                                      ISchoolPickupPointService schoolPickupPointService,
                                      MessageCommon messageCommon) {
        this.pickupPointService = pickupPointService;
        this.schoolPickupPointService = schoolPickupPointService;
        this.messageCommon = messageCommon;
    }

    /**
     * Validate that pickupPointId is an active, linked pickup point for the given school.
     * usage_type must be PICKUP_ONLY or PICKUP_DROPOFF.
     */
    public void validatePickupPointAllowedForSchool(Long tenantId, Long schoolId, Long pickupPointId) {
        PickupPointEntity pp = pickupPointService.getPickupPoint(pickupPointId, tenantId);

        if (!Boolean.TRUE.equals(pp.getIsActive())) {
            throw new AppException(AppErrorCode.PickupPoint.INACTIVE,
                    messageCommon.getMessage(AppErrorCode.PickupPoint.INACTIVE, pickupPointId));
        }

        String usage = pp.getUsageType();
        if (!"PICKUP_ONLY".equals(usage) && !"PICKUP_DROPOFF".equals(usage)) {
            throw new AppException(AppErrorCode.PickupPoint.USAGE_NOT_ALLOWED_PICKUP,
                    messageCommon.getMessage(AppErrorCode.PickupPoint.USAGE_NOT_ALLOWED_PICKUP, usage));
        }

        if (!schoolPickupPointService.isLinkedAndActive(schoolId, pickupPointId, tenantId)) {
            throw new AppException(AppErrorCode.PickupPoint.NOT_LINKED,
                    messageCommon.getMessage(AppErrorCode.PickupPoint.NOT_LINKED));
        }
    }

    /**
     * Validate that dropoffPointId is an active, linked drop-off point for the given school.
     * usage_type must be DROPOFF_ONLY or PICKUP_DROPOFF.
     */
    public void validateDropoffPointAllowedForSchool(Long tenantId, Long schoolId, Long dropoffPointId) {
        PickupPointEntity pp = pickupPointService.getPickupPoint(dropoffPointId, tenantId);

        if (!Boolean.TRUE.equals(pp.getIsActive())) {
            throw new AppException(AppErrorCode.DropoffPoint.INACTIVE,
                    messageCommon.getMessage(AppErrorCode.DropoffPoint.INACTIVE, dropoffPointId));
        }

        String usage = pp.getUsageType();
        if (!"DROPOFF_ONLY".equals(usage) && !"PICKUP_DROPOFF".equals(usage)) {
            throw new AppException(AppErrorCode.DropoffPoint.USAGE_NOT_ALLOWED_DROP,
                    messageCommon.getMessage(AppErrorCode.DropoffPoint.USAGE_NOT_ALLOWED_DROP, usage));
        }

        if (!schoolPickupPointService.isLinkedAndActive(schoolId, dropoffPointId, tenantId)) {
            throw new AppException(AppErrorCode.DropoffPoint.NOT_LINKED,
                    messageCommon.getMessage(AppErrorCode.DropoffPoint.NOT_LINKED));
        }
    }
}
