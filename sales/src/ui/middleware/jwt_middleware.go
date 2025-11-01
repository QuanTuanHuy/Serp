/*
Author: QuanTuanHuy
Description: Part of Serp Project - Simplified JWT Middleware
*/

package middleware

import (
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
	"github.com/serp/sales/src/core/domain/constant"
	"github.com/serp/sales/src/kernel/properties"
	"github.com/serp/sales/src/kernel/utils"
	"go.uber.org/zap"
)

type JWTMiddleware struct {
	appProps *properties.AppProperties
	logger   *zap.Logger
}

func NewJWTMiddleware(appProps *properties.AppProperties, logger *zap.Logger) *JWTMiddleware {
	return &JWTMiddleware{
		appProps: appProps,
		logger:   logger,
	}
}

type Claims struct {
	UserID      int64                  `json:"uid"`
	TenantID    int64                  `json:"tid"`
	Email       string                 `json:"email"`
	FullName    string                 `json:"name"`
	RealmAccess map[string]interface{} `json:"realm_access"`
	jwt.RegisteredClaims
}

func (m *JWTMiddleware) AuthenticateJWT() gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Missing authorization header")
			c.Abort()
			return
		}
		const bearerPrefix = "Bearer "
		if !strings.HasPrefix(authHeader, bearerPrefix) {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Invalid authorization header format")
			c.Abort()
			return
		}
		tokenString := strings.TrimPrefix(authHeader, bearerPrefix)
		if tokenString == "" {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Missing token")
			c.Abort()
			return
		}

		token, _, err := jwt.NewParser().ParseUnverified(tokenString, &Claims{})
		if err != nil {
			m.logger.Error("Failed to parse JWT", zap.Error(err))
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Invalid token")
			c.Abort()
			return
		}

		claims, ok := token.Claims.(*Claims)
		if !ok {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Invalid token claims")
			c.Abort()
			return
		}

		// Set user information in context
		c.Set("userID", claims.UserID)
		c.Set("tenantID", claims.TenantID)
		c.Set("userEmail", claims.Email)
		c.Set("userFullName", claims.FullName)
		c.Set("token", tokenString)
		c.Set("authenticated", true)

		m.logger.Debug("JWT authentication successful",
			zap.Int64("userID", claims.UserID),
			zap.Int64("tenantID", claims.TenantID),
			zap.String("email", claims.Email))

		c.Next()
	}
}
