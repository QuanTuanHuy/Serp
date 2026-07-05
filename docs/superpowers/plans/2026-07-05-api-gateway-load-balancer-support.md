# API Gateway Load Balancer Support Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add production load-balancer support to `api_gateway` by moving downstream services to explicit instances, load-balancing Generic Proxy HTTP routes, and adding gateway readiness plus trusted forwarded-header handling.

**Architecture:** Add a focused downstream load-balancer service under `src/core/service/loadbalancer`, then wire it into `GenericProxyController` so target selection happens per request. Keep WebSocket and BaseAPIClient paths on the first configured instance for this phase, while the new config model uses only `instances`.

**Tech Stack:** Go 1.25+, Gin, Uber FX, `httputil.ReverseProxy`, `net/http`, existing `properties`, existing resilient transport and circuit breaker utilities.

---

## Scope

This plan implements phase one from the approved spec:

- Config migration from `host`/`port` to `instances`.
- Generic Proxy HTTP round-robin load balancing.
- Active and passive downstream instance health.
- Gateway-only readiness endpoint.
- Trusted forwarded-header handling for deployments behind an external load balancer.
- BaseAPIClient and WebSocket compatibility through primary instance URL helpers.

This plan does not implement WebSocket load balancing, BaseAPIClient load balancing, service discovery, weighted algorithms, least-in-flight routing, deployment drain state, or per-instance circuit breakers.

## File Structure

Create:

- `api_gateway/src/core/service/loadbalancer/service_load_balancer.go`  
  Owns `ServiceInstance`, round-robin selection, active/passive health state, and health-check lifecycle.

- `api_gateway/src/core/service/loadbalancer/service_load_balancer_test.go`  
  Unit tests for healthy selection, fallback, passive ejection, and active recovery.

- `api_gateway/src/core/service/loadbalancer/registry.go`  
  Builds and owns per-service load balancers from service properties.

- `api_gateway/src/core/service/loadbalancer/registry_test.go`  
  Verifies registry lookup and lifecycle-safe construction.

- `api_gateway/src/kernel/properties/proxy_properties.go`  
  Holds trusted proxy config for north-south LB support.

- `api_gateway/src/ui/middleware/trusted_proxy_middleware.go`  
  Strips forwarded headers from untrusted sources and configures Gin trusted proxies.

- `api_gateway/src/ui/middleware/trusted_proxy_middleware_test.go`  
  Verifies trusted and untrusted forwarded-header behavior.

Modify:

- `api_gateway/src/kernel/properties/external_service_properties.go`  
  Replace runtime `Host`/`Port` model with `Instances`, `PrimaryURL`, `PrimaryURLWithPath`, defaults, and validation.

- `api_gateway/src/config/default.yaml`  
  Add full local-default service `instances` config so default profile can start.

- `api_gateway/src/config/local.yaml`  
  Replace `host`/`port` service targets with `instances`.

- `api_gateway/src/config/production.yaml`  
  Replace `host`/`port` service targets with `instances`.

- `api_gateway/src/cmd/bootstrap/all.go`  
  Provide `ProxyProperties`, `TrustedProxyMiddleware`, and `loadbalancer.Registry`.

- `api_gateway/src/ui/router/router.go`  
  Install trusted proxy middleware before IP rate limiting and add `/actuator/readiness`.

- `api_gateway/src/ui/controller/common/generic_proxy_controller.go`  
  Use per-request load-balancer selection and report passive health.

- `api_gateway/src/ui/controller/common/generic_proxy_controller_test.go`  
  Update test fixtures to `instances` and add distribution/error tests.

- `api_gateway/src/ui/controller/common/websocket_proxy_controller.go`  
  Use `PrimaryURL()` for phase-one single-target WebSocket compatibility.

- `api_gateway/src/ui/controller/common/websocket_proxy_controller_test.go`  
  Update test fixtures to `instances`.

- `api_gateway/src/ui/router/account_router_test.go`  
  Update test service fixtures to `instances`.

- `api_gateway/src/infrastructure/client/account/*.go`  
  Use `AccountService.PrimaryURLWithPath("/account-service")` in constructors.

- `api_gateway/src/infrastructure/client/crm/*.go`  
  Use `CrmService.PrimaryURLWithPath("/crm")` in constructors.

- `api_gateway/src/infrastructure/client/ptm/*.go`  
  Use `PTMTask.PrimaryURLWithPath("/ptm-task")` or `PTMSchedule.PrimaryURLWithPath("/ptm-schedule")` in constructors.

---

### Task 1: Service Instances Config Model

**Files:**
- Modify: `api_gateway/src/kernel/properties/external_service_properties.go`
- Create: `api_gateway/src/kernel/properties/external_service_properties_test.go`

- [ ] **Step 1: Write config validation tests**

Create `api_gateway/src/kernel/properties/external_service_properties_test.go`:

```go
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package properties

import (
	"strings"
	"testing"
	"time"
)

func TestServiceProperty_PrimaryURL_UsesFirstInstance(t *testing.T) {
	prop := ServiceProperty{
		Instances: []ServiceInstanceProperty{
			{Name: "account-1", URL: "http://account-1:8080"},
			{Name: "account-2", URL: "http://account-2:8080"},
		},
	}

	got := prop.PrimaryURL()

	if got != "http://account-1:8080" {
		t.Fatalf("expected first instance URL, got %q", got)
	}
}

func TestServiceProperty_PrimaryURLWithPath_JoinsPath(t *testing.T) {
	prop := ServiceProperty{
		Instances: []ServiceInstanceProperty{
			{Name: "crm-1", URL: "http://crm:8086/"},
		},
	}

	got := prop.PrimaryURLWithPath("/crm")

	if got != "http://crm:8086/crm" {
		t.Fatalf("expected joined URL, got %q", got)
	}
}

func TestServiceProperty_ApplyDefaults_DefaultsHealthPathAndAlgorithm(t *testing.T) {
	prop := ServiceProperty{
		Instances: []ServiceInstanceProperty{
			{Name: "sales-1", URL: "http://sales:8090"},
		},
		LoadBalancer: ServiceLoadBalancerProperty{
			ActiveHealthCheck: ActiveHealthCheckProperty{Enabled: true},
		},
	}

	prop.ApplyDefaults()

	if prop.HealthPath != "/actuator/health" {
		t.Fatalf("expected default health path, got %q", prop.HealthPath)
	}
	if prop.LoadBalancer.Algorithm != "round-robin" {
		t.Fatalf("expected round-robin, got %q", prop.LoadBalancer.Algorithm)
	}
	if prop.LoadBalancer.ActiveHealthCheck.Interval != 10*time.Second {
		t.Fatalf("expected default active interval, got %s", prop.LoadBalancer.ActiveHealthCheck.Interval)
	}
	if prop.LoadBalancer.ActiveHealthCheck.Timeout != 2*time.Second {
		t.Fatalf("expected default active timeout, got %s", prop.LoadBalancer.ActiveHealthCheck.Timeout)
	}
}

func TestServiceProperty_ValidateRejectsMissingInstances(t *testing.T) {
	prop := ServiceProperty{}

	prop.ApplyDefaults()
	err := prop.Validate("accountService")

	if err == nil || !strings.Contains(err.Error(), "accountService") {
		t.Fatalf("expected missing instances validation error, got %v", err)
	}
}

func TestServiceProperty_ValidateRejectsDuplicateInstanceNames(t *testing.T) {
	prop := ServiceProperty{
		Instances: []ServiceInstanceProperty{
			{Name: "crm-1", URL: "http://crm-1:8086"},
			{Name: "crm-1", URL: "http://crm-2:8086"},
		},
	}

	prop.ApplyDefaults()
	err := prop.Validate("crmService")

	if err == nil || !strings.Contains(err.Error(), "duplicate") {
		t.Fatalf("expected duplicate validation error, got %v", err)
	}
}

func TestServiceProperty_ValidateRejectsUnsupportedAlgorithm(t *testing.T) {
	prop := ServiceProperty{
		LoadBalancer: ServiceLoadBalancerProperty{Algorithm: "least-in-flight"},
		Instances: []ServiceInstanceProperty{
			{Name: "crm-1", URL: "http://crm-1:8086"},
		},
	}

	prop.ApplyDefaults()
	err := prop.Validate("crmService")

	if err == nil || !strings.Contains(err.Error(), "round-robin") {
		t.Fatalf("expected unsupported algorithm validation error, got %v", err)
	}
}

func TestServiceProperty_ValidateRejectsInvalidURL(t *testing.T) {
	prop := ServiceProperty{
		Instances: []ServiceInstanceProperty{
			{Name: "bad", URL: "://bad-url"},
		},
	}

	prop.ApplyDefaults()
	err := prop.Validate("badService")

	if err == nil || !strings.Contains(err.Error(), "invalid URL") {
		t.Fatalf("expected invalid URL validation error, got %v", err)
	}
}

func TestExternalServiceProperties_ValidateAllServices(t *testing.T) {
	props := newValidExternalServicePropertiesForTest()

	err := props.ApplyDefaultsAndValidate()

	if err != nil {
		t.Fatalf("expected valid properties, got %v", err)
	}
}

func newValidExternalServicePropertiesForTest() ExternalServiceProperties {
	service := func(name string, rawURL string) ServiceProperty {
		return ServiceProperty{
			Instances: []ServiceInstanceProperty{{Name: name + "-1", URL: rawURL}},
		}
	}

	return ExternalServiceProperties{
		AccountService:      service("account", "http://account:8081"),
		PTMTask:             service("ptm-task", "http://ptm-task:8083"),
		PTMSchedule:         service("ptm-schedule", "http://ptm-schedule:8084"),
		PurchaseService:     service("purchase", "http://purchase:8088"),
		LogisticsService:    service("logistics", "http://logistics:8089"),
		Logistics2Service:   service("logistics2", "http://logistics2:9004"),
		FirstMileService:    service("first-mile", "http://first-mile:8093"),
		SecondMileService:   service("second-mile", "http://second-mile:8095"),
		CrmService:          service("crm", "http://crm:8086"),
		NotificationService: service("notification", "http://notification:8090"),
		SalesService:        service("sales", "http://sales:8090"),
		DiscussService:      service("discuss", "http://discuss:8092"),
		PmCoreService:       service("pm-core", "http://pm-core:8093"),
		TtcrsService:        service("ttcrs", "http://ttcrs:8094"),
	}
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run from `api_gateway/`:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/kernel/properties -run 'TestServiceProperty_|TestExternalServiceProperties_' -count=1
```

Expected: FAIL because `ServiceInstanceProperty`, `ServiceLoadBalancerProperty`, `PrimaryURL`, `PrimaryURLWithPath`, and validation methods are not defined.

- [ ] **Step 3: Implement service instance properties**

Modify `api_gateway/src/kernel/properties/external_service_properties.go` to this structure:

```go
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package properties

import (
	"fmt"
	"net/url"
	"strings"
	"time"

	"github.com/golibs-starter/golib/config"
)

const (
	LoadBalancerAlgorithmRoundRobin = "round-robin"
	DefaultHealthPath               = "/actuator/health"
)

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
	Algorithm              string                     `mapstructure:"algorithm"`
	ActiveHealthCheck      ActiveHealthCheckProperty  `mapstructure:"activeHealthCheck"`
	PassiveHealthCheck     PassiveHealthCheckProperty `mapstructure:"passiveHealthCheck"`
	AllowUnhealthyFallback bool                       `mapstructure:"allowUnhealthyFallback"`
}

type ServiceProperty struct {
	Timeout       time.Duration                 `mapstructure:"timeout"`
	HealthPath    string                        `mapstructure:"healthPath"`
	WebSocketPath string                        `mapstructure:"webSocketPath"`
	LoadBalancer  ServiceLoadBalancerProperty   `mapstructure:"loadBalancer"`
	Instances     []ServiceInstanceProperty     `mapstructure:"instances"`
}

func (s *ServiceProperty) ApplyDefaults() {
	if s.HealthPath == "" {
		s.HealthPath = DefaultHealthPath
	}
	if s.LoadBalancer.Algorithm == "" {
		s.LoadBalancer.Algorithm = LoadBalancerAlgorithmRoundRobin
	}
	active := &s.LoadBalancer.ActiveHealthCheck
	if active.Enabled {
		if active.Interval == 0 {
			active.Interval = 10 * time.Second
		}
		if active.Timeout == 0 {
			active.Timeout = 2 * time.Second
		}
		if active.UnhealthyThreshold == 0 {
			active.UnhealthyThreshold = 2
		}
		if active.HealthyThreshold == 0 {
			active.HealthyThreshold = 1
		}
	}
	passive := &s.LoadBalancer.PassiveHealthCheck
	if passive.Enabled {
		if passive.ConsecutiveFailures == 0 {
			passive.ConsecutiveFailures = 3
		}
		if passive.EjectionTime == 0 {
			passive.EjectionTime = 30 * time.Second
		}
	}
}

func (s ServiceProperty) Validate(serviceName string) error {
	if len(s.Instances) == 0 {
		return fmt.Errorf("%s must configure at least one instance", serviceName)
	}
	if s.LoadBalancer.Algorithm != LoadBalancerAlgorithmRoundRobin {
		return fmt.Errorf("%s loadBalancer.algorithm must be %q", serviceName, LoadBalancerAlgorithmRoundRobin)
	}
	if s.LoadBalancer.ActiveHealthCheck.Enabled && s.HealthPath == "" {
		return fmt.Errorf("%s healthPath is required when active health check is enabled", serviceName)
	}
	if err := validateActiveHealthCheck(serviceName, s.LoadBalancer.ActiveHealthCheck); err != nil {
		return err
	}
	if err := validatePassiveHealthCheck(serviceName, s.LoadBalancer.PassiveHealthCheck); err != nil {
		return err
	}

	seen := make(map[string]struct{}, len(s.Instances))
	for _, instance := range s.Instances {
		if strings.TrimSpace(instance.Name) == "" {
			return fmt.Errorf("%s instance name is required", serviceName)
		}
		if _, exists := seen[instance.Name]; exists {
			return fmt.Errorf("%s has duplicate instance name %q", serviceName, instance.Name)
		}
		seen[instance.Name] = struct{}{}

		parsed, err := url.Parse(instance.URL)
		if err != nil || parsed.Scheme == "" || parsed.Host == "" {
			return fmt.Errorf("%s instance %q has invalid URL %q", serviceName, instance.Name, instance.URL)
		}
		if parsed.Scheme != "http" && parsed.Scheme != "https" {
			return fmt.Errorf("%s instance %q URL scheme must be http or https", serviceName, instance.Name)
		}
	}

	return nil
}

func validateActiveHealthCheck(serviceName string, active ActiveHealthCheckProperty) error {
	if !active.Enabled {
		return nil
	}
	if active.Interval <= 0 {
		return fmt.Errorf("%s active health interval must be > 0", serviceName)
	}
	if active.Timeout <= 0 {
		return fmt.Errorf("%s active health timeout must be > 0", serviceName)
	}
	if active.UnhealthyThreshold <= 0 {
		return fmt.Errorf("%s active health unhealthyThreshold must be > 0", serviceName)
	}
	if active.HealthyThreshold <= 0 {
		return fmt.Errorf("%s active health healthyThreshold must be > 0", serviceName)
	}
	return nil
}

func validatePassiveHealthCheck(serviceName string, passive PassiveHealthCheckProperty) error {
	if !passive.Enabled {
		return nil
	}
	if passive.ConsecutiveFailures <= 0 {
		return fmt.Errorf("%s passive health consecutiveFailures must be > 0", serviceName)
	}
	if passive.EjectionTime <= 0 {
		return fmt.Errorf("%s passive health ejectionTime must be > 0", serviceName)
	}
	return nil
}

func (s ServiceProperty) PrimaryURL() string {
	if len(s.Instances) == 0 {
		return ""
	}
	return strings.TrimRight(s.Instances[0].URL, "/")
}

func (s ServiceProperty) PrimaryURLWithPath(path string) string {
	base := s.PrimaryURL()
	if base == "" {
		return ""
	}
	if path == "" || path == "/" {
		return base
	}
	return base + "/" + strings.Trim(path, "/")
}

type ExternalServiceProperties struct {
	AccountService      ServiceProperty
	PTMTask             ServiceProperty
	PTMSchedule         ServiceProperty
	PurchaseService     ServiceProperty
	LogisticsService    ServiceProperty
	Logistics2Service   ServiceProperty
	FirstMileService    ServiceProperty
	SecondMileService   ServiceProperty
	CrmService          ServiceProperty
	NotificationService ServiceProperty
	SalesService        ServiceProperty
	DiscussService      ServiceProperty
	PmCoreService       ServiceProperty
	TtcrsService        ServiceProperty
}

func (e ExternalServiceProperties) Prefix() string {
	return "external.services"
}

func NewExternalServicePropeties(loader config.Loader) (*ExternalServiceProperties, error) {
	props := ExternalServiceProperties{}
	if err := loader.Bind(&props); err != nil {
		return &props, err
	}
	return &props, props.ApplyDefaultsAndValidate()
}

func (e *ExternalServiceProperties) ApplyDefaultsAndValidate() error {
	services := map[string]*ServiceProperty{
		"accountService":      &e.AccountService,
		"ptmTask":             &e.PTMTask,
		"ptmSchedule":         &e.PTMSchedule,
		"purchaseService":     &e.PurchaseService,
		"logisticsService":    &e.LogisticsService,
		"logistics2Service":   &e.Logistics2Service,
		"firstMileService":    &e.FirstMileService,
		"secondMileService":   &e.SecondMileService,
		"crmService":          &e.CrmService,
		"notificationService": &e.NotificationService,
		"salesService":        &e.SalesService,
		"discussService":      &e.DiscussService,
		"pmCoreService":       &e.PmCoreService,
		"ttcrsService":        &e.TtcrsService,
	}

	for name, service := range services {
		service.ApplyDefaults()
		if err := service.Validate(name); err != nil {
			return err
		}
	}

	return nil
}
```

- [ ] **Step 4: Run config tests**

Run:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/kernel/properties -run 'TestServiceProperty_|TestExternalServiceProperties_' -count=1
```

Expected: PASS.

- [ ] **Step 5: Commit config model**

Run:

```powershell
git add src/kernel/properties/external_service_properties.go src/kernel/properties/external_service_properties_test.go
git commit -m "feat(gateway): add service instance config model"
```

Expected: commit succeeds.

---

### Task 2: YAML Config Migration

**Files:**
- Modify: `api_gateway/src/config/default.yaml`
- Modify: `api_gateway/src/config/local.yaml`
- Modify: `api_gateway/src/config/production.yaml`

- [ ] **Step 1: Update `default.yaml` with local instance defaults**

Replace `api_gateway/src/config/default.yaml` with:

```yaml
app:
  name: API Gateway
  port: 8080

external:
  services:
    accountService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: account-local
          url: http://localhost:8081
    ptmTask:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: ptm-task-local
          url: http://localhost:8083
    ptmSchedule:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: ptm-schedule-local
          url: http://localhost:8084
    purchaseService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: purchase-local
          url: http://localhost:8088
    logisticsService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: logistics-local
          url: http://localhost:8089
    logistics2Service:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: logistics2-local
          url: http://localhost:9004
    firstMileService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: first-mile-local
          url: http://localhost:8093
    secondMileService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: second-mile-local
          url: http://localhost:8095
    crmService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: crm-local
          url: http://localhost:8086
    notificationService:
      timeout: 15s
      healthPath: /actuator/health
      webSocketPath: /notification/ws
      instances:
        - name: notification-local
          url: http://localhost:8090
    salesService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: sales-local
          url: http://localhost:8090
    discussService:
      timeout: 15s
      healthPath: /actuator/health
      webSocketPath: /discuss/ws/discuss
      instances:
        - name: discuss-local
          url: http://localhost:8092
    pmCoreService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: pm-core-local
          url: http://localhost:8093
    ttcrsService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: ttcrs-local
          url: http://localhost:8094
```

- [ ] **Step 2: Update `local.yaml` services**

In `api_gateway/src/config/local.yaml`, replace every service `host` and `port` pair under `external.services` with one `instances` entry.

Use this service block:

```yaml
external:
  services:
    accountService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: account-local
          url: http://localhost:8081
    ptmTask:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: ptm-task-local
          url: http://localhost:8083
    ptmSchedule:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: ptm-schedule-local
          url: http://localhost:8084
    purchaseService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: purchase-local
          url: http://localhost:8088
    logisticsService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: logistics-local
          url: http://localhost:8089
    logistics2Service:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: logistics2-local
          url: http://localhost:9004
    firstMileService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: first-mile-local
          url: http://localhost:8093
    secondMileService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: second-mile-local
          url: http://localhost:8095
    crmService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: crm-local
          url: http://localhost:8086
    notificationService:
      timeout: 15s
      healthPath: /actuator/health
      webSocketPath: /notification/ws
      instances:
        - name: notification-local
          url: http://localhost:8090
    salesService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: sales-local
          url: http://localhost:8090
    discussService:
      timeout: 15s
      healthPath: /actuator/health
      webSocketPath: /discuss/ws/discuss
      instances:
        - name: discuss-local
          url: http://localhost:8092
    pmCoreService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: pm-core-local
          url: http://localhost:8093
    ttcrsService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: ttcrs-local
          url: http://localhost:8094
```

- [ ] **Step 3: Update `production.yaml` services**

In `api_gateway/src/config/production.yaml`, replace every service `host` and `port` pair under `external.services` with one env-based instance URL.

Use this service block:

```yaml
external:
  services:
    accountService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: account-1
          url: http://${ACCOUNT_SERVICE_HOST}:${ACCOUNT_SERVICE_PORT}
    ptmTask:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: ptm-task-1
          url: http://${PTM_TASK_SERVICE_HOST}:${PTM_TASK_SERVICE_PORT}
    ptmSchedule:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: ptm-schedule-1
          url: http://${PTM_SCHEDULE_SERVICE_HOST}:${PTM_SCHEDULE_SERVICE_PORT}
    purchaseService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: purchase-1
          url: http://${PURCHASE_SERVICE_HOST}:${PURCHASE_SERVICE_PORT}
    logisticsService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: logistics-1
          url: http://${LOGISTICS_SERVICE_HOST}:${LOGISTICS_SERVICE_PORT}
    logistics2Service:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: logistics2-1
          url: http://${LOGISTICS2_SERVICE_HOST}:${LOGISTICS2_SERVICE_PORT}
    firstMileService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: first-mile-1
          url: http://${FIRST_MILE_SERVICE_HOST}:${FIRST_MILE_SERVICE_PORT}
    secondMileService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: second-mile-1
          url: http://${SECOND_MILE_SERVICE_HOST}:${SECOND_MILE_SERVICE_PORT}
    crmService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: crm-1
          url: http://${CRM_SERVICE_HOST}:${CRM_SERVICE_PORT}
    notificationService:
      timeout: 15s
      healthPath: /actuator/health
      webSocketPath: /notification/ws
      instances:
        - name: notification-1
          url: http://${NOTIFICATION_SERVICE_HOST}:${NOTIFICATION_SERVICE_PORT}
    salesService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: sales-1
          url: http://${SALES_SERVICE_HOST}:${SALES_SERVICE_PORT}
    discussService:
      timeout: 15s
      healthPath: /actuator/health
      webSocketPath: /discuss/ws/discuss
      instances:
        - name: discuss-1
          url: http://${DISCUSS_SERVICE_HOST}:${DISCUSS_SERVICE_PORT}
    pmCoreService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: pm-core-1
          url: http://${PM_CORE_SERVICE_HOST}:${PM_CORE_SERVICE_PORT}
    ttcrsService:
      timeout: 15s
      healthPath: /actuator/health
      instances:
        - name: ttcrs-1
          url: http://${TTCRS_SERVICE_HOST}:${TTCRS_SERVICE_PORT}
```

- [ ] **Step 4: Run property package tests**

Run from `api_gateway/`:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/kernel/properties -count=1
```

Expected: PASS.

- [ ] **Step 5: Commit YAML migration**

Run:

```powershell
git add src/config/default.yaml src/config/local.yaml src/config/production.yaml
git commit -m "chore(gateway): migrate service targets to instances"
```

Expected: commit succeeds.

---

### Task 3: BaseAPIClient and WebSocket Primary Instance Compatibility

**Files:**
- Modify: `api_gateway/src/infrastructure/client/account/*.go`
- Modify: `api_gateway/src/infrastructure/client/crm/*.go`
- Modify: `api_gateway/src/infrastructure/client/ptm/*.go`
- Modify: `api_gateway/src/ui/controller/common/websocket_proxy_controller.go`
- Modify: `api_gateway/src/ui/controller/common/websocket_proxy_controller_test.go`
- Modify: `api_gateway/src/ui/router/account_router_test.go`
- Modify: `api_gateway/src/ui/controller/common/generic_proxy_controller_test.go`

- [ ] **Step 1: Update account adapter constructors**

In each account adapter constructor, replace:

```go
baseUrl := "http://" + authProps.AccountService.Host + ":" + authProps.AccountService.Port + "/account-service"
```

or the same expression with `keycloakProps` instead of `authProps`, with:

```go
baseUrl := authProps.AccountService.PrimaryURLWithPath("/account-service")
```

For `api_gateway/src/infrastructure/client/account/keycloak_client_adapter.go`, use:

```go
baseUrl := keycloakProps.AccountService.PrimaryURLWithPath("/account-service")
```

Apply this to:

- `api_gateway/src/infrastructure/client/account/auth_client_adapter.go`
- `api_gateway/src/infrastructure/client/account/department_client_adapter.go`
- `api_gateway/src/infrastructure/client/account/keycloak_client_adapter.go`
- `api_gateway/src/infrastructure/client/account/menu_display_client_adapter.go`
- `api_gateway/src/infrastructure/client/account/module_access_client_adapter.go`
- `api_gateway/src/infrastructure/client/account/module_client_adapter.go`
- `api_gateway/src/infrastructure/client/account/organization_client_adapter.go`
- `api_gateway/src/infrastructure/client/account/permission_client_adapter.go`
- `api_gateway/src/infrastructure/client/account/role_client_adapter.go`
- `api_gateway/src/infrastructure/client/account/subscription_client_adapter.go`
- `api_gateway/src/infrastructure/client/account/subscription_plan_client_adapter.go`
- `api_gateway/src/infrastructure/client/account/user_client_adapter.go`

- [ ] **Step 2: Update CRM adapter constructors**

In each CRM adapter constructor, replace:

```go
baseURL := fmt.Sprintf("http://%s:%s/crm", props.CrmService.Host, props.CrmService.Port)
```

with:

```go
baseURL := props.CrmService.PrimaryURLWithPath("/crm")
```

Remove `fmt` import if it becomes unused.

Apply this to:

- `api_gateway/src/infrastructure/client/crm/contact_client_adapter.go`
- `api_gateway/src/infrastructure/client/crm/customer_client_adapter.go`
- `api_gateway/src/infrastructure/client/crm/lead_client_adapter.go`
- `api_gateway/src/infrastructure/client/crm/opportunity_client_adapter.go`

- [ ] **Step 3: Update PTM task adapter constructors**

In PTM task adapters, replace:

```go
baseURL := "http://" + taskManagerProps.PTMTask.Host + ":" + taskManagerProps.PTMTask.Port + "/ptm-task"
```

with:

```go
baseURL := taskManagerProps.PTMTask.PrimaryURLWithPath("/ptm-task")
```

Apply this to:

- `api_gateway/src/infrastructure/client/ptm/note_client_adapter.go`
- `api_gateway/src/infrastructure/client/ptm/project_client_adapter.go`
- `api_gateway/src/infrastructure/client/ptm/task_client_adapter.go`

- [ ] **Step 4: Update PTM schedule adapter constructors**

In PTM schedule adapters, replace:

```go
baseURL := "http://" + props.PTMSchedule.Host + ":" + props.PTMSchedule.Port + "/ptm-schedule"
```

with:

```go
baseURL := props.PTMSchedule.PrimaryURLWithPath("/ptm-schedule")
```

Apply this to:

- `api_gateway/src/infrastructure/client/ptm/availability_calendar_client_adapter.go`
- `api_gateway/src/infrastructure/client/ptm/schedule_event_client_adapter.go`
- `api_gateway/src/infrastructure/client/ptm/schedule_plan_client_adapter.go`
- `api_gateway/src/infrastructure/client/ptm/schedule_task_client_adapter.go`
- `api_gateway/src/infrastructure/client/ptm/schedule_window_client_adapter.go`

- [ ] **Step 5: Update WebSocket proxy to use primary instance**

In `api_gateway/src/ui/controller/common/websocket_proxy_controller.go`, replace:

```go
baseURL := svc.BaseURL()
```

with:

```go
baseURL := svc.PrimaryURL()
```

Keep the existing parse and registration behavior.

- [ ] **Step 6: Update test fixtures to use `Instances`**

Replace test service properties such as:

```go
properties.ServiceProperty{Host: host, Port: port}
```

with:

```go
properties.ServiceProperty{
	Instances: []properties.ServiceInstanceProperty{
		{Name: "test-1", URL: "http://" + net.JoinHostPort(host, port)},
	},
}
```

For tests that already parse `upstream.URL`, use:

```go
properties.ServiceProperty{
	Instances: []properties.ServiceInstanceProperty{
		{Name: "test-1", URL: upstream.URL},
	},
}
```

Apply this to:

- `api_gateway/src/ui/controller/common/generic_proxy_controller_test.go`
- `api_gateway/src/ui/controller/common/websocket_proxy_controller_test.go`
- `api_gateway/src/ui/router/account_router_test.go`

Add `net` imports only where `net.JoinHostPort` is used.

- [ ] **Step 7: Run compile-focused tests**

Run:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/infrastructure/client/account ./src/infrastructure/client/crm ./src/infrastructure/client/ptm ./src/ui/controller/common ./src/ui/router -run '^$' -count=1
```

Expected: PASS.

- [ ] **Step 8: Commit compatibility changes**

Run:

```powershell
git add src/infrastructure/client/account src/infrastructure/client/crm src/infrastructure/client/ptm src/ui/controller/common src/ui/router/account_router_test.go
git commit -m "refactor(gateway): use primary service instances for clients"
```

Expected: commit succeeds.

---

### Task 4: Load Balancer Core

**Files:**
- Create: `api_gateway/src/core/service/loadbalancer/service_load_balancer.go`
- Create: `api_gateway/src/core/service/loadbalancer/service_load_balancer_test.go`

- [ ] **Step 1: Write load balancer unit tests**

Create `api_gateway/src/core/service/loadbalancer/service_load_balancer_test.go`:

```go
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package loadbalancer

import (
	"context"
	"errors"
	"net/http"
	"net/http/httptest"
	"net/url"
	"testing"
	"time"

	"github.com/serp/api-gateway/src/kernel/properties"
)

func TestServiceLoadBalancer_NextRoundRobinHealthyInstances(t *testing.T) {
	lb := newTestLoadBalancer(t, "crm", false, false)

	first, err := lb.Next()
	if err != nil {
		t.Fatalf("first next: %v", err)
	}
	second, err := lb.Next()
	if err != nil {
		t.Fatalf("second next: %v", err)
	}
	third, err := lb.Next()
	if err != nil {
		t.Fatalf("third next: %v", err)
	}

	if first.Name != "crm-1" || second.Name != "crm-2" || third.Name != "crm-1" {
		t.Fatalf("unexpected sequence: %s, %s, %s", first.Name, second.Name, third.Name)
	}
}

func TestServiceLoadBalancer_NextSkipsUnhealthyInstances(t *testing.T) {
	lb := newTestLoadBalancer(t, "crm", false, false)
	lb.Report("crm-1", http.StatusInternalServerError, nil)

	first, err := lb.Next()
	if err != nil {
		t.Fatalf("next: %v", err)
	}
	second, err := lb.Next()
	if err != nil {
		t.Fatalf("second next: %v", err)
	}

	if first.Name != "crm-2" || second.Name != "crm-2" {
		t.Fatalf("expected only healthy crm-2, got %s and %s", first.Name, second.Name)
	}
}

func TestServiceLoadBalancer_AllUnhealthyWithoutFallbackReturnsError(t *testing.T) {
	lb := newTestLoadBalancer(t, "crm", false, false)
	lb.Report("crm-1", http.StatusInternalServerError, nil)
	lb.Report("crm-2", http.StatusInternalServerError, nil)

	_, err := lb.Next()

	if !errors.Is(err, ErrNoHealthyInstance) {
		t.Fatalf("expected ErrNoHealthyInstance, got %v", err)
	}
}

func TestServiceLoadBalancer_AllUnhealthyWithFallbackSelectsInstance(t *testing.T) {
	lb := newTestLoadBalancer(t, "crm", true, false)
	lb.Report("crm-1", http.StatusInternalServerError, nil)
	lb.Report("crm-2", http.StatusInternalServerError, nil)

	got, err := lb.Next()
	if err != nil {
		t.Fatalf("expected fallback target, got %v", err)
	}

	if got.Name == "" {
		t.Fatalf("expected selected instance name")
	}
}

func TestServiceLoadBalancer_PassiveSuccessResetsFailures(t *testing.T) {
	lb := newTestLoadBalancer(t, "crm", false, false)
	lb.Report("crm-1", http.StatusInternalServerError, nil)
	lb.Report("crm-1", http.StatusOK, nil)
	lb.Report("crm-1", http.StatusInternalServerError, nil)

	got, err := lb.Next()
	if err != nil {
		t.Fatalf("next: %v", err)
	}

	if got.Name != "crm-1" {
		t.Fatalf("expected crm-1 to remain healthy after reset, got %s", got.Name)
	}
}

func TestServiceLoadBalancer_ActiveHealthRecoversInstance(t *testing.T) {
	var healthy bool
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if healthy {
			w.WriteHeader(http.StatusOK)
			return
		}
		w.WriteHeader(http.StatusServiceUnavailable)
	}))
	defer server.Close()

	u, err := url.Parse(server.URL)
	if err != nil {
		t.Fatalf("parse server URL: %v", err)
	}

	lb := NewServiceLoadBalancer(
		"crm",
		[]ServiceInstance{{Name: "crm-1", URL: u}},
		properties.ServiceLoadBalancerProperty{
			Algorithm: properties.LoadBalancerAlgorithmRoundRobin,
			ActiveHealthCheck: properties.ActiveHealthCheckProperty{
				Enabled:            true,
				Interval:           time.Hour,
				Timeout:            time.Second,
				UnhealthyThreshold: 1,
				HealthyThreshold:   1,
			},
		},
		"/actuator/health",
		server.Client(),
	)

	lb.runActiveHealthCheck(context.Background())
	_, err = lb.Next()
	if !errors.Is(err, ErrNoHealthyInstance) {
		t.Fatalf("expected unhealthy after failed health check, got %v", err)
	}

	healthy = true
	lb.runActiveHealthCheck(context.Background())
	got, err := lb.Next()
	if err != nil {
		t.Fatalf("expected recovered instance, got %v", err)
	}
	if got.Name != "crm-1" {
		t.Fatalf("expected crm-1, got %s", got.Name)
	}
}

func newTestLoadBalancer(t *testing.T, serviceName string, allowFallback bool, active bool) *ServiceLoadBalancer {
	t.Helper()

	firstURL, err := url.Parse("http://" + serviceName + "-1:8080")
	if err != nil {
		t.Fatalf("parse first URL: %v", err)
	}
	secondURL, err := url.Parse("http://" + serviceName + "-2:8080")
	if err != nil {
		t.Fatalf("parse second URL: %v", err)
	}

	return NewServiceLoadBalancer(
		serviceName,
		[]ServiceInstance{
			{Name: serviceName + "-1", URL: firstURL},
			{Name: serviceName + "-2", URL: secondURL},
		},
		properties.ServiceLoadBalancerProperty{
			Algorithm:              properties.LoadBalancerAlgorithmRoundRobin,
			AllowUnhealthyFallback: allowFallback,
			ActiveHealthCheck: properties.ActiveHealthCheckProperty{
				Enabled:            active,
				Interval:           time.Hour,
				Timeout:            time.Second,
				UnhealthyThreshold: 1,
				HealthyThreshold:   1,
			},
			PassiveHealthCheck: properties.PassiveHealthCheckProperty{
				Enabled:             true,
				ConsecutiveFailures: 1,
				EjectionTime:        time.Minute,
			},
		},
		"/actuator/health",
		http.DefaultClient,
	)
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run from `api_gateway/`:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/core/service/loadbalancer -run 'TestServiceLoadBalancer_' -count=1
```

Expected: FAIL because the package implementation does not exist.

- [ ] **Step 3: Implement the load balancer**

Create `api_gateway/src/core/service/loadbalancer/service_load_balancer.go`:

```go
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package loadbalancer

import (
	"context"
	"errors"
	"net/http"
	"net/url"
	"sync"
	"sync/atomic"
	"time"

	"github.com/serp/api-gateway/src/kernel/properties"
)

var ErrNoHealthyInstance = errors.New("no healthy service instance")

type ServiceInstance struct {
	Name string
	URL  *url.URL
}

type instanceState struct {
	instance                ServiceInstance
	healthy                 bool
	activeSuccesses         int
	activeFailures          int
	passiveFailures         int
	passiveEjectedUntil     time.Time
	lastStateChange         time.Time
}

type ServiceLoadBalancer struct {
	serviceName string
	props       properties.ServiceLoadBalancerProperty
	healthPath  string
	client      *http.Client
	counter     atomic.Uint64

	mu        sync.RWMutex
	instances map[string]*instanceState
	order     []string
}

func NewServiceLoadBalancer(
	serviceName string,
	instances []ServiceInstance,
	props properties.ServiceLoadBalancerProperty,
	healthPath string,
	client *http.Client,
) *ServiceLoadBalancer {
	if client == nil {
		client = http.DefaultClient
	}
	lb := &ServiceLoadBalancer{
		serviceName: serviceName,
		props:       props,
		healthPath:  healthPath,
		client:      client,
		instances:   make(map[string]*instanceState, len(instances)),
		order:       make([]string, 0, len(instances)),
	}
	now := time.Now()
	for _, instance := range instances {
		copied := cloneURL(instance.URL)
		lb.instances[instance.Name] = &instanceState{
			instance: ServiceInstance{Name: instance.Name, URL: copied},
			healthy:  true,
			lastStateChange: now,
		}
		lb.order = append(lb.order, instance.Name)
	}
	return lb
}

func (l *ServiceLoadBalancer) Start(ctx context.Context) {
	if !l.props.ActiveHealthCheck.Enabled {
		return
	}
	ticker := time.NewTicker(l.props.ActiveHealthCheck.Interval)
	go func() {
		defer ticker.Stop()
		for {
			select {
			case <-ctx.Done():
				return
			case <-ticker.C:
				l.runActiveHealthCheck(ctx)
			}
		}
	}()
}

func (l *ServiceLoadBalancer) Next() (*ServiceInstance, error) {
	now := time.Now()
	candidates := l.snapshotCandidates(now, true)
	if len(candidates) == 0 && l.props.AllowUnhealthyFallback {
		candidates = l.snapshotCandidates(now, false)
	}
	if len(candidates) == 0 {
		return nil, ErrNoHealthyInstance
	}

	idx := int(l.counter.Add(1)-1) % len(candidates)
	selected := candidates[idx]
	return &selected, nil
}

func (l *ServiceLoadBalancer) Report(instanceName string, statusCode int, err error) {
	if !l.props.PassiveHealthCheck.Enabled {
		return
	}

	l.mu.Lock()
	defer l.mu.Unlock()

	state, ok := l.instances[instanceName]
	if !ok {
		return
	}

	failed := err != nil || statusCode >= http.StatusInternalServerError
	if !failed {
		state.passiveFailures = 0
		return
	}

	state.passiveFailures++
	if state.passiveFailures >= l.props.PassiveHealthCheck.ConsecutiveFailures {
		state.healthy = false
		state.passiveEjectedUntil = time.Now().Add(l.props.PassiveHealthCheck.EjectionTime)
		state.lastStateChange = time.Now()
	}
}

func (l *ServiceLoadBalancer) runActiveHealthCheck(ctx context.Context) {
	l.mu.RLock()
	names := append([]string(nil), l.order...)
	l.mu.RUnlock()

	for _, name := range names {
		l.checkInstance(ctx, name)
	}
}

func (l *ServiceLoadBalancer) checkInstance(ctx context.Context, instanceName string) {
	l.mu.RLock()
	state, ok := l.instances[instanceName]
	if !ok {
		l.mu.RUnlock()
		return
	}
	healthURL := state.instance.URL.ResolveReference(&url.URL{Path: l.healthPath}).String()
	l.mu.RUnlock()

	checkCtx, cancel := context.WithTimeout(ctx, l.props.ActiveHealthCheck.Timeout)
	defer cancel()

	req, err := http.NewRequestWithContext(checkCtx, http.MethodGet, healthURL, nil)
	if err != nil {
		l.recordActiveFailure(instanceName)
		return
	}

	resp, err := l.client.Do(req)
	if err != nil {
		l.recordActiveFailure(instanceName)
		return
	}
	defer resp.Body.Close()

	if resp.StatusCode >= http.StatusOK && resp.StatusCode < http.StatusMultipleChoices {
		l.recordActiveSuccess(instanceName)
		return
	}
	l.recordActiveFailure(instanceName)
}

func (l *ServiceLoadBalancer) recordActiveSuccess(instanceName string) {
	l.mu.Lock()
	defer l.mu.Unlock()

	state, ok := l.instances[instanceName]
	if !ok {
		return
	}
	state.activeFailures = 0
	state.activeSuccesses++
	if state.activeSuccesses >= l.props.ActiveHealthCheck.HealthyThreshold {
		state.healthy = true
		state.passiveFailures = 0
		state.passiveEjectedUntil = time.Time{}
		state.lastStateChange = time.Now()
	}
}

func (l *ServiceLoadBalancer) recordActiveFailure(instanceName string) {
	l.mu.Lock()
	defer l.mu.Unlock()

	state, ok := l.instances[instanceName]
	if !ok {
		return
	}
	state.activeSuccesses = 0
	state.activeFailures++
	if state.activeFailures >= l.props.ActiveHealthCheck.UnhealthyThreshold {
		state.healthy = false
		state.lastStateChange = time.Now()
	}
}

func (l *ServiceLoadBalancer) snapshotCandidates(now time.Time, healthyOnly bool) []ServiceInstance {
	l.mu.RLock()
	defer l.mu.RUnlock()

	candidates := make([]ServiceInstance, 0, len(l.order))
	for _, name := range l.order {
		state := l.instances[name]
		if healthyOnly && !state.isAvailable(now) {
			continue
		}
		candidates = append(candidates, ServiceInstance{
			Name: state.instance.Name,
			URL:  cloneURL(state.instance.URL),
		})
	}
	return candidates
}

func (s *instanceState) isAvailable(now time.Time) bool {
	if !s.healthy {
		if !s.passiveEjectedUntil.IsZero() && now.After(s.passiveEjectedUntil) {
			return true
		}
		return false
	}
	return true
}

func cloneURL(input *url.URL) *url.URL {
	if input == nil {
		return nil
	}
	copied := *input
	return &copied
}
```

- [ ] **Step 4: Run load balancer tests**

Run:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/core/service/loadbalancer -run 'TestServiceLoadBalancer_' -count=1
```

Expected: PASS.

- [ ] **Step 5: Commit load balancer core**

Run:

```powershell
git add src/core/service/loadbalancer/service_load_balancer.go src/core/service/loadbalancer/service_load_balancer_test.go
git commit -m "feat(gateway): add downstream service load balancer"
```

Expected: commit succeeds.

---

### Task 5: Load Balancer Registry

**Files:**
- Create: `api_gateway/src/core/service/loadbalancer/registry.go`
- Create: `api_gateway/src/core/service/loadbalancer/registry_test.go`
- Modify: `api_gateway/src/cmd/bootstrap/all.go`

- [ ] **Step 1: Write registry tests**

Create `api_gateway/src/core/service/loadbalancer/registry_test.go`:

```go
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package loadbalancer

import (
	"testing"

	"github.com/serp/api-gateway/src/kernel/properties"
)

func TestRegistry_GetReturnsConfiguredService(t *testing.T) {
	props := newRegistryTestProps()

	registry, err := NewRegistry(props, nil)
	if err != nil {
		t.Fatalf("new registry: %v", err)
	}

	lb, ok := registry.Get("crm")
	if !ok {
		t.Fatalf("expected crm load balancer")
	}

	instance, err := lb.Next()
	if err != nil {
		t.Fatalf("next: %v", err)
	}
	if instance.Name != "crm-1" {
		t.Fatalf("expected crm-1, got %s", instance.Name)
	}
}

func TestRegistry_GetMissingServiceReturnsFalse(t *testing.T) {
	registry, err := NewRegistry(newRegistryTestProps(), nil)
	if err != nil {
		t.Fatalf("new registry: %v", err)
	}

	_, ok := registry.Get("missing")

	if ok {
		t.Fatalf("expected missing service to return false")
	}
}

func newRegistryTestProps() *properties.ExternalServiceProperties {
	service := func(name string, rawURL string) properties.ServiceProperty {
		prop := properties.ServiceProperty{
			Instances: []properties.ServiceInstanceProperty{{Name: name + "-1", URL: rawURL}},
		}
		prop.ApplyDefaults()
		return prop
	}

	props := properties.ExternalServiceProperties{
		AccountService:      service("account", "http://account:8081"),
		PTMTask:             service("ptm-task", "http://ptm-task:8083"),
		PTMSchedule:         service("ptm-schedule", "http://ptm-schedule:8084"),
		PurchaseService:     service("purchase", "http://purchase:8088"),
		LogisticsService:    service("logistics", "http://logistics:8089"),
		Logistics2Service:   service("logistics2", "http://logistics2:9004"),
		FirstMileService:    service("first-mile", "http://first-mile:8093"),
		SecondMileService:   service("second-mile", "http://second-mile:8095"),
		CrmService:          service("crm", "http://crm:8086"),
		NotificationService: service("notification", "http://notification:8090"),
		SalesService:        service("sales", "http://sales:8090"),
		DiscussService:      service("discuss", "http://discuss:8092"),
		PmCoreService:       service("pm-core", "http://pm-core:8093"),
		TtcrsService:        service("ttcrs", "http://ttcrs:8094"),
	}
	_ = props.ApplyDefaultsAndValidate()
	return &props
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/core/service/loadbalancer -run 'TestRegistry_' -count=1
```

Expected: FAIL because `Registry` and `NewRegistry` do not exist.

- [ ] **Step 3: Implement registry**

Create `api_gateway/src/core/service/loadbalancer/registry.go`:

```go
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package loadbalancer

import (
	"fmt"
	"net/http"
	"net/url"

	"github.com/serp/api-gateway/src/kernel/properties"
)

type Registry struct {
	services map[string]*ServiceLoadBalancer
}

func NewRegistry(
	props *properties.ExternalServiceProperties,
	healthClient *http.Client,
) (*Registry, error) {
	services := map[string]properties.ServiceProperty{
		"account":      props.AccountService,
		"accountV2":    props.AccountService,
		"crm":          props.CrmService,
		"notification": props.NotificationService,
		"sales":        props.SalesService,
		"purchase":     props.PurchaseService,
		"logistics":    props.LogisticsService,
		"logistics2":   props.Logistics2Service,
		"first-mile":   props.FirstMileService,
		"second-mile":  props.SecondMileService,
		"discuss":      props.DiscussService,
		"pm-core":      props.PmCoreService,
		"ttcrs":        props.TtcrsService,
	}

	registry := &Registry{services: make(map[string]*ServiceLoadBalancer, len(services))}
	for name, service := range services {
		instances, err := buildInstances(name, service.Instances)
		if err != nil {
			return nil, err
		}
		registry.services[name] = NewServiceLoadBalancer(
			name,
			instances,
			service.LoadBalancer,
			service.HealthPath,
			healthClient,
		)
	}
	return registry, nil
}

func (r *Registry) Get(serviceName string) (*ServiceLoadBalancer, bool) {
	lb, ok := r.services[serviceName]
	return lb, ok
}

func buildInstances(serviceName string, props []properties.ServiceInstanceProperty) ([]ServiceInstance, error) {
	instances := make([]ServiceInstance, 0, len(props))
	for _, prop := range props {
		parsed, err := url.Parse(prop.URL)
		if err != nil {
			return nil, fmt.Errorf("parse instance URL for %s/%s: %w", serviceName, prop.Name, err)
		}
		instances = append(instances, ServiceInstance{Name: prop.Name, URL: parsed})
	}
	return instances, nil
}
```

- [ ] **Step 4: Provide registry in bootstrap**

In `api_gateway/src/cmd/bootstrap/all.go`, add this import:

```go
	"github.com/serp/api-gateway/src/core/service/loadbalancer"
```

Add this provider near the other utilities:

```go
		fx.Provide(loadbalancer.NewRegistry),
```

- [ ] **Step 5: Run registry tests**

Run:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/core/service/loadbalancer -run 'TestRegistry_|TestServiceLoadBalancer_' -count=1
```

Expected: PASS.

- [ ] **Step 6: Commit registry**

Run:

```powershell
git add src/core/service/loadbalancer src/cmd/bootstrap/all.go
git commit -m "feat(gateway): add downstream load balancer registry"
```

Expected: commit succeeds.

---

### Task 6: Generic Proxy Load Balancing

**Files:**
- Modify: `api_gateway/src/ui/controller/common/generic_proxy_controller.go`
- Modify: `api_gateway/src/ui/controller/common/generic_proxy_controller_test.go`

- [ ] **Step 1: Write generic proxy distribution test**

Append this test to `api_gateway/src/ui/controller/common/generic_proxy_controller_test.go`:

```go
func TestGenericProxyController_CRM_LoadBalancesAcrossHealthyInstances(t *testing.T) {
	gin.SetMode(gin.TestMode)

	var firstHits int32
	first := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		atomic.AddInt32(&firstHits, 1)
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"instance":"first"}`))
	}))
	defer first.Close()

	var secondHits int32
	second := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		atomic.AddInt32(&secondHits, 1)
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"instance":"second"}`))
	}))
	defer second.Close()

	controller := newTestGenericProxyControllerWithInstances(t, []properties.ServiceInstanceProperty{
		{Name: "crm-1", URL: first.URL},
		{Name: "crm-2", URL: second.URL},
	})

	r := gin.New()
	r.Any("/crm/api/v1/*proxyPath", controller.ProxyHandler("crm"))
	gateway := httptest.NewServer(r)
	defer gateway.Close()

	for i := 0; i < 4; i++ {
		resp, err := http.Get(gateway.URL + "/crm/api/v1/leads")
		if err != nil {
			t.Fatalf("request %d failed: %v", i, err)
		}
		_, _ = io.ReadAll(resp.Body)
		resp.Body.Close()
		if resp.StatusCode != http.StatusOK {
			t.Fatalf("expected status 200, got %d", resp.StatusCode)
		}
	}

	if atomic.LoadInt32(&firstHits) != 2 || atomic.LoadInt32(&secondHits) != 2 {
		t.Fatalf("expected 2 hits each, got first=%d second=%d", firstHits, secondHits)
	}
}

func TestGenericProxyController_CRM_AllUnhealthyReturns503(t *testing.T) {
	gin.SetMode(gin.TestMode)

	upstream := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusInternalServerError)
	}))
	defer upstream.Close()

	controller := newTestGenericProxyControllerWithInstances(t, []properties.ServiceInstanceProperty{
		{Name: "crm-1", URL: upstream.URL},
	})

	r := gin.New()
	r.Any("/crm/api/v1/*proxyPath", controller.ProxyHandler("crm"))
	gateway := httptest.NewServer(r)
	defer gateway.Close()

	resp, err := http.Get(gateway.URL + "/crm/api/v1/leads")
	if err != nil {
		t.Fatalf("first request failed: %v", err)
	}
	_, _ = io.ReadAll(resp.Body)
	resp.Body.Close()
	if resp.StatusCode != http.StatusInternalServerError {
		t.Fatalf("expected upstream 500 on first request, got %d", resp.StatusCode)
	}

	resp, err = http.Get(gateway.URL + "/crm/api/v1/leads")
	if err != nil {
		t.Fatalf("second request failed: %v", err)
	}
	_, _ = io.ReadAll(resp.Body)
	resp.Body.Close()

	if resp.StatusCode != http.StatusServiceUnavailable {
		t.Fatalf("expected 503 when instance is passively unhealthy, got %d", resp.StatusCode)
	}
}

func newTestGenericProxyControllerWithInstances(
	t *testing.T,
	instances []properties.ServiceInstanceProperty,
) *GenericProxyController {
	t.Helper()

	service := properties.ServiceProperty{
		LoadBalancer: properties.ServiceLoadBalancerProperty{
			Algorithm: properties.LoadBalancerAlgorithmRoundRobin,
			PassiveHealthCheck: properties.PassiveHealthCheckProperty{
				Enabled:             true,
				ConsecutiveFailures: 1,
				EjectionTime:        time.Minute,
			},
		},
		Instances: instances,
	}
	service.ApplyDefaults()

	props := &properties.ExternalServiceProperties{
		CrmService: service,
	}
	registry, err := loadbalancer.NewRegistry(
		&properties.ExternalServiceProperties{
			AccountService:      service,
			PTMTask:             service,
			PTMSchedule:         service,
			PurchaseService:     service,
			LogisticsService:    service,
			Logistics2Service:   service,
			FirstMileService:    service,
			SecondMileService:   service,
			CrmService:          service,
			NotificationService: service,
			SalesService:        service,
			DiscussService:      service,
			PmCoreService:       service,
			TtcrsService:        service,
		},
		nil,
	)
	if err != nil {
		t.Fatalf("new registry: %v", err)
	}

	return NewGenericProxyController(props, registry, defaultResilienceProps(), properties.NewDefaultTransportProperties())
}
```

Add imports used by the test:

```go
	"sync/atomic"
	"time"

	"github.com/serp/api-gateway/src/core/service/loadbalancer"
```

- [ ] **Step 2: Run tests to verify they fail**

Run:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/ui/controller/common -run 'TestGenericProxyController_CRM_LoadBalancesAcrossHealthyInstances|TestGenericProxyController_CRM_AllUnhealthyReturns503' -count=1
```

Expected: FAIL because `NewGenericProxyController` does not accept a registry and the controller does not load-balance.

- [ ] **Step 3: Modify controller fields and constructor**

In `api_gateway/src/ui/controller/common/generic_proxy_controller.go`, add import:

```go
	"context"

	"github.com/serp/api-gateway/src/core/service/loadbalancer"
```

Change `GenericProxyController`:

```go
type GenericProxyController struct {
	proxies       map[string]*httputil.ReverseProxy
	loadBalancers *loadbalancer.Registry
}
```

Change constructor signature:

```go
func NewGenericProxyController(
	svcProps *properties.ExternalServiceProperties,
	loadBalancers *loadbalancer.Registry,
	resProps *properties.ResilienceProperties,
	transportProps *properties.TransportProperties,
) *GenericProxyController {
	routes := buildServiceRoutes(svcProps)

	controller := &GenericProxyController{
		proxies:       make(map[string]*httputil.ReverseProxy, len(routes)),
		loadBalancers: loadBalancers,
	}
```

Keep the loop that builds one proxy per route.

- [ ] **Step 4: Add selected instance context helpers**

Add below `GenericProxyController`:

```go
type selectedInstanceContextKey struct{}

func withSelectedInstance(ctx context.Context, instance *loadbalancer.ServiceInstance) context.Context {
	return context.WithValue(ctx, selectedInstanceContextKey{}, instance)
}

func selectedInstanceFromContext(ctx context.Context) (*loadbalancer.ServiceInstance, bool) {
	instance, ok := ctx.Value(selectedInstanceContextKey{}).(*loadbalancer.ServiceInstance)
	return instance, ok
}
```

- [ ] **Step 5: Select target in `ProxyHandler`**

Replace the returned handler body with:

```go
	return func(ctx *gin.Context) {
		lb, ok := c.loadBalancers.Get(serviceName)
		if !ok {
			utils.AbortErrorHandleCustomMessage(ctx, constant.GeneralInternalServerError,
				fmt.Sprintf("Load balancer not configured for service: %s", serviceName))
			return
		}

		instance, err := lb.Next()
		if err != nil {
			writeServiceUnavailable(ctx.Writer)
			ctx.Abort()
			return
		}

		req := ctx.Request.WithContext(withSelectedInstance(ctx.Request.Context(), instance))
		proxy.ServeHTTP(ctx.Writer, req)
	}
```

Add this helper near `stripHopByHopHeaders` so the no-target path uses the same response body as the existing circuit-breaker path:

```go
func writeServiceUnavailable(w http.ResponseWriter) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusServiceUnavailable)
	_, _ = w.Write([]byte(`{"code":503,"message":"Service temporarily unavailable. Please try again later."}`))
}
```

- [ ] **Step 6: Update `buildProxy` director to use selected instance**

In `buildProxy`, stop using a fixed runtime target. Replace:

```go
remote, err := url.Parse(route.Target)
if err != nil {
	return nil, fmt.Errorf("invalid target URL for %s: %w", route.Name, err)
}

cb := createCircuitBreaker(route.Name, resProps)

proxy := httputil.NewSingleHostReverseProxy(remote)
```

with:

```go
cb := createCircuitBreaker(route.Name, resProps)

proxy := &httputil.ReverseProxy{}
```

Inside `proxy.Director`, replace target assignment with:

```go
		instance, ok := selectedInstanceFromContext(req.Context())
		if !ok || instance.URL == nil {
			return
		}

		req.Host = instance.URL.Host
		req.URL.Scheme = instance.URL.Scheme
		req.URL.Host = instance.URL.Host
```

Keep header cloning, hop-by-hop stripping, forwarded headers, path rewrite, and query preservation. Remove `net/url` from imports if it becomes unused.

- [ ] **Step 7: Report passive health from response and error paths**

Add to `buildProxy`:

```go
	proxy.ModifyResponse = func(resp *http.Response) error {
		if instance, ok := selectedInstanceFromContext(resp.Request.Context()); ok {
			if lb, exists := c.loadBalancers.Get(route.Name); exists {
				lb.Report(instance.Name, resp.StatusCode, nil)
			}
		}
		return nil
	}
```

At the top of `proxy.ErrorHandler`, add:

```go
		if instance, ok := selectedInstanceFromContext(r.Context()); ok {
			if lb, exists := c.loadBalancers.Get(route.Name); exists {
				lb.Report(instance.Name, 0, err)
			}
		}
```

Inside the existing circuit-breaker branch of `proxy.ErrorHandler`, replace the inline 503 body write with:

```go
			writeServiceUnavailable(w)
			return
```

- [ ] **Step 8: Update all `NewGenericProxyController` test calls**

Every existing test call must pass a registry:

```go
registry, err := loadbalancer.NewRegistry(svcProps, nil)
if err != nil {
	t.Fatalf("new registry: %v", err)
}
controller := NewGenericProxyController(svcProps, registry, defaultResilienceProps(), properties.NewDefaultTransportProperties())
```

- [ ] **Step 9: Run generic proxy tests**

Run:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/ui/controller/common -run 'TestGenericProxyController_' -count=1
```

Expected: PASS.

- [ ] **Step 10: Commit generic proxy load balancing**

Run:

```powershell
git add src/ui/controller/common/generic_proxy_controller.go src/ui/controller/common/generic_proxy_controller_test.go
git commit -m "feat(gateway): load balance generic proxy routes"
```

Expected: commit succeeds.

---

### Task 7: Active Health Lifecycle

**Files:**
- Modify: `api_gateway/src/core/service/loadbalancer/registry.go`
- Modify: `api_gateway/src/core/service/loadbalancer/registry_test.go`
- Modify: `api_gateway/src/cmd/bootstrap/all.go`

- [ ] **Step 1: Add registry lifecycle methods**

In `api_gateway/src/core/service/loadbalancer/registry.go`, add:

```go
func (r *Registry) Start(ctx context.Context) {
	for _, lb := range r.services {
		lb.Start(ctx)
	}
}
```

Add `context` to imports.

- [ ] **Step 2: Add FX lifecycle constructor**

In `api_gateway/src/core/service/loadbalancer/registry.go`, add:

```go
func NewRegistryWithLifecycle(
	lc fx.Lifecycle,
	props *properties.ExternalServiceProperties,
) (*Registry, error) {
	registry, err := NewRegistry(props, nil)
	if err != nil {
		return nil, err
	}

	var cancel context.CancelFunc
	lc.Append(fx.Hook{
		OnStart: func(ctx context.Context) error {
			healthCtx, stop := context.WithCancel(context.Background())
			cancel = stop
			registry.Start(healthCtx)
			return nil
		},
		OnStop: func(ctx context.Context) error {
			if cancel != nil {
				cancel()
			}
			return nil
		},
	})

	return registry, nil
}
```

Add import:

```go
	"go.uber.org/fx"
```

- [ ] **Step 3: Update bootstrap provider**

In `api_gateway/src/cmd/bootstrap/all.go`, replace:

```go
		fx.Provide(loadbalancer.NewRegistry),
```

with:

```go
		fx.Provide(loadbalancer.NewRegistryWithLifecycle),
```

- [ ] **Step 4: Run loadbalancer tests**

Run:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/core/service/loadbalancer -count=1
```

Expected: PASS.

- [ ] **Step 5: Commit active health lifecycle**

Run:

```powershell
git add src/core/service/loadbalancer/registry.go src/core/service/loadbalancer/registry_test.go src/cmd/bootstrap/all.go
git commit -m "feat(gateway): start downstream active health checks"
```

Expected: commit succeeds.

---

### Task 8: Trusted Proxy Middleware and Readiness

**Files:**
- Create: `api_gateway/src/kernel/properties/proxy_properties.go`
- Create: `api_gateway/src/ui/middleware/trusted_proxy_middleware.go`
- Create: `api_gateway/src/ui/middleware/trusted_proxy_middleware_test.go`
- Modify: `api_gateway/src/cmd/bootstrap/all.go`
- Modify: `api_gateway/src/ui/router/router.go`
- Modify: `api_gateway/src/config/default.yaml`
- Modify: `api_gateway/src/config/local.yaml`
- Modify: `api_gateway/src/config/production.yaml`

- [ ] **Step 1: Create proxy properties**

Create `api_gateway/src/kernel/properties/proxy_properties.go`:

```go
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package properties

import (
	"fmt"
	"net"

	"github.com/golibs-starter/golib/config"
)

type ProxyProperties struct {
	TrustForwardedHeaders bool     `mapstructure:"trustForwardedHeaders"`
	TrustedProxies        []string `mapstructure:"trustedProxies"`
}

func (p ProxyProperties) Prefix() string {
	return "app.proxy"
}

func NewProxyProperties(loader config.Loader) (*ProxyProperties, error) {
	props := ProxyProperties{}
	if err := loader.Bind(&props); err != nil {
		return &props, err
	}
	return &props, props.Validate()
}

func (p ProxyProperties) Validate() error {
	if !p.TrustForwardedHeaders {
		return nil
	}
	if len(p.TrustedProxies) == 0 {
		return fmt.Errorf("app.proxy.trustedProxies is required when trustForwardedHeaders is true")
	}
	for _, trusted := range p.TrustedProxies {
		if ip := net.ParseIP(trusted); ip != nil {
			continue
		}
		if _, _, err := net.ParseCIDR(trusted); err != nil {
			return fmt.Errorf("invalid trusted proxy %q: %w", trusted, err)
		}
	}
	return nil
}
```

- [ ] **Step 2: Write trusted proxy middleware tests**

Create `api_gateway/src/ui/middleware/trusted_proxy_middleware_test.go`:

```go
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package middleware

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/kernel/properties"
)

func TestTrustedProxyMiddleware_UntrustedRemoteStripsForwardedHeaders(t *testing.T) {
	gin.SetMode(gin.TestMode)

	middleware := NewTrustedProxyMiddleware(&properties.ProxyProperties{
		TrustForwardedHeaders: true,
		TrustedProxies:        []string{"10.0.0.0/8"},
	})

	router := gin.New()
	router.Use(middleware.Handler())
	router.GET("/test", func(c *gin.Context) {
		if got := c.Request.Header.Get("X-Forwarded-Proto"); got != "" {
			t.Fatalf("expected forwarded proto stripped, got %q", got)
		}
		c.Status(http.StatusOK)
	})

	req := httptest.NewRequest(http.MethodGet, "/test", nil)
	req.RemoteAddr = "203.0.113.10:12345"
	req.Header.Set("X-Forwarded-Proto", "https")
	req.Header.Set("X-Forwarded-For", "198.51.100.1")

	resp := httptest.NewRecorder()
	router.ServeHTTP(resp, req)

	if resp.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", resp.Code)
	}
}

func TestTrustedProxyMiddleware_TrustedRemoteKeepsForwardedHeaders(t *testing.T) {
	gin.SetMode(gin.TestMode)

	middleware := NewTrustedProxyMiddleware(&properties.ProxyProperties{
		TrustForwardedHeaders: true,
		TrustedProxies:        []string{"10.0.0.0/8"},
	})

	router := gin.New()
	router.Use(middleware.Handler())
	router.GET("/test", func(c *gin.Context) {
		if got := c.Request.Header.Get("X-Forwarded-Proto"); got != "https" {
			t.Fatalf("expected forwarded proto kept, got %q", got)
		}
		c.Status(http.StatusOK)
	})

	req := httptest.NewRequest(http.MethodGet, "/test", nil)
	req.RemoteAddr = "10.1.2.3:12345"
	req.Header.Set("X-Forwarded-Proto", "https")

	resp := httptest.NewRecorder()
	router.ServeHTTP(resp, req)

	if resp.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", resp.Code)
	}
}
```

- [ ] **Step 3: Implement trusted proxy middleware**

Create `api_gateway/src/ui/middleware/trusted_proxy_middleware.go`:

```go
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package middleware

import (
	"net"

	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/kernel/properties"
)

type TrustedProxyMiddleware struct {
	props    *properties.ProxyProperties
	trusted  []*net.IPNet
	trustedIPs []net.IP
}

func NewTrustedProxyMiddleware(props *properties.ProxyProperties) *TrustedProxyMiddleware {
	m := &TrustedProxyMiddleware{props: props}
	for _, trusted := range props.TrustedProxies {
		if ip := net.ParseIP(trusted); ip != nil {
			m.trustedIPs = append(m.trustedIPs, ip)
			continue
		}
		if _, network, err := net.ParseCIDR(trusted); err == nil {
			m.trusted = append(m.trusted, network)
		}
	}
	return m
}

func (m *TrustedProxyMiddleware) Handler() gin.HandlerFunc {
	return func(c *gin.Context) {
		if m.props == nil || !m.props.TrustForwardedHeaders || !m.isTrustedRemote(c.Request.RemoteAddr) {
			stripForwardedHeaders(c)
		}
		c.Next()
	}
}

func (m *TrustedProxyMiddleware) TrustedProxyValues() []string {
	if m.props == nil {
		return nil
	}
	return append([]string(nil), m.props.TrustedProxies...)
}

func (m *TrustedProxyMiddleware) isTrustedRemote(remoteAddr string) bool {
	host, _, err := net.SplitHostPort(remoteAddr)
	if err != nil {
		host = remoteAddr
	}
	ip := net.ParseIP(host)
	if ip == nil {
		return false
	}
	for _, trustedIP := range m.trustedIPs {
		if trustedIP.Equal(ip) {
			return true
		}
	}
	for _, network := range m.trusted {
		if network.Contains(ip) {
			return true
		}
	}
	return false
}

func stripForwardedHeaders(c *gin.Context) {
	c.Request.Header.Del("X-Forwarded-For")
	c.Request.Header.Del("X-Forwarded-Host")
	c.Request.Header.Del("X-Forwarded-Proto")
}
```

- [ ] **Step 4: Wire middleware and readiness into router**

In `api_gateway/src/ui/router/router.go`, add `TrustedProxyMiddleware` to `RegisterRoutersIn`:

```go
	TrustedProxyMiddleware        *middleware.TrustedProxyMiddleware
```

At the start of `RegisterGinRouters`, before CORS and rate limit:

```go
	if p.TrustedProxyMiddleware != nil {
		if trusted := p.TrustedProxyMiddleware.TrustedProxyValues(); len(trusted) > 0 {
			if err := p.Engine.SetTrustedProxies(trusted); err != nil {
				panic(err)
			}
		}
		p.Engine.Use(p.TrustedProxyMiddleware.Handler())
	}
```

Add readiness route next to health and info:

```go
	group.GET("/actuator/readiness", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"status": "UP"})
	})
```

Add `net/http` import to `router.go`.

- [ ] **Step 5: Provide proxy properties and middleware**

In `api_gateway/src/cmd/bootstrap/all.go`, add:

```go
		golib.ProvideProps(properties.NewProxyProperties),
		fx.Provide(middleware.NewTrustedProxyMiddleware),
```

Place `NewProxyProperties` beside other app properties and middleware provider beside other middleware.

- [ ] **Step 6: Add proxy config to YAML files**

Add this under `app:` in `default.yaml`, `local.yaml`, and `production.yaml`:

```yaml
  proxy:
    trustForwardedHeaders: false
    trustedProxies: []
```

For production, keep `false` by default. Deployment can turn it on after the external LB CIDR is known.

- [ ] **Step 7: Run middleware and router tests**

Run:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/ui/middleware ./src/ui/router -run 'TestTrustedProxyMiddleware_|Test' -count=1
```

Expected: PASS.

- [ ] **Step 8: Commit readiness and trusted proxy support**

Run:

```powershell
git add src/kernel/properties/proxy_properties.go src/ui/middleware/trusted_proxy_middleware.go src/ui/middleware/trusted_proxy_middleware_test.go src/ui/router/router.go src/cmd/bootstrap/all.go src/config/default.yaml src/config/local.yaml src/config/production.yaml
git commit -m "feat(gateway): add readiness and trusted proxy handling"
```

Expected: commit succeeds.

---

### Task 9: Forwarded Header Target Scheme

**Files:**
- Modify: `api_gateway/src/ui/controller/common/generic_proxy_controller.go`
- Modify: `api_gateway/src/ui/controller/common/generic_proxy_controller_test.go`

- [ ] **Step 1: Add forwarded proto test**

Append this test to `api_gateway/src/ui/controller/common/generic_proxy_controller_test.go`:

```go
func TestGenericProxyController_ForwardsTrustedProto(t *testing.T) {
	gin.SetMode(gin.TestMode)

	var gotProto string
	upstream := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotProto = r.Header.Get("X-Forwarded-Proto")
		w.WriteHeader(http.StatusOK)
	}))
	defer upstream.Close()

	controller := newTestGenericProxyControllerWithInstances(t, []properties.ServiceInstanceProperty{
		{Name: "crm-1", URL: upstream.URL},
	})

	r := gin.New()
	r.Any("/crm/api/v1/*proxyPath", controller.ProxyHandler("crm"))
	gateway := httptest.NewServer(r)
	defer gateway.Close()

	req, err := http.NewRequest(http.MethodGet, gateway.URL+"/crm/api/v1/leads", nil)
	if err != nil {
		t.Fatalf("new request: %v", err)
	}
	req.Header.Set("X-Forwarded-Proto", "https")

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		t.Fatalf("request failed: %v", err)
	}
	_, _ = io.ReadAll(resp.Body)
	resp.Body.Close()

	if gotProto != "https" {
		t.Fatalf("expected forwarded proto https, got %q", gotProto)
	}
}
```

- [ ] **Step 2: Run the focused test**

Run:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/ui/controller/common -run '^TestGenericProxyController_ForwardsTrustedProto$' -count=1
```

Expected: FAIL if `GenericProxyController` still overwrites `X-Forwarded-Proto` with `"http"`.

- [ ] **Step 3: Update proto helper**

In `api_gateway/src/ui/controller/common/generic_proxy_controller.go`, add:

```go
func forwardedProto(req *http.Request) string {
	if proto := req.Header.Get("X-Forwarded-Proto"); proto != "" {
		return proto
	}
	if req.TLS != nil {
		return "https"
	}
	return "http"
}
```

Replace:

```go
req.Header.Set("X-Forwarded-Proto", "http")
```

with:

```go
req.Header.Set("X-Forwarded-Proto", forwardedProto(req))
```

The trusted proxy middleware from Task 8 strips spoofed forwarded headers before this helper runs.

- [ ] **Step 4: Run generic proxy tests**

Run:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/ui/controller/common -run 'TestGenericProxyController_' -count=1
```

Expected: PASS.

- [ ] **Step 5: Commit forwarded proto fix**

Run:

```powershell
git add src/ui/controller/common/generic_proxy_controller.go src/ui/controller/common/generic_proxy_controller_test.go
git commit -m "fix(gateway): preserve trusted forwarded proto"
```

Expected: commit succeeds.

---

### Task 10: Final Verification

**Files:**
- No source edits unless verification reveals a failure.

- [ ] **Step 1: Run gofmt**

Run from `api_gateway/`:

```powershell
gofmt -w src/kernel/properties src/core/service/loadbalancer src/ui/middleware src/ui/router src/ui/controller/common src/infrastructure/client/account src/infrastructure/client/crm src/infrastructure/client/ptm src/cmd/bootstrap
```

Expected: command exits 0.

- [ ] **Step 2: Run focused tests**

Run:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/kernel/properties ./src/core/service/loadbalancer ./src/ui/middleware ./src/ui/controller/common ./src/ui/router -count=1
```

Expected: PASS.

- [ ] **Step 3: Run full gateway tests**

Run:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./... -count=1
```

Expected: PASS.

- [ ] **Step 4: Run vet**

Run:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go vet ./...
```

Expected: PASS.

- [ ] **Step 5: Remove local Go cache**

From repo root, resolve and remove only the local cache under `api_gateway`:

```powershell
$cachePath = Resolve-Path 'api_gateway\.codex-go-cache' -ErrorAction SilentlyContinue
$apiGatewayPath = (Resolve-Path 'api_gateway').Path
if ($cachePath -and $cachePath.Path.StartsWith($apiGatewayPath)) {
  Remove-Item -LiteralPath $cachePath.Path -Recurse -Force
}
```

Expected: cache removed if present.

- [ ] **Step 6: Check final status**

Run:

```powershell
git status --short
```

Expected: no uncommitted changes from this plan. Ignore unrelated user changes outside `api_gateway` and docs if they are present.

- [ ] **Step 7: Final commit if formatting changed files**

If Step 1 changed formatting after the last feature commit, run:

```powershell
git add api_gateway
git commit -m "chore(gateway): format load balancer support changes"
```

Expected: commit succeeds only if there are formatting-only changes.
