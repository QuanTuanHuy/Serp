/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.port.concurrency;

/**
 * Exclusive lock for mutating a rep's calendar within the current transaction.
 * Used to serialize conflict checks and activity/rep-time-block writes per rep.
 */
public interface IRepCalendarLockPort {

    void acquireExclusiveForRep(Long tenantId, Long teamMemberId);
}
