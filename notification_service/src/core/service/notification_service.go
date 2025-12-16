/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/serp/notification-service/src/core/domain/dto/request"
	"github.com/serp/notification-service/src/core/domain/entity"
	client "github.com/serp/notification-service/src/core/port/client"
	store "github.com/serp/notification-service/src/core/port/store"
	"gorm.io/gorm"
)

type INotificationService interface {
	Create(ctx context.Context, tx *gorm.DB, userID int64, req *request.CreateNotificationRequest) (*entity.NotificationEntity, error)
	GetByID(ctx context.Context, id int64) (*entity.NotificationEntity, error)
	GetList(ctx context.Context, userID int64, params *request.GetNotificationParams) ([]*entity.NotificationEntity, int64, error)

	MarkAsRead(ctx context.Context, tx *gorm.DB, id, userID int64) error
	MarkAllAsRead(ctx context.Context, tx *gorm.DB, userID int64) error
	Delete(ctx context.Context, tx *gorm.DB, id, userID int64) error

	GetUnreadCount(ctx context.Context, userID int64) (int64, error)

	CreateBulk(ctx context.Context, tx *gorm.DB, notifications []*entity.NotificationEntity) error

	ValidateNotification(ctx context.Context, notification *entity.NotificationEntity) error
	EnrichNotification(ctx context.Context, notification *entity.NotificationEntity) (*entity.NotificationEntity, error)
}

type NotificationService struct {
	notificationPort store.INotificationPort
	redisPort        client.IRedisPort
}

func (n *NotificationService) Create(ctx context.Context, tx *gorm.DB, userID int64, req *request.CreateNotificationRequest) (*entity.NotificationEntity, error) {
	panic("unimplemented")
}

func (n *NotificationService) CreateBulk(ctx context.Context, tx *gorm.DB, notifications []*entity.NotificationEntity) error {
	panic("unimplemented")
}

func (n *NotificationService) Delete(ctx context.Context, tx *gorm.DB, id int64, userID int64) error {
	panic("unimplemented")
}

func (n *NotificationService) EnrichNotification(ctx context.Context, notification *entity.NotificationEntity) (*entity.NotificationEntity, error) {
	panic("unimplemented")
}

func (n *NotificationService) GetByID(ctx context.Context, id int64) (*entity.NotificationEntity, error) {
	panic("unimplemented")
}

func (n *NotificationService) GetList(ctx context.Context, userID int64, params *request.GetNotificationParams) ([]*entity.NotificationEntity, int64, error) {
	panic("unimplemented")
}

func (n *NotificationService) GetUnreadCount(ctx context.Context, userID int64) (int64, error) {
	panic("unimplemented")
}

func (n *NotificationService) MarkAllAsRead(ctx context.Context, tx *gorm.DB, userID int64) error {
	panic("unimplemented")
}

func (n *NotificationService) MarkAsRead(ctx context.Context, tx *gorm.DB, id int64, userID int64) error {
	panic("unimplemented")
}

func (n *NotificationService) ValidateNotification(ctx context.Context, notification *entity.NotificationEntity) error {
	panic("unimplemented")
}

func NewNotificationService(
	notificationPort store.INotificationPort,
	redisPort client.IRedisPort,
) INotificationService {
	return &NotificationService{
		notificationPort: notificationPort,
		redisPort:        redisPort,
	}
}
