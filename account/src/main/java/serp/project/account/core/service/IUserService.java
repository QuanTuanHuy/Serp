/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import org.springframework.data.util.Pair;
import serp.project.account.core.domain.dto.request.CreateUserDto;
import serp.project.account.core.domain.dto.request.CreateUserForOrgRequest;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.dto.response.UserProfileResponse;
import serp.project.account.core.domain.entity.UserEntity;

import java.util.List;

public interface IUserService {
    UserEntity createUser(CreateUserDto request);

    UserEntity createUser(Long organizationId, CreateUserForOrgRequest request);

    UserEntity updateUser(Long userId, UserEntity update);

    UserEntity getUserByEmail(String email);

    UserEntity getUserById(Long userId);

    UserProfileResponse getUserProfile(Long userId);

    List<UserEntity> getUsersByOrganizationId(Long organizationId);

    List<UserProfileResponse> getUserProfilesByIds(List<Long> userIds);

    void addRolesToUser(Long userId, List<Long> roleIds);

    void removeRolesFromUser(Long userId, List<Long> roleIds);

    void updateKeycloakUser(Long userId, String keycloakId);

    Pair<Long, List<UserEntity>> getUsers(GetUserParams params);

    Integer countUsersByOrganizationId(Long organizationId);
}
