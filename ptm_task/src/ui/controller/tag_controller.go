/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/core/usecase"
	"github.com/serp/ptm-task/src/kernel/utils"
)

type TagController struct {
	tagUseCase usecase.ITagUsecase
}

func (c2 *TagController) CreateTag(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	var req request.CreateTagDTO
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}
	tag, err := c2.tagUseCase.CreateTag(c, userID, &req)
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

func (c2 *TagController) UpdateTag(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	tagID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	var req request.UpdateTagDTO
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}
	tag, err := c2.tagUseCase.UpdateTag(c, userID, tagID, &req)
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

func (c2 *TagController) DeleteTag(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	tagID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	err := c2.tagUseCase.DeleteTag(c, userID, tagID)
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

func (c2 *TagController) GetTags(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	tags, err := c2.tagUseCase.GetTagsByUserID(c, userID)
	if err != nil {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		return
	}
	utils.SuccessfulHandle(c, tags)
}

func (c2 *TagController) GetTagByID(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	tagID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	tag, err := c2.tagUseCase.GetTagByID(c, userID, tagID)
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

func (c2 *TagController) AttachTag(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	tagID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	var req request.TagAttachDTO
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}
	rt := enum.ResourceType(strings.ToUpper(req.ResourceType))
	if rt != enum.ResourceProject && rt != enum.ResourceTask && rt != enum.ResourceNote {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, constant.InvalidQueryParameters)
		return
	}
	if err := c2.tagUseCase.TagResource(c, userID, tagID, rt, req.ResourceID); err != nil {
		if err.Error() == constant.TagNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.TagNotFound)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, nil)
}

func (c2 *TagController) AttachTagBatch(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	tagID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	var req request.TagAttachBatchDTO
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}
	rt := enum.ResourceType(strings.ToUpper(req.ResourceType))
	if rt != enum.ResourceProject && rt != enum.ResourceTask && rt != enum.ResourceNote {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, constant.InvalidQueryParameters)
		return
	}
	if err := c2.tagUseCase.TagResourcesBatch(c, userID, tagID, rt, req.ResourceIDs); err != nil {
		if err.Error() == constant.TagNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.TagNotFound)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, nil)
}

func (c2 *TagController) DetachTag(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	tagID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	var req request.TagAttachDTO
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}
	rt := enum.ResourceType(strings.ToUpper(req.ResourceType))
	if rt != enum.ResourceProject && rt != enum.ResourceTask && rt != enum.ResourceNote {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, constant.InvalidQueryParameters)
		return
	}
	if err := c2.tagUseCase.RemoveTagFromResource(c, userID, tagID, rt, req.ResourceID); err != nil {
		if err.Error() == constant.TagNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.TagNotFound)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, nil)
}

func (c2 *TagController) GetTagsForResource(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	resourceType := c.Param("resourceType")
	resourceID, valid := utils.ValidateAndParseID(c, "resourceId")
	if !valid {
		return
	}
	rt := enum.ResourceType(strings.ToUpper(resourceType))
	if rt != enum.ResourceProject && rt != enum.ResourceTask && rt != enum.ResourceNote {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, constant.InvalidQueryParameters)
		return
	}
	tags, err := c2.tagUseCase.GetTagsForResource(c, userID, rt, resourceID)
	if err != nil {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		return
	}
	utils.SuccessfulHandle(c, tags)
}

func NewTagController(tagUseCase usecase.ITagUsecase) *TagController {
	return &TagController{tagUseCase: tagUseCase}
}
