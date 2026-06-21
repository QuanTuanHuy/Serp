/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import serp.project.pmcore.application.search.query.global.PmGlobalSearchQuery;
import serp.project.pmcore.application.search.query.global.PmGlobalSearchQueryHandler;
import serp.project.pmcore.application.search.query.global.PmGlobalSearchResponseView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PmGlobalSearchControllerTest {

    @Mock
    private AuthUtils authUtils;

    @Mock
    private ResponseUtils responseUtils;

    @Mock
    private PmGlobalSearchQueryHandler handler;

    private PmGlobalSearchController controller;

    @BeforeEach
    void setUp() {
        controller = new PmGlobalSearchController(authUtils, responseUtils, handler);
    }

    @Test
    void searchShouldResolveAuthAndDelegateToHandler() {
        PmGlobalSearchResponseView view = new PmGlobalSearchResponseView("serp", 5, List.of());
        GeneralResponse<PmGlobalSearchResponseView> envelope = GeneralResponse.<PmGlobalSearchResponseView>builder()
                .data(view)
                .build();

        when(authUtils.getCurrentTenantId()).thenReturn(Optional.of(1L));
        when(authUtils.getCurrentUserId()).thenReturn(Optional.of(2L));
        when(authUtils.getCurrentGroups()).thenReturn(Set.of("devs"));
        when(handler.handle(any(PmGlobalSearchQuery.class))).thenReturn(view);
        when(responseUtils.success(view)).thenReturn(envelope);

        ResponseEntity<GeneralResponse<PmGlobalSearchResponseView>> response = controller.search("serp", 5, 10L);

        assertEquals(envelope, response.getBody());
        verify(handler).handle(new PmGlobalSearchQuery(1L, 2L, Set.of("devs"), "serp", 5, 10L));
    }

    @Test
    void searchShouldRejectMissingTenant() {
        when(authUtils.getCurrentTenantId()).thenReturn(Optional.empty());

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> controller.search("serp", 5, null));

        assertEquals(DomainErrorCode.TENANT_NOT_FOUND, exception.getErrorCode());
    }
}
