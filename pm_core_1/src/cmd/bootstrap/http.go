/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"os"
	"time"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

func NewGinEngine(zapLogger *zap.Logger) *gin.Engine {
	env := os.Getenv("APP_PROFILES")
	if env == "production" {
		gin.SetMode(gin.ReleaseMode)
	} else {
		gin.SetMode(gin.DebugMode)
	}

	engine := gin.New()

	engine.Use(gin.Recovery())
	engine.Use(ginLogger(zapLogger))

	return engine
}

func ginLogger(zapLogger *zap.Logger) gin.HandlerFunc {
	return func(c *gin.Context) {
		start := time.Now()
		path := c.Request.URL.Path
		query := c.Request.URL.RawQuery

		c.Next()

		latency := time.Since(start)

		zapLogger.Info("HTTP request",
			zap.Int("status", c.Writer.Status()),
			zap.String("method", c.Request.Method),
			zap.String("path", path),
			zap.String("query", query),
			zap.String("ip", c.ClientIP()),
			zap.Duration("latency", latency),
			zap.String("user-agent", c.Request.UserAgent()),
		)
	}
}
