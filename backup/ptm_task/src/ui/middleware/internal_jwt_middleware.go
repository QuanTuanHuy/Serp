/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package middleware

import (
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/kernel/utils"
)

type InternalJWTMiddleware struct {
	jwtUtils *utils.JWTUtils
}

func NewInternalJWTMiddleware(jwtUtils *utils.JWTUtils) *InternalJWTMiddleware {
	return &InternalJWTMiddleware{
		jwtUtils: jwtUtils,
	}
}

func (m *InternalJWTMiddleware) ValidateServiceToken() gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" || !strings.HasPrefix(authHeader, "Bearer ") {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Missing or invalid authorization header")
			c.Abort()
			return
		}

		token := strings.TrimPrefix(authHeader, "Bearer ")
		claims, err := m.jwtUtils.ValidateInternalToken(token)
		if err != nil {
			log.Error("Error validating token: ", err)
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Invalid or expired token")
			c.Abort()
			return
		}
		if !m.isServiceToken(claims) {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Not a service token")
			c.Abort()
			return
		}

		c.Set("clientID", claims.ClientID)
		c.Next()
	}
}

func (m *InternalJWTMiddleware) isServiceToken(claims *utils.InternalClaims) bool {
	if claims.AuthorizedParty != "" && claims.ClientID != "" {
		return strings.HasPrefix(claims.AuthorizedParty, "serp-") && strings.HasPrefix(claims.ClientID, "serp-")
	}
	return false
}
