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

	"github.com/gin-gonic/gin"
	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/core/domain/constant"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/kernel/utils"
	"github.com/sony/gobreaker/v2"
)

// wsRoute holds the pre-built proxy and metadata for a single WebSocket service.
type wsRoute struct {
	proxy     *httputil.ReverseProxy
	targetURL *url.URL
}

// WebSocketProxyController provides WebSocket reverse-proxy handlers.
type WebSocketProxyController struct {
	routes map[string]*wsRoute
}

// NewWebSocketProxyController creates proxy instances for every service that
// has a non-empty WebSocketPath configured.
func NewWebSocketProxyController(
	svcProps *properties.ExternalServiceProperties,
	resProps *properties.ResilienceProperties,
) *WebSocketProxyController {
	services := map[string]properties.ServiceProperty{
		"discuss":      svcProps.DiscussService,
		"notification": svcProps.NotificationService,
	}

	routes := make(map[string]*wsRoute, len(services))

	for name, svc := range services {
		if svc.WebSocketPath == "" {
			log.Warnf("[WebSocketProxy] Skipping %s: no WebSocketPath configured", name)
			continue
		}

		baseURL := svc.BaseURL()
		target, err := url.Parse(baseURL)
		if err != nil {
			log.Warnf("[WebSocketProxy] Failed to parse target URL for %s (%s): %v", name, baseURL, err)
			continue
		}

		proxy := buildWebSocketProxy(name, target, svc.WebSocketPath, resProps)
		routes[name] = &wsRoute{proxy: proxy, targetURL: target}
		log.Infof("[WebSocketProxy] Registered %s -> %s%s", name, baseURL, svc.WebSocketPath)
	}

	return &WebSocketProxyController{routes: routes}
}

// ProxyHandler returns a gin.HandlerFunc that proxies WebSocket connections to
// the named backend service.
func (c *WebSocketProxyController) ProxyHandler(serviceName string) gin.HandlerFunc {
	route, exists := c.routes[serviceName]
	if !exists {
		return func(ctx *gin.Context) {
			log.Errorf("[WebSocketProxy] No route configured for service: %s", serviceName)
			utils.AbortErrorHandleCustomMessage(ctx, constant.GeneralInternalServerError,
				fmt.Sprintf("WebSocket proxy not configured for service: %s", serviceName))
		}
	}

	return func(ctx *gin.Context) {
		route.proxy.ServeHTTP(ctx.Writer, ctx.Request)
	}
}

// buildWebSocketProxy creates a pre-configured reverse proxy that correctly
// forwards WebSocket upgrade requests, including hop-by-hop headers that
// standard HTTP proxies strip.
func buildWebSocketProxy(
	name string,
	target *url.URL,
	wsPath string,
	resProps *properties.ResilienceProperties,
) *httputil.ReverseProxy {
	cb := createWSCircuitBreaker(name, resProps)

	proxy := httputil.NewSingleHostReverseProxy(target)

	// Use a circuit-breaker-aware transport but WITHOUT retry logic,
	// because WebSocket upgrades are not idempotent.
	proxy.Transport = utils.NewCircuitBreakerTransport(http.DefaultTransport, cb)

	proxy.Director = func(req *http.Request) {
		// Preserve original WebSocket headers before any modification.
		// upgradeHeader := req.Header.Get("Upgrade")
		// connectionHeader := req.Header.Get("Connection")
		// secWSKey := req.Header.Get("Sec-WebSocket-Key")
		// secWSVersion := req.Header.Get("Sec-WebSocket-Version")
		// secWSProtocol := req.Header.Get("Sec-WebSocket-Protocol")
		// secWSExtensions := req.Header.Get("Sec-WebSocket-Extensions")

		// Clone headers to avoid mutating the original request.
		req.Header = req.Header.Clone()

		// Set target routing.
		req.URL.Scheme = target.Scheme
		req.URL.Host = target.Host
		req.URL.Path = wsPath
		req.Host = target.Host

		// Restore WebSocket hop-by-hop headers that would otherwise be stripped.
		// if upgradeHeader != "" {
		// 	req.Header.Set("Upgrade", upgradeHeader)
		// }
		// if connectionHeader != "" {
		// 	req.Header.Set("Connection", connectionHeader)
		// }
		// if secWSKey != "" {
		// 	req.Header.Set("Sec-WebSocket-Key", secWSKey)
		// }
		// if secWSVersion != "" {
		// 	req.Header.Set("Sec-WebSocket-Version", secWSVersion)
		// }
		// if secWSProtocol != "" {
		// 	req.Header.Set("Sec-WebSocket-Protocol", secWSProtocol)
		// }
		// if secWSExtensions != "" {
		// 	req.Header.Set("Sec-WebSocket-Extensions", secWSExtensions)
		// }

		// Standard proxy headers.
		// if clientIP, _, err := net.SplitHostPort(req.RemoteAddr); err == nil {
		// 	if prior := req.Header.Get("X-Forwarded-For"); prior != "" {
		// 		req.Header.Set("X-Forwarded-For", prior+", "+clientIP)
		// 	} else {
		// 		req.Header.Set("X-Forwarded-For", clientIP)
		// 	}
		// }
		// req.Header.Set("X-Forwarded-Host", req.Host)
		// req.Header.Set("X-Forwarded-Proto", schemeFromRequest(req))
	}

	// FlushInterval -1 enables streaming (required for WebSocket / SSE).
	proxy.FlushInterval = -1

	proxy.ErrorHandler = func(w http.ResponseWriter, r *http.Request, err error) {
		log.Errorf("[WebSocketProxy] %s proxy error: %v (path=%s)", name, err, r.URL.Path)

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

	return proxy
}

// createWSCircuitBreaker builds a gobreaker circuit breaker for a WebSocket service.
func createWSCircuitBreaker(name string, props *properties.ResilienceProperties) *gobreaker.CircuitBreaker[*http.Response] {
	settings := gobreaker.Settings{
		Name:        fmt.Sprintf("ws-proxy-%s", name),
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

// schemeFromRequest returns "wss"/"https" or "ws"/"http" depending on TLS.
// func schemeFromRequest(req *http.Request) string {
// 	if strings.EqualFold(req.Header.Get("X-Forwarded-Proto"), "https") || req.TLS != nil {
// 		return "https"
// 	}
// 	return "http"
// }
