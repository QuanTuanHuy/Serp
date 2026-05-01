package serp.project.crm.core.service.impl;

import org.junit.jupiter.api.Test;
import serp.project.crm.core.domain.entity.AccountEntity;
import serp.project.crm.core.domain.entity.MeetingRequestEntity;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.enums.AccountType;
import serp.project.crm.core.domain.enums.MeetingRequestType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class MeetingRequestPriorityServiceTest {

    private final MeetingRequestPriorityService service = new MeetingRequestPriorityService();

    @Test
    void urgentHighValueCustomerRequest_hasHigherPriority() {
        MeetingRequestEntity request = MeetingRequestEntity.builder()
                .meetingType(MeetingRequestType.NEGOTIATION)
                .requestedDeadline(Instant.now().plus(6, ChronoUnit.HOURS).toEpochMilli())
                .build();

        AccountEntity account = AccountEntity.builder()
                .accountType(AccountType.CUSTOMER)
                .build();

        OpportunityEntity opportunity = OpportunityEntity.builder()
                .estimatedValue(new BigDecimal("700000000"))
                .build();

        int score = service.calculate(request, account, opportunity);

        assertThat(score).isGreaterThanOrEqualTo(75);
    }

    @Test
    void relaxedRequestWithoutOpportunity_hasLowerPriority() {
        MeetingRequestEntity request = MeetingRequestEntity.builder()
                .meetingType(MeetingRequestType.DISCOVERY)
                .requestedDeadline(Instant.now().plus(10, ChronoUnit.DAYS).toEpochMilli())
                .build();

        AccountEntity account = AccountEntity.builder()
                .accountType(AccountType.PROSPECT)
                .build();

        int score = service.calculate(request, account, null);

        assertThat(score).isLessThan(30);
    }
}
