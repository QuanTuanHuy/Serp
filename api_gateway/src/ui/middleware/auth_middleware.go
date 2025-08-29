package middleware

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/kernel/utils"
)

func AuthMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		var accessToken string
		if authHeader != "" && strings.HasPrefix(authHeader, "Bearer ") {
			accessToken = strings.TrimPrefix(authHeader, "Bearer ")
		}

		forwardHeaders := http.Header{}
		if authHeader != "" {
			forwardHeaders.Set("Authorization", authHeader)
		}

		authContext := &utils.AuthContext{
			AccessToken: accessToken,
			Headers:     forwardHeaders,
		}
		ctx := utils.SetAuthContext(c.Request.Context(), authContext)
		c.Request = c.Request.WithContext(ctx)

		c.Next()
	}
}
