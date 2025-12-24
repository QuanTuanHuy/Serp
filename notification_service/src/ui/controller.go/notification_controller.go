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

func (n *NotificationController) GetNotificationByID(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}

	id, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	notification, err := n.notificationUseCase.GetNotificationByID(c.Request.Context(), userID, id)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandle(c, "Notification retrieved successfully", notification)
}

func (n *NotificationController) DeleteNotificationByID(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	id, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	err = n.notificationUseCase.DeleteNotificationByID(c.Request.Context(), userID, id)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandle(c, "Notification deleted successfully", nil)
}

func (n *NotificationController) GetUnreadCount(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	count, err := n.notificationUseCase.GetUnreadCount(c.Request.Context(), userID)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandle(c, "Unread notification count retrieved successfully", gin.H{"unread_count": count})
}

func (n *NotificationController) UpdateNotificationByID(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	id, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	var req request.UpdateNotificationRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}
	notification, err := n.notificationUseCase.UpdateNotificationByID(c.Request.Context(), userID, id, &req)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandle(c, "Notification updated successfully", notification)
}

func (n *NotificationController) MarkAllAsRead(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	err = n.notificationUseCase.MarkAllAsRead(c.Request.Context(), userID)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandle(c, "All notifications marked as read successfully", nil)
}
