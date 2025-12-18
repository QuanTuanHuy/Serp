/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package common

import (
	"fmt"
	"net/http"
	"net/http/httputil"
	"net/url"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/core/domain/constant"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type GenericProxyController struct {
	props *properties.ExternalServiceProperties
}

func NewGenericProxyController(props *properties.ExternalServiceProperties) *GenericProxyController {
	return &GenericProxyController{
		props: props,
	}
}

func (c *GenericProxyController) ProxyToCRM(ctx *gin.Context) {
	target := fmt.Sprintf("http://%s:%s", c.props.CrmService.Host, c.props.CrmService.Port)
	c.proxy(ctx, target, "/crm/api/v1/proxy", "/crm/api/v1")
}

func (c *GenericProxyController) ProxyToNotification(ctx *gin.Context) {
	target := fmt.Sprintf("http://%s:%s", c.props.NotificationService.Host, c.props.NotificationService.Port)
	c.proxy(ctx, target, "/ns/api/v1", "/notification/api/v1")
}

func (c *GenericProxyController) proxy(ctx *gin.Context, target string, sourcePrefix, targetPrefix string) {
	remote, err := url.Parse(target)
	if err != nil {
		utils.AbortErrorHandleCustomMessage(ctx, constant.GeneralInternalServerError, "Invalid target URL")
		return
	}

	proxy := httputil.NewSingleHostReverseProxy(remote)
	proxy.Director = func(req *http.Request) {
		req.Header = ctx.Request.Header
		req.Host = remote.Host
		req.URL.Scheme = remote.Scheme
		req.URL.Host = remote.Host

		if sourcePrefix != "" && targetPrefix != "" {
			req.URL.Path = strings.Replace(ctx.Request.URL.Path, sourcePrefix, targetPrefix, 1)
		} else {
			req.URL.Path = ctx.Request.URL.Path
		}
		req.URL.RawQuery = ctx.Request.URL.RawQuery
	}

	proxy.ServeHTTP(ctx.Writer, ctx.Request)
}
