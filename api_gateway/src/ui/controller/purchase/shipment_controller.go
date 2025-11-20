/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/core/domain/constant"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/purchase"
	service "github.com/serp/api-gateway/src/core/service/purchase"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type ShipmentController struct {
	shipmentService service.IShipmentService
}

func (s *ShipmentController) CreateShipment(ctx *gin.Context) {
	var req request.CreateShipmentRequest
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.shipmentService.CreateShipment(ctx.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *ShipmentController) UpdateShipment(ctx *gin.Context) {
	shipmentId := ctx.Param("shipmentId")
	if shipmentId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	var req request.UpdateShipmentRequest
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.shipmentService.UpdateShipment(ctx.Request.Context(), shipmentId, &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *ShipmentController) DeleteShipment(ctx *gin.Context) {
	shipmentId := ctx.Param("shipmentId")
	if shipmentId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.shipmentService.DeleteShipment(ctx.Request.Context(), shipmentId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *ShipmentController) GetShipment(ctx *gin.Context) {
	shipmentId := ctx.Param("shipmentId")
	if shipmentId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.shipmentService.GetShipment(ctx.Request.Context(), shipmentId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *ShipmentController) GetShipmentsByOrderId(ctx *gin.Context) {
	orderId := ctx.Param("orderId")
	if orderId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.shipmentService.GetShipmentsByOrderId(ctx.Request.Context(), orderId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *ShipmentController) ImportShipment(ctx *gin.Context) {
	shipmentId := ctx.Param("shipmentId")
	if shipmentId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.shipmentService.ImportShipment(ctx.Request.Context(), shipmentId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *ShipmentController) AddItemToShipment(ctx *gin.Context) {
	shipmentId := ctx.Param("shipmentId")
	if shipmentId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	var req request.AddItemToShipmentRequest
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.shipmentService.AddItemToShipment(ctx.Request.Context(), shipmentId, &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *ShipmentController) UpdateItemInShipment(ctx *gin.Context) {
	shipmentId := ctx.Param("shipmentId")
	itemId := ctx.Param("itemId")
	if shipmentId == "" || itemId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	var req request.UpdateItemInShipmentRequest
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.shipmentService.UpdateItemInShipment(ctx.Request.Context(), shipmentId, itemId, &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *ShipmentController) DeleteItemFromShipment(ctx *gin.Context) {
	shipmentId := ctx.Param("shipmentId")
	itemId := ctx.Param("itemId")
	if shipmentId == "" || itemId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.shipmentService.DeleteItemFromShipment(ctx.Request.Context(), shipmentId, itemId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *ShipmentController) UpdateShipmentFacility(ctx *gin.Context) {
	shipmentId := ctx.Param("shipmentId")
	if shipmentId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	var req request.UpdateShipmentFacilityRequest
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.shipmentService.UpdateShipmentFacility(ctx.Request.Context(), shipmentId, &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func NewShipmentController(shipmentService service.IShipmentService) *ShipmentController {
	return &ShipmentController{
		shipmentService: shipmentService,
	}
}
