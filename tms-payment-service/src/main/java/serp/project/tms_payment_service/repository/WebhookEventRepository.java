package serp.project.tms_payment_service.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.tms_payment_service.entity.WebhookEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    boolean existsByEventKey(String eventKey);

    Optional<WebhookEvent> findByEventKey(String eventKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from WebhookEvent e where e.id = :id")
    Optional<WebhookEvent> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select e.id
            from WebhookEvent e
            where e.nextRetryAt <= :now
              and e.status in ('PENDING', 'FAILED')
              and e.attemptCount < e.maxAttempts
            order by e.createdAt asc
            """)
    List<Long> findRetryableEventIds(@Param("now") LocalDateTime now);
}
