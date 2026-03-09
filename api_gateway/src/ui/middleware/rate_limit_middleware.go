/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package middleware

import (
	"fmt"
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/core/domain/constant"
	port "github.com/serp/api-gateway/src/core/port/rate_limiter"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type RateLimitMiddleware struct {
	rateLimiter        port.IRateLimiterPort
	props              *properties.RateLimitProperties
	routeOverrideIndex map[string]*properties.RouteOverride
}

var wildcardRateLimitMethods = []string{
	http.MethodGet,
	http.MethodPost,
	http.MethodPut,
	http.MethodPatch,
	http.MethodDelete,
	http.MethodHead,
	http.MethodOptions,
}

func NewRateLimitMiddleware(
	rateLimiter port.IRateLimiterPort,
	props *properties.RateLimitProperties,
) *RateLimitMiddleware {
	m := &RateLimitMiddleware{
		rateLimiter:        rateLimiter,
		props:              props,
		routeOverrideIndex: make(map[string]*properties.RouteOverride),
	}

	for _, override := range props.RouteOverrides {
		o := override
		normalizedPath := normalizeRateLimitPath(o.Path)
		if o.Method == "" || o.Method == "*" {
			for _, method := range wildcardRateLimitMethods {
				key := buildRouteOverrideIndexKey(method, normalizedPath)
				m.routeOverrideIndex[key] = &o
			}
		} else {
			key := buildRouteOverrideIndexKey(o.Method, normalizedPath)
			m.routeOverrideIndex[key] = &o
		}
	}

	return m
}

// IPRateLimit returns a Gin middleware that applies IP-based rate limiting.
// This should be applied globally at the engine level.
func (m *RateLimitMiddleware) IPRateLimit() gin.HandlerFunc {
	return func(c *gin.Context) {
		if !m.props.Enabled {
			c.Next()
			return
		}

		clientIP := c.ClientIP()
		if clientIP == "" {
			clientIP = "unknown"
		}

		rule := m.props.DefaultIP
		override, routeKey := m.resolveRouteOverride(c)
		isRouteOverride := false
		if override != nil && override.IP != nil {
			rule = *override.IP
			isRouteOverride = true
		}

		if !isValidRateLimitRule(rule) {
			log.Warn(c,
				"Invalid IP rate limit rule, allowing request. limit=",
				rule.Limit,
				", windowSecs=",
				rule.WindowSecs,
			)
			c.Next()
			return
		}

		key := fmt.Sprintf("ip:%s", clientIP)
		if isRouteOverride {
			key = fmt.Sprintf("ip:%s:%s", clientIP, routeKey)
		}

		result, err := m.rateLimiter.CheckRateLimit(c.Request.Context(), key, rule.Limit, rule.WindowSecs)
		if err != nil {
			log.Warn(c, "Rate limiter unavailable, allowing request: ", err)
			c.Next()
			return
		}

		setRateLimitHeaders(c, result)

		if !result.Allowed {
			c.Header("Retry-After", strconv.Itoa(result.RetryAfter))
			utils.AbortErrorHandleCustomMessage(c,
				constant.GeneralTooManyRequests,
				"Rate limit exceeded. Try again later.",
			)
			c.Abort()
			return
		}

		c.Next()
	}
}

// UserRateLimit returns a Gin middleware that applies user-based rate limiting.
// This should be applied per-group after JWT middleware that sets "userID" in context.
func (m *RateLimitMiddleware) UserRateLimit() gin.HandlerFunc {
	return func(c *gin.Context) {
		if !m.props.Enabled {
			c.Next()
			return
		}

		userID, exists := c.Get("userID")
		if !exists {
			c.Next()
			return
		}

		rule := m.props.DefaultUser
		override, routeKey := m.resolveRouteOverride(c)
		isRouteOverride := false
		if override != nil && override.User != nil {
			rule = *override.User
			isRouteOverride = true
		}

		if !isValidRateLimitRule(rule) {
			log.Warn(c,
				"Invalid user rate limit rule, allowing request. limit=",
				rule.Limit,
				", windowSecs=",
				rule.WindowSecs,
			)
			c.Next()
			return
		}

		key := fmt.Sprintf("user:%v", userID)
		if isRouteOverride {
			key = fmt.Sprintf("user:%v:%s", userID, routeKey)
		}

		result, err := m.rateLimiter.CheckRateLimit(c.Request.Context(), key, rule.Limit, rule.WindowSecs)
		if err != nil {
			log.Warn(c, "Rate limiter unavailable for user, allowing request: ", err)
			c.Next()
			return
		}

		setRateLimitHeaders(c, result)

		if !result.Allowed {
			c.Header("Retry-After", strconv.Itoa(result.RetryAfter))
			utils.AbortErrorHandleCustomMessage(c,
				constant.GeneralTooManyRequests,
				"User rate limit exceeded. Try again later.",
			)
			c.Abort()
			return
		}

		c.Next()
	}
}

func setRateLimitHeaders(c *gin.Context, result *port.RateLimitResult) {
	c.Header("X-RateLimit-Limit", strconv.Itoa(result.Limit))
	c.Header("X-RateLimit-Remaining", strconv.Itoa(result.Remaining))
	c.Header("X-RateLimit-Reset", strconv.FormatInt(result.ResetAt, 10))
}

func (m *RateLimitMiddleware) resolveRouteOverride(c *gin.Context) (*properties.RouteOverride, string) {
	method := c.Request.Method

	requestPath := normalizeRateLimitPath(c.Request.URL.Path)
	if requestPath != "" {
		requestKey := buildRouteOverrideIndexKey(method, requestPath)
		if override, ok := m.routeOverrideIndex[requestKey]; ok {
			return override, requestKey
		}
	}

	fullPath := normalizeRateLimitPath(c.FullPath())
	if fullPath != "" {
		fullPathKey := buildRouteOverrideIndexKey(method, fullPath)
		if override, ok := m.routeOverrideIndex[fullPathKey]; ok {
			return override, fullPathKey
		}

		return nil, fullPathKey
	}

	if requestPath != "" {
		return nil, buildRouteOverrideIndexKey(method, requestPath)
	}

	return nil, buildRouteOverrideIndexKey(method, "/")
}

func buildRouteOverrideIndexKey(method string, path string) string {
	return strings.ToUpper(method) + ":" + normalizeRateLimitPath(path)
}

func normalizeRateLimitPath(path string) string {
	if path == "" {
		return ""
	}

	if path == "/" {
		return path
	}

	trimmedPath := strings.TrimRight(path, "/")
	if trimmedPath == "" {
		return "/"
	}

	return trimmedPath
}

func isValidRateLimitRule(rule properties.RateLimitRule) bool {
	return rule.Limit > 0 && rule.WindowSecs > 0
}
