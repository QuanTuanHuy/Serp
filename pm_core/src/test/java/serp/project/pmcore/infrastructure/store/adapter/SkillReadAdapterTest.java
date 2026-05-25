/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.skill.entity.SkillEntity;
import serp.project.pmcore.domain.skill.entity.UserSkillEntity;
import serp.project.pmcore.domain.skill.entity.WorkItemSkillEntity;
import serp.project.pmcore.domain.skill.enums.SkillProficiency;
import serp.project.pmcore.domain.skill.enums.SkillRequirementType;
import serp.project.pmcore.domain.skill.enums.SkillSource;
import serp.project.pmcore.infrastructure.store.mapper.SkillMapper;
import serp.project.pmcore.infrastructure.store.mapper.UserSkillMapper;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemSkillMapper;
import serp.project.pmcore.infrastructure.store.model.SkillModel;
import serp.project.pmcore.infrastructure.store.model.UserSkillModel;
import serp.project.pmcore.infrastructure.store.model.WorkItemSkillModel;
import serp.project.pmcore.infrastructure.store.repository.ISkillRepository;
import serp.project.pmcore.infrastructure.store.repository.IUserSkillRepository;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemSkillRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillReadAdapterTest {
    private static final Long TENANT_ID = 2L;

    @Mock
    private ISkillRepository skillRepository;
    @Mock
    private IWorkItemSkillRepository workItemSkillRepository;
    @Mock
    private IUserSkillRepository userSkillRepository;

    private SkillReadAdapter skillReadAdapter;
    private WorkItemSkillReadAdapter workItemSkillReadAdapter;
    private UserSkillReadAdapter userSkillReadAdapter;

    @BeforeEach
    void setUp() {
        skillReadAdapter = new SkillReadAdapter(skillRepository, new SkillMapper());
        workItemSkillReadAdapter = new WorkItemSkillReadAdapter(workItemSkillRepository, new WorkItemSkillMapper());
        userSkillReadAdapter = new UserSkillReadAdapter(userSkillRepository, new UserSkillMapper());
    }

    @Test
    void listActiveByIdsShouldUseTenantScopedActiveRepositoryQuery() {
        when(skillRepository.findAllByTenantIdAndIdInAndActiveTrue(TENANT_ID, List.of(1L)))
                .thenReturn(List.of(SkillModel.builder().id(1L).tenantId(TENANT_ID).code("java").name("Java").active(true).build()));

        List<SkillEntity> result = skillReadAdapter.listActiveByIds(TENANT_ID, List.of(1L));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getCode()).isEqualTo("java");
        verify(skillRepository).findAllByTenantIdAndIdInAndActiveTrue(TENANT_ID, List.of(1L));
    }

    @Test
    void listActiveByIdsShouldReturnEmptyWithoutRepositoryCallWhenInputEmpty() {
        assertThat(skillReadAdapter.listActiveByIds(TENANT_ID, List.of())).isEmpty();

        verify(skillRepository, never()).findAllByTenantIdAndIdInAndActiveTrue(TENANT_ID, List.of());
    }

    @Test
    void listActiveByWorkItemIdsShouldUseTenantScopedRepositoryQuery() {
        when(workItemSkillRepository.findAllByTenantIdAndWorkItemIdIn(TENANT_ID, List.of(10L)))
                .thenReturn(List.of(WorkItemSkillModel.builder()
                        .id(11L).tenantId(TENANT_ID).projectId(3L).workItemId(10L).skillId(1L)
                        .requirementType(SkillRequirementType.REQUIRED).minProficiency(SkillProficiency.WORKING)
                        .weight(1).source(SkillSource.MANUAL).build()));

        List<WorkItemSkillEntity> result = workItemSkillReadAdapter.listActiveByWorkItemIds(TENANT_ID, List.of(10L));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getRequirementType()).isEqualTo(SkillRequirementType.REQUIRED);
        verify(workItemSkillRepository).findAllByTenantIdAndWorkItemIdIn(TENANT_ID, List.of(10L));
    }

    @Test
    void listActiveByUserIdsShouldUseTenantScopedRepositoryQuery() {
        when(userSkillRepository.findAllByTenantIdAndUserIdIn(TENANT_ID, List.of(20L)))
                .thenReturn(List.of(UserSkillModel.builder()
                        .id(21L).tenantId(TENANT_ID).userId(20L).skillId(1L)
                        .proficiency(SkillProficiency.EXPERT).confidence(100).source(SkillSource.MANUAL).build()));

        List<UserSkillEntity> result = userSkillReadAdapter.listActiveByUserIds(TENANT_ID, List.of(20L));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getProficiency()).isEqualTo(SkillProficiency.EXPERT);
        verify(userSkillRepository).findAllByTenantIdAndUserIdIn(TENANT_ID, List.of(20L));
    }
}
