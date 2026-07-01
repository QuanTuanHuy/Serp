/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.usecase.user.command.UserAccountCommandService;
import serp.project.account.core.usecase.user.command.UserProvisioningCoordinator;
import serp.project.account.core.usecase.user.command.UserRoleCoordinator;
import serp.project.account.core.usecase.user.export.UserExportService;
import serp.project.account.core.usecase.user.password.UserPasswordResetCoordinator;
import serp.project.account.core.usecase.user.query.UserQueryService;
import serp.project.account.kernel.utils.ResponseUtils;

@ExtendWith(MockitoExtension.class)
class UserUseCaseGetUsersTest {

    private static final String ORGANIZATION_REQUIRED_FOR_MODULE_FILTER =
            "organizationId is required when moduleId is provided";

    @Mock
    private IOrganizationService organizationService;

    @Mock
    private UserProvisioningCoordinator userProvisioningCoordinator;

    @Mock
    private UserRoleCoordinator userRoleCoordinator;

    @Mock
    private UserAccountCommandService userAccountCommandService;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private UserPasswordResetCoordinator userPasswordResetCoordinator;

    @Mock
    private UserExportService userExportService;

    @Mock
    private ResponseUtils responseUtils;

    @InjectMocks
    private UserUseCase userUseCase;

    @Test
    void getUsersWithModuleIdWithoutOrganizationIdShouldReturnBadRequest() {
        GeneralResponse<?> badRequest = GeneralResponse.builder()
                .status("ERROR")
                .code(400)
                .message(ORGANIZATION_REQUIRED_FOR_MODULE_FILTER)
                .build();
        doReturn(badRequest).when(responseUtils).badRequest(ORGANIZATION_REQUIRED_FOR_MODULE_FILTER);

        GeneralResponse<?> response = userUseCase.getUsers(GetUserParams.builder()
                .moduleId(20L)
                .build());

        assertEquals(400, response.getCode());
        assertEquals(ORGANIZATION_REQUIRED_FOR_MODULE_FILTER, response.getMessage());
        verify(responseUtils).badRequest(ORGANIZATION_REQUIRED_FOR_MODULE_FILTER);
        verifyNoInteractions(userQueryService);
    }
}
