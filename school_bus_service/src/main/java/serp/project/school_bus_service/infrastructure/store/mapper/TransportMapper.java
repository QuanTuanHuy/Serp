package serp.project.school_bus_service.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.school_bus_service.application.dto.response.*;
import serp.project.school_bus_service.infrastructure.store.model.*;
import serp.project.school_bus_service.kernel.shared.base.BaseMapper;

import java.util.List;

@Component
public class TransportMapper extends BaseMapper {

    public RequestStudentResponse toRequestStudentResponse(RequestStudentEntity entity) {
        RequestStudentResponse r = enrich(new RequestStudentResponse(), entity);
        r.setRequestId(entity.getRequest().getId());
        r.setStudentId(entity.getStudent().getId());
        r.setStudentName(entity.getStudent().getFullName());
        r.setPickupPointId(entity.getPickupPoint() == null ? null : entity.getPickupPoint().getId());
        r.setPickupPointName(entity.getPickupPoint() == null ? null : entity.getPickupPoint().getName());
        r.setPickupPointAddress(entity.getPickupPoint() == null ? null : entity.getPickupPoint().getAddress());
        r.setPickupPointLatitude(entity.getPickupPoint() == null ? null : entity.getPickupPoint().getLatitude());
        r.setPickupPointLongitude(entity.getPickupPoint() == null ? null : entity.getPickupPoint().getLongitude());
        return r;
    }

    public TransportRequestResponse toTransportRequestResponse(TransportRequestEntity entity) {
        TransportRequestResponse r = enrich(new TransportRequestResponse(), entity);
        r.setParentProfileId(entity.getParentProfile().getId());
        r.setParentProfileName(entity.getParentProfile().getFullName());
        r.setSchoolId(entity.getSchool().getId());
        r.setSchoolName(entity.getSchool().getName());
        r.setSchoolLatitude(entity.getSchool().getLatitude());
        r.setSchoolLongitude(entity.getSchool().getLongitude());
        r.setRequestCode(entity.getRequestCode());
        r.setRequestedAt(entity.getRequestedAt());
        r.setRequestSource(entity.getRequestSource() == null ? null : entity.getRequestSource().name());
        r.setRequestType(entity.getRequestType().name());
        r.setStatus(entity.getStatus().name());
        r.setEffectiveFrom(entity.getEffectiveFrom());
        r.setEffectiveTo(entity.getEffectiveTo());
        r.setNotes(entity.getNotes());
        r.setApprovedBy(entity.getApprovedBy());
        r.setApprovedAt(entity.getApprovedAt());
        r.setRejectionReason(entity.getRejectionReason());
        r.setChangeReason(entity.getChangeReason());
        return r;
    }

    public TransportRequestDetailResponse toTransportRequestDetailResponse(TransportRequestEntity entity,
            List<RequestStudentEntity> students) {
        TransportRequestDetailResponse r = new TransportRequestDetailResponse();
        r.setRequest(toTransportRequestResponse(entity));
        r.setStudents(mapList(students, this::toRequestStudentResponse));
        return r;
    }

    public StudentSubscriptionResponse toStudentSubscriptionResponse(StudentSubscriptionEntity entity) {
        StudentSubscriptionResponse r = enrich(new StudentSubscriptionResponse(), entity);
        r.setSubscriptionCode(entity.getSubscriptionCode());
        r.setStudentId(entity.getStudent().getId());
        r.setStudentName(entity.getStudent().getFullName());
        r.setSchoolId(entity.getSchool().getId());
        r.setSchoolName(entity.getSchool().getName());
        r.setPickupPointId(entity.getPickupPoint() == null ? null : entity.getPickupPoint().getId());
        r.setPickupPointName(entity.getPickupPoint() == null ? null : entity.getPickupPoint().getName());
        r.setDropoffPointId(entity.getDropoffPoint() == null ? null : entity.getDropoffPoint().getId());
        r.setDropoffPointName(entity.getDropoffPoint() == null ? null : entity.getDropoffPoint().getName());
        r.setTripOption(entity.getTripOption().name());
        r.setMonday(entity.getMonday());
        r.setTuesday(entity.getTuesday());
        r.setWednesday(entity.getWednesday());
        r.setThursday(entity.getThursday());
        r.setFriday(entity.getFriday());
        r.setSaturday(entity.getSaturday());
        r.setSunday(entity.getSunday());
        r.setEffectiveFrom(entity.getEffectiveFrom());
        r.setEffectiveTo(entity.getEffectiveTo());
        r.setStatus(entity.getStatus().name());
        r.setSourceRequestId(entity.getSourceRequest() == null ? null : entity.getSourceRequest().getId());
        return r;
    }

    public TransportRequestHistoryResponse toTransportRequestHistoryResponse(TransportRequestHistoryEntity entity) {
        TransportRequestHistoryResponse r = enrich(new TransportRequestHistoryResponse(), entity);
        r.setRequestId(entity.getRequest().getId());
        r.setOldStatus(entity.getOldStatus());
        r.setNewStatus(entity.getNewStatus());
        r.setChangedBy(entity.getChangedBy());
        r.setChangedAt(entity.getChangedAt());
        r.setReason(entity.getReason());
        r.setNotes(entity.getNotes());
        return r;
    }
}
