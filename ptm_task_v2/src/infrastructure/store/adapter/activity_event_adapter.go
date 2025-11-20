/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"fmt"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/port/store"
	"github.com/serp/ptm-task/src/infrastructure/store/mapper"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type ActivityEventAdapter struct {
	db     *gorm.DB
	mapper *mapper.ActivityEventMapper
}

func NewActivityEventAdapter(db *gorm.DB) store.IActivityEventStorePort {
	return &ActivityEventAdapter{
		db:     db,
		mapper: mapper.NewActivityEventMapper(),
	}
}

// CreateActivityEvent creates a new activity event
func (a *ActivityEventAdapter) CreateActivityEvent(ctx context.Context, tx *gorm.DB, activity *entity.ActivityEventEntity) error {
	db := a.getDB(tx)
	activityModel := a.mapper.ToModel(activity)

	if err := db.WithContext(ctx).Create(activityModel).Error; err != nil {
		return fmt.Errorf("failed to create activity event: %w", err)
	}

	activity.ID = activityModel.ID
	activity.CreatedAt = activityModel.CreatedAt.UnixMilli()
	activity.UpdatedAt = activityModel.UpdatedAt.UnixMilli()

	return nil
}

// GetActivityEventByID retrieves an activity event by ID
func (a *ActivityEventAdapter) GetActivityEventByID(ctx context.Context, id int64) (*entity.ActivityEventEntity, error) {
	var activityModel model.ActivityEventModel

	if err := a.db.WithContext(ctx).First(&activityModel, id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get activity event by id: %w", err)
	}

	return a.mapper.ToEntity(&activityModel), nil
}

// GetActivityEventsByUserID retrieves activity events for a user with filters
func (a *ActivityEventAdapter) GetActivityEventsByUserID(ctx context.Context, userID int64, filter *store.ActivityEventFilter) ([]*entity.ActivityEventEntity, error) {
	var activityModels []*model.ActivityEventModel

	query := a.buildActivityQuery(userID, filter)

	if err := query.WithContext(ctx).Find(&activityModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get activity events by user id: %w", err)
	}

	return a.mapper.ToEntities(activityModels), nil
}

// Helper functions

func (a *ActivityEventAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}

func (a *ActivityEventAdapter) buildActivityQuery(userID int64, filter *store.ActivityEventFilter) *gorm.DB {
	if filter == nil {
		filter = &store.ActivityEventFilter{}
	}

	query := a.db.Where("user_id = ? AND active_status = ?", userID, "ACTIVE")

	// Created time filter
	if filter.CreatedFrom != nil {
		query = query.Where("created_at >= ?", *filter.CreatedFrom)
	}
	if filter.CreatedTo != nil {
		query = query.Where("created_at <= ?", *filter.CreatedTo)
	}

	// Default sorting by created_at DESC
	query = query.Order("created_at DESC")

	// Pagination
	if filter.Limit != nil && *filter.Limit > 0 {
		query = query.Limit(*filter.Limit)
	}
	if filter.Offset != nil && *filter.Offset > 0 {
		query = query.Offset(*filter.Offset)
	}

	return query
}
