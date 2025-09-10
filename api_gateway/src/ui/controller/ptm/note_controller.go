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

type NoteController struct {
	noteService service.INoteService
}

func (n *NoteController) CreateNote(c *gin.Context) {
	var req request.CreateNoteRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := n.noteService.CreateNote(c.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (n *NoteController) GetAllNotes(c *gin.Context) {
	res, err := n.noteService.GetAllNotes(c.Request.Context())
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (n *NoteController) GetNoteByID(c *gin.Context) {
	noteID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	res, err := n.noteService.GetNoteByID(c.Request.Context(), noteID)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (n *NoteController) DeleteNote(c *gin.Context) {
	noteID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	res, err := n.noteService.DeleteNote(c.Request.Context(), noteID)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (n *NoteController) LockNote(c *gin.Context) {
	noteID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	res, err := n.noteService.LockNote(c.Request.Context(), noteID)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (n *NoteController) UnlockNote(c *gin.Context) {
	noteID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	res, err := n.noteService.UnlockNote(c.Request.Context(), noteID)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func NewNoteController(noteService service.INoteService) *NoteController {
	return &NoteController{
		noteService: noteService,
	}
}
