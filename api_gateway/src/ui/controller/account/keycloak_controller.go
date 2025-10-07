/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/
package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/core/domain/constant"
	service "github.com/serp/api-gateway/src/core/service/account"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type KeycloakController struct {
	keycloakService service.IKeycloakService
}

func (k *KeycloakController) GetKeycloakClientSecret(c *gin.Context) {
	clientId := c.Param("clientId")
	secret, err := k.keycloakService.GetKeycloakClientSecret(c.Request.Context(), clientId)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	utils.SuccessfulHandle(c, secret)
}

func NewKeycloakController(keycloakService service.IKeycloakService) *KeycloakController {
	return &KeycloakController{
		keycloakService: keycloakService,
	}
}
