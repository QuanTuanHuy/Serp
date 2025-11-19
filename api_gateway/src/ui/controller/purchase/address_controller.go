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

type AddressController struct {
	addressService service.IAddressService
}

func (a *AddressController) CreateAddress(c *gin.Context) {
	var req request.CreateAddressRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := a.addressService.CreateAddress(c.Request.Context(), &req)
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

	var req request.UpdateAddressRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := a.addressService.UpdateAddress(c.Request.Context(), addressId, &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func NewAddressController(addressService service.IAddressService) *AddressController {
	return &AddressController{addressService: addressService}
}
