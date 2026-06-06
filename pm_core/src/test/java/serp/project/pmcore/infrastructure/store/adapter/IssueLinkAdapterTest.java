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
import serp.project.pmcore.infrastructure.store.mapper.IssueLinkMapper;
import serp.project.pmcore.infrastructure.store.repository.IIssueLinkRepository;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IssueLinkAdapterTest {
    @Mock
    private IIssueLinkRepository issueLinkRepository;
    @Mock
    private IssueLinkMapper issueLinkMapper;

    @InjectMocks
    private IssueLinkAdapter issueLinkAdapter;

    @Test
    void deleteShouldHardDeleteTenantScopedLink() {
        issueLinkAdapter.delete(55L, 1L);

        verify(issueLinkRepository).deleteByIdAndTenantId(55L, 1L);
    }

    @Test
    void repositoryDeleteShouldFlushBeforeFollowUpWrites() throws NoSuchMethodException {
        Method method = IIssueLinkRepository.class.getMethod(
                "deleteByIdAndTenantId",
                Long.class,
                Long.class
        );

        Modifying modifying = method.getAnnotation(Modifying.class);

        assertThat(modifying).isNotNull();
        assertThat(modifying.flushAutomatically()).isTrue();
        assertThat(modifying.clearAutomatically()).isTrue();
    }
}
