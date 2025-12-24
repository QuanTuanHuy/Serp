/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package common

import (
	"fmt"
	"net/http"
	"net/http/httputil"
	"net/url"
	"strings"
	"sync"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/core/domain/constant"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/kernel/utils"
	"github.com/sony/gobreaker/v2"
)

const (
	ServiceCRM          = "crm"
	ServiceNotification = "notification"
	ServiceAccount      = "account"
	ServicePTM          = "ptm"
	ServicePurchase     = "purchase"
	ServiceLogistics    = "logistics"
)

type GenericProxyController struct {
	props    *properties.ExternalServiceProperties
	breakers map[string]*gobreaker.CircuitBreaker[*http.Response]
	mu       sync.RWMutex
}

func NewGenericProxyController(props *properties.ExternalServiceProperties) *GenericProxyController {
	controller := &GenericProxyController{
		props:    props,
		breakers: make(map[string]*gobreaker.CircuitBreaker[*http.Response]),
	}

	services := []string{ServiceCRM, ServiceNotification, ServiceAccount, ServicePTM, ServicePurchase, ServiceLogistics}
	for _, svc := range services {
		controller.breakers[svc] = controller.createCircuitBreaker(svc)
	}

	return controller
}

func (c *GenericProxyController) ProxyToCRM(ctx *gin.Context) {
	target := fmt.Sprintf("http://%s:%s", c.props.CrmService.Host, c.props.CrmService.Port)
	c.proxyWithResilience(ctx, target, "/crm/api/v1/proxy", "/crm/api/v1", ServiceCRM)
}

func (c *GenericProxyController) ProxyToNotification(ctx *gin.Context) {
	target := fmt.Sprintf("http://%s:%s", c.props.NotificationService.Host, c.props.NotificationService.Port)
	c.proxyWithResilience(ctx, target, "/ns/api/v1", "/notification/api/v1", ServiceNotification)
}

func (c *GenericProxyController) createCircuitBreaker(name string) *gobreaker.CircuitBreaker[*http.Response] {
	settings := gobreaker.Settings{
		Name:        fmt.Sprintf("proxy-%s", name),
		MaxRequests: 3,
		Interval:    60 * time.Second,
		Timeout:     30 * time.Second,
		ReadyToTrip: func(counts gobreaker.Counts) bool {
			if counts.ConsecutiveFailures >= 5 {
				return true
			}
			if counts.Requests >= 10 {
				failureRatio := float64(counts.TotalFailures) / float64(counts.Requests)
				return failureRatio >= 0.6
			}
			return false
		},
		OnStateChange: func(name string, from gobreaker.State, to gobreaker.State) {
			// TODO: Add observability logging/metrics here
			log.Info("[CircuitBreaker] %s: %s -> %s", name, from, to)
		},
	}

	return gobreaker.NewCircuitBreaker[*http.Response](settings)
}

func (c *GenericProxyController) getCircuitBreaker(serviceName string) *gobreaker.CircuitBreaker[*http.Response] {
	c.mu.RLock()
	cb, exists := c.breakers[serviceName]
	c.mu.RUnlock()

	if exists {
		return cb
	}

	c.mu.Lock()
	defer c.mu.Unlock()

	if cb, exists = c.breakers[serviceName]; exists {
		return cb
	}

	cb = c.createCircuitBreaker(serviceName)
	c.breakers[serviceName] = cb
	return cb
}

// proxyWithResilience proxies request with circuit breaker and retry support
func (c *GenericProxyController) proxyWithResilience(ctx *gin.Context, target, sourcePrefix, targetPrefix, serviceName string) {
	remote, err := url.Parse(target)
	if err != nil {
		utils.AbortErrorHandleCustomMessage(ctx, constant.GeneralInternalServerError, "Invalid target URL")
		return
	}

	cb := c.getCircuitBreaker(serviceName)

	proxy := httputil.NewSingleHostReverseProxy(remote)

	// Chain: Retry -> CircuitBreaker -> DefaultTransport
	proxy.Transport = utils.NewResilientTransport(
		cb,
		3,
		100*time.Millisecond,
		2*time.Second,
	)

	proxy.Director = func(req *http.Request) {
		req.Header = ctx.Request.Header.Clone()
		req.Host = remote.Host
		req.URL.Scheme = remote.Scheme
		req.URL.Host = remote.Host

		if sourcePrefix != "" && targetPrefix != "" {
			req.URL.Path = strings.Replace(ctx.Request.URL.Path, sourcePrefix, targetPrefix, 1)
		} else {
			req.URL.Path = ctx.Request.URL.Path
		}
		req.URL.RawQuery = ctx.Request.URL.RawQuery
	}

	proxy.ErrorHandler = func(w http.ResponseWriter, r *http.Request, err error) {
		if err == gobreaker.ErrOpenState || err == gobreaker.ErrTooManyRequests {
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusServiceUnavailable)
			w.Write([]byte(`{"code":503,"message":"Service temporarily unavailable. Please try again later."}`))
			return
		}

		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusBadGateway)
		w.Write([]byte(`{"code":502,"message":"Bad Gateway"}`))
	}

	proxy.ServeHTTP(ctx.Writer, ctx.Request)
}
