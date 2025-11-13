/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.store.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.mailservice.core.domain.enums.EmailStatus;
import serp.project.mailservice.infrastructure.store.model.EmailModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailRepository extends JpaRepository<EmailModel, Long> {
    Optional<EmailModel> findByMessageId(String messageId);

    List<EmailModel> findByTenantIdAndStatus(Long tenantId, EmailStatus status, Pageable pageable);

    @Query("SELECT e FROM EmailModel e WHERE e.status = :status AND e.nextRetryAt <= :beforeTime " +
           "AND e.retryCount < :maxRetries ORDER BY e.nextRetryAt ASC")
    List<EmailModel> findEmailsForRetry(@Param("status") EmailStatus status,
                                        @Param("beforeTime") LocalDateTime beforeTime,
                                        @Param("maxRetries") int maxRetries,
                                        Pageable pageable);

    @Query("SELECT e FROM EmailModel e WHERE e.status = :status ORDER BY e.priority DESC, e.createdAt ASC")
    List<EmailModel> findPendingEmails(@Param("status") EmailStatus status, Pageable pageable);

    List<EmailModel> findByUserId(Long userId, Pageable pageable);

    long countByTenantIdAndStatus(Long tenantId, EmailStatus status);
}
