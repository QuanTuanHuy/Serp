/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/core/domain/constant"
	service "github.com/serp/api-gateway/src/core/service/account"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type UserController struct {
	userService service.IUserService
}

func (u *UserController) GetMyProfile(c *gin.Context) {
	res, err := u.userService.GetMyProfile(c.Request.Context())
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
		return
	}
	c.JSON(res.Code, res)
}

func NewUserController(userService service.IUserService) *UserController {
	return &UserController{
		userService: userService,
	}
}
