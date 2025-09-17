/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/core/domain/constant"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/ptm"
	service "github.com/serp/api-gateway/src/core/service/ptm"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type UserTagController struct {
	userTagService service.IUserTagService
}

func (u *UserTagController) CreateUserTag(c *gin.Context) {
	var req request.CreateUserTagRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := u.userTagService.CreateUserTag(c.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (u *UserTagController) GetUserTags(c *gin.Context) {
	res, err := u.userTagService.GetUserTags(c.Request.Context())
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (u *UserTagController) GetUserTagByID(c *gin.Context) {
	tagID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	res, err := u.userTagService.GetUserTagByID(c.Request.Context(), tagID)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (u *UserTagController) UpdateUserTag(c *gin.Context) {
	tagID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	var req request.UpdateUserTagRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := u.userTagService.UpdateUserTag(c.Request.Context(), tagID, &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (u *UserTagController) DeleteUserTag(c *gin.Context) {
	tagID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	res, err := u.userTagService.DeleteUserTag(c.Request.Context(), tagID)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func NewUserTagController(userTagService service.IUserTagService) *UserTagController {
	return &UserTagController{
		userTagService: userTagService,
	}
}
