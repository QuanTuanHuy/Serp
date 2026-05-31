# school_bus_service — Coding Rules

## Service Architecture

1. **Table-backed services** must `extend AbstractBaseService<OwnEntity, Long>` and only inject **their own** repository.
2. **Domain/policy services** (no table ownership) go in `service/domain/`, do not extend `AbstractBaseService`.
3. **Algorithm services** go in `service/algorithm/`.
4. No `@RequiredArgsConstructor` — use explicit constructors with field injection.
5. **No direct cross-table repository injection** in a service — inject the service interface for that table instead.
6. Use `@Lazy` on injections that would create circular Spring dependencies.
7. Methods return DTOs/responses; entity returns are allowed only for `internal` methods (called between services).
8. Reuse base methods from `AbstractBaseService` where possible (`findById`, `softDeleteById`, etc.).

## Enum Parsing Rule

**Enums that need String → Enum conversion must define a static `parse(String)` method on the enum itself.**

```java
public enum MyEnum {
    VALUE_A,
    VALUE_B;

    public static MyEnum parse(String value) {
        try { return valueOf(value == null ? "" : value.toUpperCase()); }
        catch (IllegalArgumentException e) {
            throw new AppException(AppErrorCode.INVALID_REQUEST, "Invalid myEnum: " + value);
        }
    }
}
```

- **Do NOT** create private `parseXxx(String)` helper methods in service classes.
- Call `MyEnum.parse(value)` directly at the call site.
- If `null`/blank should return `null` instead of throwing, add a separate `parseNullable(String)` method to the enum (see `TripOption.parseNullable`).
- Enums with `parse()`: `RouteDirection`, `PlanningMethod`, `TripOption`, `SubscriptionStatus`, `RequestType`, `TripStatus`, `RouteLocationType`, `ShiftType`.

## Repository JPQL Enum Parameters

When a JPQL `@Query` compares against an enum-typed field, the `@Param` type **must** be the enum type, not `String`.

```java
// WRONG
@Param("direction") String direction

// CORRECT
@Param("direction") RouteDirection direction
```

Pass a parsed enum value at the call site: `repository.findBy(..., RouteDirection.parse(req.getRouteDirection()))`.
