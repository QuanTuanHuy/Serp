/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import serp.project.account.infrastructure.store.model.UserInvitationModel;

import java.util.Optional;

@Repository
public interface IUserInvitationRepository extends IBaseRepository<UserInvitationModel> {
    Optional<UserInvitationModel> findByToken(String token);

    Optional<UserInvitationModel> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<UserInvitationModel> findByOrganizationIdAndEmailAndStatus(
            Long organizationId, String email, String status);

    Page<UserInvitationModel> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<UserInvitationModel> findByOrganizationIdAndStatus(
            Long organizationId, String status, Pageable pageable);

    Integer countByOrganizationIdAndStatus(Long organizationId, String status);
}
