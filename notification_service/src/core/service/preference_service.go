/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"fmt"

	"github.com/serp/notification-service/src/core/domain/dto/request"
	"github.com/serp/notification-service/src/core/domain/entity"
	"github.com/serp/notification-service/src/core/domain/enum"
	client "github.com/serp/notification-service/src/core/port/client"
	store "github.com/serp/notification-service/src/core/port/store"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

type IPreferenceService interface {
	GetOrCreate(ctx context.Context, tx *gorm.DB, userID int64) (*entity.NotificationPreferenceEntity, error)
	GetByUserID(ctx context.Context, userID int64) (*entity.NotificationPreferenceEntity, error)
	Update(ctx context.Context, tx *gorm.DB, userID int64,
		req *request.UpdatePreferenceRequest) (*entity.NotificationPreferenceEntity, error)

	IsTypeEnabled(ctx context.Context, userID int64, notificationType enum.NotificationType) (bool, error)

	ShouldDeliver(ctx context.Context,
		pref *entity.NotificationPreferenceEntity,
		notification *entity.NotificationEntity,
		channel enum.DeliveryChannel,
	) (bool, error)
}

const (
	PreferenceCacheKey        = "serp:notification:preference:%d"
	PreferenceCacheTTLSeconds = 600
)

type PreferenceService struct {
	preferencePort store.IPreferencePort
	redisPort      client.IRedisPort
	logger         *zap.Logger
}

func (p *PreferenceService) GetByUserID(ctx context.Context, userID int64) (*entity.NotificationPreferenceEntity, error) {
	var cached entity.NotificationPreferenceEntity
	cacheKey := fmt.Sprintf(PreferenceCacheKey, userID)
	err := p.redisPort.GetFromRedis(ctx, cacheKey, &cached)
	if err == nil && cached.UserID != 0 {
		return &cached, nil
	}

	pref, err := p.preferencePort.GetByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}
	if pref == nil {
		return entity.NewNotificationPreference(userID), nil
	}

	go func() {
		_ = p.redisPort.SetToRedis(ctx, cacheKey, pref, PreferenceCacheTTLSeconds)
	}()

	return pref, nil
}

func (p *PreferenceService) GetOrCreate(ctx context.Context, tx *gorm.DB, userID int64) (*entity.NotificationPreferenceEntity, error) {
	existing, err := p.preferencePort.GetByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}
	if existing != nil {
		return existing, nil
	}

	defaultPref := entity.NewNotificationPreference(userID)
	created, err := p.preferencePort.Create(ctx, tx, defaultPref)
	if err != nil {
		p.logger.Error("failed to create default preference", zap.Int64("userID", userID), zap.Error(err))
		return nil, err
	}
	return created, nil
}

func (p *PreferenceService) IsTypeEnabled(ctx context.Context, userID int64, notificationType enum.NotificationType) (bool, error) {
	// TODO: implement category-based preference check
	return true, nil
}

func (p *PreferenceService) ShouldDeliver(
	ctx context.Context,
	pref *entity.NotificationPreferenceEntity,
	notification *entity.NotificationEntity,
	channel enum.DeliveryChannel,
) (bool, error) {
	if !pref.IsChannelEnabled(channel) {
		return false, nil
	}

	if notification.Priority != enum.PriorityUrgent {
		if pref.IsQuietHoursNow() {
			// During quiet hours, only allow in-app notifications
			if channel != enum.ChannelInApp {
				return false, nil
			}
		}
	}

	return true, nil
}

func (p *PreferenceService) Update(
	ctx context.Context,
	tx *gorm.DB,
	userID int64,
	req *request.UpdatePreferenceRequest,
) (*entity.NotificationPreferenceEntity, error) {

	existing, err := p.GetOrCreate(ctx, tx, userID)
	if err != nil {
		p.logger.Error("failed to get or create preference", zap.Int64("userID", userID), zap.Error(err))
		return nil, err
	}

	existing = p.appPlyUpdate(existing, req)

	if err := existing.Validate(); err != nil {
		return nil, err
	}

	updated, err := p.preferencePort.Update(ctx, tx, existing)
	if err != nil {
		p.logger.Error("failed to update preference", zap.Int64("userID", userID), zap.Error(err))
		return nil, err
	}

	go func() {
		cacheKey := fmt.Sprintf(PreferenceCacheKey, userID)
		_ = p.redisPort.SetToRedis(ctx, cacheKey, updated, PreferenceCacheTTLSeconds)
	}()

	return updated, nil
}

func (p *PreferenceService) appPlyUpdate(pref *entity.NotificationPreferenceEntity, req *request.UpdatePreferenceRequest,
) *entity.NotificationPreferenceEntity {
	if req.EnableInApp != nil {
		pref.EnableInApp = *req.EnableInApp
	}
	if req.EnableEmail != nil {
		pref.EnableEmail = *req.EnableEmail
	}
	if req.EnablePush != nil {
		pref.EnablePush = *req.EnablePush
	}

	if req.QuietHoursEnabled != nil {
		pref.QuietHoursEnabled = *req.QuietHoursEnabled
	}
	if req.QuietHoursStartMin != nil {
		pref.QuietHoursStartMin = req.QuietHoursStartMin
	}
	if req.QuietHoursEndMin != nil {
		pref.QuietHoursEndMin = req.QuietHoursEndMin
	}

	return pref
}

func NewPreferenceService(
	preferencePort store.IPreferencePort,
	redisPort client.IRedisPort,
	logger *zap.Logger,
) IPreferenceService {
	return &PreferenceService{
		preferencePort: preferencePort,
		redisPort:      redisPort,
		logger:         logger,
	}
}
