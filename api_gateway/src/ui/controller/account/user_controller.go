/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
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
	page, pageSize, valid := utils.ValidatePaginationParams(c)
	if !valid {
		return
	}

	sortBy := utils.ParseStringQuery(c, "sortBy")
	sortDir := utils.ParseStringQuery(c, "sortDir")
	search := utils.ParseStringQuery(c, "search")
	status := utils.ParseStringQuery(c, "status")
	organizationID := utils.ParseInt64Query(c, "organizationId")

	params := &request.GetUserParams{
		Page:           &page,
		PageSize:       &pageSize,
		SortBy:         sortBy,
		SortDir:        sortDir,
		Search:         search,
		OrganizationID: organizationID,
		Status:         status,
	}

	res, err := u.userService.GetUsers(c.Request.Context(), params)
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
