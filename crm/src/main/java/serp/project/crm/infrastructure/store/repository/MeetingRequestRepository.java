/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.crm.infrastructure.store.model.MeetingRequestModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRequestRepository extends JpaRepository<MeetingRequestModel, Long> {

    Optional<MeetingRequestModel> findByIdAndTenantId(Long id, Long tenantId);

    Page<MeetingRequestModel> findByTenantId(Long tenantId, Pageable pageable);

    Page<MeetingRequestModel> findByTenantIdAndStatus(Long tenantId, String status, Pageable pageable);

    List<MeetingRequestModel> findTop100ByStatusAndLatestStartGreaterThanOrderByRequestedDeadlineAscPriorityScoreDescCreatedAtAsc(
            String status,
            Long nowMs);
}
