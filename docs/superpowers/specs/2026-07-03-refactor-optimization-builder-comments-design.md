# Refactor Optimization Builder Comments

Design specification to refactor, clean up, and document `IOptimizationProjectModelBuilder` and `OptimizationProjectModelBuilder` following Java best practices.

## User Review Required

No critical/breaking changes. The changes are strictly cosmetic (comments and JavaDoc improvements) and do not modify runtime behavior or business logic.

## Proposed Changes

### Java Optimization Component

#### [MODIFY] [IOptimizationProjectModelBuilder.java](file:///d:/User2/open_source/serp/pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/IOptimizationProjectModelBuilder.java)
- Add complete interface-level JavaDoc.
- Add method-level Javadoc for the `build` method specifying parameters, return values, and exceptions.

#### [MODIFY] [OptimizationProjectModelBuilder.java](file:///d:/User2/open_source/serp/pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl/OptimizationProjectModelBuilder.java)
- Add class-level Javadoc explaining the aggregation steps.
- Strip redundant inline comments that duplicate self-documenting code.
- Enhance/rewrite comments for complex logic (DFS cycle detection, Kahn's topological sort, critical path calculations, priority score weight math).

---

## Verification Plan

### Automated Tests
- Run the maven package/compile check on `pm_core` module to verify that no compilation errors were introduced:
  ```bash
  mvnw.cmd clean compile
  ```
- Run tests on `pm_core` module:
  ```bash
  mvnw.cmd test
  ```
