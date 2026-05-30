/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill.query.user.batch;

import org.junit.jupiter.api.Test;
import serp.project.pmcore.application.skill.UserSkillView;
import serp.project.pmcore.domain.skill.entity.UserSkillEntity;
import serp.project.pmcore.domain.skill.enums.SkillProficiency;
import serp.project.pmcore.domain.skill.enums.SkillSource;
import serp.project.pmcore.domain.skill.service.ISkillService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListUsersSkillsQueryHandlerTest {
    private static final Long TENANT_ID = 2L;

    @Test
    void handleShouldGroupSkillsByRequestedUserId() {
        ISkillService skillService = mock(ISkillService.class);
        ListUsersSkillsQueryHandler handler = new ListUsersSkillsQueryHandler(skillService);
        when(skillService.listUsersSkills(TENANT_ID, List.of(20L, 30L)))
                .thenReturn(List.of(
                        userSkill(20L, 1L, SkillProficiency.EXPERT),
                        userSkill(20L, 2L, SkillProficiency.WORKING)
                ));

        Map<Long, List<UserSkillView>> result = handler.handle(new ListUsersSkillsQuery(
                List.of(20L, 30L),
                TENANT_ID
        ));

        verify(skillService).listUsersSkills(TENANT_ID, List.of(20L, 30L));
        assertThat(result).containsOnlyKeys(20L, 30L);
        assertThat(result.get(20L)).extracting(UserSkillView::getSkillId).containsExactly(1L, 2L);
        assertThat(result.get(30L)).isEmpty();
    }

    private UserSkillEntity userSkill(Long userId, Long skillId, SkillProficiency proficiency) {
        return UserSkillEntity.builder()
                .id(userId + skillId)
                .tenantId(TENANT_ID)
                .userId(userId)
                .skillId(skillId)
                .proficiency(proficiency)
                .confidence(90)
                .source(SkillSource.MANUAL)
                .build();
    }
}
