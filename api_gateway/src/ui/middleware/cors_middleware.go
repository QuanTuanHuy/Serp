/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package middleware

import (
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/kernel/properties"
)

type CorsMiddleware struct {
	corsProps *properties.CorsProperties
}

func NewCorsMiddleware(corsProps *properties.CorsProperties) *CorsMiddleware {
	return &CorsMiddleware{
		corsProps: corsProps,
	}
}

func (c *CorsMiddleware) Handler() gin.HandlerFunc {
	return func(ctx *gin.Context) {
		origin := ctx.Request.Header.Get("Origin")

		if !c.isOriginAllowed(origin) {
			log.Warn(ctx, "CORS: Origin not allowed: ", origin)
			ctx.AbortWithStatus(http.StatusForbidden)
			return
		}

		c.setCorsHeaders(ctx, origin)

		if ctx.Request.Method == http.MethodOptions {
			ctx.AbortWithStatus(http.StatusNoContent)
			return
		}

		ctx.Next()
	}
}

func (c *CorsMiddleware) isOriginAllowed(origin string) bool {
	// Same-origin requests
	if origin == "" {
		return true
	}

	if c.corsProps.AllowAllOrigins {
		return true
	}

	for _, allowedOrigin := range c.corsProps.AllowedOrigins {
		if c.corsProps.AllowWildcard {
			if c.matchWildcard(allowedOrigin, origin) {
				return true
			}
		} else {
			if allowedOrigin == origin {
				return true
			}
		}
	}

	return false
}

func (c *CorsMiddleware) matchWildcard(pattern, origin string) bool {
	// E.g., "*.example.com" matches "api.example.com"
	if strings.HasPrefix(pattern, "*.") {
		domain := pattern[2:]
		return strings.HasSuffix(origin, "."+domain) || origin == domain
	}
	return pattern == origin
}

func (c *CorsMiddleware) setCorsHeaders(ctx *gin.Context, origin string) {
	if c.corsProps.AllowAllOrigins {
		ctx.Header("Access-Control-Allow-Origin", "*")
	} else if origin != "" {
		ctx.Header("Access-Control-Allow-Origin", origin)
	}

	if len(c.corsProps.AllowedMethods) > 0 {
		ctx.Header("Access-Control-Allow-Methods", strings.Join(c.corsProps.AllowedMethods, ", "))
	}

	if len(c.corsProps.AllowedHeaders) > 0 {
		ctx.Header("Access-Control-Allow-Headers", strings.Join(c.corsProps.AllowedHeaders, ", "))
	}

	if len(c.corsProps.ExposedHeaders) > 0 {
		ctx.Header("Access-Control-Expose-Headers", strings.Join(c.corsProps.ExposedHeaders, ", "))
	}

	if c.corsProps.AllowCredentials {
		ctx.Header("Access-Control-Allow-Credentials", "true")
	}

	if c.corsProps.MaxAge > 0 {
		ctx.Header("Access-Control-Max-Age", strconv.Itoa(c.corsProps.MaxAge))
	}

	ctx.Header("Vary", "Origin")
}
