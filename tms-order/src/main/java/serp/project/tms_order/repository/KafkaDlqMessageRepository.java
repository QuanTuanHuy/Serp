/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.tms_order.domain.KafkaDlqMessage;
import serp.project.tms_order.enums.KafkaDlqMessageStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface KafkaDlqMessageRepository extends JpaRepository<KafkaDlqMessage, Long> {

    @Query("""
            SELECT message
            FROM KafkaDlqMessage message
            WHERE message.status IN :statuses
              AND (message.nextRetryAt IS NULL OR message.nextRetryAt <= :now)
            ORDER BY message.id ASC
            """)
    List<KafkaDlqMessage> findRetryableMessages(
            @Param("statuses") List<KafkaDlqMessageStatus> statuses,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}
