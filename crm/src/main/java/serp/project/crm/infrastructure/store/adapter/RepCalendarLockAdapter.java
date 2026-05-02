/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.adapter;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.crm.core.port.concurrency.IRepCalendarLockPort;

import java.util.Objects;

/**
 * PostgreSQL transaction-scoped advisory lock ({@code pg_advisory_xact_lock}) so only one
 * transaction per (tenant, rep) can confirm a slot at a time.
 */
@Component
@RequiredArgsConstructor
public class RepCalendarLockAdapter implements IRepCalendarLockPort {

    /**
     * Namespace key to avoid colliding with other advisory lock users in the same database.
     */
    private static final int ADVISORY_LOCK_NAMESPACE = 0x43524D52; // "CRMR"

    private final EntityManager entityManager;

    @Override
    public void acquireExclusiveForRep(Long tenantId, Long teamMemberId) {
        if (tenantId == null || teamMemberId == null) {
            throw new IllegalArgumentException("tenantId and teamMemberId are required");
        }
        int subKey = Objects.hash(tenantId, teamMemberId);
        entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(:ns, :sub)")
                .setParameter("ns", ADVISORY_LOCK_NAMESPACE)
                .setParameter("sub", subKey)
                .getResultList();
    }
}
