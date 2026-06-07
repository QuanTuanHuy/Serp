/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.entity.LeadEntity;
import serp.project.crm.core.domain.entity.MeetingRequestEntity;
import serp.project.crm.core.domain.entity.OpportunityEntity;

public interface INotificationPublisher {
    void publishMeetingAssigned(ActivityEntity activity, Long tenantId);

    void publishMeetingUpdated(ActivityEntity activity, Long tenantId);

    void publishMeetingCompleted(ActivityEntity activity, Long tenantId);

    void publishMeetingCancelled(ActivityEntity activity, Long tenantId);

    void publishMeetingRequestScheduled(MeetingRequestEntity request, Long tenantId);

    void publishMeetingRequestFailed(MeetingRequestEntity request, Long tenantId);

    void publishLeadAssigned(LeadEntity lead, Long tenantId, Long previousAssignedTo);

    void publishOpportunityAssigned(OpportunityEntity opportunity, Long tenantId, Long previousAssignedTo);
}
