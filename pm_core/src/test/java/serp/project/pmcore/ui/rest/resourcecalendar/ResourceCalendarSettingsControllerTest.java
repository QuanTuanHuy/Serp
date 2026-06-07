/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.resourcecalendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.resourcecalendar.settings.GetResourceCalendarSettingsOverviewQuery;
import serp.project.pmcore.application.resourcecalendar.settings.GetResourceCalendarSettingsOverviewQueryHandler;
import serp.project.pmcore.application.resourcecalendar.settings.ResourceCalendarSettingsOverviewView;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceCalendarSettingsControllerTest {
    @Mock
    private AuthUtils authUtils;
    @Mock
    private ResponseUtils responseUtils;
    @Mock
    private GetResourceCalendarSettingsOverviewQueryHandler handler;
    @InjectMocks
    private ResourceCalendarSettingsController controller;

    @Test
    void getOverviewShouldResolveTenantAndReturnResponse() {
        ResourceCalendarSettingsOverviewView view = new ResourceCalendarSettingsOverviewView(
                List.of(), List.of(), List.of(), List.of(), 1L, 2L, 3L
        );
        GeneralResponse<ResourceCalendarSettingsOverviewView> envelope = new GeneralResponse<>();
        when(authUtils.getCurrentTenantId()).thenReturn(Optional.of(10L));
        when(handler.handle(new GetResourceCalendarSettingsOverviewQuery(10L))).thenReturn(view);
        when(responseUtils.success(view)).thenReturn(envelope);

        var response = controller.getOverview();

        assertThat(response.getBody()).isSameAs(envelope);
    }
}
