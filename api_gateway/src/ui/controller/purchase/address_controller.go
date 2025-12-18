/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/core/domain/constant"
	service "github.com/serp/api-gateway/src/core/service/purchase"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type AddressController struct {
	addressService service.IAddressService
}

func (a *AddressController) CreateAddress(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := a.addressService.CreateAddress(c.Request.Context(), req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (a *AddressController) UpdateAddress(c *gin.Context) {
	addressId := c.Param("addressId")
	if addressId == "" {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := a.addressService.UpdateAddress(c.Request.Context(), addressId, req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (a *AddressController) GetAddressesByEntityId(c *gin.Context) {
	entityId := c.Param("entityId")
	if entityId == "" {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := a.addressService.GetAddressesByEntityId(c.Request.Context(), entityId)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func NewAddressController(addressService service.IAddressService) *AddressController {
	return &AddressController{addressService: addressService}
}
