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

type CommentController struct {
	commentService service.ICommentService
}

func (c *CommentController) CreateComment(ctx *gin.Context) {
	var req request.CreateCommentRequest
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := c.commentService.CreateComment(ctx.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *CommentController) GetCommentByID(ctx *gin.Context) {
	commentID, valid := utils.ValidateAndParseID(ctx, "id")
	if !valid {
		return
	}

	res, err := c.commentService.GetCommentByID(ctx.Request.Context(), commentID)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *CommentController) UpdateComment(ctx *gin.Context) {
	commentID, valid := utils.ValidateAndParseID(ctx, "id")
	if !valid {
		return
	}

	var req request.UpdateCommentRequest
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := c.commentService.UpdateComment(ctx.Request.Context(), commentID, &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *CommentController) DeleteComment(ctx *gin.Context) {
	commentID, valid := utils.ValidateAndParseID(ctx, "id")
	if !valid {
		return
	}

	res, err := c.commentService.DeleteComment(ctx.Request.Context(), commentID)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func NewCommentController(commentService service.ICommentService) *CommentController {
	return &CommentController{
		commentService: commentService,
	}
}
