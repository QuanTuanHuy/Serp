package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/usecase"
	"github.com/serp/ptm-task/src/kernel/utils"
)

type CommentController struct {
	commentUseCase usecase.ICommentUseCase
}

func (cc *CommentController) CreateComment(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	var request request.CreateCommentDTO

	if !utils.ValidateAndBindJSON(c, &request) {
		return
	}

	comment, err := cc.commentUseCase.CreateComment(c, userID, &request)
	if err != nil {
		if err.Error() == constant.TaskNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, constant.TaskNotFound)
		} else if err.Error() == constant.CreateCommentForbidden {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, constant.CreateCommentForbidden)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, comment)
}

func (cc *CommentController) UpdateComment(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	commentID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	var request request.UpdateCommentDTO
	if !utils.ValidateAndBindJSON(c, &request) {
		return
	}

	comment, err := cc.commentUseCase.UpdateComment(c, userID, commentID, &request)
	if err != nil {
		if err.Error() == constant.CommentNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, constant.CommentNotFound)
		} else if err.Error() == constant.UpdateCommentForbidden {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, constant.UpdateCommentForbidden)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}

	utils.SuccessfulHandle(c, comment)
}

func (cc *CommentController) GetCommentByID(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	commentID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	comment, err := cc.commentUseCase.GetCommentByID(c, userID, commentID)
	if err != nil {
		if err.Error() == constant.CommentNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.CommentNotFound)
		} else if err.Error() == constant.GetCommentForbidden {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, constant.GetCommentForbidden)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, comment)
}

func (cc *CommentController) DeleteComment(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	commentID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	err := cc.commentUseCase.DeleteComment(c, userID, commentID)
	if err != nil {
		if err.Error() == constant.CommentNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, constant.CommentNotFound)
		} else if err.Error() == constant.DeleteCommentForbidden {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, constant.DeleteCommentForbidden)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}

	utils.SuccessfulHandle(c, nil)
}

func NewCommentController(commentUseCase usecase.ICommentUseCase) *CommentController {
	return &CommentController{
		commentUseCase: commentUseCase,
	}
}
