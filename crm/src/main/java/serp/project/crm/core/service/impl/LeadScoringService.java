/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.entity.LeadEntity;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.port.store.IActivityPort;
import serp.project.crm.core.service.ILeadScoringService;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadScoringService implements ILeadScoringService {

    private final IActivityPort activityPort;

    // Profile scores
    private static final int SCORE_TITLE_C_LEVEL = 25;
    private static final int SCORE_TITLE_MANAGER = 15;
    private static final int SCORE_INDUSTRY_TECH_FINANCE = 20;
    private static final int SCORE_HIGH_VALUE = 20;

    // Activity scores
    private static final int SCORE_ACT_MEETING = 30;
    private static final int SCORE_ACT_CALL = 15;
    private static final int SCORE_ACT_EMAIL = 5;
    private static final int SCORE_ACT_TASK = 2;

    @Override
    public Integer calculateSmartScore(LeadEntity lead) {
        if (lead == null) {
            return 0;
        }

        int profileScore = calculateProfileScore(lead);
        int engagementScore = calculateEngagementScore(lead.getId(), lead.getTenantId());
        int totalScore = profileScore + engagementScore;
        return Math.min(totalScore, 100);
    }

    private int calculateProfileScore(LeadEntity lead) {
        int score = 0;

        if (lead.getJobTitle() != null) {
            String title = lead.getJobTitle().toLowerCase();
            if (title.contains("ceo") || title.contains("cto") || title.contains("director")
                    || title.contains("founder") || title.contains("president")) {
                score += SCORE_TITLE_C_LEVEL;
            } else if (title.contains("manager") || title.contains("head") || title.contains("lead")) {
                score += SCORE_TITLE_MANAGER;
            }
        }

        if (lead.getIndustry() != null) {
            String industry = lead.getIndustry().toLowerCase();
            if (industry.contains("technology") || industry.contains("software") || industry.contains("finance")
                    || industry.contains("banking")) {
                score += SCORE_INDUSTRY_TECH_FINANCE;
            }
        }

        if (lead.getEstimatedValue() != null && lead.getEstimatedValue().compareTo(new BigDecimal("100000000")) > 0) {
            score += SCORE_HIGH_VALUE;
        }

        if (lead.getPhone() != null && !lead.getPhone().isEmpty())
            score += 5;
        if (lead.getEmail() != null && !lead.getEmail().isEmpty())
            score += 5;
        if (lead.getCompany() != null && !lead.getCompany().isEmpty())
            score += 5;

        return score;
    }

    private int calculateEngagementScore(long leadId, long tenantId) {
        List<ActivityEntity> activities = activityPort.findByLeadId(leadId, tenantId);

        int score = 0;
        for (var activity : activities) {
            if (!activity.isCompleted()) {
                continue;
            }
            int basePoint = getBasePointForActivity(activity.getActivityType());
            double timeDecayMultiplier = calculateTimeDecayMultiplier(activity.getActivityDate(), LocalDate.now());

            score += (int) (basePoint * timeDecayMultiplier);
        }

        return score;
    }

    private int getBasePointForActivity(ActivityType type) {
        if (type == null)
            return 0;
        return switch (type) {
            case MEETING -> SCORE_ACT_MEETING;
            case CALL -> SCORE_ACT_CALL;
            case EMAIL -> SCORE_ACT_EMAIL;
            case TASK -> SCORE_ACT_TASK;
        };
    }

    private double calculateTimeDecayMultiplier(Long activityTimestamp, LocalDate now) {
        if (activityTimestamp == null)
            return 0.0;

        LocalDate activityDate = Instant.ofEpochMilli(activityTimestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        long daysDiff = ChronoUnit.DAYS.between(activityDate, now);

        if (daysDiff <= 7) {
            return 1.0; // Trong vòng 1 tuần: 100% điểm
        } else if (daysDiff <= 30) {
            return 0.7;
        } else if (daysDiff <= 90) {
            return 0.3;
        } else {
            return 0.0;
        }
    }
}
