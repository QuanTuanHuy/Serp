/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.domain.enums;

public enum ConsumerInboxAcquireResult {
    ACQUIRED,
    ALREADY_PROCESSED,
    ALREADY_DEAD;

    public boolean shouldProcess() {
        return this == ACQUIRED;
    }
}
