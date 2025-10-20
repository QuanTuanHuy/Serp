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

type SubscriptionPlanController struct {
subscriptionPlanService service.ISubscriptionPlanService
}

func (s *SubscriptionPlanController) CreatePlan(c *gin.Context) {
var req request.CreateSubscriptionPlanRequest
if err := c.ShouldBindJSON(&req); err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := s.subscriptionPlanService.CreatePlan(c.Request.Context(), &req)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (s *SubscriptionPlanController) UpdatePlan(c *gin.Context) {
planIdStr := c.Param("planId")
planId, err := strconv.ParseInt(planIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

var req request.UpdateSubscriptionPlanRequest
if err := c.ShouldBindJSON(&req); err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := s.subscriptionPlanService.UpdatePlan(c.Request.Context(), planId, &req)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (s *SubscriptionPlanController) DeletePlan(c *gin.Context) {
planIdStr := c.Param("planId")
planId, err := strconv.ParseInt(planIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := s.subscriptionPlanService.DeletePlan(c.Request.Context(), planId)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (s *SubscriptionPlanController) GetPlanById(c *gin.Context) {
planIdStr := c.Param("planId")
planId, err := strconv.ParseInt(planIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := s.subscriptionPlanService.GetPlanById(c.Request.Context(), planId)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (s *SubscriptionPlanController) GetPlanByCode(c *gin.Context) {
planCode := c.Param("planCode")

res, err := s.subscriptionPlanService.GetPlanByCode(c.Request.Context(), planCode)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (s *SubscriptionPlanController) GetAllPlans(c *gin.Context) {
res, err := s.subscriptionPlanService.GetAllPlans(c.Request.Context())
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (s *SubscriptionPlanController) AddModuleToPlan(c *gin.Context) {
planIdStr := c.Param("planId")
planId, err := strconv.ParseInt(planIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

var req request.AddModuleToPlanRequest
if err := c.ShouldBindJSON(&req); err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := s.subscriptionPlanService.AddModuleToPlan(c.Request.Context(), planId, &req)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (s *SubscriptionPlanController) RemoveModuleFromPlan(c *gin.Context) {
planIdStr := c.Param("planId")
planId, err := strconv.ParseInt(planIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

moduleIdStr := c.Param("moduleId")
moduleId, err := strconv.ParseInt(moduleIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := s.subscriptionPlanService.RemoveModuleFromPlan(c.Request.Context(), planId, moduleId)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (s *SubscriptionPlanController) GetPlanModules(c *gin.Context) {
planIdStr := c.Param("planId")
planId, err := strconv.ParseInt(planIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := s.subscriptionPlanService.GetPlanModules(c.Request.Context(), planId)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func NewSubscriptionPlanController(subscriptionPlanService service.ISubscriptionPlanService) *SubscriptionPlanController {
return &SubscriptionPlanController{
subscriptionPlanService: subscriptionPlanService,
}
}
