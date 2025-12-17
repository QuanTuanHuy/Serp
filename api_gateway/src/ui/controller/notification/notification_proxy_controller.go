/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package notification

import (
	"fmt"
	"net/http"
	"net/http/httputil"
	"net/url"

	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/core/domain/constant"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type NotificationProxyController struct {
	props *properties.ExternalServiceProperties
}

func NewNotificationProxyController(
	props *properties.ExternalServiceProperties,
) *NotificationProxyController {
	return &NotificationProxyController{
		props: props,
	}
}

func (c *NotificationProxyController) ProxyWebSocket(ctx *gin.Context) {
	targetHost := c.props.NotificationService.Host
	targetPort := c.props.NotificationService.Port

	if len(targetHost) > 0 && targetHost[:4] != "http" {
		targetHost = "http://" + targetHost
	}

	targetURLStr := fmt.Sprintf("%s:%s", targetHost, targetPort)

	targetURL, err := url.Parse(targetURLStr)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}

	proxy := httputil.NewSingleHostReverseProxy(targetURL)

	originalDirector := proxy.Director
	proxy.Director = func(req *http.Request) {
		originalDirector(req)
		// Rewrite path to match notification service expectation
		req.URL.Path = "/notification/api/v1/ws"
		req.Host = targetURL.Host

		if auth := ctx.GetHeader("Authorization"); auth != "" {
			req.Header.Set("Authorization", auth)
		}
	}

	proxy.ErrorHandler = func(w http.ResponseWriter, r *http.Request, err error) {
		w.WriteHeader(http.StatusBadGateway)
	}

	proxy.ServeHTTP(ctx.Writer, ctx.Request)
}
