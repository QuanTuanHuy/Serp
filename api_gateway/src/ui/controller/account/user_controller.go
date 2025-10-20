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

func (u *UserController) GetUsers(c *gin.Context) {
	var page *int
	var pageSize *int
	var sortBy *string
	var sortDir *string
	var search *string

	if pageStr := c.Query("page"); pageStr != "" {
		if p, err := strconv.Atoi(pageStr); err == nil {
			page = &p
		}
	}

	if pageSizeStr := c.Query("pageSize"); pageSizeStr != "" {
		if ps, err := strconv.Atoi(pageSizeStr); err == nil {
			pageSize = &ps
		}
	}

	if sb := c.Query("sortBy"); sb != "" {
		sortBy = &sb
	}

	if sd := c.Query("sortDir"); sd != "" {
		sortDir = &sd
	}

	if s := c.Query("search"); s != "" {
		search = &s
	}

	res, err := u.userService.GetUsers(c.Request.Context(), page, pageSize, sortBy, sortDir, search)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (u *UserController) AssignRolesToUser(c *gin.Context) {
	var req request.AssignRoleToUserDto
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := u.userService.AssignRolesToUser(c.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func NewUserController(userService service.IUserService) *UserController {
	return &UserController{
		userService: userService,
	}
}
