# API Gateway Circuit Breaker Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the HTTP proxy circuit breaker count one result per logical user request after retries finish.

**Architecture:** Keep retry behavior in `RetryTransport`, but move it inside the breaker boundary by constructing `CircuitBreakerTransport(RetryTransport(DefaultTransport))`. `CircuitBreakerTransport` will continue translating final 5xx responses into breaker failures while returning the upstream response to the caller.

**Tech Stack:** Go, Gin reverse proxy tests, `net/http`, `httptest`, `github.com/sony/gobreaker/v2`.

---

### Task 1: Reverse Proxy Regression Test

**Files:**
- Modify: `api_gateway/src/ui/controller/common/generic_proxy_controller_test.go`

- [ ] **Step 1: Write the failing test**

Replace `TestGenericProxyController_CRM_CircuitBreakerOpensAfter5xxWithRetries` with a test named `TestGenericProxyController_CRM_CircuitBreakerCountsLogicalRequestsAfterRetries`.

The test sends five failing `GET` requests and asserts each still returns 500 from upstream. It then sends a sixth `GET` request and asserts the proxy returns 503 without increasing upstream hit count.

- [ ] **Step 2: Run test to verify it fails**

Run from `api_gateway`:

```bash
go test ./src/ui/controller/common -run '^TestGenericProxyController_CRM_CircuitBreakerCountsLogicalRequestsAfterRetries$' -count=1
```

Expected: FAIL before implementation because the existing transport opens the circuit after retry attempts are counted individually.

### Task 2: Transport Chain Fix

**Files:**
- Modify: `api_gateway/src/kernel/utils/http_transport.go`

- [ ] **Step 1: Implement minimal transport chain change**

Change `NewResilientTransport` from `Retry -> CircuitBreaker -> Base` to `CircuitBreaker -> Retry -> Base`:

```go
retryTransport := NewRetryTransport(http.DefaultTransport, maxRetries, initialDelay, maxDelay)
cbTransport := NewCircuitBreakerTransport(retryTransport, cb)

return &ResilientTransport{
	transport: cbTransport,
}
```

Update the nearby comment to match the new chain.

- [ ] **Step 2: Run the failing test again**

Run from `api_gateway`:

```bash
go test ./src/ui/controller/common -run '^TestGenericProxyController_CRM_CircuitBreakerCountsLogicalRequestsAfterRetries$' -count=1
```

Expected: PASS.

### Task 3: Regression Sweep

**Files:**
- Verify: `api_gateway/src/ui/controller/common/generic_proxy_controller_test.go`
- Verify: `api_gateway/src/kernel/utils/http_transport.go`

- [ ] **Step 1: Verify POST remains non-retry**

Run from `api_gateway`:

```bash
go test ./src/ui/controller/common -run '^TestGenericProxyController_CRM_POSTDoesNotRetry$' -count=1
```

Expected: PASS with exactly one upstream hit for the failing POST.

- [ ] **Step 2: Format and run package tests**

Run from `api_gateway`:

```bash
go fmt ./...
go test ./src/ui/controller/common -count=1
```

Expected: PASS.
