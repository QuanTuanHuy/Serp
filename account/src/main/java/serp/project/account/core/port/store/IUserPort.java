/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.util.Pair;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.UserStatus;

public interface IUserPort {
    UserEntity save(UserEntity user);

    UserEntity getUserByEmail(String email);

    UserEntity getUserById(Long id);

    Pair<Long, List<UserEntity>> getUsers(GetUserParams params);

    List<UserEntity> getUsersByIds(List<Long> userIds);

    List<UserEntity> getUsersByOrganizationId(Long organizationId);

    List<UserEntity> getUsersByOrganizationIdAndIds(Long organizationId, List<Long> userIds);

    Integer countUsersByOrganizationId(Long organizationId);

    Integer countUsersByOrganizationIdAndStatus(Long organizationId, UserStatus status);

    Long countUsers();

    Long countUsersByStatus(UserStatus status);

    Map<Long, Long> countUsersByOrganizationIds(List<Long> organizationIds);

    Integer countUsersByOrganizationIdAndCreatedBetween(Long organizationId, LocalDateTime from, LocalDateTime to);

    Integer countAdminUsersByOrganizationId(Long organizationId);

    Map<String, Integer> countUsersByStatusForOrganization(Long organizationId);
}
