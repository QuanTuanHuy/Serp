/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import org.springframework.stereotype.Service;
import serp.project.crm.core.domain.entity.AccountEntity;
import serp.project.crm.core.domain.entity.MeetingRequestEntity;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.enums.AccountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class MeetingRequestPriorityService {

    private static final BigDecimal HIGH_VALUE = new BigDecimal("500000000");
    private static final BigDecimal MEDIUM_VALUE = new BigDecimal("100000000");

    public int calculate(MeetingRequestEntity request, AccountEntity account, OpportunityEntity opportunity) {
        int score = request.getMeetingType() != null ? request.getMeetingType().getPriorityPoints() : 0;
        score += calculateUrgencyScore(request);
        score += calculateOpportunityScore(opportunity);

        if (account != null && AccountType.CUSTOMER.equals(account.getAccountType())) {
            score += 5;
        }

        return Math.min(score, 100);
    }

    private int calculateUrgencyScore(MeetingRequestEntity request) {
        if (request.getRequestedDeadline() == null) {
            return 0;
        }

        long hoursLeft = ChronoUnit.HOURS.between(Instant.now(), Instant.ofEpochMilli(request.getRequestedDeadline()));
        if (hoursLeft <= 24) {
            return 50;
        }
        if (hoursLeft <= 72) {
            return 35;
        }
        if (hoursLeft <= 168) {
            return 20;
        }
        return 10;
    }

    private int calculateOpportunityScore(OpportunityEntity opportunity) {
        if (opportunity == null || opportunity.getEstimatedValue() == null) {
            return 0;
        }

        BigDecimal value = opportunity.getEstimatedValue();
        if (value.compareTo(HIGH_VALUE) >= 0) {
            return 20;
        }
        if (value.compareTo(MEDIUM_VALUE) >= 0) {
            return 10;
        }
        return 0;
    }
}
