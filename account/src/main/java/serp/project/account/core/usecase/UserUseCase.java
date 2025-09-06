/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.service.IKeycloakUserService;
import serp.project.account.core.service.IUserService;
import serp.project.account.kernel.utils.PaginationUtils;
import serp.project.account.kernel.utils.ResponseUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserUseCase {
    private final IUserService userService;
    private final IKeycloakUserService keycloakUserService;

    private final ResponseUtils responseUtils;
    private final PaginationUtils paginationUtils;

    public GeneralResponse<?> assignRoles(String email, List<String> roleNames) {
        try {
            UserEntity user = userService.getUserByEmail(email);
            if (user == null) {
                return responseUtils.notFound(Constants.ErrorMessage.USER_NOT_FOUND);
            }
            keycloakUserService.assignRoles(user.getKeycloakId(), roleNames);
            return responseUtils.success("Roles assigned successfully");
        } catch (Exception e) {
            log.error("Assign roles failed: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> assignClientRoles(String email, String clientId, List<String> roleNames) {
        try {
            UserEntity user = userService.getUserByEmail(email);
            if (user == null) {
                return responseUtils.notFound(Constants.ErrorMessage.USER_NOT_FOUND);
            }
            keycloakUserService.assignClientRoles(user.getKeycloakId(), clientId, roleNames);
            return responseUtils.success("Client roles assigned successfully");
        } catch (Exception e) {
            log.error("Assign client roles failed: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getUsers(GetUserParams params) {
        try {
            var response = userService.getUsers(params);
            return responseUtils.success(paginationUtils.getResponse(response.getFirst(), response.getSecond()));
        } catch (Exception e) {
            log.error("Get users failed: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }
}
