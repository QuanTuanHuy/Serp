package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/notification-service/src/core/domain/dto/request"
	"github.com/serp/notification-service/src/core/usecase"
	"github.com/serp/notification-service/src/kernel/utils"
)

type PreferenceController struct {
	prefUseCase usecase.IPreferenceUseCase
}

func NewPreferenceController(
	prefUseCase usecase.IPreferenceUseCase,
) *PreferenceController {
	return &PreferenceController{
		prefUseCase: prefUseCase,
	}
}

func (p *PreferenceController) GetPreferences(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}

	pref, err := p.prefUseCase.GetPreferences(c.Request.Context(), userID)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}

	utils.SuccessfulHandle(c, "Preferences retrieved successfully", pref)
}

func (p *PreferenceController) UpdatePreferences(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}

	var req request.UpdatePreferenceRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}
	updatedPref, err := p.prefUseCase.UpdatePreferences(c.Request.Context(), userID, &req)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandle(c, "Preferences updated successfully", updatedPref)
}
