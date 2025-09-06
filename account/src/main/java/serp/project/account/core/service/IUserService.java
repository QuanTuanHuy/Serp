/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import org.springframework.data.util.Pair;
import serp.project.account.core.domain.dto.request.CreateUserDto;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.entity.UserEntity;

import java.util.List;

public interface IUserService {
    UserEntity createUser(CreateUserDto request);
    UserEntity getUserByEmail(String email);
    void updateKeycloakUser(Long userId, String keycloakId);
    Pair<Long, List<UserEntity>> getUsers(GetUserParams params);
}
