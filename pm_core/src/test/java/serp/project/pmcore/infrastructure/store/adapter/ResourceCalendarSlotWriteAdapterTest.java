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
import serp.project.pmcore.domain.resourcecalendar.enums.ResourceCalendarSlotSource;
import serp.project.pmcore.domain.resourcecalendar.model.GeneratedResourceCalendarSlot;
import serp.project.pmcore.infrastructure.store.repository.IResourceCalendarSlotRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResourceCalendarSlotWriteAdapterTest {
    @Mock
    private IResourceCalendarSlotRepository repository;

    @InjectMocks
    private ResourceCalendarSlotWriteAdapter adapter;

    @Test
    void replaceGeneratedSlotsShouldHardDeleteProfileAndExceptionSourcesBeforeInsert() {
        List<GeneratedResourceCalendarSlot> slots = List.of(new GeneratedResourceCalendarSlot(
                10L, 20L, 1791411600000L, 1791440400000L, 28800000L,
                ResourceCalendarSlotSource.PROFILE, null
        ));

        adapter.replaceGeneratedSlots(10L, List.of(20L), 1791411600000L, 1791498000000L, slots);

        verify(repository).hardDeleteGeneratedSlots(
                eq(10L),
                eq(List.of(20L)),
                any(),
                any(),
                eq(List.of("PROFILE", "EXCEPTION"))
        );
        verify(repository).saveAll(any());
    }
}
