/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package middleware

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
)

type jwtContextAuthenticator interface {
	AuthenticateJWTContext(c *gin.Context) bool
}

type userRateLimitApplier interface {
	ApplyUserRateLimit(c *gin.Context) bool
}

type AccountProxyJWTGateMiddleware struct {
	authenticator jwtContextAuthenticator
	userLimiter   userRateLimitApplier
}

type accountProxyRoutePattern struct {
	method  string
	pattern string
}

var accountProxyProtectedOverrides = []accountProxyRoutePattern{
	{method: http.MethodGet, pattern: "/api/v1/modules/my-modules"},
}

var accountProxyPublicRoutes = []accountProxyRoutePattern{
	{method: http.MethodPost, pattern: "/api/v1/auth/login"},
	{method: http.MethodPost, pattern: "/api/v1/auth/register"},
	{method: http.MethodPost, pattern: "/api/v1/auth/get-token"},
	{method: http.MethodPost, pattern: "/api/v1/auth/refresh-token"},
	{method: http.MethodPost, pattern: "/api/v1/auth/revoke-token"},
	{method: http.MethodGet, pattern: "/api/v1/auth/reset-password/validate"},
	{method: http.MethodPost, pattern: "/api/v1/auth/reset-password/confirm"},
	{method: http.MethodGet, pattern: "/api/v1/permissions/**"},
	{method: http.MethodGet, pattern: "/api/v1/roles/**"},
	{method: http.MethodGet, pattern: "/api/v1/modules/**"},
	{method: http.MethodGet, pattern: "/api/v1/menu-displays/**"},
	{method: http.MethodGet, pattern: "/api/v1/subscription-plans/**"},
	{method: http.MethodGet, pattern: "/api/v1/organizations/*/departments"},
	{method: http.MethodGet, pattern: "/api/v1/organizations/*/departments/*"},
	{method: http.MethodGet, pattern: "/api/v1/organizations/*/departments/*/tree"},
	{method: http.MethodGet, pattern: "/api/v1/organizations/*/departments/*/members"},
	{method: http.MethodPost, pattern: "/api/v1/invitations/*/accept"},
}

func NewAccountProxyJWTGateMiddleware(
	jwtMiddleware *JWTMiddleware,
	rateLimitMiddleware *RateLimitMiddleware,
) *AccountProxyJWTGateMiddleware {
	return newAccountProxyJWTGateMiddleware(jwtMiddleware, rateLimitMiddleware)
}

func newAccountProxyJWTGateMiddleware(
	authenticator jwtContextAuthenticator,
	userLimiter userRateLimitApplier,
) *AccountProxyJWTGateMiddleware {
	return &AccountProxyJWTGateMiddleware{
		authenticator: authenticator,
		userLimiter:   userLimiter,
	}
}

func (m *AccountProxyJWTGateMiddleware) Handler(appPath string) gin.HandlerFunc {
	return func(c *gin.Context) {
		normalizedPath := normalizeAccountProxyPath(c.Request.URL.Path, appPath)
		if isAccountProxyPublicRoute(c.Request.Method, normalizedPath) {
			c.Next()
			return
		}

		if !m.authenticator.AuthenticateJWTContext(c) {
			return
		}
		if !m.userLimiter.ApplyUserRateLimit(c) {
			return
		}

		c.Next()
	}
}

func isAccountProxyPublicRoute(method string, requestPath string) bool {
	if matchesAccountProxyRoute(accountProxyProtectedOverrides, method, requestPath) {
		return false
	}

	return matchesAccountProxyRoute(accountProxyPublicRoutes, method, requestPath)
}

func matchesAccountProxyRoute(patterns []accountProxyRoutePattern, method string, requestPath string) bool {
	for _, route := range patterns {
		if route.method != method {
			continue
		}
		if matchAccountProxyPathPattern(route.pattern, requestPath) {
			return true
		}
	}
	return false
}

func matchAccountProxyPathPattern(pattern string, requestPath string) bool {
	pattern = normalizeAccountProxyPath(pattern, "")
	requestPath = normalizeAccountProxyPath(requestPath, "")

	if strings.HasSuffix(pattern, "/**") {
		prefix := strings.TrimSuffix(pattern, "/**")
		return requestPath == prefix || strings.HasPrefix(requestPath, prefix+"/")
	}

	patternParts := splitAccountProxyPath(pattern)
	requestParts := splitAccountProxyPath(requestPath)
	if len(patternParts) != len(requestParts) {
		return false
	}

	for i := range patternParts {
		if patternParts[i] == "*" {
			continue
		}
		if patternParts[i] != requestParts[i] {
			return false
		}
	}

	return true
}

func normalizeAccountProxyPath(path string, appPath string) string {
	if path == "" {
		return "/"
	}

	normalized := "/" + strings.Trim(path, "/")
	if appPath != "" && appPath != "/" {
		normalizedAppPath := "/" + strings.Trim(appPath, "/")
		if normalized == normalizedAppPath {
			return "/"
		}
		if strings.HasPrefix(normalized, normalizedAppPath+"/") {
			normalized = strings.TrimPrefix(normalized, normalizedAppPath)
		}
	}

	if normalized != "/" {
		normalized = strings.TrimRight(normalized, "/")
	}
	if normalized == "" {
		return "/"
	}

	return normalized
}

func splitAccountProxyPath(path string) []string {
	path = strings.Trim(path, "/")
	if path == "" {
		return []string{}
	}
	return strings.Split(path, "/")
}
