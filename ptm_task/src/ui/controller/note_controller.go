package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/usecase"
	"github.com/serp/ptm-task/src/kernel/utils"
)

type NoteController struct {
	noteUseCase usecase.INoteUseCase
}

func (n *NoteController) CreateNote(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	var request request.CreateNoteDTO

	if !utils.ValidateAndBindJSON(c, &request) {
		return
	}

	note, err := n.noteUseCase.CreateNote(c, userID, &request)
	if err != nil {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		return
	}
	utils.SuccessfulHandle(c, note)
}

func (n *NoteController) GetAllNotes(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}

	notes, err := n.noteUseCase.GetNotesByUserID(c, userID)
	if err != nil {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		return
	}
	utils.SuccessfulHandle(c, notes)
}

func (n *NoteController) GetNoteByID(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	noteID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	note, err := n.noteUseCase.GetNoteByID(c, userID, noteID)
	if err != nil {
		if err.Error() == constant.NoteNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.NoteNotFound)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, note)
}

func (n *NoteController) DeleteNote(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	noteID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	err := n.noteUseCase.DeleteNote(c, userID, noteID)
	if err != nil {
		if err.Error() == constant.NoteNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.NoteNotFound)
		} else if err.Error() == constant.DeleteNoteForbidden {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, constant.DeleteNoteForbidden)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, nil)
}

func (n *NoteController) LockNote(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	noteID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	var request request.LockNoteDTO
	if !utils.ValidateAndBindJSON(c, &request) {
		return
	}

	note, err := n.noteUseCase.LockNote(c, userID, noteID, &request)
	if err != nil {
		if err.Error() == constant.LockNoteForbidden {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, constant.LockNoteForbidden)
		} else if err.Error() == constant.NoteNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.NoteNotFound)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, note)
}

func (n *NoteController) UnlockNote(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	noteID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	var request request.UnlockNoteDTO
	if !utils.ValidateAndBindJSON(c, &request) {
		return
	}

	note, err := n.noteUseCase.UnlockNote(c, userID, noteID, request.NotePassword)
	if err != nil {
		if err.Error() == constant.NotePasswordMismatch {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, constant.NotePasswordMismatch)
		} else if err.Error() == constant.NoteNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.NoteNotFound)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, note)
}

func NewNoteController(noteUseCase usecase.INoteUseCase) *NoteController {
	return &NoteController{
		noteUseCase: noteUseCase,
	}
}
