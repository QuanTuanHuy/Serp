# API Gateway Performance Design

## Context

`api_gateway` is the central HTTP and WebSocket gateway for SERP services. The
current implementation uses Go, Gin, Uber FX, Redis rate limiting, JWT/JWKS
validation, `httputil.ReverseProxy`, and downstream client adapters.

The performance work should focus first on two hot paths:

1. Generic reverse proxy routes, such as CRM, purchase, logistics, sales,
   pm-core, and similar pass-through traffic.
2. Custom controller and adapter routes, such as account, PTM, and CRM routes
   that bind requests, call `BaseAPIClient`, read and unmarshal downstream
   responses, then return the gateway response.

JWT, Redis rate limiting, and WebSocket proxy behavior are still relevant, but
they are secondary unless benchmark results show that they are material to p95,
p99, or throughput for the selected hot paths.

## Current Observations

- `GenericProxyController` pre-builds per-service reverse proxies and uses a
  resilient transport with retry and circuit breaker behavior.
- `BaseAPIClient` uses `http.Client{Timeout: timeout}` with the default
  transport behavior and reads downstream response bodies fully before
  unmarshalling.
- Retry currently buffers request bodies when needed; this is correct for
  retryability, but it can become expensive for large request bodies.
- JWT middleware validates the same token multiple times during a single
  authenticated request when it validates claims, checks token type, and extracts
  roles.
- Redis-backed rate limiting uses multiple Redis commands in a pipeline for each
  checked request.
- Production config has resilience and service timeout settings, but transport
  pool settings are not yet explicit.

## Goals

- Reduce p95 and p99 latency added by the gateway.
- Increase request throughput for generic proxy and custom adapter hot paths.
- Establish a repeatable baseline before optimization, using mock upstreams to
  isolate gateway overhead from downstream service behavior.
- Measure middleware overhead layer by layer: minimal path, IP rate limit, JWT,
  and user rate limit where applicable.
- Keep behavioral correctness for path rewriting, query preservation, header
  forwarding, retry behavior, circuit breaker behavior, and downstream response
  shapes.
- Keep production defaults conservative and configurable.

## Non-Goals

- Do not redesign the gateway routing model in this phase.
- Do not replace Gin, FX, Redis, gobreaker, or `httputil.ReverseProxy`.
- Do not optimize WebSocket proxying first unless benchmarks or production
  symptoms show it is the primary bottleneck.
- Do not depend on real downstream services for the first baseline.
- Do not introduce hard pass/fail performance thresholds until a trustworthy
  baseline exists.

## Recommended Approach

Use a measurement-first loop:

1. Add Go-native benchmark harnesses with mock upstream services.
2. Capture baseline numbers for generic proxy and custom adapter hot paths.
3. Add middleware-layer benchmark variants to identify the cost of rate limiting
   and JWT validation separately.
4. Optimize the largest measured bottlenecks first.
5. Compare before and after each change using benchmark output.

This keeps the work grounded in gateway-specific measurements rather than
guesswork.

## Benchmark Architecture

Benchmarks should live under `api_gateway` and run with `go test -bench`.
They should use `httptest.Server` for mock upstreams and Gin test mode for the
gateway router.

### Generic Proxy Benchmarks

Scenarios:

- GET with small response body.
- GET with medium response body.
- POST with small request body.
- POST with medium request body.
- Query string preservation.
- Header forwarding and path rewrite path.

Measurements:

- `ns/op`
- `B/op`
- `allocs/op`
- Optional p50, p95, p99, and RPS from a separate load harness if added later.

### Custom Adapter Benchmarks

Scenarios:

- Controller -> service -> adapter -> `BaseAPIClient` -> mock upstream ->
  unmarshal response.
- Small JSON response.
- Medium JSON response.
- POST/PUT request body marshal and downstream response unmarshal.

Measurements:

- `ns/op`
- `B/op`
- `allocs/op`
- Response body size sensitivity.

### Middleware Layering

Each representative scenario should have variants:

- Minimal path without JWT and rate limit where possible.
- IP rate limit enabled.
- JWT authentication enabled.
- JWT plus user rate limit enabled where routes normally use it.

The intent is to identify which middleware layer changes latency and allocations
before deciding whether to optimize Redis rate limiting, JWT parsing, or the
core proxy/adapter path.

## Optimization Scope

### Generic Reverse Proxy

- Add an explicit, configurable upstream `http.Transport` instead of relying on
  `http.DefaultTransport`.
- Tune connection pooling through settings such as `MaxIdleConns`,
  `MaxIdleConnsPerHost`, `MaxConnsPerHost`, `IdleConnTimeout`,
  `ResponseHeaderTimeout`, and `ExpectContinueTimeout`.
- Keep response streaming behavior for proxy routes.
- Preserve retry behavior only for idempotent methods.
- Avoid buffering large request bodies unless retry behavior requires it.
- Keep circuit breaker behavior: network errors and 5xx responses count as
  failures, while 4xx responses do not.

### Custom Controller and Adapter Path

- Give `BaseAPIClient` a shared or configurable transport pool.
- Preserve context propagation and existing service timeout settings.
- Keep full-body reads for stable DTO responses, but document and avoid this
  pattern for export, file, or other large streaming responses.
- Reduce per-request `INFO` logging if benchmarks show logging materially hurts
  throughput.
- Prefer generic proxy routes for pure pass-through endpoints that do not need
  gateway validation, request shaping, or response shaping.

### Middleware Hot Path

- Measure Redis rate limit cost before changing the algorithm.
- If Redis is material, consider reducing round trips with a Lua script or a
  carefully bounded local short-window burst cache.
- If JWT is material, parse and validate once per request, then reuse claims and
  roles from Gin context.
- Do not weaken authentication, authorization, or rate limiting semantics for
  performance.

## Production-Grade Performance Requirements

### Timeout Budget

- Gateway requests must not wait on upstream services indefinitely.
- Dial, TLS handshake, response header, idle connection, and full request
  timeouts should be explicit where applicable.
- Retry delays must fit inside the request's effective latency budget.

### Connection Management

- Connections should be pooled and reused per upstream service.
- `MaxConnsPerHost` should prevent the gateway from overwhelming a slow
  downstream service.
- Idle connection settings should balance reuse with avoiding stale sockets.

### Retry and Circuit Breaker Semantics

- Retry only idempotent methods.
- Do not retry WebSocket upgrades.
- Treat network errors and 5xx responses as circuit breaker failures.
- Treat 4xx responses as completed downstream responses, not infrastructure
  failures.
- When the circuit breaker is open, fail fast with a clear 503 response.

### Backpressure

- The gateway should fail fast or shed work when an upstream service is slow or
  unhealthy instead of accumulating unbounded in-flight requests.
- Transport pool limits, request timeouts, circuit breakers, and rate limits are
  the first backpressure controls for this phase.

### Observability

The gateway should expose enough data to explain performance changes:

- Request count by route, service, method, and status.
- Gateway latency histogram.
- Upstream duration histogram.
- Error count by service and error class.
- Retry count.
- Circuit breaker state changes.
- Rate limit allow/deny counts.

Metrics instrumentation can be implemented after the first benchmark phase if it
would require a new dependency or broader operational design.

## Verification Strategy

### Baseline

- Run existing focused tests for generic proxy behavior.
- Run new benchmark harnesses for generic proxy and custom adapter paths.
- Capture benchmark output before code optimization.

### Behavioral Tests

Keep or add tests for:

- Path rewriting.
- Query string preservation.
- Header forwarding.
- Retry only for idempotent methods.
- No retry for non-idempotent methods.
- Circuit breaker fail-fast behavior.
- Timeout behavior if transport settings change request behavior.

### Benchmark Comparison

For each optimization:

- Run the same benchmark before and after the change.
- Compare `ns/op`, `B/op`, and `allocs/op`.
- Keep changes only when performance improves or production safety clearly
  improves without measurable regression.

Suggested commands from `api_gateway/`:

```bash
go test ./src/ui/controller/common -run '^Test' -count=1
go test ./src/ui/controller/common -bench . -benchmem -run '^$' -count=5
go test ./... -count=1
go vet ./...
```

The exact package list may change when the benchmark harness is added.

## Rollout Plan

1. Add benchmark harnesses for generic proxy and custom adapter hot paths.
2. Record baseline results.
3. Add explicit configurable transport pooling for proxy and adapter paths.
4. Re-run benchmarks and compare before/after results.
5. Tune retry, timeout, and body buffering behavior based on measured results.
6. Measure middleware overhead and optimize JWT or Redis rate limiting only if
   the data supports it.
7. Keep configuration defaults conservative and document production tuning
   guidance.

## Implementation Planning Decisions

- The first benchmark harness should live in the nearest existing packages:
  generic proxy benchmarks in `src/ui/controller/common`, `BaseAPIClient`
  benchmarks in `src/kernel/utils`, and adapter-specific benchmarks in the
  relevant adapter package only when that adapter is being optimized.
- Transport pool settings should use a new transport-specific properties struct
  under `app.transport` rather than expanding resilience settings. Resilience
  remains responsible for retry and circuit breaker behavior.
- First-phase measurement should use Go benchmark output: `ns/op`, `B/op`, and
  `allocs/op`. Load-test style p95, p99, and RPS reporting should be added only
  after the Go benchmark baseline identifies which hot path needs broader
  concurrency testing.
