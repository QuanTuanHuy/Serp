/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.request.OpportunityFilterRequest;
import serp.project.crm.core.domain.dto.response.OpportunityResponse;
import serp.project.crm.core.domain.dto.response.user.UserProfileResponse;
import serp.project.crm.core.domain.entity.AccountEntity;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.entity.LeadEntity;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.OpportunityStage;
import serp.project.crm.core.mapper.OpportunityDtoMapper;
import serp.project.crm.core.port.client.IUserProfileClient;
import serp.project.crm.core.port.store.IActivityPort;
import serp.project.crm.core.port.store.ILeadPort;
import serp.project.crm.core.service.IAccountService;
import serp.project.crm.core.service.IOpportunityService;
import serp.project.crm.core.service.ITeamMemberService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpportunityUseCaseTest {

    private IOpportunityService opportunityService;
    private IAccountService accountService;
    private ITeamMemberService teamMemberService;
    private IActivityPort activityPort;
    private ILeadPort leadPort;
    private IUserProfileClient userProfileClient;
    private OpportunityUseCase opportunityUseCase;

    @BeforeEach
    void setUp() {
        opportunityService = mock(IOpportunityService.class);
        accountService = mock(IAccountService.class);
        teamMemberService = mock(ITeamMemberService.class);
        activityPort = mock(IActivityPort.class);
        leadPort = mock(ILeadPort.class);
        userProfileClient = mock(IUserProfileClient.class);

        opportunityUseCase = new OpportunityUseCase(
                opportunityService,
                accountService,
                teamMemberService,
                activityPort,
                leadPort,
                userProfileClient,
                new OpportunityDtoMapper(),
                new ResponseUtils());
    }

    @Test
    void getOpportunityByIdEnrichesNamesAndActivitySummary() {
        Long tenantId = 20L;
        Long now = System.currentTimeMillis();
        OpportunityEntity opportunity = OpportunityEntity.builder()
                .id(1L)
                .tenantId(tenantId)
                .name("Expansion deal")
                .accountId(10L)
                .leadId(30L)
                .assignedTo(40L)
                .stage(OpportunityStage.PROPOSAL)
                .estimatedValue(BigDecimal.valueOf(120_000_000))
                .probability(50)
                .expectedCloseDate(LocalDate.now().plusDays(5))
                .build();

        ActivityEntity completedPast = ActivityEntity.builder()
                .id(100L)
                .tenantId(tenantId)
                .opportunityId(1L)
                .status(ActivityStatus.COMPLETED)
                .activityDate(now - 86_400_000L)
                .updatedAt(now - 80_000_000L)
                .build();
        ActivityEntity overdueOpen = ActivityEntity.builder()
                .id(101L)
                .tenantId(tenantId)
                .opportunityId(1L)
                .status(ActivityStatus.PLANNED)
                .dueDate(now - 3_600_000L)
                .updatedAt(now - 3_000_000L)
                .build();
        ActivityEntity futureOpen = ActivityEntity.builder()
                .id(102L)
                .tenantId(tenantId)
                .opportunityId(1L)
                .status(ActivityStatus.PLANNED)
                .activityDate(now + 7_200_000L)
                .updatedAt(now)
                .build();
        ActivityEntity cancelledFuture = ActivityEntity.builder()
                .id(103L)
                .tenantId(tenantId)
                .opportunityId(1L)
                .status(ActivityStatus.CANCELLED)
                .activityDate(now + 1_800_000L)
                .updatedAt(now)
                .build();

        when(opportunityService.getOpportunityById(1L, tenantId)).thenReturn(Optional.of(opportunity));
        when(accountService.getAccountsByIds(List.of(10L), tenantId)).thenReturn(List.of(
                AccountEntity.builder().id(10L).name("Acme Manufacturing").build()));
        when(leadPort.findByIds(List.of(30L), tenantId)).thenReturn(List.of(
                LeadEntity.builder().id(30L).name("Jane Lead").build()));
        when(userProfileClient.getUserProfilesByIds(List.of(40L))).thenReturn(List.of(
                UserProfileResponse.builder()
                        .id(40L)
                        .firstName("Alex")
                        .lastName("Owner")
                        .build()));
        when(activityPort.findByOpportunityIds(List.of(1L), tenantId)).thenReturn(List.of(
                completedPast, overdueOpen, futureOpen, cancelledFuture));

        GeneralResponse<?> response = opportunityUseCase.getOpportunityById(1L, tenantId);

        OpportunityResponse data = (OpportunityResponse) response.getData();
        assertThat(data.getAccountName()).isEqualTo("Acme Manufacturing");
        assertThat(data.getLeadName()).isEqualTo("Jane Lead");
        assertThat(data.getAssignedToName()).isEqualTo("Alex Owner");
        assertThat(data.getOpenActivityCount()).isEqualTo(2);
        assertThat(data.getOverdueActivityCount()).isEqualTo(1);
        assertThat(data.getNextActivityAt()).isEqualTo(futureOpen.getActivityDate());
        assertThat(data.getLastActivityAt()).isEqualTo(futureOpen.getActivityDate());
    }

    @Test
    void filterOpportunitiesEnrichesEveryReturnedOpportunity() {
        Long tenantId = 20L;
        OpportunityFilterRequest filter = OpportunityFilterRequest.builder().build();
        OpportunityEntity first = OpportunityEntity.builder()
                .id(1L)
                .tenantId(tenantId)
                .name("First")
                .accountId(10L)
                .assignedTo(40L)
                .stage(OpportunityStage.PROSPECTING)
                .estimatedValue(BigDecimal.TEN)
                .build();
        OpportunityEntity second = OpportunityEntity.builder()
                .id(2L)
                .tenantId(tenantId)
                .name("Second")
                .accountId(11L)
                .stage(OpportunityStage.NEGOTIATION)
                .estimatedValue(BigDecimal.ONE)
                .build();

        when(opportunityService.filterOpportunities(eq(filter), eq(tenantId), any()))
                .thenReturn(org.springframework.data.util.Pair.of(List.of(first, second), 2L));
        when(accountService.getAccountsByIds(List.of(10L, 11L), tenantId)).thenReturn(List.of(
                AccountEntity.builder().id(10L).name("Account A").build(),
                AccountEntity.builder().id(11L).name("Account B").build()));
        when(userProfileClient.getUserProfilesByIds(List.of(40L))).thenReturn(List.of(
                UserProfileResponse.builder()
                        .id(40L)
                        .firstName("Owner")
                        .lastName("A")
                        .build()));
        when(activityPort.findByOpportunityIds(List.of(1L, 2L), tenantId)).thenReturn(List.of());

        GeneralResponse<?> response = opportunityUseCase.filterOpportunities(filter, tenantId);

        serp.project.crm.core.domain.dto.PageResponse<?> page =
                (serp.project.crm.core.domain.dto.PageResponse<?>) response.getData();
        List<?> items = page.getItems();
        OpportunityResponse firstResponse = (OpportunityResponse) items.get(0);
        OpportunityResponse secondResponse = (OpportunityResponse) items.get(1);
        assertThat(firstResponse.getAccountName()).isEqualTo("Account A");
        assertThat(firstResponse.getAssignedToName()).isEqualTo("Owner A");
        assertThat(firstResponse.getOpenActivityCount()).isZero();
        assertThat(secondResponse.getAccountName()).isEqualTo("Account B");
        assertThat(secondResponse.getAssignedToName()).isNull();
        assertThat(secondResponse.getOpenActivityCount()).isZero();
    }
}
