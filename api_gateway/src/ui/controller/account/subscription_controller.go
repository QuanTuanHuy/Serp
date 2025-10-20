/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
"strconv"

"github.com/gin-gonic/gin"
"github.com/serp/api-gateway/src/core/domain/constant"
request "github.com/serp/api-gateway/src/core/domain/dto/request/account"
service "github.com/serp/api-gateway/src/core/service/account"
"github.com/serp/api-gateway/src/kernel/utils"
)

type SubscriptionController struct {
subscriptionService service.ISubscriptionService
}

func (s *SubscriptionController) Subscribe(c *gin.Context) {
organizationIdStr := c.Param("organizationId")
organizationId, err := strconv.ParseInt(organizationIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

var req request.SubscribeRequest
if err := c.ShouldBindJSON(&req); err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := s.subscriptionService.Subscribe(c.Request.Context(), organizationId, &req)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (s *SubscriptionController) StartTrial(c *gin.Context) {
organizationIdStr := c.Param("organizationId")
organizationId, err := strconv.ParseInt(organizationIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

planIdStr := c.Query("planId")
planId, err := strconv.ParseInt(planIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := s.subscriptionService.StartTrial(c.Request.Context(), organizationId, planId)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (s *SubscriptionController) ActivateSubscription(c *gin.Context) {
subscriptionIdStr := c.Param("subscriptionId")
subscriptionId, err := strconv.ParseInt(subscriptionIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := s.subscriptionService.ActivateSubscription(c.Request.Context(), subscriptionId)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (s *SubscriptionController) RejectSubscription(c *gin.Context) {
subscriptionIdStr := c.Param("subscriptionId")
subscriptionId, err := strconv.ParseInt(subscriptionIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

var req request.RejectSubscriptionRequest
if err := c.ShouldBindJSON(&req); err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := s.subscriptionService.RejectSubscription(c.Request.Context(), subscriptionId, &req)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (s *SubscriptionController) UpgradeSubscription(c *gin.Context) {
var req request.UpgradeSubscriptionRequest
if err := c.ShouldBindJSON(&req); err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := s.subscriptionService.UpgradeSubscription(c.Request.Context(), &req)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (s *SubscriptionController) DowngradeSubscription(c *gin.Context) {
var req request.DowngradeSubscriptionRequest
if err := c.ShouldBindJSON(&req); err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := s.subscriptionService.DowngradeSubscription(c.Request.Context(), &req)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (s *SubscriptionController) CancelSubscription(c *gin.Context) {
var req request.CancelSubscriptionRequest
if err := c.ShouldBindJSON(&req); err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := s.subscriptionService.CancelSubscription(c.Request.Context(), &req)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (s *SubscriptionController) RenewSubscription(c *gin.Context) {
res, err := s.subscriptionService.RenewSubscription(c.Request.Context())
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (s *SubscriptionController) ExtendTrial(c *gin.Context) {
subscriptionIdStr := c.Param("subscriptionId")
subscriptionId, err := strconv.ParseInt(subscriptionIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

var req request.ExtendTrialRequest
if err := c.ShouldBindJSON(&req); err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := s.subscriptionService.ExtendTrial(c.Request.Context(), subscriptionId, &req)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (s *SubscriptionController) ExpireSubscription(c *gin.Context) {
subscriptionIdStr := c.Param("subscriptionId")
subscriptionId, err := strconv.ParseInt(subscriptionIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := s.subscriptionService.ExpireSubscription(c.Request.Context(), subscriptionId)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (s *SubscriptionController) GetActiveSubscription(c *gin.Context) {
organizationIdStr := c.Param("organizationId")
organizationId, err := strconv.ParseInt(organizationIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := s.subscriptionService.GetActiveSubscription(c.Request.Context(), organizationId)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (s *SubscriptionController) GetSubscriptionById(c *gin.Context) {
subscriptionIdStr := c.Param("subscriptionId")
subscriptionId, err := strconv.ParseInt(subscriptionIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := s.subscriptionService.GetSubscriptionById(c.Request.Context(), subscriptionId)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (s *SubscriptionController) GetSubscriptionHistory(c *gin.Context) {
organizationIdStr := c.Param("organizationId")
organizationId, err := strconv.ParseInt(organizationIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := s.subscriptionService.GetSubscriptionHistory(c.Request.Context(), organizationId)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func NewSubscriptionController(subscriptionService service.ISubscriptionService) *SubscriptionController {
return &SubscriptionController{
subscriptionService: subscriptionService,
}
}
