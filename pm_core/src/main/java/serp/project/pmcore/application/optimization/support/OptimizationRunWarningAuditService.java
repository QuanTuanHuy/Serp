/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunWarningEntity;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunWarningPort;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OptimizationRunWarningAuditService {
    private final IOptimizationRunWarningPort optimizationRunWarningPort;
    private final JsonUtils jsonUtils;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordInvalidOverrideWarning(Long tenantId,
                                             Long userId,
                                             Long runId,
                                             Long workItemId,
                                             String message) {
        long now = System.currentTimeMillis();
        OptimizationRunWarningEntity warning = OptimizationRunWarningEntity.builder()
                .tenantId(tenantId)
                .runId(runId)
                .workItemId(workItemId)
                .severity("ERROR")
                .code(OptimizationWarningCode.INVALID_OVERRIDE.name())
                .message(message)
                .detailsJson(jsonUtils.toJson(Map.of("workItemId", workItemId)))
                .build();
        warning.applyCreate(userId, now);
        optimizationRunWarningPort.saveAll(List.of(warning));
    }
}
