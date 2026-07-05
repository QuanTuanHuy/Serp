# API Gateway Load Balancer Support Design

## Goal

Design `api_gateway` so it can work in a production load-balanced topology:

```text
Client
-> External Load Balancer / Ingress
-> api_gateway instance 1..N
-> api_gateway downstream load balancer
-> downstream service instance 1..N
```

The first implementation phase should cover:

- North-south load balancing in front of multiple API Gateway instances.
- East-west load balancing from API Gateway to multiple downstream instances for Generic Proxy HTTP routes.

## Current State

`api_gateway` currently assumes one configured target per downstream service:

- `ServiceProperty` has `Host`, `Port`, `Timeout`, and `WebSocketPath`.
- `ServiceProperty.BaseURL()` builds one URL from `host` and `port`.
- `GenericProxyController` builds one `httputil.ReverseProxy` per service using `httputil.NewSingleHostReverseProxy`.
- `WebSocketProxyController` also selects one target URL per WebSocket service.
- `BaseAPIClient` callers use one base URL per adapter.
- `/actuator/health` and `/actuator/info` already exist.

This design changes the service target model from one target to explicit instances, while limiting the first runtime load-balancing change to Generic Proxy HTTP routes.

## Decisions

Use a two-layer load-balancing design:

- North-south: an external load balancer or ingress sends traffic to multiple API Gateway instances.
- East-west: API Gateway chooses among multiple instances of each downstream service.

Use static configuration first:

- No service registry in phase one.
- No DNS refresh logic in phase one.
- Downstream instances are listed directly in YAML.

Use round-robin plus active and passive health checks:

- Round-robin selects among healthy instances.
- Active health checks periodically call each instance health endpoint.
- Passive health checks observe real proxy traffic and temporarily eject instances after repeated failures.

Use a configurable all-unhealthy policy:

- Default: fail fast with `503`.
- Per service: `allowUnhealthyFallback: true` can route to an unhealthy instance as a last resort.

Use gateway-only readiness:

- Gateway readiness should verify gateway startup state, not downstream health.
- Downstream failures should affect routing and metrics, not remove every API Gateway instance from the external load balancer.

## Phase Scope

### In Scope

- Add explicit service instances in config.
- Migrate `external.services.*` away from single `host` and `port`.
- Add Generic Proxy HTTP load balancing.
- Add active and passive instance health state.
- Add gateway readiness endpoint.
- Add trusted forwarded-header handling for deployments behind an external load balancer.
- Keep BaseAPIClient and WebSocket functional by using the first configured instance as their primary URL.

### Out of Scope

- Service discovery registry such as Consul, Eureka, or Kubernetes API.
- DNS-based endpoint refresh.
- Weighted round-robin.
- Least-in-flight instance selection.
- WebSocket downstream load balancing.
- BaseAPIClient downstream load balancing.
- Deployment drain and graceful rollout state.
- Full metrics implementation, unless metrics already exist in the gateway stack.

## Configuration Model

Move downstream target configuration to `instances`.

Example:

```yaml
external:
  services:
    accountService:
      timeout: 15s
      healthPath: /actuator/health
      webSocketPath:
      loadBalancer:
        algorithm: round-robin
        activeHealthCheck:
          enabled: true
          interval: 10s
          timeout: 2s
          unhealthyThreshold: 2
          healthyThreshold: 1
        passiveHealthCheck:
          enabled: true
          consecutiveFailures: 3
          ejectionTime: 30s
        allowUnhealthyFallback: false
      instances:
        - name: account-1
          url: http://account-1:8080
        - name: account-2
          url: http://account-2:8080
```

Recommended property shape:

```go
type ServiceInstanceProperty struct {
    Name string `mapstructure:"name"`
    URL  string `mapstructure:"url"`
}

type ActiveHealthCheckProperty struct {
    Enabled            bool          `mapstructure:"enabled"`
    Interval           time.Duration `mapstructure:"interval"`
    Timeout            time.Duration `mapstructure:"timeout"`
    UnhealthyThreshold int           `mapstructure:"unhealthyThreshold"`
    HealthyThreshold   int           `mapstructure:"healthyThreshold"`
}

type PassiveHealthCheckProperty struct {
    Enabled             bool          `mapstructure:"enabled"`
    ConsecutiveFailures int           `mapstructure:"consecutiveFailures"`
    EjectionTime        time.Duration `mapstructure:"ejectionTime"`
}

type ServiceLoadBalancerProperty struct {
    Algorithm                string                     `mapstructure:"algorithm"`
    ActiveHealthCheck        ActiveHealthCheckProperty  `mapstructure:"activeHealthCheck"`
    PassiveHealthCheck       PassiveHealthCheckProperty `mapstructure:"passiveHealthCheck"`
    AllowUnhealthyFallback   bool                       `mapstructure:"allowUnhealthyFallback"`
}

type ServiceProperty struct {
    Timeout      time.Duration                  `mapstructure:"timeout"`
    HealthPath   string                         `mapstructure:"healthPath"`
    WebSocketPath string                        `mapstructure:"webSocketPath"`
    LoadBalancer ServiceLoadBalancerProperty    `mapstructure:"loadBalancer"`
    Instances    []ServiceInstanceProperty      `mapstructure:"instances"`
}
```

Phase one should only accept `algorithm: round-robin`.

If `healthPath` is omitted and active health checks are enabled, use `/actuator/health` as the default health path.

## Configuration Validation

Fail fast during startup when config is invalid:

- A service used by the gateway has no `instances`.
- An instance has an empty name.
- An instance has an empty or invalid URL.
- Instance names are duplicated within the same service.
- Instance URL scheme is not `http` or `https`.
- `loadBalancer.algorithm` is anything other than `round-robin`.
- `healthPath` is empty after defaults are applied while active health checks are enabled.
- Active health interval, timeout, or thresholds are invalid when active health is enabled.
- Passive failure threshold or ejection duration is invalid when passive health is enabled.

Because the decision is to migrate fully to `instances`, `host` and `port` should not be required for runtime routing after this change.

## Routing Architecture

Introduce a small downstream load-balancing component for Generic Proxy HTTP.

Suggested concepts:

```go
type ServiceInstance struct {
    Name string
    URL  *url.URL
}

type ServiceLoadBalancer interface {
    Next() (*ServiceInstance, error)
    Report(instanceName string, statusCode int, err error)
    Start(ctx context.Context)
}
```

`GenericProxyController` should keep service route metadata, but target selection moves from construction time to request time.

Request flow:

```text
Gin route
-> GenericProxyController.ProxyHandler(serviceName)
-> service load balancer selects instance
-> selected instance is stored on request context
-> ReverseProxy director rewrites target host, scheme, path, and proxy headers
-> transport forwards request
-> response or error updates passive health
```

The path rewrite rules stay the same:

- `SourcePrefix` is matched against the public gateway path.
- `TargetPrefix` replaces the source prefix.
- Query strings, method, body, and Authorization header are preserved.

The selected instance should be placed on request context before calling the reverse proxy, so `Director`, `ModifyResponse`, and `ErrorHandler` can all refer to the same target.

## Round-Robin Selection

Round-robin should select only healthy instances by default:

1. Build the current candidate list from healthy instances.
2. If the candidate list is not empty, choose the next instance by atomic counter.
3. If no candidate is healthy and `allowUnhealthyFallback` is false, return no target and respond `503`.
4. If no candidate is healthy and `allowUnhealthyFallback` is true, choose from all configured instances by round-robin.

The load balancer must be safe for concurrent requests.

## Health Model

Each instance should track:

- Name and URL.
- Current health state.
- Consecutive active successes.
- Consecutive active failures.
- Consecutive passive failures.
- Ejection deadline for passive ejection.
- Last state change time.

Instances start as healthy after config validation. Active and passive health checks can then move them to unhealthy. This preserves startup availability and avoids a cold start where the gateway returns `503` before the first active health cycle completes.

### Active Health Check

When enabled, the gateway periodically calls:

```text
instance.url + service.healthPath
```

Behavior:

- HTTP 2xx counts as success.
- Network error, timeout, or non-2xx counts as failure.
- After `unhealthyThreshold` failures, mark instance unhealthy.
- After `healthyThreshold` successes, mark instance healthy again.
- Health check calls use a small timeout independent of normal proxy request timeout.

### Passive Health Check

When enabled, real proxy traffic updates instance health:

- Network errors count as passive failures.
- HTTP 5xx responses count as passive failures.
- HTTP 2xx, 3xx, and 4xx reset passive failure count.
- After `consecutiveFailures`, mark instance unhealthy until `ejectionTime` elapses.

Passive health should not hide upstream responses. For example, if an instance returns HTTP 500, the gateway should still return that upstream response while recording the passive failure.

## Circuit Breaker Interaction

The existing circuit breaker can remain service-scoped in phase one.

The new instance health model does not replace the circuit breaker:

- Circuit breaker protects the service route from repeated upstream failure.
- Passive health removes a failing instance from the candidate set.
- Active health brings an instance back after it recovers.

Per-instance circuit breakers are a possible later improvement after the first load-balancing phase is stable.

## North-South Load Balancer Support

API Gateway should support running multiple instances behind an external load balancer or ingress.

### Readiness

Add a gateway readiness endpoint, for example:

```text
GET /actuator/readiness
```

If `p.App.Path()` is configured, the endpoint is registered under the application base path like the existing actuator routes.

Readiness should be gateway-only:

- Config loaded and validated.
- Routers registered.
- Generic proxy routes initialized.
- Load balancer registry initialized.

Readiness should not fail just because one downstream service is unhealthy. Otherwise, a single downstream incident could remove all API Gateway instances from the external load balancer.

### Liveness

Keep the existing `/actuator/health` behavior as the liveness endpoint.

### Trusted Forwarded Headers

Add config to control which upstream proxy addresses are trusted, for example:

```yaml
app:
  proxy:
    trustedProxies:
      - 10.0.0.0/8
      - 172.16.0.0/12
      - 192.168.0.0/16
    trustForwardedHeaders: true
```

When enabled:

- Configure Gin trusted proxies.
- Honor `X-Forwarded-For`, `X-Forwarded-Host`, and `X-Forwarded-Proto` only from trusted proxies.
- Preserve and append `X-Forwarded-For` correctly.
- Set upstream `X-Forwarded-Proto` from the trusted incoming header or request TLS state, not hardcoded `"http"`.

The gateway should remain stateless:

- JWT data comes from each request.
- Rate limit state remains in Redis.
- No local session state is required for horizontal scaling.

## Compatibility With BaseAPIClient and WebSocket

Phase one only load-balances Generic Proxy HTTP routes.

To keep the rest of the gateway functional after the config migration:

- `ServiceProperty` should expose a helper such as `DefaultInstanceURL()` or `PrimaryURL()`.
- The helper returns the first configured instance URL.
- `BaseAPIClient` custom controller paths use the primary instance as a single target in phase one.
- `WebSocketProxyController` uses the primary instance as a single target in phase one.

This keeps the `instances` config model consistent without forcing WebSocket sticky behavior or BaseAPIClient load balancing into the first implementation phase.

## Error Handling

Startup errors:

- Invalid load-balancer config fails startup.
- Missing required service instances fail startup.
- Unsupported algorithms fail startup.

Runtime errors:

- No healthy instance and no fallback returns HTTP 503 with the existing gateway unavailable response shape.
- Circuit breaker open returns HTTP 503 as it does today.
- Network proxy errors return HTTP 502 unless circuit breaker behavior maps them to 503.
- Upstream 5xx responses are returned to the caller while also recording passive failure.
- Active health check failures do not directly return responses to clients; they only affect candidate selection.

## Testing

Add focused tests in the nearest existing packages.

Configuration tests:

- Valid `instances` config binds correctly.
- Missing instances is invalid for configured gateway services.
- Duplicate instance names fail validation.
- Invalid instance URLs fail validation.
- Unsupported algorithm fails validation.

Load balancer unit tests:

- Round-robin distributes across healthy instances.
- Unhealthy instances are skipped.
- All-unhealthy without fallback returns no target.
- All-unhealthy with fallback still selects an instance.
- Passive failures eject an instance after the configured threshold.
- Passive success resets failure count.
- Active health failure marks an instance unhealthy after threshold.
- Active health success recovers an instance after threshold.

Generic proxy tests:

- Requests distribute across two mock upstreams.
- Path rewrite remains correct.
- Query string remains correct.
- Request body remains correct.
- Authorization header remains correct.
- HTTP 500 records passive failure but preserves the upstream response.
- Network error records passive failure and returns the existing proxy error response.

North-south tests:

- Readiness returns 200 when gateway initialization is complete.
- Readiness does not fail when all downstream instances are unhealthy.
- Forwarded headers are honored only when the request comes through a trusted proxy.

Suggested verification from `api_gateway/`:

```bash
go test ./src/kernel/properties ./src/ui/controller/common ./src/ui/router -count=1
go test ./... -count=1
go vet ./...
```

## Rollout Plan

1. Add and validate the new `instances` config model.
2. Update `default.yaml`, `local.yaml`, and `production.yaml` to use `instances`.
3. Add the Generic Proxy HTTP `ServiceLoadBalancer`.
4. Add active and passive health state.
5. Wire load balancing into `GenericProxyController`.
6. Add readiness and trusted proxy-header support.
7. Keep BaseAPIClient and WebSocket on primary instance.
8. Verify path rewrite, headers, body forwarding, health behavior, and full module tests.

## Future Phases

- Apply the same load balancer to `BaseAPIClient` adapters.
- Add WebSocket load balancing with explicit sticky or connection-affinity behavior.
- Add weighted round-robin or least-in-flight after metrics show the need.
- Add service discovery when deployment infrastructure is ready.
- Add per-instance circuit breakers.
- Add drain state and graceful rollout support for API Gateway instances behind the external load balancer.
