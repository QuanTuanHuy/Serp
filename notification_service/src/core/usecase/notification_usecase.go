/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"

	"github.com/serp/notification-service/src/core/domain/dto/request"
	"github.com/serp/notification-service/src/core/domain/dto/response"
	"github.com/serp/notification-service/src/core/domain/entity"
	"github.com/serp/notification-service/src/core/domain/mapper"
	"github.com/serp/notification-service/src/core/service"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

type INotificationUseCase interface {
	CreateNotification(ctx context.Context, userID int64, req *request.CreateNotificationRequest) (*response.NotificationResponse, error)

	GetNotifications(ctx context.Context, userID int64, params *request.GetNotificationParams) (*response.NotificationListResponse, error)
	GetNotificationByID(ctx context.Context, userID, id int64) (*response.NotificationResponse, error)

	UpdateNotificationByID(ctx context.Context, userID, id int64, req *request.UpdateNotificationRequest) (*response.NotificationResponse, error)
	MarkAllAsRead(ctx context.Context, userID int64) error
	DeleteNotificationByID(ctx context.Context, userID, id int64) error

	GetUnreadCount(ctx context.Context, userID int64) (int64, error)
}

type NotificationUseCase struct {
	notificationService service.INotificationService
	preferenceService   service.IPreferenceService
	deliveryService     service.IDeliveryService
	txService           service.ITransactionService

	logger *zap.Logger
}

func (n *NotificationUseCase) CreateNotification(ctx context.Context, userID int64, req *request.CreateNotificationRequest) (*response.NotificationResponse, error) {
	var notification *entity.NotificationEntity

	err := n.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		var err error
		notification, err = n.notificationService.Create(ctx, tx, userID, req)
		if err != nil {
			return err
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	go func() {
		if deliveryErr := n.deliveryService.Deliver(context.Background(), notification); deliveryErr != nil {
			n.logger.Error("failed to deliver notification", zap.Error(deliveryErr), zap.Int64("notification_id", notification.ID))
		}
	}()
	return mapper.NotificationEntityToResponse(notification), nil
}

func (n *NotificationUseCase) GetNotifications(ctx context.Context, userID int64, params *request.GetNotificationParams) (*response.NotificationListResponse, error) {
	notifications, total, err := n.notificationService.GetList(ctx, userID, params)
	if err != nil {
		return nil, err
	}
	unreadCount, err := n.notificationService.GetUnreadCount(ctx, userID)
	if err != nil {
		return nil, err
	}
	return &response.NotificationListResponse{
		Notifications: mapper.NotificationEntitiesToResponses(notifications),
		TotalCount:    total,
		UnreadCount:   unreadCount,
		Page:          params.Page,
		PageSize:      params.PageSize,
	}, nil
}

func (n *NotificationUseCase) DeleteNotificationByID(ctx context.Context, userID int64, id int64) error {
	return n.notificationService.Delete(ctx, nil, id, userID)
}

func (n *NotificationUseCase) GetNotificationByID(ctx context.Context, userID int64, id int64) (*response.NotificationResponse, error) {
	notification, err := n.notificationService.GetByIDAndUserID(ctx, id, userID)
	if err != nil {
		return nil, err
	}
	return mapper.NotificationEntityToResponse(notification), nil
}

func (n *NotificationUseCase) GetUnreadCount(ctx context.Context, userID int64) (int64, error) {
	return n.notificationService.GetUnreadCount(ctx, userID)
}

func (n *NotificationUseCase) MarkAllAsRead(ctx context.Context, userID int64) error {
	return n.notificationService.MarkAllAsRead(ctx, nil, userID)
}

func (n *NotificationUseCase) UpdateNotificationByID(
	ctx context.Context,
	userID int64,
	id int64,
	req *request.UpdateNotificationRequest,
) (*response.NotificationResponse, error) {
	notification, err := n.notificationService.GetByIDAndUserID(ctx, id, userID)
	if err != nil {
		return nil, err
	}
	if req.IsRead != nil {
		notification.IsRead = *req.IsRead
	}
	if req.IsArchived != nil {
		notification.IsArchived = *req.IsArchived
	}
	updatedNotification, err := n.notificationService.Update(ctx, nil, notification)
	if err != nil {
		return nil, err
	}
	return mapper.NotificationEntityToResponse(updatedNotification), nil
}

func NewNotificationUseCase(
	notificationService service.INotificationService,
	preferenceService service.IPreferenceService,
	deliveryService service.IDeliveryService,
	txService service.ITransactionService,
	logger *zap.Logger,
) INotificationUseCase {
	return &NotificationUseCase{
		notificationService: notificationService,
		preferenceService:   preferenceService,
		deliveryService:     deliveryService,
		txService:           txService,
		logger:              logger,
	}
}
