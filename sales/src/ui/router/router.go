/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"fmt"

	"github.com/gin-gonic/gin"
	"github.com/serp/sales/src/kernel/properties"
	"github.com/serp/sales/src/ui/controller"
	"github.com/serp/sales/src/ui/middleware"
	"go.uber.org/zap"
)

type RouterConfig struct {
	AppProps            *properties.AppProperties
	Engine              *gin.Engine
	QuotationController *controller.QuotationController
	JWTMiddleware       *middleware.JWTMiddleware
	Logger              *zap.Logger
}

func RegisterRoutes(config *RouterConfig) {
	config.Engine.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"status":  "UP",
			"service": config.AppProps.Name,
		})
	})

	apiV1 := config.Engine.Group(fmt.Sprintf("%s/api/v1", config.AppProps.Path))
	apiV1.Use(config.JWTMiddleware.AuthenticateJWT())
	{
		quotations := apiV1.Group("/quotations")
		{
			quotations.POST("", config.QuotationController.CreateQuotation)
			quotations.GET("", config.QuotationController.GetAllQuotations)
			quotations.GET("/:id", config.QuotationController.GetQuotationByID)
			quotations.PUT("/:id", config.QuotationController.UpdateQuotation)
			quotations.DELETE("/:id", config.QuotationController.DeleteQuotation)
		}
	}

	config.Logger.Info("Routes registered successfully")
}
