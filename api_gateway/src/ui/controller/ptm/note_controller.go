/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/core/domain/constant"
	service "github.com/serp/api-gateway/src/core/service/ptm"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type NoteController struct {
	noteService service.INoteService
}

func (n *NoteController) CreateNote(c *gin.Context) {
	var payload map[string]any
	if err := c.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := n.noteService.CreateNote(c.Request.Context(), payload)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (n *NoteController) SearchNotes(c *gin.Context) {
	payload := map[string]any{}
	queryParams := c.Request.URL.Query()
	for key, values := range queryParams {
		if len(values) > 0 {
			payload[key] = values[0]
		}
	}

	res, err := n.noteService.SearchNotes(c.Request.Context(), payload)
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

func (n *NoteController) UpdateNote(c *gin.Context) {
	noteID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	var payload map[string]any
	if err := c.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := n.noteService.UpdateNote(c.Request.Context(), noteID, payload)
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

func (n *NoteController) GetNotesByProjectID(c *gin.Context) {
	projectID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	res, err := n.noteService.GetNotesByProjectID(c.Request.Context(), projectID)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (n *NoteController) GetNotesByTaskID(c *gin.Context) {
	taskID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	res, err := n.noteService.GetNotesByTaskID(c.Request.Context(), taskID)
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
