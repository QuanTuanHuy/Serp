/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"
	"fmt"

	"github.com/serp/notification-service/src/core/domain/constant"
	"github.com/serp/notification-service/src/core/domain/dto/request"
	"github.com/serp/notification-service/src/core/domain/entity"
	"github.com/serp/notification-service/src/core/domain/enum"
	"github.com/serp/notification-service/src/core/domain/mapper"
	client "github.com/serp/notification-service/src/core/port/client"
	store "github.com/serp/notification-service/src/core/port/store"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

type INotificationService interface {
	Create(ctx context.Context, tx *gorm.DB, userID int64, req *request.CreateNotificationRequest) (*entity.NotificationEntity, error)
	Update(ctx context.Context, tx *gorm.DB, notification *entity.NotificationEntity) (*entity.NotificationEntity, error)
	GetByID(ctx context.Context, id int64) (*entity.NotificationEntity, error)
	GetByIDAndUserID(ctx context.Context, id int64, userID int64) (*entity.NotificationEntity, error)
	GetList(ctx context.Context, userID int64, params *request.GetNotificationParams) ([]*entity.NotificationEntity, int64, error)

	MarkAsRead(ctx context.Context, tx *gorm.DB, id, userID int64) error
	MarkAllAsRead(ctx context.Context, tx *gorm.DB, userID int64) error
	Delete(ctx context.Context, tx *gorm.DB, id, userID int64) error

	GetUnreadCount(ctx context.Context, userID int64) (int64, error)

	CreateBulk(ctx context.Context, tx *gorm.DB, notifications []*entity.NotificationEntity) error

	ValidateNotification(notification *entity.NotificationEntity) error
	EnrichNotification(ctx context.Context, notification *entity.NotificationEntity) (*entity.NotificationEntity, error)
}

type NotificationService struct {
	notificationPort store.INotificationPort
	redisPort        client.IRedisPort

	logger *zap.Logger
}

const (
	UnreadCountCacheKey        = "serp:notification:unread:%d"
	UnreadCountCacheTTLSeConds = 300
)

func (n *NotificationService) Create(ctx context.Context, tx *gorm.DB, userID int64, req *request.CreateNotificationRequest) (*entity.NotificationEntity, error) {
	notification := mapper.CreateRequestToEntity(req)
	notification.UserID = userID
	notification.Status = enum.NotificationUnread

	if err := n.ValidateNotification(notification); err != nil {
		return nil, err
	}

	created, err := n.notificationPort.Create(ctx, tx, notification)
	if err != nil {
		return nil, err
	}

	go func() {
		n.invalidateUserCache(context.Background(), userID)
	}()

	return created, nil
}

func (n *NotificationService) Update(ctx context.Context, tx *gorm.DB, notification *entity.NotificationEntity) (*entity.NotificationEntity, error) {
	updated, err := n.notificationPort.Update(ctx, tx, notification.ID, notification)
	if err != nil {
		n.logger.Error("failed to update notification", zap.Int64("notificationID", notification.ID), zap.Int64("userID", notification.UserID), zap.Error(err))
		return nil, err
	}
	return updated, nil
}

func (n *NotificationService) CreateBulk(ctx context.Context, tx *gorm.DB, notifications []*entity.NotificationEntity) error {
	if len(notifications) == 0 {
		return nil
	}
	for _, notification := range notifications {
		if err := n.ValidateNotification(notification); err != nil {
			return err
		}
	}

	for _, notification := range notifications {
		notification.Status = enum.NotificationUnread
	}
	_, err := n.notificationPort.CreateBatch(ctx, tx, notifications)
	if err != nil {
		n.logger.Error("failed to create bulk notifications", zap.Error(err))
		return err
	}

	go func() {
		userIDs := n.extractUniqueUserIDs(notifications)
		for _, userID := range userIDs {
			n.invalidateUserCache(context.Background(), userID)
		}
	}()

	return nil
}

func (n *NotificationService) extractUniqueUserIDs(notifications []*entity.NotificationEntity) []int64 {
	userIDSet := make(map[int64]struct{})
	for _, notification := range notifications {
		userIDSet[notification.UserID] = struct{}{}
	}
	uniqueUserIDs := make([]int64, 0, len(userIDSet))
	for userID := range userIDSet {
		uniqueUserIDs = append(uniqueUserIDs, userID)
	}
	return uniqueUserIDs
}

func (n *NotificationService) Delete(ctx context.Context, tx *gorm.DB, id int64, userID int64) error {
	notification, err := n.GetByIDAndUserID(ctx, id, userID)
	if err != nil {
		return err
	}
	err = n.notificationPort.Delete(ctx, tx, notification.ID)
	if err != nil {
		n.logger.Error("failed to delete notification", zap.Int64("notificationID", id), zap.Int64("userID", userID), zap.Error(err))
		return err
	}
	go func() {
		n.invalidateUserCache(context.Background(), userID)
	}()
	return nil
}

func (n *NotificationService) EnrichNotification(ctx context.Context, notification *entity.NotificationEntity) (*entity.NotificationEntity, error) {
	panic("unimplemented")
}

func (n *NotificationService) GetByID(ctx context.Context, id int64) (*entity.NotificationEntity, error) {
	notification, err := n.notificationPort.GetByID(ctx, id)
	if err != nil {
		return nil, err
	}
	if notification == nil {
		return nil, errors.New(constant.ErrNotifcationNotFound)
	}
	return notification, nil
}

func (n *NotificationService) GetByIDAndUserID(ctx context.Context, id int64, userID int64) (*entity.NotificationEntity, error) {
	notification, err := n.notificationPort.GetByID(ctx, id)
	if err != nil {
		return nil, err
	}
	if notification == nil || notification.UserID != userID {
		return nil, errors.New(constant.ErrNotifcationNotFound)
	}
	return notification, nil
}

func (n *NotificationService) GetList(ctx context.Context, userID int64, params *request.GetNotificationParams) ([]*entity.NotificationEntity, int64, error) {
	params.UserID = &userID
	notifications, total, err := n.notificationPort.GetList(ctx, params)
	if err != nil {
		return nil, 0, err
	}
	return notifications, total, nil
}

func (n *NotificationService) GetUnreadCount(ctx context.Context, userID int64) (int64, error) {
	var count int64 = -1

	cachekey := fmt.Sprintf(UnreadCountCacheKey, userID)
	err := n.redisPort.GetFromRedis(ctx, cachekey, &count)
	if err == nil && count >= 0 {
		return count, nil
	}

	count, err = n.notificationPort.CountUnread(ctx, userID)
	if err != nil {
		n.logger.Error("failed to get unread count", zap.Int64("userID", userID), zap.Error(err))
		return 0, err
	}

	go func() {
		err := n.redisPort.SetToRedis(context.Background(), cachekey, count, UnreadCountCacheTTLSeConds)
		if err != nil {
			n.logger.Error("failed to cache unread count", zap.Int64("userID", userID), zap.Error(err))
		}
	}()

	return count, nil
}

func (n *NotificationService) MarkAllAsRead(ctx context.Context, tx *gorm.DB, userID int64) error {
	err := n.notificationPort.UpdateAllUnread(ctx, tx, userID)
	if err != nil {
		n.logger.Error("failed to mark all as read", zap.Int64("userID", userID), zap.Error(err))
		return err
	}
	go func() {
		n.redisPort.SetToRedis(context.Background(), fmt.Sprintf(UnreadCountCacheKey, userID), 0, UnreadCountCacheTTLSeConds)
	}()
	return nil
}

func (n *NotificationService) MarkAsRead(ctx context.Context, tx *gorm.DB, id int64, userID int64) error {
	notification, err := n.GetByIDAndUserID(ctx, id, userID)
	if err != nil {
		return err
	}
	if notification.IsRead {
		return nil
	}

	notification.MarkAsRead()
	_, err = n.notificationPort.Update(ctx, tx, id, notification)
	if err != nil {
		n.logger.Error("failed to mark as read", zap.Int64("notificationID", id), zap.Int64("userID", userID), zap.Error(err))
		return err
	}

	go func() {
		n.decrementUnreadCountInCache(context.Background(), userID)
	}()

	return nil
}

func (n *NotificationService) ValidateNotification(notification *entity.NotificationEntity) error {
	if notification.Title == "" {
		return errors.New(constant.ErrEmptyTitle)
	}
	if len(notification.Title) > 255 {
		return errors.New(constant.ErrTitleTooLong)
	}

	if !enum.IsValidNotificationType(notification.Type) {
		return errors.New(constant.ErrInvalidType)
	}

	return nil
}

func (n *NotificationService) invalidateUserCache(ctx context.Context, userID int64) {
	unreadKey := fmt.Sprintf(UnreadCountCacheKey, userID)
	n.redisPort.DeleteKeyFromRedis(ctx, unreadKey)
}

func (n *NotificationService) decrementUnreadCountInCache(ctx context.Context, userID int64) {
	unreadKey := fmt.Sprintf(UnreadCountCacheKey, userID)
	n.redisPort.DecrementInRedis(ctx, unreadKey)
}

func NewNotificationService(
	notificationPort store.INotificationPort,
	redisPort client.IRedisPort,
	logger *zap.Logger,
) INotificationService {
	return &NotificationService{
		notificationPort: notificationPort,
		redisPort:        redisPort,
		logger:           logger,
	}
}
