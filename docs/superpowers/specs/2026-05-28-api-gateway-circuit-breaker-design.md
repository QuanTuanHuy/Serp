# API Gateway Circuit Breaker Design

## Context

`api_gateway` proxies HTTP traffic through a resilient transport that combines retry and circuit breaker behavior. The current reverse proxy chain is `Retry -> CircuitBreaker -> BaseTransport`, so every upstream retry attempt is counted by the circuit breaker.

With `maxRetries: 3` and `consecutiveFailures: 5`, a single failing idempotent gateway request can count as four breaker failures. Two user requests can therefore open the circuit, which is too sensitive for production traffic.

## Decision

The proxy circuit breaker must count one failure or success per logical user request, after the retry policy has finished.

The transport chain should become:

```text
CircuitBreaker -> Retry -> BaseTransport
```

The retry transport remains responsible for retrying only idempotent HTTP methods. The circuit breaker wraps that full retry cycle and records the final result once.

## Behavior

- `GET`, `HEAD`, `PUT`, `DELETE`, and `OPTIONS` may still retry according to `maxRetries`, `initialDelay`, and `maxDelay`.
- `POST`, `PATCH`, and other non-idempotent methods still do not retry.
- HTTP 4xx responses do not count as circuit breaker failures.
- HTTP 5xx responses count as circuit breaker failures only after the final allowed attempt.
- Network or transport errors count as circuit breaker failures only after retries are exhausted.
- When the circuit is open, the proxy fails fast with the existing 503 response and does not call upstream.
- WebSocket proxy behavior remains retry-free and circuit-breaker-aware.

## Testing

Update the focused reverse proxy circuit breaker regression test:

- With default `consecutiveFailures: 5` and `maxRetries: 3`, the first four failing `GET` user requests should return upstream 500 and continue reaching upstream.
- The fifth failing `GET` user request should still reach upstream, return 500, and open the circuit.
- The sixth `GET` user request should return 503 from the proxy error handler without hitting upstream.
- The existing `POST` no-retry test should continue to assert one upstream hit for one failing non-idempotent request.

Run focused tests first, then the package test:

```bash
go test ./src/ui/controller/common -run '^TestGenericProxyController_CRM_CircuitBreakerCountsLogicalRequestsAfterRetries$' -count=1
go test ./src/ui/controller/common -run '^TestGenericProxyController_CRM_POSTDoesNotRetry$' -count=1
go test ./src/ui/controller/common -count=1
```
