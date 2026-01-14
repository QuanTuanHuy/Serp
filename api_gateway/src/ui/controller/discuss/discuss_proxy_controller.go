/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package discuss

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

type DiscussProxyController struct {
	props *properties.ExternalServiceProperties
}

func NewDiscussProxyController(
	props *properties.ExternalServiceProperties,
) *DiscussProxyController {
	return &DiscussProxyController{
		props: props,
	}
}

func (c *DiscussProxyController) ProxyWebSocket(ctx *gin.Context) {
	targetHost := c.props.DiscussService.Host
	targetPort := c.props.DiscussService.Port

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
		// Rewrite path: /ws/discuss -> /discuss/ws/discuss
		req.URL.Path = "/discuss/ws/discuss"
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
