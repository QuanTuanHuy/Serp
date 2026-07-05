/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.entity.RepTimeBlockEntity;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.domain.enums.RepTimeBlockType;
import serp.project.crm.core.domain.enums.TeamMemberStatus;
import serp.project.crm.core.port.store.IRepTimeBlockPort;
import serp.project.crm.core.port.store.ITeamMemberPort;
import serp.project.crm.core.service.IRepTimeBlockService;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RepTimeBlockService implements IRepTimeBlockService {

    private final IRepTimeBlockPort repTimeBlockPort;
    private final ITeamMemberPort teamMemberPort;

    @Override
    @Transactional
    public void syncFromActivity(ActivityEntity activity, Long tenantId) {
        if (activity == null || activity.getId() == null) {
            return;
        }

        if (!shouldCreateBlock(activity)) {
            repTimeBlockPort.deleteByActivityId(activity.getId(), tenantId);
            return;
        }

        TeamMemberEntity teamMember = teamMemberPort.findByUserId(activity.getAssignedTo(), tenantId)
                .filter(member -> TeamMemberStatus.ACTIVE.equals(member.getStatus()))
                .orElse(null);
        if (teamMember == null) {
            repTimeBlockPort.deleteByActivityId(activity.getId(), tenantId);
            return;
        }

        RepTimeBlockEntity block = repTimeBlockPort.findByActivityId(activity.getId(), tenantId)
                .orElseGet(() -> RepTimeBlockEntity.builder()
                        .tenantId(tenantId)
                        .activityId(activity.getId())
                        .createdBy(activity.getUpdatedBy() != null ? activity.getUpdatedBy() : activity.getCreatedBy())
                        .build());

        block.setTenantId(tenantId);
        block.setTeamMemberId(teamMember.getId());
        block.setActivityId(activity.getId());
        block.setStartTime(activity.getActivityDate());
        block.setEndTime(activity.getActivityDate() + Duration.ofMinutes(activity.getDurationMinutes()).toMillis());
        block.setBlockType(RepTimeBlockType.MEETING);
        block.setUpdatedBy(activity.getUpdatedBy() != null ? activity.getUpdatedBy() : activity.getCreatedBy());

        repTimeBlockPort.save(block);
    }

    @Override
    @Transactional
    public void removeByActivityId(Long activityId, Long tenantId) {
        if (activityId == null) {
            return;
        }
        repTimeBlockPort.deleteByActivityId(activityId, tenantId);
    }

    private boolean shouldCreateBlock(ActivityEntity activity) {
        return ActivityType.MEETING.equals(activity.getActivityType())
                && ActivityStatus.PLANNED.equals(activity.getStatus())
                && activity.getAssignedTo() != null
                && activity.getActivityDate() != null
                && activity.getDurationMinutes() != null
                && activity.getDurationMinutes() > 0;
    }
}
