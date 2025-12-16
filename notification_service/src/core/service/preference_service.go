/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/serp/notification-service/src/core/domain/entity"
	"github.com/serp/notification-service/src/core/domain/enum"
	client "github.com/serp/notification-service/src/core/port/client"
	store "github.com/serp/notification-service/src/core/port/store"
	"gorm.io/gorm"
)

type IPreferenceService interface {
	GetOrCreate(ctx context.Context, tx *gorm.DB, userID int64) (*entity.NotificationPreferenceEntity, error)
	GetByUserID(ctx context.Context, userID int64) (*entity.NotificationPreferenceEntity, error)
	Update(ctx context.Context, tx *gorm.DB, userID int64, req any) (*entity.NotificationPreferenceEntity, error)

	IsChannelEnabled(ctx context.Context, userID int64, channel enum.DeliveryChannel) (bool, error)
	GetEnabledChannels(ctx context.Context, userID int64) ([]enum.DeliveryChannel, error)

	IsTypeEnabled(ctx context.Context, userID int64, notificationType enum.NotificationType) (bool, error)

	IsQuietHours(ctx context.Context, userID int64, checkTimeMin int) (bool, error)

	ShouldDeliver(ctx context.Context, userID int64, notification *entity.NotificationEntity,
		channel enum.DeliveryChannel, currentTimeMin int) (bool, error)
}

type PreferenceService struct {
	preferencePort store.IPreferencePort
	redisPort      client.IRedisPort
}

func (p *PreferenceService) GetByUserID(ctx context.Context, userID int64) (*entity.NotificationPreferenceEntity, error) {
	panic("unimplemented")
}

func (p *PreferenceService) GetEnabledChannels(ctx context.Context, userID int64) ([]enum.DeliveryChannel, error) {
	panic("unimplemented")
}

func (p *PreferenceService) GetOrCreate(ctx context.Context, tx *gorm.DB, userID int64) (*entity.NotificationPreferenceEntity, error) {
	panic("unimplemented")
}

func (p *PreferenceService) IsChannelEnabled(ctx context.Context, userID int64, channel enum.DeliveryChannel) (bool, error) {
	panic("unimplemented")
}

func (p *PreferenceService) IsQuietHours(ctx context.Context, userID int64, checkTimeMin int) (bool, error) {
	panic("unimplemented")
}

func (p *PreferenceService) IsTypeEnabled(ctx context.Context, userID int64, notificationType enum.NotificationType) (bool, error) {
	panic("unimplemented")
}

func (p *PreferenceService) ShouldDeliver(ctx context.Context, userID int64, notification *entity.NotificationEntity, channel enum.DeliveryChannel, currentTimeMin int) (bool, error) {
	panic("unimplemented")
}

func (p *PreferenceService) Update(ctx context.Context, tx *gorm.DB, userID int64, req any) (*entity.NotificationPreferenceEntity, error) {
	panic("unimplemented")
}

func NewPreferenceService(
	preferencePort store.IPreferencePort,
	redisPort client.IRedisPort,
) IPreferenceService {
	return &PreferenceService{
		preferencePort: preferencePort,
		redisPort:      redisPort,
	}
}
