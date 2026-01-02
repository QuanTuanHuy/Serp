/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Optimization REST API
 */

package serp.project.ptm_optimization.ui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.ptm_optimization.core.domain.dto.GeneralResponse;
import serp.project.ptm_optimization.core.domain.dto.request.OptimizationRequest;
import serp.project.ptm_optimization.core.domain.dto.response.StrategyInfoResponse;
import serp.project.ptm_optimization.core.domain.enums.StrategyType;
import serp.project.ptm_optimization.core.usecase.OptimizationUseCase;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.PlanResult;
import serp.project.ptm_optimization.kernel.utils.ResponseUtils;

@RestController
@RequestMapping("/api/v1/optimization")
@RequiredArgsConstructor
@Slf4j
public class OptimizationController {

    private final OptimizationUseCase optimizationUseCase;
    private final ResponseUtils responseUtils;

    @PostMapping("/schedule")
    public ResponseEntity<GeneralResponse<PlanResult>> optimize(
            @RequestBody OptimizationRequest request,
            @RequestParam(defaultValue = "AUTO") StrategyType strategy) {
        log.info("Received optimization request: tasks={}, strategy={}",
                request.getTasks().size(), strategy);

        PlanResult result = optimizationUseCase.optimize(
                request.getTasks(),
                request.getWindows(),
                request.getWeights(),
                request.getParams(),
                strategy);

        return ResponseEntity.ok(responseUtils.success(result));
    }

    @PostMapping("/schedule-with-fallback")
    public ResponseEntity<GeneralResponse<PlanResult>> optimizeWithFallback(
            @RequestBody OptimizationRequest request) {
        log.info("Received optimization request with fallback: tasks={}",
                request.getTasks().size());

        PlanResult result = optimizationUseCase.optimizeWithFallback(
                request.getTasks(),
                request.getWindows(),
                request.getWeights(),
                request.getParams());

        return ResponseEntity.ok(responseUtils.success(result));
    }

    @GetMapping("/strategies")
    public ResponseEntity<GeneralResponse<StrategyInfoResponse>> getStrategies() {
        // TODO: Implement strategy info endpoint
        return ResponseEntity.ok(responseUtils.success(null));
    }

}
