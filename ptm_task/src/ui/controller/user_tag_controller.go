/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/usecase"
	"github.com/serp/ptm-task/src/kernel/utils"
)

type UserTagController struct {
	userTagUseCase usecase.IUserTagUsecase
}

func (u *UserTagController) CreateTag(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	var request request.CreateTagDTO
	if !utils.ValidateAndBindJSON(c, &request) {
		return
	}

	tag, err := u.userTagUseCase.CreateUserTag(c, userID, &request)
	if err != nil {
		if err.Error() == constant.TagAlreadyInUse {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, constant.TagAlreadyInUse)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, tag)
}

func (u *UserTagController) UpdateTag(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	tagID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	var request request.UpdateTagDTO
	if !utils.ValidateAndBindJSON(c, &request) {
		return
	}
	tag, err := u.userTagUseCase.UpdateUserTag(c, userID, tagID, &request)
	if err != nil {
		if err.Error() == constant.TagNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, constant.TagNotFound)
		} else if err.Error() == constant.UpdateTagForbidden {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, constant.UpdateTagForbidden)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, tag)
}

func (u *UserTagController) DeleteTag(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	tagID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	err := u.userTagUseCase.DeleteUserTag(c, userID, tagID)
	if err != nil {
		if err.Error() == constant.TagNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.TagNotFound)
		} else if err.Error() == constant.DeleteTagForbidden {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, constant.DeleteTagForbidden)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, "Tag deleted successfully")
}

func (u *UserTagController) GetTags(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	tags, err := u.userTagUseCase.GetTagsByUserID(c, userID)
	if err != nil {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		return
	}
	utils.SuccessfulHandle(c, tags)
}

func (u *UserTagController) GetTagByID(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	tagID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	tag, err := u.userTagUseCase.GetTagByID(c, userID, tagID)
	if err != nil {
		if err.Error() == constant.TagNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.TagNotFound)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, tag)
}

func NewUserTagController(userTagUseCase usecase.IUserTagUsecase) *UserTagController {
	return &UserTagController{
		userTagUseCase: userTagUseCase,
	}
}
