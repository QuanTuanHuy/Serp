/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.command.discard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewAssembler;
import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewView;
import serp.project.pmcore.application.optimization.support.OptimizationRunGuard;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunEntity;
import serp.project.pmcore.domain.optimization.enums.OptimizationRunStatus;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunItemPort;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunPort;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunWarningPort;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class DiscardOptimizationRunCommandHandler
        implements ICommandHandler<DiscardOptimizationRunCommand, OptimizationRunReviewView> {

    private final IOptimizationRunPort optimizationRunPort;
    private final IOptimizationRunItemPort optimizationRunItemPort;
    private final IOptimizationRunWarningPort optimizationRunWarningPort;
    private final OptimizationRunGuard optimizationRunGuard;
    private final OptimizationRunReviewAssembler optimizationRunReviewAssembler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OptimizationRunReviewView handle(DiscardOptimizationRunCommand command) {
        validate(command);
        OptimizationRunEntity run = optimizationRunGuard.requireRunInProject(
                command.tenantId(),
                command.projectId(),
                command.runId()
        );
        optimizationRunGuard.ensureStatus(
                run,
                Set.of(OptimizationRunStatus.GENERATED, OptimizationRunStatus.PARTIALLY_APPLIED),
                "discarded"
        );
        long now = System.currentTimeMillis();
        run.setStatus(OptimizationRunStatus.DISCARDED);
        run.setDiscardedAt(now);
        run.applyUpdate(command.userId(), now);
        OptimizationRunEntity savedRun = optimizationRunPort.save(run);
        return optimizationRunReviewAssembler.toView(
                savedRun,
                optimizationRunItemPort.listByRunId(command.tenantId(), command.runId()),
                optimizationRunWarningPort.listByRunId(command.tenantId(), command.runId())
        );
    }

    private void validate(DiscardOptimizationRunCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Discard optimization run command is required");
        }
        if (command.tenantId() == null || command.tenantId() <= 0) {
            throw new IllegalArgumentException("tenantId must be a positive number");
        }
        if (command.userId() == null || command.userId() <= 0) {
            throw new IllegalArgumentException("userId must be a positive number");
        }
        if (command.projectId() == null || command.projectId() <= 0) {
            throw new IllegalArgumentException("projectId must be a positive number");
        }
        if (command.runId() == null || command.runId() <= 0) {
            throw new IllegalArgumentException("runId must be a positive number");
        }
    }
}
