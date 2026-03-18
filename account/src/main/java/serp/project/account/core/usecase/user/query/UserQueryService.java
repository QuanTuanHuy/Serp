/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase.user.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.dto.response.UserDetailResponse;
import serp.project.account.core.domain.dto.response.UserProfileResponse;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.IUserService;
import serp.project.account.kernel.utils.PaginationUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {
    private final IUserService userService;
    private final UserProfileAssembler userProfileAssembler;
    private final UserDetailAssembler userDetailAssembler;
    private final PaginationUtils paginationUtils;

    public UserProfileResponse getUserProfile(Long userId) {
        return userService.getUserProfile(userId);
    }

    public Map<String, Object> getUsers(GetUserParams params) {
        var result = userService.getUsers(params);
        var userProfiles = userProfileAssembler.assembleListView(result.getSecond());
        int page = params.getPage() != null && params.getPage() >= 0
                ? params.getPage()
                : PaginationUtils.DEFAULT_PAGE;
        int pageSize = params.getPageSize() != null && params.getPageSize() > 0
                ? params.getPageSize()
                : PaginationUtils.DEFAULT_PAGE_SIZE;

        return paginationUtils.getResponse(result.getFirst(), page, pageSize, userProfiles);
    }

    public List<UserProfileResponse> getUsersByIds(List<Long> ids) {
        return userProfileAssembler.assembleBasic(userService.getUsersByIds(ids));
    }

    public Object getUserStats(Long organizationId) {
        return userService.getUserStats(organizationId);
    }

    public UserDetailResponse getUserDetail(Long organizationId, Long userId) {
        var user = userService.getUserById(userId);
        if (user == null) {
            throw new AppException(Constants.ErrorMessage.USER_NOT_FOUND);
        }
        if (!Objects.equals(user.getPrimaryOrganizationId(), organizationId)) {
            throw new AppException(Constants.ErrorMessage.USER_NOT_IN_ORGANIZATION,
                    Constants.HttpStatusCode.FORBIDDEN);
        }

        return userDetailAssembler.assemble(user, organizationId);
    }
}
