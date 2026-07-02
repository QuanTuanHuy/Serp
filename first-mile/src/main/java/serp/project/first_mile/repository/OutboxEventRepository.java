/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.OutboxEvent;
import serp.project.first_mile.enums.OutboxEventStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT event
            FROM OutboxEvent event
            WHERE event.status IN :statuses
              AND (event.nextRetryAt IS NULL OR event.nextRetryAt <= :now)
            ORDER BY event.id ASC
            """)
    List<OutboxEvent> findPublishableEvents(
            @Param("statuses") List<OutboxEventStatus> statuses,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}
