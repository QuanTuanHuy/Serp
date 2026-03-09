/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package common

import (
	"fmt"
	"net"
	"net/http"
	"net/http/httputil"
	"net/url"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/core/domain/constant"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/kernel/utils"
	"github.com/sony/gobreaker/v2"
)

// ServiceRoute defines the proxy configuration for a single backend service.
type ServiceRoute struct {
	Name         string
	SourcePrefix string
	TargetPrefix string
	Target       string
}

// hopByHopHeaders are headers that must not be forwarded by proxies.
var hopByHopHeaders = []string{
	"Connection",
	"Keep-Alive",
	"Proxy-Authenticate",
	"Proxy-Authorization",
	"TE",
	"Trailer",
	"Transfer-Encoding",
	"Upgrade",
}

type GenericProxyController struct {
	proxies map[string]*httputil.ReverseProxy
}

func NewGenericProxyController(
	svcProps *properties.ExternalServiceProperties,
	resProps *properties.ResilienceProperties,
) *GenericProxyController {
	routes := buildServiceRoutes(svcProps)

	controller := &GenericProxyController{
		proxies: make(map[string]*httputil.ReverseProxy, len(routes)),
	}

	for _, route := range routes {
		proxy, err := controller.buildProxy(route, resProps)
		if err != nil {
			log.Warn("Failed to build proxy for service %s: %v", route.Name, err)
			continue
		}
		controller.proxies[route.Name] = proxy
	}

	return controller
}

// buildServiceRoutes creates the full list of service routes from properties.
func buildServiceRoutes(props *properties.ExternalServiceProperties) []ServiceRoute {
	return []ServiceRoute{
		{
			Name:         "crm",
			SourcePrefix: "/crm/api/v1",
			TargetPrefix: "/crm/api/v1",
			Target:       props.CrmService.BaseURL(),
		},
		{
			Name:         "notification",
			SourcePrefix: "/ns/api/v1",
			TargetPrefix: "/notification/api/v1",
			Target:       props.NotificationService.BaseURL(),
		},
		{
			Name:         "sales",
			SourcePrefix: "/sales/api/v1",
			TargetPrefix: "/sales/api/v1",
			Target:       props.SalesService.BaseURL(),
		},
		{
			Name:         "purchase",
			SourcePrefix: "/purchase-service/api/v1",
			TargetPrefix: "/purchase/api/v1",
			Target:       props.PurchaseService.BaseURL(),
		},
		{
			Name:         "logistics",
			SourcePrefix: "/logistics/api/v1",
			TargetPrefix: "/logistics/api/v1",
			Target:       props.LogisticsService.BaseURL(),
		},
		{
			Name:         "discuss",
			SourcePrefix: "/discuss/api/v1",
			TargetPrefix: "/discuss/api/v1",
			Target:       props.DiscussService.BaseURL(),
		},
	}
}

// ProxyHandler returns a gin.HandlerFunc that proxies requests to the named service.
func (c *GenericProxyController) ProxyHandler(serviceName string) gin.HandlerFunc {
	proxy, exists := c.proxies[serviceName]
	if !exists {
		return func(ctx *gin.Context) {
			utils.AbortErrorHandleCustomMessage(ctx, constant.GeneralInternalServerError,
				fmt.Sprintf("Proxy not configured for service: %s", serviceName))
		}
	}

	return func(ctx *gin.Context) {
		proxy.ServeHTTP(ctx.Writer, ctx.Request)
	}
}

// buildProxy creates a pre-configured reverse proxy with circuit breaker and retry support.
func (c *GenericProxyController) buildProxy(route ServiceRoute, resProps *properties.ResilienceProperties) (*httputil.ReverseProxy, error) {
	remote, err := url.Parse(route.Target)
	if err != nil {
		return nil, fmt.Errorf("invalid target URL for %s: %w", route.Name, err)
	}

	cb := createCircuitBreaker(route.Name, resProps)

	proxy := httputil.NewSingleHostReverseProxy(remote)

	proxy.Transport = utils.NewResilientTransport(
		cb,
		resProps.MaxRetries,
		resProps.InitialDelay,
		resProps.MaxDelay,
	)

	sourcePrefix := route.SourcePrefix
	targetPrefix := route.TargetPrefix

	proxy.Director = func(req *http.Request) {
		// Clone headers, then sanitize
		req.Header = req.Header.Clone()
		stripHopByHopHeaders(req.Header)

		// Set standard proxy headers
		if clientIP, _, err := net.SplitHostPort(req.RemoteAddr); err == nil {
			if prior := req.Header.Get("X-Forwarded-For"); prior != "" {
				req.Header.Set("X-Forwarded-For", prior+", "+clientIP)
			} else {
				req.Header.Set("X-Forwarded-For", clientIP)
			}
		}
		req.Header.Set("X-Forwarded-Host", req.Host)
		req.Header.Set("X-Forwarded-Proto", "http")

		req.Host = remote.Host
		req.URL.Scheme = remote.Scheme
		req.URL.Host = remote.Host

		if sourcePrefix != "" && targetPrefix != "" {
			if strings.HasPrefix(req.URL.Path, sourcePrefix) {
				req.URL.Path = targetPrefix + strings.TrimPrefix(req.URL.Path, sourcePrefix)
			}
		}
		// RawQuery is preserved automatically since req is the incoming request
	}

	proxy.ErrorHandler = func(w http.ResponseWriter, r *http.Request, err error) {
		if err == gobreaker.ErrOpenState || err == gobreaker.ErrTooManyRequests {
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusServiceUnavailable)
			_, _ = w.Write([]byte(`{"code":503,"message":"Service temporarily unavailable. Please try again later."}`))
			return
		}

		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusBadGateway)
		_, _ = w.Write([]byte(`{"code":502,"message":"Bad Gateway"}`))
	}

	return proxy, nil
}

func createCircuitBreaker(name string, props *properties.ResilienceProperties) *gobreaker.CircuitBreaker[*http.Response] {
	settings := gobreaker.Settings{
		Name:        fmt.Sprintf("proxy-%s", name),
		MaxRequests: props.MaxRequests,
		Interval:    props.Interval,
		Timeout:     props.Timeout,
		ReadyToTrip: func(counts gobreaker.Counts) bool {
			if counts.ConsecutiveFailures >= props.ConsecutiveFailures {
				return true
			}
			if counts.Requests >= props.MinRequests {
				failureRatio := float64(counts.TotalFailures) / float64(counts.Requests)
				return failureRatio >= props.FailureRatio
			}
			return false
		},
		OnStateChange: func(name string, from gobreaker.State, to gobreaker.State) {
			log.Infof("[CircuitBreaker] %s: %s -> %s", name, from, to)
		},
	}

	return gobreaker.NewCircuitBreaker[*http.Response](settings)
}

// stripHopByHopHeaders removes hop-by-hop headers that must not be forwarded.
func stripHopByHopHeaders(h http.Header) {
	for _, header := range hopByHopHeaders {
		h.Del(header)
	}
}
