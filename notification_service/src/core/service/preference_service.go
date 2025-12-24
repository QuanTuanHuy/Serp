/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"
	"fmt"
	"slices"
	"time"

	"github.com/serp/notification-service/src/core/domain/constant"
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

	IsChannelEnabled(ctx context.Context, userID int64, channel enum.DeliveryChannel) (bool, error)
	GetEnabledChannels(ctx context.Context, userID int64) ([]enum.DeliveryChannel, error)

	IsTypeEnabled(ctx context.Context, userID int64, notificationType enum.NotificationType) (bool, error)

	IsQuietHours(ctx context.Context, userID int64, checkTimeMin int) (bool, error)
	IsQuietHoursNow(ctx context.Context, userID int64) (bool, error)

	ShouldDeliver(ctx context.Context, userID int64, notification *entity.NotificationEntity,
		channel enum.DeliveryChannel) (bool, error)
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
	pref, err := p.preferencePort.GetByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}
	if pref == nil {
		return nil, errors.New(constant.ErrPreferenceNotFound)
	}
	return pref, nil
}

func (p *PreferenceService) GetEnabledChannels(ctx context.Context, userID int64) ([]enum.DeliveryChannel, error) {
	pref, err := p.getCachedPreference(ctx, userID)
	if err != nil {
		return nil, err
	}
	enabledChannels := []enum.DeliveryChannel{}
	if pref.EnableInApp {
		enabledChannels = append(enabledChannels, enum.ChannelInApp)
	}
	if pref.EnableEmail {
		enabledChannels = append(enabledChannels, enum.ChannelEmail)
	}
	if pref.EnablePush {
		enabledChannels = append(enabledChannels, enum.ChannelPush)
	}
	return enabledChannels, nil
}

func (p *PreferenceService) GetOrCreate(ctx context.Context, tx *gorm.DB, userID int64) (*entity.NotificationPreferenceEntity, error) {
	existing, err := p.preferencePort.GetByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}
	if existing != nil {
		return existing, nil
	}

	defaultPref := p.createDefaultPreference(userID)
	created, err := p.preferencePort.Create(ctx, tx, defaultPref)
	if err != nil {
		p.logger.Error("failed to create default preference", zap.Int64("userID", userID), zap.Error(err))
		return nil, err
	}
	return created, nil
}

func (p *PreferenceService) createDefaultPreference(userID int64) *entity.NotificationPreferenceEntity {
	return &entity.NotificationPreferenceEntity{
		UserID: userID,

		EnableInApp: true,
		EnableEmail: false,
		EnablePush:  true,

		QuietHoursEnabled: false,
	}
}

func (p *PreferenceService) IsChannelEnabled(ctx context.Context, userID int64, channel enum.DeliveryChannel) (bool, error) {
	enabledChannels, err := p.GetEnabledChannels(ctx, userID)
	if err != nil {
		return false, err
	}
	return slices.Contains(enabledChannels, channel), nil
}

func (p *PreferenceService) IsQuietHours(ctx context.Context, userID int64, checkTimeMin int) (bool, error) {
	pref, err := p.getCachedPreference(ctx, userID)
	if err != nil {
		return false, err
	}
	if !pref.QuietHoursEnabled {
		return false, nil
	}
	if pref.QuietHoursStartMin == nil || pref.QuietHoursEndMin == nil {
		return false, nil
	}
	start := *pref.QuietHoursStartMin
	end := *pref.QuietHoursEndMin
	if start < end {
		return checkTimeMin >= start && checkTimeMin < end, nil
	}
	return checkTimeMin >= start || checkTimeMin < end, nil
}

func (p *PreferenceService) IsQuietHoursNow(ctx context.Context, userID int64) (bool, error) {
	currentTimeMin := func() int {
		now := time.Now()
		return now.Hour()*60 + now.Minute()
	}()
	return p.IsQuietHours(ctx, userID, currentTimeMin)
}

func (p *PreferenceService) IsTypeEnabled(ctx context.Context, userID int64, notificationType enum.NotificationType) (bool, error) {
	// TODO: implement category-based preference check
	return true, nil
}

func (p *PreferenceService) ShouldDeliver(
	ctx context.Context,
	userID int64,
	notification *entity.NotificationEntity,
	channel enum.DeliveryChannel,
) (bool, error) {
	_, err := p.getCachedPreference(ctx, userID)
	if err != nil {
		return false, err
	}

	channelEnabled, _ := p.IsChannelEnabled(ctx, userID, channel)
	if !channelEnabled {
		return false, err
	}

	if notification.Priority != enum.PriorityUrgent {
		isQuite, _ := p.IsQuietHoursNow(ctx, userID)
		if isQuite {
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

	if err := p.validatePreference(existing); err != nil {
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

func (p *PreferenceService) validatePreference(pref *entity.NotificationPreferenceEntity) error {
	if pref.QuietHoursStartMin != nil {
		if *pref.QuietHoursStartMin < 0 || *pref.QuietHoursStartMin >= 1440 {
			return errors.New(constant.ErrInvalidQuietHours)
		}
	}
	if pref.QuietHoursEndMin != nil {
		if *pref.QuietHoursEndMin < 0 || *pref.QuietHoursEndMin >= 1440 {
			return errors.New(constant.ErrInvalidQuietHours)
		}
	}
	return nil
}

func (p *PreferenceService) getCachedPreference(ctx context.Context, userID int64) (*entity.NotificationPreferenceEntity, error) {
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
		return p.createDefaultPreference(userID), nil
	}

	go func() {
		_ = p.redisPort.SetToRedis(ctx, cacheKey, pref, PreferenceCacheTTLSeconds)
	}()

	return pref, nil
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
