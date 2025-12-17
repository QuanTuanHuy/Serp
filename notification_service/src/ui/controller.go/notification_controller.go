/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/notification-service/src/core/domain/dto/request"
	"github.com/serp/notification-service/src/core/usecase"
	"github.com/serp/notification-service/src/kernel/utils"
)

type NotificationController struct {
	notificationUseCase usecase.INotificationUseCase
}

func NewNotificationController(
	notificationUseCase usecase.INotificationUseCase,
) *NotificationController {
	return &NotificationController{
		notificationUseCase: notificationUseCase,
	}
}

func (n *NotificationController) CreateNotification(c *gin.Context) {
	var req request.CreateNotificationRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}

	result, err := n.notificationUseCase.CreateNotification(c.Request.Context(), req.UserID, &req)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandle(c, "Notification created successfully", result)
}

func (n *NotificationController) GetNotifications(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}

	var params request.GetNotificationParams
	if !utils.ValidateAndBindQuery(c, &params) {
		return
	}
	notifications, err := n.notificationUseCase.GetNotifications(c.Request.Context(), userID, &params)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandle(c, "Notifications retrieved successfully", notifications)
}
