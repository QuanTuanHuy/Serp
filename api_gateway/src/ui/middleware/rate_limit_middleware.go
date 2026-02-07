/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package middleware

import (
	"fmt"
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
		if o.Method == "" || o.Method == "*" {
			for _, method := range []string{"GET", "POST", "PUT", "PATCH", "DELETE"} {
				key := method + ":" + o.Path
				m.routeOverrideIndex[key] = &o
			}
		} else {
			key := strings.ToUpper(o.Method) + ":" + o.Path
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
		rule := m.props.DefaultIP

		routeKey := c.Request.Method + ":" + c.FullPath()
		if override, ok := m.routeOverrideIndex[routeKey]; ok && override.IP != nil {
			rule = *override.IP
		}

		key := fmt.Sprintf("ip:%s", clientIP)
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

		routeKey := c.Request.Method + ":" + c.FullPath()
		if override, ok := m.routeOverrideIndex[routeKey]; ok && override.User != nil {
			rule = *override.User
		}

		key := fmt.Sprintf("user:%v", userID)
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
