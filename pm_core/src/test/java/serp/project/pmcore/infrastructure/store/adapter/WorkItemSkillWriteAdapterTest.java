/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.Modifying;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemSkillMapper;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemSkillRepository;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkItemSkillWriteAdapterTest {
    @Mock
    private IWorkItemSkillRepository workItemSkillRepository;
    @Mock
    private WorkItemSkillMapper workItemSkillMapper;

    @InjectMocks
    private WorkItemSkillWriteAdapter workItemSkillWriteAdapter;

    @Test
    void deleteActiveShouldHardDeleteTenantProjectWorkItemSkills() {
        workItemSkillWriteAdapter.deleteActive(2L, 3L, 4L);

        verify(workItemSkillRepository).deleteAllByTenantIdAndProjectIdAndWorkItemId(2L, 3L, 4L);
    }

    @Test
    void repositoryDeleteShouldFlushBeforeReplacementInserts() throws NoSuchMethodException {
        Method method = IWorkItemSkillRepository.class.getMethod(
                "deleteAllByTenantIdAndProjectIdAndWorkItemId",
                Long.class,
                Long.class,
                Long.class
        );

        Modifying modifying = method.getAnnotation(Modifying.class);

        assertThat(modifying).isNotNull();
        assertThat(modifying.flushAutomatically()).isTrue();
        assertThat(modifying.clearAutomatically()).isTrue();
    }
}
