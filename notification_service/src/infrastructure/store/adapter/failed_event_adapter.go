/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"errors"

	"github.com/serp/notification-service/src/core/domain/entity"
	port "github.com/serp/notification-service/src/core/port/store"
	"github.com/serp/notification-service/src/infrastructure/store/mapper"
	"github.com/serp/notification-service/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type FailedEventAdapter struct {
	BaseStoreAdapter
}

func (f *FailedEventAdapter) GetFailedEvent(ctx context.Context, eventID string) (*entity.FailedEventEntity, error) {
	var failedEvent model.FailedEventModel
	err := f.db.WithContext(ctx).
		Where("event_id = ?", eventID).
		First(&failedEvent).Error

	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}

	return mapper.ToFailedEventEntity(&failedEvent), nil
}

func (f *FailedEventAdapter) RecordFailedEvent(ctx context.Context, failedEvent *entity.FailedEventEntity) (*entity.FailedEventEntity, error) {
	var existing model.FailedEventModel
	err := f.db.WithContext(ctx).
		Where("event_id = ?", failedEvent.EventID).
		First(&existing).Error

	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			newRecord := &model.FailedEventModel{
				EventID:      failedEvent.EventID,
				EventType:    failedEvent.EventType,
				Topic:        failedEvent.Topic,
				MessageKey:   failedEvent.MessageKey,
				MessageValue: failedEvent.MessageValue,
				RetryCount:   1,
				LastError:    failedEvent.LastError,
				Status:       model.FailedEventStatusPending,
			}
			if err := f.db.WithContext(ctx).Create(newRecord).Error; err != nil {
				return nil, err
			}
			return mapper.ToFailedEventEntity(newRecord), nil
		}
		return nil, err
	}

	existing.RetryCount++
	existing.LastError = failedEvent.LastError
	if err := f.db.WithContext(ctx).Save(&existing).Error; err != nil {
		return nil, err
	}

	return mapper.ToFailedEventEntity(&existing), nil
}

func (f *FailedEventAdapter) MarkAsSentToDLQ(ctx context.Context, eventID string) error {
	return f.db.WithContext(ctx).
		Model(&model.FailedEventModel{}).
		Where("event_id = ?", eventID).
		Update("status", model.FailedEventStatusSentToDLQ).Error
}

func (f *FailedEventAdapter) DeleteFailedEvent(ctx context.Context, eventID string) error {
	return f.db.WithContext(ctx).
		Where("event_id = ?", eventID).
		Delete(&model.FailedEventModel{}).Error
}

func NewFailedEventAdapter(db *gorm.DB) port.IFailedEventPort {
	return &FailedEventAdapter{
		BaseStoreAdapter: BaseStoreAdapter{db: db},
	}
}
