/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.second_mile.domain.KafkaDlqMessage;
import serp.project.second_mile.enums.KafkaDlqMessageStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface KafkaDlqMessageRepository extends JpaRepository<KafkaDlqMessage, Long> {
    @Query("""
            SELECT message
            FROM KafkaDlqMessage message
            WHERE message.status IN :statuses
              AND (message.nextRetryAt IS NULL OR message.nextRetryAt <= :currentTime)
            ORDER BY message.id ASC
            """)
    List<KafkaDlqMessage> findRetryableMessages(
            @Param("statuses") List<KafkaDlqMessageStatus> statuses,
            @Param("currentTime") LocalDateTime currentTime,
            Pageable pageable
    );
}
