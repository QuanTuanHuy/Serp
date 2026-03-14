/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import org.springframework.data.util.Pair;
import serp.project.account.core.domain.entity.UserInvitationEntity;

import java.util.List;
import java.util.Optional;

public interface IUserInvitationPort {
    UserInvitationEntity save(UserInvitationEntity invitation);

    Optional<UserInvitationEntity> getByToken(String token);

    Optional<UserInvitationEntity> getById(Long id, Long organizationId);

    Optional<UserInvitationEntity> getPendingByOrgAndEmail(Long organizationId, String email);

    Pair<Long, List<UserInvitationEntity>> getByOrganizationId(Long organizationId, int page, int pageSize);

    Pair<Long, List<UserInvitationEntity>> getByOrganizationIdAndStatus(
            Long organizationId, String status, int page, int pageSize);

    Integer countPendingByOrganizationId(Long organizationId);

    void deleteById(Long id);
}
