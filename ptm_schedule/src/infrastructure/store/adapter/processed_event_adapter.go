/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"errors"
	"time"

	port "github.com/serp/ptm-schedule/src/core/port/store"
	"github.com/serp/ptm-schedule/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type ProcessedEventAdapter struct {
	BaseStoreAdapter
}

func (p *ProcessedEventAdapter) IsEventProcessed(ctx context.Context, eventID string) (bool, error) {
	var count int64
	err := p.db.WithContext(ctx).
		Model(&model.ProcessedEventModel{}).
		Where("event_id = ?", eventID).
		Count(&count).Error
	if err != nil {
		return false, err
	}
	return count > 0, nil
}

func (p *ProcessedEventAdapter) MarkEventProcessed(ctx context.Context, eventID, eventType, topic string) error {
	event := &model.ProcessedEventModel{
		EventID:     eventID,
		EventType:   eventType,
		Topic:       topic,
		ProcessedAt: time.Now(),
	}

	err := p.db.WithContext(ctx).Create(event).Error
	if err != nil {
		// Handle duplicate key error gracefully (event was processed by another consumer)
		if errors.Is(err, gorm.ErrDuplicatedKey) {
			return nil
		}
		return err
	}
	return nil
}

func (p *ProcessedEventAdapter) CleanupOldEvents(ctx context.Context, olderThan time.Duration) (int64, error) {
	cutoffTime := time.Now().Add(-olderThan)
	result := p.db.WithContext(ctx).
		Where("processed_at < ?", cutoffTime).
		Delete(&model.ProcessedEventModel{})

	if result.Error != nil {
		return 0, result.Error
	}
	return result.RowsAffected, nil
}

func NewProcessedEventAdapter(db *gorm.DB) port.IProcessedEventPort {
	return &ProcessedEventAdapter{
		BaseStoreAdapter: BaseStoreAdapter{db: db},
	}
}
