/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.OrderTransitionOutbox;
import serp.project.first_mile.enums.OrderTransitionOutboxStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface OrderTransitionOutboxRepository extends JpaRepository<OrderTransitionOutbox, Long> {
    List<OrderTransitionOutbox> findByStatusInAndNextRetryAtLessThanEqualOrderByIdAsc(
            Collection<OrderTransitionOutboxStatus> statuses,
            LocalDateTime nextRetryAt,
            Pageable pageable
    );
}
