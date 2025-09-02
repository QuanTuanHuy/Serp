/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.service.IUserService;
import serp.project.account.kernel.utils.PaginationUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserUseCase {
    private final IUserService userService;

    private final ResponseUtils responseUtils;
    private final PaginationUtils paginationUtils;

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
