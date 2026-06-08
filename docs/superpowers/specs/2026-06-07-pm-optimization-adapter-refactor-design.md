# PM Optimization Adapter Refactor Design

## Goal

Refactor PM Core optimization capacity/calendar code so infrastructure adapters only handle persistence, while calendar fallback, coverage, and capacity workload rules live in the optimization domain.

## Design

Move persistence-facing optimization adapters into `infrastructure.store.adapter`, matching the rest of `pm_core`. These adapters should call Spring Data repositories and translate persistence models into domain-facing read models or entities. They should not orchestrate fallback calendars, coverage status, workload deduction, or warning generation.

Introduce domain service interfaces under `domain.optimization.service` with implementations under `domain.optimization.service.impl` for the two behaviors currently mixed into infrastructure:

- `ResourceCalendarService` resolves working capacity by reading real calendar slots through a port, generating fallback weekday capacity for missing users, merging slots, and calculating coverage status.
- `ResourceCapacityService` resolves net capacity by calling the calendar service, reading existing planned/unplanned workload through persistence ports, deducting workload from available capacity, and producing workload buckets and warnings.

The domain exposes `IResourceCalendarService` and `IResourceCapacityService` for callers such as `OptimizationProjectModelBuilder`. Infrastructure implements only lower-level read ports for real calendar slots and workload data. This removes infrastructure-to-domain-port orchestration while preserving existing behavior.

## Boundaries

- Store adapters may import domain ports and domain models to implement persistence seams.
- Store adapters must not depend on other domain ports for orchestration.
- Domain behavior should be consumed through service interfaces, not `port` interfaces.
- Fallback weekday calendar generation is a domain policy, not a persistence adapter.
- Existing persistence repositories and schemas stay unchanged.
- Existing optimization behavior, warnings, and capacity source modes should remain compatible.

## Testing

- Move calendar behavior tests from infrastructure adapter tests to domain service tests.
- Keep a focused store adapter test for mapping and repository delegation.
- Move capacity workload deduction behavior tests from infrastructure adapter tests to domain service tests.
- Run the focused optimization tests before handoff.
