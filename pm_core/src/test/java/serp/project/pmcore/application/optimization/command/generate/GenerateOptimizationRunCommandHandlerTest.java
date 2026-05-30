/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.command.generate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewAssembler;
import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewView;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunEntity;
import serp.project.pmcore.domain.optimization.enums.CapacityCoverageStatus;
import serp.project.pmcore.domain.optimization.enums.CapacitySourceMode;
import serp.project.pmcore.domain.optimization.enums.OptimizationCapability;
import serp.project.pmcore.domain.optimization.enums.OptimizationChangeScope;
import serp.project.pmcore.domain.optimization.enums.OptimizationObjective;
import serp.project.pmcore.domain.optimization.enums.OptimizationRunStatus;
import serp.project.pmcore.domain.optimization.enums.OptimizationSolverStatus;
import serp.project.pmcore.domain.optimization.model.CapacityResolutionResult;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmDescriptor;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmOptions;
import serp.project.pmcore.domain.optimization.model.OptimizationDependencyGraph;
import serp.project.pmcore.domain.optimization.model.OptimizationProblem;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;
import serp.project.pmcore.domain.optimization.model.OptimizationRunSummary;
import serp.project.pmcore.domain.optimization.model.OptimizationSolution;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunItemPort;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunPort;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunWarningPort;
import serp.project.pmcore.domain.optimization.service.IOptimizationAlgorithm;
import serp.project.pmcore.domain.optimization.service.IOptimizationAlgorithmRegistry;
import serp.project.pmcore.domain.optimization.service.IOptimizationProjectModelBuilder;
import serp.project.pmcore.domain.optimization.service.OptimizationSolutionValidator;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateOptimizationRunCommandHandlerTest {
    @Mock
    private IOptimizationProjectModelBuilder optimizationProjectModelBuilder;
    @Mock
    private IOptimizationAlgorithmRegistry optimizationAlgorithmRegistry;
    @Mock
    private IOptimizationRunPort optimizationRunPort;
    @Mock
    private IOptimizationRunItemPort optimizationRunItemPort;
    @Mock
    private IOptimizationRunWarningPort optimizationRunWarningPort;
    @Mock
    private OptimizationRunReviewAssembler optimizationRunReviewAssembler;
    @Mock
    private OptimizationSolutionValidator optimizationSolutionValidator;
    @Mock
    private JsonUtils jsonUtils;

    @InjectMocks
    private GenerateOptimizationRunCommandHandler handler;

    @Test
    void handleShouldPersistAlgorithmMetadata() {
        OptimizationProjectModel projectModel = emptyProjectModel();
        IOptimizationAlgorithm algorithm = stubAlgorithm();
        OptimizationRunEntity savedRun = OptimizationRunEntity.builder()
                .id(900L)
                .tenantId(1L)
                .projectId(2L)
                .status(OptimizationRunStatus.GENERATED)
                .algorithmKey(OptimizationAlgorithmKeys.GREEDY_BALANCED)
                .algorithmVersion(OptimizationAlgorithmKeys.DEFAULT_VERSION)
                .solverStatus(OptimizationSolverStatus.FEASIBLE.name())
                .objectiveScore(BigDecimal.valueOf(7.5))
                .build();

        when(optimizationProjectModelBuilder.build(any())).thenReturn(projectModel);
        when(optimizationAlgorithmRegistry.resolve(OptimizationAlgorithmKeys.GREEDY_BALANCED)).thenReturn(algorithm);
        when(optimizationSolutionValidator.validate(any(), any())).thenAnswer(invocation -> invocation.getArgument(1));
        when(jsonUtils.toJson(any())).thenReturn("{}");
        when(optimizationRunPort.save(any())).thenReturn(savedRun);
        when(optimizationRunItemPort.saveAll(any())).thenReturn(List.of());
        when(optimizationRunReviewAssembler.toView(any(), any(), any()))
                .thenReturn(OptimizationRunReviewView.builder().build());

        handler.handle(new GenerateOptimizationRunCommand(
                1L,
                10L,
                2L,
                "SELECTED_WORK_ITEMS",
                null,
                OptimizationObjective.BALANCED_WORKLOAD,
                OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE,
                1_000L,
                10_000L,
                List.of(100L)
        ));

        ArgumentCaptor<OptimizationRunEntity> runCaptor = ArgumentCaptor.forClass(OptimizationRunEntity.class);
        verify(optimizationRunPort).save(runCaptor.capture());
        OptimizationRunEntity run = runCaptor.getValue();
        assertThat(run.getAlgorithmKey()).isEqualTo(OptimizationAlgorithmKeys.GREEDY_BALANCED);
        assertThat(run.getObjective()).isEqualTo(OptimizationObjective.BALANCED_WORKLOAD.name());
        assertThat(run.getChangeScope()).isEqualTo(OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE.name());
        assertThat(run.getAlgorithmVersion()).isEqualTo(OptimizationAlgorithmKeys.DEFAULT_VERSION);
        assertThat(run.getSolverStatus()).isEqualTo(OptimizationSolverStatus.FEASIBLE.name());
        assertThat(run.getObjectiveScore()).isEqualByComparingTo("7.5");
    }

    @Test
    void handleShouldRejectWhenAlgorithmCapabilitiesDoNotSupportRequestedScope() {
        IOptimizationAlgorithm algorithm = stubAlgorithm(Set.of(OptimizationCapability.ASSIGNMENT));
        when(optimizationAlgorithmRegistry.resolve(OptimizationAlgorithmKeys.GREEDY_BALANCED)).thenReturn(algorithm);

        assertThatThrownBy(() -> handler.handle(new GenerateOptimizationRunCommand(
                1L,
                10L,
                2L,
                "SELECTED_WORK_ITEMS",
                OptimizationAlgorithmKeys.GREEDY_BALANCED,
                OptimizationObjective.BALANCED_WORKLOAD,
                OptimizationChangeScope.SCHEDULE_ONLY,
                1_000L,
                10_000L,
                List.of(100L)
        ))).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not support scheduling");
    }

    private OptimizationProjectModel emptyProjectModel() {
        return new OptimizationProjectModel(
                1L,
                2L,
                null,
                1_000L,
                10_000L,
                new OptimizationDependencyGraph(List.of(), List.of(), List.of(), Map.of(), Map.of(), List.of()),
                List.of(),
                List.of(),
                new CapacityResolutionResult(
                        List.of(),
                        CapacitySourceMode.FALLBACK_WEEKDAY_8H_UTC,
                        CapacityCoverageStatus.NOT_REQUIRED,
                        CapacityCoverageStatus.NOT_REQUIRED,
                        List.of(),
                        null,
                        null,
                        0L,
                        0L,
                        0L,
                        List.of(),
                        List.of()
                ),
                List.of(),
                Map.of()
        );
    }

    private IOptimizationAlgorithm stubAlgorithm() {
        return stubAlgorithm(Set.of(OptimizationCapability.ASSIGNMENT, OptimizationCapability.SCHEDULING));
    }

    private IOptimizationAlgorithm stubAlgorithm(Set<OptimizationCapability> capabilities) {
        return new IOptimizationAlgorithm() {
            @Override
            public OptimizationAlgorithmDescriptor descriptor() {
                return new OptimizationAlgorithmDescriptor(
                        OptimizationAlgorithmKeys.GREEDY_BALANCED,
                        OptimizationAlgorithmKeys.DEFAULT_VERSION,
                        capabilities
                );
            }

            @Override
            public OptimizationSolution solve(OptimizationProblem problem, OptimizationAlgorithmOptions options) {
                return new OptimizationSolution(
                        Map.of(),
                        Map.of(),
                        List.of(),
                        OptimizationRunSummary.builder().scopeSize(0).warningsCount(0).build(),
                        descriptor(),
                        OptimizationSolverStatus.FEASIBLE,
                        BigDecimal.valueOf(7.5)
                );
            }
        };
    }
}
