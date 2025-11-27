package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/mapper"
	"github.com/serp/ptm-task/src/core/usecase"
	"github.com/serp/ptm-task/src/kernel/utils"
)

type NoteController struct {
	noteUseCase usecase.INoteUseCase
	mapper      *mapper.NoteMapper
}

func NewNoteController(noteUseCase usecase.INoteUseCase) *NoteController {
	return &NoteController{
		noteUseCase: noteUseCase,
		mapper:      mapper.NewNoteMapper(),
	}
}

func (nc *NoteController) CreateNote(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	tenantID, err := utils.GetTenantIDFromContext(c)
	if err != nil {
		return
	}

	var req request.CreateNoteRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}
	note := nc.mapper.CreateRequestToEntity(&req, userID, tenantID)
	note, err = nc.noteUseCase.CreateNote(c.Request.Context(), userID, note)
	if err != nil {
		utils.ErrorHandle(c, err)
	}

	utils.SuccessfulHandle(c, "Note created successfully", nc.mapper.EntityToResponse(note))
}

func (nc *NoteController) GetNotesByProjectID(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	projectID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	var filter request.NoteFilterRequest
	if !utils.ValidateAndBindQuery(c, &filter) {
		return
	}
	notes, err := nc.noteUseCase.GetNotesByProjectID(c.Request.Context(), userID, projectID, nc.mapper.FilterMapper(&filter))
	if err != nil {
		utils.ErrorHandle(c, err)
	}
	utils.SuccessfulHandle(c, "Notes retrieved successfully", nc.mapper.EntitiesToResponses(notes))
}

func (nc *NoteController) GetNotesByTaskID(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	taskID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	var filter request.NoteFilterRequest
	if !utils.ValidateAndBindQuery(c, &filter) {
		return
	}
	notes, err := nc.noteUseCase.GetNotesByTaskID(c.Request.Context(), userID, taskID, nc.mapper.FilterMapper(&filter))
	if err != nil {
		utils.ErrorHandle(c, err)
	}
	utils.SuccessfulHandle(c, "Notes retrieved successfully", nc.mapper.EntitiesToResponses(notes))
}

func (nc *NoteController) GetNoteByID(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	noteID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	note, err := nc.noteUseCase.GetNoteByID(c.Request.Context(), userID, noteID)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandle(c, "Note retrieved successfully", nc.mapper.EntityToResponse(note))
}

func (nc *NoteController) UpdateNote(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	noteID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	var req request.UpdateNoteRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}
	existingNote, err := nc.noteUseCase.GetNoteByID(c.Request.Context(), userID, noteID)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	updatedNote := nc.mapper.UpdateRequestToEntity(&req, existingNote)
	err = nc.noteUseCase.UpdateNote(c.Request.Context(), userID, updatedNote)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandle(c, "Note updated successfully", nc.mapper.EntityToResponse(updatedNote))
}

func (nc *NoteController) DeleteNote(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	noteID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	err = nc.noteUseCase.DeleteNote(c.Request.Context(), userID, noteID)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandle(c, "Note deleted successfully", nil)
}

func (nc *NoteController) SearchNotes(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	var req request.SearchNotesRequest
	if !utils.ValidateAndBindQuery(c, &req) {
		return
	}
	notes, err := nc.noteUseCase.SearchNotes(c.Request.Context(), userID, req.Query, nc.mapper.FilterMapper(&req.NoteFilterRequest))
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandle(c, "Notes retrieved successfully", nc.mapper.EntitiesToResponses(notes))
}
