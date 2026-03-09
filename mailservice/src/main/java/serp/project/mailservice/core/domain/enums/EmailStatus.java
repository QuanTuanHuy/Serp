/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.enums;

import java.util.Map;
import java.util.Set;

public enum EmailStatus {
    PENDING,
    SENT,
    FAILED,
    RETRY,
    CANCELLED;

    private static final Map<EmailStatus, Set<EmailStatus>> VALID_TRANSITIONS = Map.of(
            PENDING, Set.of(SENT, FAILED, RETRY, CANCELLED),
            SENT, Set.of(),
            FAILED, Set.of(RETRY, CANCELLED),
            RETRY, Set.of(SENT, FAILED, CANCELLED),
            CANCELLED, Set.of()
    );

    public boolean canTransitionTo(EmailStatus target) {
        Set<EmailStatus> allowed = VALID_TRANSITIONS.get(this);
        return allowed != null && allowed.contains(target);
    }

    public boolean isTerminal() {
        return this == SENT || this == CANCELLED;
    }

    public boolean isRetryable() {
        return this == FAILED || this == RETRY;
    }

    public boolean isPending() {
        return this == PENDING;
    }
}
