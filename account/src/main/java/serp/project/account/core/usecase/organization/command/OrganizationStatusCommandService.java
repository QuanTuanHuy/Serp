/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase.organization.command;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.dto.request.UpdateOrganizationStatusRequest;
import serp.project.account.core.domain.dto.response.OrganizationStatusUpdateResponse;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.OrganizationStatus;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.service.IUserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationStatusCommandService {
    private final IOrganizationService organizationService;
    private final IUserService userService;

    @Transactional(rollbackFor = Exception.class)
    public OrganizationStatusUpdateResponse updateOrganizationStatus(
            Long organizationId,
            Long updatedBy,
            UpdateOrganizationStatusRequest request) {
        long now = Instant.now().toEpochMilli();
        log.info("Updating organization {} status to {} by {}", organizationId, request.getStatus(), updatedBy);

        var organization = organizationService.updateOrganizationStatus(organizationId, request.getStatus());
        var users = userService.getUsersByOrganizationId(organizationId);

        int affectedUsers = 0;
        int activatedUsers = 0;
        int suspendedUsers = 0;

        for (var user : users) {
            if (request.getStatus() == OrganizationStatus.SUSPENDED) {
                updateUserStatus(user, UserStatus.SUSPENDED, now);
                affectedUsers++;
                suspendedUsers++;
                continue;
            }

            if (user.getStatus() == UserStatus.SUSPENDED) {
                updateUserStatus(user, UserStatus.ACTIVE, now);
                affectedUsers++;
                activatedUsers++;
            }
        }

        return OrganizationStatusUpdateResponse.builder()
                .organization(organization)
                .affectedUsers(affectedUsers)
                .activatedUsers(activatedUsers)
                .suspendedUsers(suspendedUsers)
                .build();
    }

    private void updateUserStatus(UserEntity user, UserStatus status, long now) {
        var patch = UserEntity.builder()
                .status(status)
                .updatedAt(now)
                .build();
        userService.updateUser(user.getId(), patch);
    }
}
