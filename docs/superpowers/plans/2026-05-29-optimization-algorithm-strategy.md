# PM Core Optimization Algorithm Strategy Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a replaceable optimization algorithm strategy layer for `pm_core`, preserve the current greedy behavior first, then add validation, scheduling accuracy fixes, and smaller greedy internals.

**Architecture:** `pm_core` remains responsible for problem building, solution validation, persistence, review, and apply safety. Algorithms implement a domain contract that receives normalized optimization input and returns a solution; the first algorithm wraps the current greedy behavior as `greedy-balanced`.

**Tech Stack:** Java 21, Spring Boot 3.5, JUnit 5, Mockito, Flyway, PostgreSQL, Maven via direct `mvn` commands.

---

## Source Spec

Read this before implementing: `docs/superpowers/specs/2026-05-29-optimization-algorithm-strategy-design.md`.

Run all Maven commands from `pm_core/`.

## File Structure

### Existing Files To Modify

- `pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/dto/request/GenerateOptimizationRunRequest.java`
  - Add `algorithmKey` to the generate request.
- `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommand.java`
  - Add `algorithmKey`.
- `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandler.java`
  - Resolve algorithm, validate solution, persist algorithm metadata.
- `pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewView.java`
  - Add algorithm metadata to review output.
- `pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssembler.java`
  - Map algorithm metadata from run entity.
- `pm_core/src/main/java/serp/project/pmcore/domain/optimization/entity/OptimizationRunEntity.java`
  - Add algorithm metadata fields.
- `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationBuilderInput.java`
  - Add `algorithmKey`.
- `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationGenerationResult.java`
  - Keep as the legacy generator result while `OptimizationSolution` becomes the algorithm-facing contract.
- `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationScheduleSuggestion.java`
  - Add `allocatedEffortMillis` during scheduling accuracy work.
- `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGenerator.java`
  - Preserve existing behavior in Step 1; split internals in Step 4.
- `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/OptimizationRunModel.java`
  - Add algorithm metadata columns.
- `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/mapper/OptimizationRunMapper.java`
  - Map algorithm metadata.

### New Main Files

- `pm_core/src/main/java/serp/project/pmcore/domain/optimization/constant/OptimizationAlgorithmKeys.java`
  - Stable algorithm key constants.
- `pm_core/src/main/java/serp/project/pmcore/domain/optimization/enums/OptimizationCapability.java`
  - Algorithm capability enum.
- `pm_core/src/main/java/serp/project/pmcore/domain/optimization/enums/OptimizationSolverStatus.java`
  - Solver status enum.
- `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationAlgorithmDescriptor.java`
  - Algorithm identity and capability metadata.
- `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationAlgorithmOptions.java`
  - Immutable algorithm options derived from the generate request.
- `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationProblem.java`
  - Adapter around `OptimizationProjectModel` for the new contract.
- `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationSolution.java`
  - Algorithm output contract.
- `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/IOptimizationAlgorithm.java`
  - Strategy interface for all algorithms.
- `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/IOptimizationAlgorithmRegistry.java`
  - Registry interface.
- `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl/OptimizationAlgorithmRegistry.java`
  - Spring-backed registry.
- `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl/GreedyBalancedOptimizationAlgorithm.java`
  - Adapter that exposes existing greedy generator as `greedy-balanced`.
- `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/OptimizationSolutionValidator.java`
  - Algorithm-independent solution validator.
- `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationScheduleAllocation.java`
  - Optional allocation detail introduced in scheduling accuracy work.
- `pm_core/src/main/resources/db/migration/V27__add_optimization_algorithm_metadata.sql`
  - Flyway migration for run metadata.

### New Test Files

- `pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/impl/OptimizationAlgorithmRegistryTest.java`
- `pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/OptimizationSolutionValidatorTest.java`
- `pm_core/src/test/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandlerTest.java`

### Existing Tests To Update

- `pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGeneratorTest.java`
- `pm_core/src/test/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssemblerTest.java`
- `pm_core/src/test/java/serp/project/pmcore/infrastructure/store/mapper/OptimizationFoundationMapperTest.java`

---

## PR 1: Introduce Algorithm Contract And Metadata

### Task 1: Add Algorithm Constants And Enums

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/constant/OptimizationAlgorithmKeys.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/enums/OptimizationCapability.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/enums/OptimizationSolverStatus.java`

- [ ] **Step 1: Create `OptimizationAlgorithmKeys`**

Use this complete file:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OptimizationAlgorithmKeys {
    public static final String GREEDY_BALANCED = "greedy-balanced";
    public static final String GREEDY_MINIMAL_REASSIGNMENT = "greedy-minimal-reassignment";
    public static final String GREEDY_SKILL_FIRST = "greedy-skill-first";
    public static final String GREEDY_DEADLINE_FIRST = "greedy-deadline-first";
    public static final String SOLVER_V1 = "solver-v1";
    public static final String DEFAULT_VERSION = "v1";
}
```

- [ ] **Step 2: Create `OptimizationCapability`**

Use this complete file:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.enums;

public enum OptimizationCapability {
    ASSIGNMENT,
    SCHEDULING,
    CAPACITY_AWARE,
    SKILL_AWARE,
    DEPENDENCY_AWARE
}
```

- [ ] **Step 3: Create `OptimizationSolverStatus`**

Use this complete file:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.enums;

public enum OptimizationSolverStatus {
    OPTIMAL,
    FEASIBLE,
    INFEASIBLE,
    TIME_LIMIT,
    FAILED
}
```

- [ ] **Step 4: Compile the new files**

Run from `pm_core/`:

```bash
mvn -DskipTests compile
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit**

```bash
git add pm_core/src/main/java/serp/project/pmcore/domain/optimization/constant/OptimizationAlgorithmKeys.java pm_core/src/main/java/serp/project/pmcore/domain/optimization/enums/OptimizationCapability.java pm_core/src/main/java/serp/project/pmcore/domain/optimization/enums/OptimizationSolverStatus.java
git commit -m "feat(pm): add optimization algorithm metadata types"
```

### Task 2: Add Algorithm Model Contracts

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationAlgorithmDescriptor.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationAlgorithmOptions.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationProblem.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationSolution.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationBuilderInput.java`

- [ ] **Step 1: Create `OptimizationAlgorithmDescriptor`**

Use this complete file:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import serp.project.pmcore.domain.optimization.enums.OptimizationCapability;

import java.util.Set;

public record OptimizationAlgorithmDescriptor(
        String key,
        String version,
        Set<OptimizationCapability> capabilities
) {
}
```

- [ ] **Step 2: Create `OptimizationAlgorithmOptions`**

Use this complete file:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import serp.project.pmcore.domain.optimization.enums.OptimizationMode;

public record OptimizationAlgorithmOptions(
        String algorithmKey,
        OptimizationMode mode,
        Boolean allowReassignment,
        Boolean allowScheduleChanges
) {
}
```

- [ ] **Step 3: Create `OptimizationProblem`**

Use this complete file:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

public record OptimizationProblem(
        OptimizationProjectModel projectModel,
        OptimizationBuilderInput input
) {
}
```

- [ ] **Step 4: Create `OptimizationSolution`**

Use this complete file:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import serp.project.pmcore.domain.optimization.enums.OptimizationSolverStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record OptimizationSolution(
        Map<Long, OptimizationAssignmentSuggestion> assignmentSuggestions,
        Map<Long, OptimizationScheduleSuggestion> scheduleSuggestions,
        List<OptimizationConstraintViolation> warnings,
        OptimizationRunSummary summary,
        OptimizationAlgorithmDescriptor algorithm,
        OptimizationSolverStatus solverStatus,
        BigDecimal objectiveScore
) {
    public static OptimizationSolution fromGenerationResult(OptimizationGenerationResult result,
                                                            OptimizationAlgorithmDescriptor algorithm) {
        return new OptimizationSolution(
                result.assignmentSuggestions(),
                result.scheduleSuggestions(),
                result.warnings(),
                result.summary(),
                algorithm,
                OptimizationSolverStatus.FEASIBLE,
                null
        );
    }
}
```

- [ ] **Step 5: Modify `OptimizationBuilderInput`**

Replace the record body with:

```java
public record OptimizationBuilderInput(
        Long tenantId,
        Long projectId,
        List<Long> selectedWorkItemIds,
        Long planningStart,
        Long planningEnd,
        Boolean allowReassignment,
        Boolean allowScheduleChanges,
        OptimizationMode mode,
        String algorithmKey
) {
}
```

- [ ] **Step 6: Compile and capture constructor call failures**

Run from `pm_core/`:

```bash
mvn -DskipTests compile
```

Expected: FAIL until all `OptimizationBuilderInput` constructor call sites pass `algorithmKey`. The next task fixes those call sites.

### Task 3: Add Algorithm Strategy And Registry

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/IOptimizationAlgorithm.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/IOptimizationAlgorithmRegistry.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl/OptimizationAlgorithmRegistry.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl/GreedyBalancedOptimizationAlgorithm.java`
- Create: `pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/impl/OptimizationAlgorithmRegistryTest.java`

- [ ] **Step 1: Write registry test first**

Create this test file:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.impl;

import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.enums.OptimizationCapability;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmDescriptor;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmOptions;
import serp.project.pmcore.domain.optimization.model.OptimizationProblem;
import serp.project.pmcore.domain.optimization.model.OptimizationSolution;
import serp.project.pmcore.domain.optimization.service.IOptimizationAlgorithm;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptimizationAlgorithmRegistryTest {

    @Test
    void resolveShouldReturnRegisteredAlgorithmByKey() {
        IOptimizationAlgorithm algorithm = stubAlgorithm(OptimizationAlgorithmKeys.GREEDY_BALANCED);
        OptimizationAlgorithmRegistry registry = new OptimizationAlgorithmRegistry(List.of(algorithm));

        IOptimizationAlgorithm resolved = registry.resolve(OptimizationAlgorithmKeys.GREEDY_BALANCED);

        assertThat(resolved).isSameAs(algorithm);
    }

    @Test
    void resolveShouldRejectUnknownAlgorithmKey() {
        OptimizationAlgorithmRegistry registry = new OptimizationAlgorithmRegistry(
                List.of(stubAlgorithm(OptimizationAlgorithmKeys.GREEDY_BALANCED))
        );

        assertThatThrownBy(() -> registry.resolve("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported optimization algorithm");
    }

    private IOptimizationAlgorithm stubAlgorithm(String key) {
        return new IOptimizationAlgorithm() {
            @Override
            public OptimizationAlgorithmDescriptor descriptor() {
                return new OptimizationAlgorithmDescriptor(
                        key,
                        OptimizationAlgorithmKeys.DEFAULT_VERSION,
                        Set.of(OptimizationCapability.ASSIGNMENT)
                );
            }

            @Override
            public OptimizationSolution solve(OptimizationProblem problem, OptimizationAlgorithmOptions options) {
                return null;
            }
        };
    }
}
```

- [ ] **Step 2: Run the failing test**

Run from `pm_core/`:

```bash
mvn -Dtest=OptimizationAlgorithmRegistryTest test
```

Expected: FAIL because `IOptimizationAlgorithm` and `OptimizationAlgorithmRegistry` do not exist.

- [ ] **Step 3: Create `IOptimizationAlgorithm`**

Use this complete file:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service;

import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmDescriptor;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmOptions;
import serp.project.pmcore.domain.optimization.model.OptimizationProblem;
import serp.project.pmcore.domain.optimization.model.OptimizationSolution;

public interface IOptimizationAlgorithm {
    OptimizationAlgorithmDescriptor descriptor();

    OptimizationSolution solve(OptimizationProblem problem, OptimizationAlgorithmOptions options);
}
```

- [ ] **Step 4: Create `IOptimizationAlgorithmRegistry`**

Use this complete file:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service;

public interface IOptimizationAlgorithmRegistry {
    IOptimizationAlgorithm resolve(String algorithmKey);
}
```

- [ ] **Step 5: Create `OptimizationAlgorithmRegistry`**

Use this complete file:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.impl;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.service.IOptimizationAlgorithm;
import serp.project.pmcore.domain.optimization.service.IOptimizationAlgorithmRegistry;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OptimizationAlgorithmRegistry implements IOptimizationAlgorithmRegistry {
    private final Map<String, IOptimizationAlgorithm> algorithmsByKey;

    public OptimizationAlgorithmRegistry(List<IOptimizationAlgorithm> algorithms) {
        this.algorithmsByKey = algorithms.stream()
                .collect(Collectors.toUnmodifiableMap(
                        algorithm -> algorithm.descriptor().key(),
                        Function.identity()
                ));
    }

    @Override
    public IOptimizationAlgorithm resolve(String algorithmKey) {
        String resolvedKey = algorithmKey == null || algorithmKey.isBlank()
                ? OptimizationAlgorithmKeys.GREEDY_BALANCED
                : algorithmKey;
        IOptimizationAlgorithm algorithm = algorithmsByKey.get(resolvedKey);
        if (algorithm == null) {
            throw new IllegalArgumentException("Unsupported optimization algorithm: " + resolvedKey);
        }
        return algorithm;
    }
}
```

- [ ] **Step 6: Create `GreedyBalancedOptimizationAlgorithm`**

Use this complete file:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.impl;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.enums.OptimizationCapability;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmDescriptor;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmOptions;
import serp.project.pmcore.domain.optimization.model.OptimizationProblem;
import serp.project.pmcore.domain.optimization.model.OptimizationSolution;
import serp.project.pmcore.domain.optimization.service.IOptimizationAlgorithm;

import java.util.Set;

@Service
public class GreedyBalancedOptimizationAlgorithm implements IOptimizationAlgorithm {
    private final GreedyOptimizationRunGenerator greedyOptimizationRunGenerator;

    public GreedyBalancedOptimizationAlgorithm(GreedyOptimizationRunGenerator greedyOptimizationRunGenerator) {
        this.greedyOptimizationRunGenerator = greedyOptimizationRunGenerator;
    }

    @Override
    public OptimizationAlgorithmDescriptor descriptor() {
        return new OptimizationAlgorithmDescriptor(
                OptimizationAlgorithmKeys.GREEDY_BALANCED,
                OptimizationAlgorithmKeys.DEFAULT_VERSION,
                Set.of(
                        OptimizationCapability.ASSIGNMENT,
                        OptimizationCapability.SCHEDULING,
                        OptimizationCapability.CAPACITY_AWARE,
                        OptimizationCapability.SKILL_AWARE,
                        OptimizationCapability.DEPENDENCY_AWARE
                )
        );
    }

    @Override
    public OptimizationSolution solve(OptimizationProblem problem, OptimizationAlgorithmOptions options) {
        return OptimizationSolution.fromGenerationResult(
                greedyOptimizationRunGenerator.generate(problem.projectModel(), problem.input()),
                descriptor()
        );
    }
}
```

- [ ] **Step 7: Run registry test**

Run from `pm_core/`:

```bash
mvn -Dtest=OptimizationAlgorithmRegistryTest test
```

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add pm_core/src/main/java/serp/project/pmcore/domain/optimization/model pm_core/src/main/java/serp/project/pmcore/domain/optimization/service pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/impl/OptimizationAlgorithmRegistryTest.java
git commit -m "feat(pm): introduce optimization algorithm strategy contract"
```

### Task 4: Add Algorithm Metadata To Persistence And Review

**Files:**
- Create: `pm_core/src/main/resources/db/migration/V27__add_optimization_algorithm_metadata.sql`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/entity/OptimizationRunEntity.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/OptimizationRunModel.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/mapper/OptimizationRunMapper.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewView.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssembler.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/infrastructure/store/mapper/OptimizationFoundationMapperTest.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssemblerTest.java`

- [ ] **Step 1: Add Flyway migration**

Create `V27__add_optimization_algorithm_metadata.sql`:

```sql
ALTER TABLE optimization_runs
    ADD COLUMN algorithm_key VARCHAR(100) NOT NULL DEFAULT 'greedy-balanced',
    ADD COLUMN algorithm_version VARCHAR(50) NOT NULL DEFAULT 'v1',
    ADD COLUMN solver_status VARCHAR(50) NOT NULL DEFAULT 'FEASIBLE',
    ADD COLUMN objective_score NUMERIC(18, 6);
```

- [ ] **Step 2: Add fields to `OptimizationRunEntity`**

Add imports:

```java
import java.math.BigDecimal;
```

Add fields after `summaryJson`:

```java
private String algorithmKey;
private String algorithmVersion;
private String solverStatus;
private BigDecimal objectiveScore;
```

- [ ] **Step 3: Add fields to `OptimizationRunModel`**

Add import:

```java
import java.math.BigDecimal;
```

Add fields after `summaryJson`:

```java
@Column(name = "algorithm_key", nullable = false, length = 100)
private String algorithmKey;
@Column(name = "algorithm_version", nullable = false, length = 50)
private String algorithmVersion;
@Column(name = "solver_status", nullable = false, length = 50)
private String solverStatus;
@Column(name = "objective_score", precision = 18, scale = 6)
private BigDecimal objectiveScore;
```

- [ ] **Step 4: Map fields in `OptimizationRunMapper.toModel`**

In the builder chain, after `.summaryJson(entity.getSummaryJson())`, add:

```java
.algorithmKey(entity.getAlgorithmKey())
.algorithmVersion(entity.getAlgorithmVersion())
.solverStatus(entity.getSolverStatus())
.objectiveScore(entity.getObjectiveScore())
```

- [ ] **Step 5: Map fields in `OptimizationRunMapper.toEntity`**

In the builder chain, after `.summaryJson(model.getSummaryJson())`, add:

```java
.algorithmKey(model.getAlgorithmKey())
.algorithmVersion(model.getAlgorithmVersion())
.solverStatus(model.getSolverStatus())
.objectiveScore(model.getObjectiveScore())
```

- [ ] **Step 6: Add fields to `OptimizationRunReviewView`**

Add import:

```java
import java.math.BigDecimal;
```

Add fields after `summary`:

```java
private String algorithmKey;
private String algorithmVersion;
private String solverStatus;
private BigDecimal objectiveScore;
```

- [ ] **Step 7: Map fields in `OptimizationRunReviewAssembler`**

In the review builder, after `.summary(summary)`, add:

```java
.algorithmKey(run.getAlgorithmKey())
.algorithmVersion(run.getAlgorithmVersion())
.solverStatus(run.getSolverStatus())
.objectiveScore(run.getObjectiveScore())
```

- [ ] **Step 8: Update mapper test**

In `OptimizationFoundationMapperTest`, add assertions to the optimization run mapper test. Use this data on the source entity/model:

```java
.algorithmKey("greedy-balanced")
.algorithmVersion("v1")
.solverStatus("FEASIBLE")
.objectiveScore(BigDecimal.valueOf(12.345678))
```

Add assertions:

```java
assertThat(model.getAlgorithmKey()).isEqualTo("greedy-balanced");
assertThat(model.getAlgorithmVersion()).isEqualTo("v1");
assertThat(model.getSolverStatus()).isEqualTo("FEASIBLE");
assertThat(model.getObjectiveScore()).isEqualByComparingTo("12.345678");
```

and for entity mapping:

```java
assertThat(entity.getAlgorithmKey()).isEqualTo("greedy-balanced");
assertThat(entity.getAlgorithmVersion()).isEqualTo("v1");
assertThat(entity.getSolverStatus()).isEqualTo("FEASIBLE");
assertThat(entity.getObjectiveScore()).isEqualByComparingTo("12.345678");
```

- [ ] **Step 9: Update review assembler test**

In `OptimizationRunReviewAssemblerTest`, build the run with:

```java
.algorithmKey("greedy-balanced")
.algorithmVersion("v1")
.solverStatus("FEASIBLE")
.objectiveScore(BigDecimal.valueOf(5.250000))
```

Add assertions:

```java
assertThat(view.getAlgorithmKey()).isEqualTo("greedy-balanced");
assertThat(view.getAlgorithmVersion()).isEqualTo("v1");
assertThat(view.getSolverStatus()).isEqualTo("FEASIBLE");
assertThat(view.getObjectiveScore()).isEqualByComparingTo("5.250000");
```

- [ ] **Step 10: Run focused tests**

Run from `pm_core/`:

```bash
mvn -Dtest=OptimizationFoundationMapperTest,OptimizationRunReviewAssemblerTest test
```

Expected: PASS.

- [ ] **Step 11: Commit**

```bash
git add pm_core/src/main/resources/db/migration/V27__add_optimization_algorithm_metadata.sql pm_core/src/main/java/serp/project/pmcore/domain/optimization/entity/OptimizationRunEntity.java pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/OptimizationRunModel.java pm_core/src/main/java/serp/project/pmcore/infrastructure/store/mapper/OptimizationRunMapper.java pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewView.java pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssembler.java pm_core/src/test/java/serp/project/pmcore/infrastructure/store/mapper/OptimizationFoundationMapperTest.java pm_core/src/test/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssemblerTest.java
git commit -m "feat(pm): persist optimization algorithm metadata"
```

### Task 5: Route Generate Through The Algorithm Registry

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/dto/request/GenerateOptimizationRunRequest.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/OptimizationRunController.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommand.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandler.java`
- Create: `pm_core/src/test/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandlerTest.java`

- [ ] **Step 1: Add `algorithmKey` to request DTO**

In `GenerateOptimizationRunRequest`, add:

```java
@Builder.Default
private String algorithmKey = OptimizationAlgorithmKeys.GREEDY_BALANCED;
```

Add import:

```java
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
```

- [ ] **Step 2: Add `algorithmKey` to `GenerateOptimizationRunCommand`**

Add a `String algorithmKey` field after `scope` in the record:

```java
public record GenerateOptimizationRunCommand(
        Long tenantId,
        Long userId,
        List<String> groupKeys,
        Long projectId,
        String scope,
        String algorithmKey,
        OptimizationMode mode,
        Long planningStart,
        Long planningEnd,
        Boolean allowReassignment,
        Boolean allowScheduleChanges,
        List<Long> selectedWorkItemIds
) implements ICommand<OptimizationRunReviewView> {
}
```

- [ ] **Step 3: Pass `algorithmKey` from controller**

In `OptimizationRunController.generateOptimizationRun`, pass `request.getAlgorithmKey()` immediately after `request.getScope()`:

```java
new GenerateOptimizationRunCommand(
        tenantId,
        userId,
        groupKeys,
        projectId,
        request.getScope(),
        request.getAlgorithmKey(),
        request.getMode(),
        request.getPlanningStart(),
        request.getPlanningEnd(),
        request.getAllowReassignment(),
        request.getAllowScheduleChanges(),
        request.getSelectedWorkItemIds()
)
```

- [ ] **Step 4: Update handler dependencies**

In `GenerateOptimizationRunCommandHandler`, replace:

```java
private final IOptimizationRunGenerator optimizationRunGenerator;
```

with:

```java
private final IOptimizationAlgorithmRegistry optimizationAlgorithmRegistry;
```

Add imports:

```java
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.enums.OptimizationSolverStatus;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmOptions;
import serp.project.pmcore.domain.optimization.model.OptimizationProblem;
import serp.project.pmcore.domain.optimization.model.OptimizationSolution;
import serp.project.pmcore.domain.optimization.service.IOptimizationAlgorithm;
import serp.project.pmcore.domain.optimization.service.IOptimizationAlgorithmRegistry;
```

Remove unused import:

```java
import serp.project.pmcore.domain.optimization.model.OptimizationGenerationResult;
import serp.project.pmcore.domain.optimization.service.IOptimizationRunGenerator;
```

- [ ] **Step 5: Build input with algorithm key**

Replace the `OptimizationBuilderInput` construction with:

```java
String algorithmKey = normalizeAlgorithmKey(command.algorithmKey());
OptimizationBuilderInput input = new OptimizationBuilderInput(
        command.tenantId(),
        command.projectId(),
        command.selectedWorkItemIds(),
        command.planningStart(),
        command.planningEnd(),
        command.allowReassignment(),
        command.allowScheduleChanges(),
        command.mode(),
        algorithmKey
);
```

Add helper method:

```java
private String normalizeAlgorithmKey(String algorithmKey) {
    return algorithmKey == null || algorithmKey.isBlank()
            ? OptimizationAlgorithmKeys.GREEDY_BALANCED
            : algorithmKey;
}
```

- [ ] **Step 6: Invoke the registry and solution**

Replace:

```java
OptimizationGenerationResult generation = optimizationRunGenerator.generate(projectModel, input);
```

with:

```java
IOptimizationAlgorithm algorithm = optimizationAlgorithmRegistry.resolve(algorithmKey);
OptimizationSolution solution = algorithm.solve(
        new OptimizationProblem(projectModel, input),
        new OptimizationAlgorithmOptions(
                algorithmKey,
                command.mode(),
                command.allowReassignment(),
                command.allowScheduleChanges()
        )
);
```

- [ ] **Step 7: Persist solution metadata**

In the run builder, add:

```java
.algorithmKey(solution.algorithm().key())
.algorithmVersion(solution.algorithm().version())
.solverStatus(solution.solverStatus() == null
        ? OptimizationSolverStatus.FEASIBLE.name()
        : solution.solverStatus().name())
.objectiveScore(solution.objectiveScore())
```

Replace all `generation` variable reads with `solution`:

```java
.summaryJson(jsonUtils.toJson(solution.summary()))
```

```java
List<OptimizationRunItemEntity> items = buildRunItems(command, savedRun.getId(), projectModel, solution, now);
List<OptimizationRunWarningEntity> warnings = buildWarnings(command, savedRun.getId(), solution.warnings(), now);
```

Change `buildRunItems` parameter type from `OptimizationGenerationResult generation` to `OptimizationSolution solution`, and replace:

```java
generation.assignmentSuggestions()
generation.scheduleSuggestions()
```

with:

```java
solution.assignmentSuggestions()
solution.scheduleSuggestions()
```

- [ ] **Step 8: Create handler test for metadata and fallback**

Create `GenerateOptimizationRunCommandHandlerTest` with Mockito. The test should:

1. Mock `IOptimizationProjectModelBuilder`.
2. Mock `IOptimizationAlgorithmRegistry`.
3. Return a fake algorithm descriptor `greedy-balanced/v1`.
4. Verify the saved `OptimizationRunEntity` has `algorithmKey`, `algorithmVersion`, and `solverStatus`.

Create this complete test file:

```java
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
import serp.project.pmcore.domain.optimization.enums.OptimizationCapability;
import serp.project.pmcore.domain.optimization.enums.OptimizationMode;
import serp.project.pmcore.domain.optimization.enums.OptimizationRunStatus;
import serp.project.pmcore.domain.optimization.enums.OptimizationSolverStatus;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmDescriptor;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmOptions;
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
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateOptimizationRunCommandHandlerTest {
    @Mock private IOptimizationProjectModelBuilder optimizationProjectModelBuilder;
    @Mock private IOptimizationAlgorithmRegistry optimizationAlgorithmRegistry;
    @Mock private IOptimizationRunPort optimizationRunPort;
    @Mock private IOptimizationRunItemPort optimizationRunItemPort;
    @Mock private IOptimizationRunWarningPort optimizationRunWarningPort;
    @Mock private OptimizationRunReviewAssembler optimizationRunReviewAssembler;
    @Mock private JsonUtils jsonUtils;

    @InjectMocks
    private GenerateOptimizationRunCommandHandler handler;

    @Test
    void handleShouldPersistAlgorithmMetadata() {
        OptimizationProjectModel projectModel = org.mockito.Mockito.mock(OptimizationProjectModel.class);
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
        when(jsonUtils.toJson(any())).thenReturn("{}");
        when(optimizationRunPort.save(any())).thenReturn(savedRun);
        when(optimizationRunItemPort.saveAll(any())).thenReturn(List.of());
        when(optimizationRunReviewAssembler.toView(any(), any(), any())).thenReturn(OptimizationRunReviewView.builder().build());

        handler.handle(new GenerateOptimizationRunCommand(
                1L,
                10L,
                List.of("pm-admin"),
                2L,
                "SELECTED_WORK_ITEMS",
                null,
                OptimizationMode.BALANCED_WORKLOAD,
                1_000L,
                10_000L,
                true,
                true,
                List.of(100L)
        ));

        ArgumentCaptor<OptimizationRunEntity> runCaptor = ArgumentCaptor.forClass(OptimizationRunEntity.class);
        org.mockito.Mockito.verify(optimizationRunPort).save(runCaptor.capture());
        OptimizationRunEntity run = runCaptor.getValue();
        assertThat(run.getAlgorithmKey()).isEqualTo(OptimizationAlgorithmKeys.GREEDY_BALANCED);
        assertThat(run.getAlgorithmVersion()).isEqualTo(OptimizationAlgorithmKeys.DEFAULT_VERSION);
        assertThat(run.getSolverStatus()).isEqualTo(OptimizationSolverStatus.FEASIBLE.name());
        assertThat(run.getObjectiveScore()).isEqualByComparingTo("7.5");
    }

    private IOptimizationAlgorithm stubAlgorithm() {
        return new IOptimizationAlgorithm() {
            @Override
            public OptimizationAlgorithmDescriptor descriptor() {
                return new OptimizationAlgorithmDescriptor(
                        OptimizationAlgorithmKeys.GREEDY_BALANCED,
                        OptimizationAlgorithmKeys.DEFAULT_VERSION,
                        Set.of(OptimizationCapability.ASSIGNMENT)
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
```

- [ ] **Step 9: Run focused tests**

Run from `pm_core/`:

```bash
mvn -Dtest=GenerateOptimizationRunCommandHandlerTest,OptimizationAlgorithmRegistryTest test
```

Expected: PASS.

- [ ] **Step 10: Run existing optimization generator tests**

Run from `pm_core/`:

```bash
mvn -Dtest=GreedyOptimizationRunGeneratorTest,OptimizationProjectModelBuilderTest test
```

Expected: PASS.

- [ ] **Step 11: Commit**

```bash
git add pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/dto/request/GenerateOptimizationRunRequest.java pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/OptimizationRunController.java pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommand.java pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandler.java pm_core/src/test/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandlerTest.java
git commit -m "feat(pm): route optimization generation through algorithm registry"
```

---

## PR 2: Add Solution Validator

### Task 6: Add Validator Model And Service

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/OptimizationSolutionValidator.java`
- Create: `pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/OptimizationSolutionValidatorTest.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandler.java`

- [ ] **Step 1: Write validator tests first**

Create `OptimizationSolutionValidatorTest` with tests for:

```java
@Test
void validateShouldWarnWhenReassignmentDisabledButAssigneeChanges()
```

Expected warning code: `INVALID_OVERRIDE`.

```java
@Test
void validateShouldWarnWhenScheduleChangesDisabledButScheduleExists()
```

Expected warning code: `INVALID_OVERRIDE`.

```java
@Test
void validateShouldWarnWhenScheduleRangeIsInvalid()
```

Expected warning code: `INVALID_OVERRIDE`.

```java
@Test
void validateShouldWarnWhenSuccessorStartsBeforePredecessorEnds()
```

Expected warning code: `DEPENDENCY_VIOLATION`.

Use helper construction with mocked `OptimizationProjectModel` and lightweight records. The validator tests should assert warning code, work item id, and message text.

- [ ] **Step 2: Add `DEPENDENCY_VIOLATION`**

Modify `OptimizationWarningCode`:

```java
DEPENDENCY_VIOLATION,
```

Update `GenerateOptimizationRunCommandHandler.severityOf`:

```java
case DEPENDENCY_CYCLE, DEPENDENCY_VIOLATION, NO_ELIGIBLE_ASSIGNEE, OVER_CAPACITY -> "ERROR";
```

- [ ] **Step 3: Run failing validator tests**

Run from `pm_core/`:

```bash
mvn -Dtest=OptimizationSolutionValidatorTest test
```

Expected: FAIL because `OptimizationSolutionValidator` does not exist or validation behavior is missing.

- [ ] **Step 4: Create `OptimizationSolutionValidator`**

Use this service shape:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.enums.OptimizationMode;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.OptimizationAssignmentSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.OptimizationProblem;
import serp.project.pmcore.domain.optimization.model.OptimizationScheduleSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationSolution;
import serp.project.pmcore.domain.optimization.model.OptimizationWorkItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OptimizationSolutionValidator {
    public OptimizationSolution validate(OptimizationProblem problem, OptimizationSolution solution) {
        List<OptimizationConstraintViolation> warnings = new ArrayList<>(solution.warnings());
        Map<Long, OptimizationWorkItem> itemById = problem.projectModel().workItems().stream()
                .collect(Collectors.toMap(item -> item.workItem().getId(), Function.identity()));

        validateAssignments(problem, solution.assignmentSuggestions(), itemById, warnings);
        validateSchedules(problem, solution.scheduleSuggestions(), warnings);

        return new OptimizationSolution(
                solution.assignmentSuggestions(),
                solution.scheduleSuggestions(),
                warnings,
                solution.summary(),
                solution.algorithm(),
                solution.solverStatus(),
                solution.objectiveScore()
        );
    }

    private void validateAssignments(OptimizationProblem problem,
                                     Map<Long, OptimizationAssignmentSuggestion> assignments,
                                     Map<Long, OptimizationWorkItem> itemById,
                                     List<OptimizationConstraintViolation> warnings) {
        boolean allowReassignment = Boolean.TRUE.equals(problem.input().allowReassignment())
                && problem.input().mode() != OptimizationMode.SCHEDULE_ONLY;
        for (Map.Entry<Long, OptimizationAssignmentSuggestion> entry : assignments.entrySet()) {
            OptimizationWorkItem item = itemById.get(entry.getKey());
            if (item == null) {
                warnings.add(new OptimizationConstraintViolation(
                        OptimizationWarningCode.INVALID_OVERRIDE,
                        entry.getKey(),
                        "Assignment suggestion references a work item outside the optimization problem",
                        null
                ));
                continue;
            }
            Long currentAssigneeId = item.workItem().getAssigneeId();
            Long suggestedAssigneeId = entry.getValue().suggestedAssigneeId();
            if (!allowReassignment && !Objects.equals(currentAssigneeId, suggestedAssigneeId)) {
                warnings.add(new OptimizationConstraintViolation(
                        OptimizationWarningCode.INVALID_OVERRIDE,
                        entry.getKey(),
                        "Assignment suggestion changes assignee while reassignment is disabled",
                        "currentAssigneeId=" + currentAssigneeId + ", suggestedAssigneeId=" + suggestedAssigneeId
                ));
            }
            Set<Long> candidateIds = item.candidateAssignees().stream()
                    .map(candidate -> candidate.candidateId())
                    .collect(Collectors.toSet());
            if (suggestedAssigneeId != null
                    && !candidateIds.contains(suggestedAssigneeId)
                    && !Objects.equals(currentAssigneeId, suggestedAssigneeId)) {
                warnings.add(new OptimizationConstraintViolation(
                        OptimizationWarningCode.INVALID_OVERRIDE,
                        entry.getKey(),
                        "Assignment suggestion uses an assignee outside the candidate set",
                        "suggestedAssigneeId=" + suggestedAssigneeId
                ));
            }
        }
    }

    private void validateSchedules(OptimizationProblem problem,
                                   Map<Long, OptimizationScheduleSuggestion> schedules,
                                   List<OptimizationConstraintViolation> warnings) {
        boolean allowScheduleChanges = Boolean.TRUE.equals(problem.input().allowScheduleChanges())
                && problem.input().mode() != OptimizationMode.ASSIGNMENT_ONLY;
        if (!allowScheduleChanges && !schedules.isEmpty()) {
            schedules.keySet().forEach(workItemId -> warnings.add(new OptimizationConstraintViolation(
                    OptimizationWarningCode.INVALID_OVERRIDE,
                    workItemId,
                    "Schedule suggestion exists while schedule changes are disabled",
                    null
            )));
        }
        schedules.forEach((workItemId, schedule) -> {
            if (schedule.plannedStart() == null
                    || schedule.plannedEnd() == null
                    || schedule.plannedStart() >= schedule.plannedEnd()) {
                warnings.add(new OptimizationConstraintViolation(
                        OptimizationWarningCode.INVALID_OVERRIDE,
                        workItemId,
                        "Schedule suggestion has an invalid planned range",
                        null
                ));
            }
            if (schedule.plannedStart() != null && schedule.plannedStart() < problem.projectModel().planningStart()) {
                warnings.add(new OptimizationConstraintViolation(
                        OptimizationWarningCode.INVALID_OVERRIDE,
                        workItemId,
                        "Schedule suggestion starts before the planning window",
                        "planningStart=" + problem.projectModel().planningStart()
                ));
            }
        });
        problem.projectModel().dependencyGraph().internalEdges().forEach(edge -> {
            OptimizationScheduleSuggestion predecessor = schedules.get(edge.predecessorId());
            OptimizationScheduleSuggestion successor = schedules.get(edge.successorId());
            if (predecessor != null
                    && successor != null
                    && successor.plannedStart() != null
                    && predecessor.plannedEnd() != null
                    && successor.plannedStart() < predecessor.plannedEnd()) {
                warnings.add(new OptimizationConstraintViolation(
                        OptimizationWarningCode.DEPENDENCY_VIOLATION,
                        edge.successorId(),
                        "Schedule suggestion starts before predecessor finishes",
                        edge.predecessorId() + " -> " + edge.successorId()
                ));
            }
        });
        problem.projectModel().earliestStartByWorkItemId().forEach((workItemId, earliestStart) -> {
            OptimizationScheduleSuggestion schedule = schedules.get(workItemId);
            if (schedule != null && schedule.plannedStart() != null && schedule.plannedStart() < earliestStart) {
                warnings.add(new OptimizationConstraintViolation(
                        OptimizationWarningCode.DEPENDENCY_VIOLATION,
                        workItemId,
                        "Schedule suggestion starts before external dependency earliest start",
                        "earliestStart=" + earliestStart
                ));
            }
        });
    }
}
```

This implementation assumes `OptimizationProblem` has `input()`. If it does not, first apply Task 3 Step 7.

- [ ] **Step 5: Wire validator into generate handler**

Add dependency:

```java
private final OptimizationSolutionValidator optimizationSolutionValidator;
```

Add import:

```java
import serp.project.pmcore.domain.optimization.service.OptimizationSolutionValidator;
```

After algorithm solve, add:

```java
solution = optimizationSolutionValidator.validate(new OptimizationProblem(projectModel, input), solution);
```

- [ ] **Step 6: Run focused tests**

Run from `pm_core/`:

```bash
mvn -Dtest=OptimizationSolutionValidatorTest,GenerateOptimizationRunCommandHandlerTest test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/OptimizationSolutionValidator.java pm_core/src/main/java/serp/project/pmcore/domain/optimization/enums/OptimizationWarningCode.java pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandler.java pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/OptimizationSolutionValidatorTest.java pm_core/src/test/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandlerTest.java
git commit -m "feat(pm): validate optimization algorithm solutions"
```

---

## PR 3: Fix Scheduling Accuracy

### Task 7: Add Allocated Effort To Schedule Suggestions

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationScheduleSuggestion.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGenerator.java`
- Modify: `pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGeneratorTest.java`

- [ ] **Step 1: Add failing test for multi-slot calendar window**

In `GreedyOptimizationRunGeneratorTest`, add a test that schedules one 10-hour item over two 8-hour daily slots. Assert:

```java
assertThat(schedule.allocatedEffortMillis()).isEqualTo(10 * OptimizationConstants.HOUR_MILLIS);
assertThat(schedule.plannedEnd() - schedule.plannedStart()).isGreaterThan(schedule.allocatedEffortMillis());
```

This test fails before `allocatedEffortMillis` exists.

- [ ] **Step 2: Run failing test**

Run from `pm_core/`:

```bash
mvn -Dtest=GreedyOptimizationRunGeneratorTest test
```

Expected: FAIL because `allocatedEffortMillis()` does not exist.

- [ ] **Step 3: Modify `OptimizationScheduleSuggestion`**

Add `allocatedEffortMillis` after `plannedEnd` in the record:

```java
public record OptimizationScheduleSuggestion(
        Long workItemId,
        Long assigneeId,
        Long plannedStart,
        Long plannedEnd,
        Long allocatedEffortMillis,
        OptimizationConfidence confidence,
        List<String> reasons,
        List<OptimizationConstraintViolation> violations
) {
}
```

- [ ] **Step 4: Update schedule creation**

In `GreedyOptimizationRunGenerator.generateSchedules`, replace schedule construction with:

```java
schedules.put(workItemId, new OptimizationScheduleSuggestion(
        workItemId,
        assigneeId,
        window.plannedStart(),
        window.plannedEnd(),
        item.duration().durationMillis(),
        confidence,
        reasons,
        violations
));
```

- [ ] **Step 5: Update all test constructors**

Search:

```bash
rg -n "new OptimizationScheduleSuggestion" pm_core/src
```

For every constructor call, insert effort between `plannedEnd` and `confidence`. In tests where effort is irrelevant, use:

```java
OptimizationConstants.HOUR_MILLIS
```

- [ ] **Step 6: Run focused tests**

Run from `pm_core/`:

```bash
mvn -Dtest=GreedyOptimizationRunGeneratorTest,GenerateOptimizationRunCommandHandlerTest,OptimizationSolutionValidatorTest test
```

Expected: PASS.

### Task 8: Fix Mid-Slot Capacity Allocation

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGenerator.java`
- Modify: `pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGeneratorTest.java`

- [ ] **Step 1: Add failing mid-slot test**

Add a test where:

- Slot is `0` to `8h`.
- Earliest start is `4h`.
- Duration is `6h`.
- Next slot is `24h` to `32h`.

Expected:

```java
assertThat(schedule.plannedStart()).isEqualTo(4 * OptimizationConstants.HOUR_MILLIS);
assertThat(schedule.allocatedEffortMillis()).isEqualTo(6 * OptimizationConstants.HOUR_MILLIS);
assertThat(schedule.plannedEnd()).isEqualTo(26 * OptimizationConstants.HOUR_MILLIS);
```

This proves the scheduler uses 4 hours from the first slot and 2 hours from the next slot, instead of treating all 8 hours in the first slot as available after `4h`.

- [ ] **Step 2: Run failing test**

Run from `pm_core/`:

```bash
mvn -Dtest=GreedyOptimizationRunGeneratorTest test
```

Expected: FAIL with an incorrect `plannedEnd`.

- [ ] **Step 3: Fix `findCapacityWindow` allocation math**

Inside the slot loop, replace availability calculation with:

```java
long slotAvailableStart = Math.max(slot.slotStart() + used, earliestStart);
if (slotAvailableStart >= slot.slotEnd()) {
    continue;
}
long available = Math.max(0L, slot.slotEnd() - slotAvailableStart);
long capacityRemaining = Math.max(0L, slot.capacityMillis() - used);
available = Math.min(available, capacityRemaining);
if (available == 0) {
    continue;
}
long chunk = Math.min(remaining, available);
if (plannedStart == null) {
    plannedStart = slotAvailableStart;
}
plannedEnd = slotAvailableStart + chunk;
usedBySlot.put(key, used + chunk);
remaining -= chunk;
```

Keep the fallback behavior for `remaining > 0`, but ensure it starts after the last allocated `plannedEnd`:

```java
if (remaining > 0) {
    plannedEnd = Math.max(plannedEnd, Math.max(earliestStart, planningEnd)) + remaining;
}
```

- [ ] **Step 4: Run focused test**

Run from `pm_core/`:

```bash
mvn -Dtest=GreedyOptimizationRunGeneratorTest test
```

Expected: PASS.

### Task 9: Fix Overload Summary To Use Effort

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGenerator.java`
- Modify: `pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGeneratorTest.java`

- [ ] **Step 1: Add failing overload summary test**

Add a test where one item spans two days because of calendar gaps but has effort below total capacity. Assert:

```java
assertThat(result.summary().getOverloadedAssigneeCountAfter()).isZero();
```

The current implementation may count overload by `plannedEnd - plannedStart`, which can exceed capacity because it includes non-working time.

- [ ] **Step 2: Run failing test**

Run from `pm_core/`:

```bash
mvn -Dtest=GreedyOptimizationRunGeneratorTest test
```

Expected: FAIL if overload is computed from calendar window length.

- [ ] **Step 3: Update `overloadedAssigneeCount`**

Replace this merge:

```java
schedule.plannedEnd() - schedule.plannedStart()
```

with:

```java
schedule.allocatedEffortMillis() == null
        ? 0L
        : schedule.allocatedEffortMillis()
```

Full method body should keep grouping by assignee and comparing against `totalCapacityByAssignee(projectModel.capacitySlots())`.

- [ ] **Step 4: Run focused tests**

Run from `pm_core/`:

```bash
mvn -Dtest=GreedyOptimizationRunGeneratorTest test
```

Expected: PASS.

- [ ] **Step 5: Commit scheduling accuracy changes**

```bash
git add pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationScheduleSuggestion.java pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGenerator.java pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGeneratorTest.java
git commit -m "fix(pm): measure optimization schedule effort accurately"
```

---

## PR 4: Split Greedy Internals And Add First Heuristic

### Task 10: Extract Assignment Policy

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/assignment/OptimizationAssignmentPolicy.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/assignment/GreedyAssignmentPolicy.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGenerator.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGeneratorTest.java`

- [ ] **Step 1: Create policy interface**

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.assignment;

import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmOptions;
import serp.project.pmcore.domain.optimization.model.OptimizationAssignmentSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;

import java.util.List;
import java.util.Map;

public interface OptimizationAssignmentPolicy {
    Map<Long, OptimizationAssignmentSuggestion> generateAssignments(
            OptimizationProjectModel projectModel,
            OptimizationAlgorithmOptions options,
            List<OptimizationConstraintViolation> warnings
    );
}
```

- [ ] **Step 2: Move assignment methods**

Create `GreedyAssignmentPolicy` by moving these methods from `GreedyOptimizationRunGenerator`:

- `generateAssignments`
- `chooseCandidate`
- `candidateCost`
- `skillCost`
- `addSkillReasons`
- `keepCurrentAssignment`
- `totalCapacityByAssignee`
- `CandidateCost`

Change `generateAssignments` visibility to public and match the interface signature. Convert input references from `OptimizationBuilderInput` to `OptimizationAlgorithmOptions` for mode and flags.

- [ ] **Step 3: Delegate from generator**

Inject `GreedyAssignmentPolicy` into `GreedyOptimizationRunGenerator` and replace the assignment step with:

```java
Map<Long, OptimizationAssignmentSuggestion> assignments = assignmentPolicy.generateAssignments(
        projectModel,
        new OptimizationAlgorithmOptions(
                input.algorithmKey(),
                input.mode(),
                input.allowReassignment(),
                input.allowScheduleChanges()
        ),
        warnings
);
```

- [ ] **Step 4: Run regression tests**

Run from `pm_core/`:

```bash
mvn -Dtest=GreedyOptimizationRunGeneratorTest test
```

Expected: PASS with no behavior change.

### Task 11: Extract Scheduling Policy And Summary Builder

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/schedule/OptimizationSchedulingPolicy.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/schedule/GreedySchedulingPolicy.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/summary/OptimizationSummaryBuilder.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGenerator.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGeneratorTest.java`

- [ ] **Step 1: Create scheduling interface**

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.schedule;

import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmOptions;
import serp.project.pmcore.domain.optimization.model.OptimizationAssignmentSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;
import serp.project.pmcore.domain.optimization.model.OptimizationScheduleSuggestion;

import java.util.List;
import java.util.Map;

public interface OptimizationSchedulingPolicy {
    Map<Long, OptimizationScheduleSuggestion> generateSchedules(
            OptimizationProjectModel projectModel,
            OptimizationAlgorithmOptions options,
            Map<Long, OptimizationAssignmentSuggestion> assignments,
            List<OptimizationConstraintViolation> warnings
    );
}
```

- [ ] **Step 2: Move scheduling methods**

Create `GreedySchedulingPolicy` by moving these methods from `GreedyOptimizationRunGenerator`:

- `generateSchedules`
- `capacityReasons`
- `readyQueue`
- `remainingPredecessors`
- `markSuccessorsReady`
- `scheduleComparator`
- `resolveScheduleAssignee`
- `earliestStart`
- `findCapacityWindow`
- `confidenceFor`
- `SlotKey`
- `ScheduleWindow`

Change `generateSchedules` visibility to public and match the interface signature. Convert input references from `OptimizationBuilderInput` to `OptimizationAlgorithmOptions`.

- [ ] **Step 3: Create summary builder**

Move these methods from `GreedyOptimizationRunGenerator` into `OptimizationSummaryBuilder`:

- `buildSummary`
- `itemsWithSkillRequirements`
- `itemsMissingSkillRequirements`
- `candidatesWithSkillProfiles`
- `candidatesMissingSkillProfiles`
- `requiredSkillMismatchCount`
- `skillRankingConfidence`
- `selectedCandidateSkillFits`
- `lateItemsBefore`
- `lateItemsAfter`
- `overloadedAssigneeCount`
- `confidenceLevel`
- `totalCapacityByAssignee` if it was not already moved to a shared helper

Expose:

```java
public OptimizationRunSummary buildSummary(
        OptimizationProjectModel projectModel,
        Map<Long, OptimizationAssignmentSuggestion> assignments,
        Map<Long, OptimizationScheduleSuggestion> schedules,
        List<OptimizationConstraintViolation> warnings
)
```

- [ ] **Step 4: Delegate from generator**

`GreedyOptimizationRunGenerator.generate` should become:

```java
List<OptimizationConstraintViolation> warnings = new ArrayList<>(projectModel.warnings());
OptimizationAlgorithmOptions options = new OptimizationAlgorithmOptions(
        input.algorithmKey(),
        input.mode(),
        input.allowReassignment(),
        input.allowScheduleChanges()
);
Map<Long, OptimizationAssignmentSuggestion> assignments = assignmentPolicy.generateAssignments(projectModel, options, warnings);
Map<Long, OptimizationScheduleSuggestion> schedules = schedulingPolicy.generateSchedules(projectModel, options, assignments, warnings);
OptimizationRunSummary summary = summaryBuilder.buildSummary(projectModel, assignments, schedules, warnings);
return new OptimizationGenerationResult(assignments, schedules, warnings, summary);
```

- [ ] **Step 5: Run regression tests**

Run from `pm_core/`:

```bash
mvn -Dtest=GreedyOptimizationRunGeneratorTest,OptimizationSolutionValidatorTest,GenerateOptimizationRunCommandHandlerTest test
```

Expected: PASS.

### Task 12: Add `greedy-skill-first` Algorithm

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl/GreedySkillFirstOptimizationAlgorithm.java`
- Modify: assignment policy if weighting needs a mode object.
- Test: `pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/impl/OptimizationAlgorithmRegistryTest.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGeneratorTest.java`

- [ ] **Step 1: Add registry test for skill-first**

In `OptimizationAlgorithmRegistryTest`, add:

```java
@Test
void resolveShouldReturnSkillFirstAlgorithmByKey() {
    IOptimizationAlgorithm algorithm = stubAlgorithm(OptimizationAlgorithmKeys.GREEDY_SKILL_FIRST);
    OptimizationAlgorithmRegistry registry = new OptimizationAlgorithmRegistry(List.of(algorithm));

    IOptimizationAlgorithm resolved = registry.resolve(OptimizationAlgorithmKeys.GREEDY_SKILL_FIRST);

    assertThat(resolved).isSameAs(algorithm);
}
```

- [ ] **Step 2: Create `GreedySkillFirstOptimizationAlgorithm`**

Use the same structure as `GreedyBalancedOptimizationAlgorithm`, but descriptor key is `GREEDY_SKILL_FIRST`. The first version can delegate to the same generator and rely on existing skill cost behavior:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.impl;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.enums.OptimizationCapability;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmDescriptor;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmOptions;
import serp.project.pmcore.domain.optimization.model.OptimizationProblem;
import serp.project.pmcore.domain.optimization.model.OptimizationSolution;
import serp.project.pmcore.domain.optimization.service.IOptimizationAlgorithm;

import java.util.Set;

@Service
public class GreedySkillFirstOptimizationAlgorithm implements IOptimizationAlgorithm {
    private final GreedyOptimizationRunGenerator greedyOptimizationRunGenerator;

    public GreedySkillFirstOptimizationAlgorithm(GreedyOptimizationRunGenerator greedyOptimizationRunGenerator) {
        this.greedyOptimizationRunGenerator = greedyOptimizationRunGenerator;
    }

    @Override
    public OptimizationAlgorithmDescriptor descriptor() {
        return new OptimizationAlgorithmDescriptor(
                OptimizationAlgorithmKeys.GREEDY_SKILL_FIRST,
                OptimizationAlgorithmKeys.DEFAULT_VERSION,
                Set.of(
                        OptimizationCapability.ASSIGNMENT,
                        OptimizationCapability.SCHEDULING,
                        OptimizationCapability.CAPACITY_AWARE,
                        OptimizationCapability.SKILL_AWARE,
                        OptimizationCapability.DEPENDENCY_AWARE
                )
        );
    }

    @Override
    public OptimizationSolution solve(OptimizationProblem problem, OptimizationAlgorithmOptions options) {
        return OptimizationSolution.fromGenerationResult(
                greedyOptimizationRunGenerator.generate(problem.projectModel(), problem.input()),
                descriptor()
        );
    }
}
```

- [ ] **Step 3: Add weighting difference in a follow-up commit within this PR**

After registry support passes, add a small explicit distinction in assignment scoring:

```java
boolean skillFirst = OptimizationAlgorithmKeys.GREEDY_SKILL_FIRST.equals(options.algorithmKey());
double requiredMultiplier = skillFirst ? 1.5D : 1D;
double preferredMultiplier = skillFirst ? 1.25D : 1D;
```

Apply these multipliers in `skillCost`:

```java
cost -= skillFit.matchedRequiredSkillCount() * OptimizationConstants.REQUIRED_SKILL_MATCH_BONUS * requiredMultiplier;
cost -= skillFit.matchedPreferredSkillCount() * OptimizationConstants.PREFERRED_SKILL_MATCH_BONUS * preferredMultiplier;
```

Adjust method signature from:

```java
private double skillCost(OptimizationCandidateSkillFit skillFit)
```

to:

```java
private double skillCost(OptimizationCandidateSkillFit skillFit, OptimizationAlgorithmOptions options)
```

Pass `options` from `candidateCost`.

- [ ] **Step 4: Add behavior test**

Add a test where candidate A has lower role cost but missing required skills, and candidate B has higher role cost but matches required skills. For `greedy-skill-first`, assert candidate B is chosen.

Expected assertion:

```java
assertThat(result.assignmentSuggestions().get(workItemId).suggestedAssigneeId()).isEqualTo(skillMatchedCandidateId);
```

- [ ] **Step 5: Run focused tests**

Run from `pm_core/`:

```bash
mvn -Dtest=OptimizationAlgorithmRegistryTest,GreedyOptimizationRunGeneratorTest test
```

Expected: PASS.

- [ ] **Step 6: Commit split and heuristic work**

```bash
git add pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/assignment pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/schedule pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/summary pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGenerator.java pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl/GreedySkillFirstOptimizationAlgorithm.java pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/impl/OptimizationAlgorithmRegistryTest.java pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGeneratorTest.java
git commit -m "feat(pm): split greedy optimization policies and add skill-first heuristic"
```

---

## Final Verification

- [ ] **Step 1: Run focused optimization tests**

Run from `pm_core/`:

```bash
mvn -Dtest=GreedyOptimizationRunGeneratorTest,OptimizationProjectModelBuilderTest,OptimizationAlgorithmRegistryTest,OptimizationSolutionValidatorTest,GenerateOptimizationRunCommandHandlerTest,OptimizationRunReviewAssemblerTest,OptimizationFoundationMapperTest,ApplyOptimizationRunCommandHandlerTest,UpdateOptimizationRunItemDecisionCommandHandlerTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 2: Run full pm_core tests**

Run from `pm_core/`:

```bash
mvn clean test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Inspect git diff**

Run from repo root:

```bash
git status --short
git diff --stat
```

Expected: only intentional optimization, migration, and test changes remain.

---

## Self-Review Notes

Spec coverage:

- Multiple heuristic algorithms: covered by PR 1 contract and PR 4 `greedy-skill-first`.
- Solver-ready design: covered by descriptor, solver status, objective score, and solution contract.
- `pm_core` owns problem/model/validation/persistence/apply: covered by registry routing and validator placement.
- API/schema changes: covered by request, run metadata, mapper, migration, and review response tasks.
- Solution validation: covered by PR 2.
- Scheduling accuracy: covered by PR 3.
- Direct `mvn` verification: every command uses `mvn`, not Maven wrapper.

Known sequencing constraint:

- `OptimizationProblem` should store both `OptimizationProjectModel` and `OptimizationBuilderInput` because the current greedy generator still needs input. Use the two-field record when implementing Task 3.
