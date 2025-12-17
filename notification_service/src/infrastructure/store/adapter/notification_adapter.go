/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"fmt"
	"strings"
	"time"

	"github.com/serp/notification-service/src/core/domain/dto/request"
	"github.com/serp/notification-service/src/core/domain/entity"
	"github.com/serp/notification-service/src/core/domain/enum"
	port "github.com/serp/notification-service/src/core/port/store"
	"github.com/serp/notification-service/src/infrastructure/store/mapper"
	"github.com/serp/notification-service/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type NotificationAdapter struct {
	db     *gorm.DB
	mapper *mapper.NotificationMapper
}

func NewNotificationAdapter(db *gorm.DB) port.INotificationPort {
	return &NotificationAdapter{
		db:     db,
		mapper: mapper.NewNotificationMapper(),
	}
}

func (a *NotificationAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}

func (a *NotificationAdapter) Create(ctx context.Context, tx *gorm.DB, notification *entity.NotificationEntity) (*entity.NotificationEntity, error) {
	db := a.getDB(tx)
	modelObj := a.mapper.ToModel(notification)
	if err := db.WithContext(ctx).Create(modelObj).Error; err != nil {
		return nil, fmt.Errorf("failed to create notification: %w", err)
	}
	return a.mapper.ToEntity(modelObj), nil
}

func (a *NotificationAdapter) CreateBatch(ctx context.Context, tx *gorm.DB, notifications []*entity.NotificationEntity) ([]*entity.NotificationEntity, error) {
	if len(notifications) == 0 {
		return []*entity.NotificationEntity{}, nil
	}
	db := a.getDB(tx)
	models := a.mapper.ToModels(notifications)
	if err := db.WithContext(ctx).CreateInBatches(models, 100).Error; err != nil {
		return nil, fmt.Errorf("failed to create notifications: %w", err)
	}
	return a.mapper.ToEntities(models), nil
}

func (a *NotificationAdapter) GetByID(ctx context.Context, id int64) (*entity.NotificationEntity, error) {
	var modelObj model.NotificationModel
	if err := a.db.WithContext(ctx).First(&modelObj, id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get notification: %w", err)
	}
	return a.mapper.ToEntity(&modelObj), nil
}

func (a *NotificationAdapter) buildListQuery(params *request.GetNotificationParams) *gorm.DB {
	query := a.db.Model(&model.NotificationModel{})

	if params == nil {
		return query
	}
	if params.UserID != nil {
		query = query.Where("user_id = ?", *params.UserID)
	}
	if params.TenantID != nil {
		query = query.Where("tenant_id = ?", *params.TenantID)
	}
	if params.Type != nil {
		query = query.Where("type = ?", strings.ToUpper(*params.Type))
	}
	if params.Category != nil {
		query = query.Where("category = ?", strings.ToUpper(*params.Category))
	}
	if params.Priority != nil {
		query = query.Where("priority = ?", strings.ToUpper(*params.Priority))
	}
	if params.IsRead != nil {
		query = query.Where("is_read = ?", *params.IsRead)
	}

	sortBy := strings.ToLower(params.SortBy)
	switch sortBy {
	case "createdat", "created_at":
		sortBy = "created_at"
	case "priority":
		sortBy = "priority"
	case "status":
		sortBy = "status"
	case "deliveryat", "delivery_at":
		sortBy = "delivery_at"
	default:
		sortBy = "id"
	}

	sortOrder := strings.ToUpper(params.SortOrder)
	if sortOrder != "ASC" {
		sortOrder = "DESC"
	}

	query = query.Order(fmt.Sprintf("%s %s", sortBy, sortOrder))
	return query
}

func (a *NotificationAdapter) GetList(ctx context.Context, params *request.GetNotificationParams) ([]*entity.NotificationEntity, int64, error) {
	if params == nil {
		params = &request.GetNotificationParams{}
	}
	query := a.buildListQuery(params)

	var total int64
	if err := query.WithContext(ctx).Count(&total).Error; err != nil {
		return nil, 0, fmt.Errorf("failed to count notifications: %w", err)
	}

	if params.PageSize <= 0 {
		params.PageSize = 10
	}
	if params.Page < 0 {
		params.Page = 0
	}
	offset := params.Page * params.PageSize

	var models []*model.NotificationModel
	if err := query.WithContext(ctx).
		Limit(params.PageSize).
		Offset(offset).
		Find(&models).Error; err != nil {
		return nil, 0, fmt.Errorf("failed to list notifications: %w", err)
	}

	return a.mapper.ToEntities(models), total, nil
}

func (a *NotificationAdapter) CountUnread(ctx context.Context, userID int64) (int64, error) {
	var count int64
	if err := a.db.WithContext(ctx).
		Model(&model.NotificationModel{}).
		Where("user_id = ? AND is_read = ?", userID, false).
		Count(&count).Error; err != nil {
		return 0, fmt.Errorf("failed to count unread notifications: %w", err)
	}
	return count, nil
}

func (a *NotificationAdapter) Update(ctx context.Context, tx *gorm.DB, id int64, notification *entity.NotificationEntity) (*entity.NotificationEntity, error) {
	db := a.getDB(tx)
	modelObj := a.mapper.ToModel(notification)
	modelObj.ID = id
	if err := db.WithContext(ctx).Save(modelObj).Error; err != nil {
		return nil, fmt.Errorf("failed to update notification: %w", err)
	}
	return a.mapper.ToEntity(modelObj), nil
}

func (a *NotificationAdapter) UpdateBatch(ctx context.Context, tx *gorm.DB, notifications []*entity.NotificationEntity) ([]*entity.NotificationEntity, error) {
	if len(notifications) == 0 {
		return []*entity.NotificationEntity{}, nil
	}
	db := a.getDB(tx)
	models := a.mapper.ToModels(notifications)
	for _, m := range models {
		if err := db.WithContext(ctx).Save(m).Error; err != nil {
			return nil, fmt.Errorf("failed to update notification batch: %w", err)
		}
	}
	return a.mapper.ToEntities(models), nil
}

func (a *NotificationAdapter) UpdateAllUnread(ctx context.Context, tx *gorm.DB, userID int64) error {
	db := a.getDB(tx)
	now := time.Now()
	updates := map[string]any{
		"is_read":    true,
		"read_at":    now,
		"status":     string(enum.NotificationRead),
		"updated_at": now,
	}

	if err := db.WithContext(ctx).
		Model(&model.NotificationModel{}).
		Where("user_id = ? AND is_read = ?", userID, false).
		Updates(updates).Error; err != nil {
		return fmt.Errorf("failed to mark all unread notifications: %w", err)
	}
	return nil
}

func (a *NotificationAdapter) Delete(ctx context.Context, tx *gorm.DB, id int64) error {
	db := a.getDB(tx)
	if err := db.WithContext(ctx).Delete(&model.NotificationModel{}, id).Error; err != nil {
		return fmt.Errorf("failed to delete notification: %w", err)
	}
	return nil
}
