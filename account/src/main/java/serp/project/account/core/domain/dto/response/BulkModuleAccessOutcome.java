/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.response;

import serp.project.account.core.domain.enums.BulkModuleAccessStatus;

public record BulkModuleAccessOutcome(BulkModuleAccessStatus status, String reason) {
}
