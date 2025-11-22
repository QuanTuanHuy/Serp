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

type TaskReminderAdapter struct {
	db     *gorm.DB
	mapper *mapper.TaskReminderMapper
}

func NewTaskReminderAdapter(db *gorm.DB) store.ITaskReminderPort {
	return &TaskReminderAdapter{
		db:     db,
		mapper: mapper.NewTaskReminderMapper(),
	}
}

func (a *TaskReminderAdapter) CreateReminder(ctx context.Context, tx *gorm.DB, reminder *entity.TaskReminderEntity) error {
	db := a.getDB(tx)
	reminderModel := a.mapper.ToModel(reminder)

	if err := db.WithContext(ctx).Create(reminderModel).Error; err != nil {
		return fmt.Errorf("failed to create reminder: %w", err)
	}

	return nil
}

func (a *TaskReminderAdapter) CreateReminders(ctx context.Context, tx *gorm.DB, reminders []*entity.TaskReminderEntity) error {
	if len(reminders) == 0 {
		return nil
	}

	db := a.getDB(tx)
	reminderModels := a.mapper.ToModels(reminders)

	if err := db.WithContext(ctx).CreateInBatches(reminderModels, 100).Error; err != nil {
		return fmt.Errorf("failed to create reminders: %w", err)
	}
	return nil
}

func (a *TaskReminderAdapter) GetReminderByID(ctx context.Context, id int64) (*entity.TaskReminderEntity, error) {
	var reminderModel model.TaskReminderModel

	if err := a.db.WithContext(ctx).First(&reminderModel, id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get reminder by id: %w", err)
	}

	return a.mapper.ToEntity(&reminderModel), nil
}

func (a *TaskReminderAdapter) GetRemindersByTaskID(ctx context.Context, taskID int64) ([]*entity.TaskReminderEntity, error) {
	var reminderModels []*model.TaskReminderModel

	if err := a.db.WithContext(ctx).
		Where("task_id = ? AND active_status = ?", taskID, "ACTIVE").
		Order("scheduled_time_ms ASC").
		Find(&reminderModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get reminders by task id: %w", err)
	}

	return a.mapper.ToEntities(reminderModels), nil
}

func (a *TaskReminderAdapter) GetRemindersByUserID(ctx context.Context, userID int64, filter *store.ReminderFilter) ([]*entity.TaskReminderEntity, error) {
	var reminderModels []*model.TaskReminderModel

	query := a.buildReminderQuery(userID, filter)

	if err := query.WithContext(ctx).Find(&reminderModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get reminders by user id: %w", err)
	}

	return a.mapper.ToEntities(reminderModels), nil
}

func (a *TaskReminderAdapter) GetPendingReminders(ctx context.Context, currentTimeMs int64, limit int) ([]*entity.TaskReminderEntity, error) {
	var reminderModels []*model.TaskReminderModel

	query := a.db.WithContext(ctx).
		Where("reminder_status = ? AND scheduled_time_ms <= ? AND active_status = ?",
			"PENDING", currentTimeMs, "ACTIVE").
		Order("scheduled_time_ms ASC")

	if limit > 0 {
		query = query.Limit(limit)
	}

	if err := query.Find(&reminderModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get pending reminders: %w", err)
	}

	return a.mapper.ToEntities(reminderModels), nil
}

func (a *TaskReminderAdapter) GetDueReminders(ctx context.Context, currentTimeMs int64, limit int) ([]*entity.TaskReminderEntity, error) {
	var reminderModels []*model.TaskReminderModel

	query := a.db.WithContext(ctx).
		Where("scheduled_time_ms <= ? AND reminder_status IN ? AND active_status = ?",
			currentTimeMs, []string{"PENDING", "SENT"}, "ACTIVE").
		Where("is_read = ?", false).
		Order("scheduled_time_ms ASC")

	if limit > 0 {
		query = query.Limit(limit)
	}

	if err := query.Find(&reminderModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get due reminders: %w", err)
	}

	return a.mapper.ToEntities(reminderModels), nil
}

func (a *TaskReminderAdapter) UpdateReminder(ctx context.Context, tx *gorm.DB, reminder *entity.TaskReminderEntity) error {
	db := a.getDB(tx)
	reminderModel := a.mapper.ToModel(reminder)

	if err := db.WithContext(ctx).Save(reminderModel).Error; err != nil {
		return fmt.Errorf("failed to update reminder: %w", err)
	}

	reminder.UpdatedAt = reminderModel.UpdatedAt.UnixMilli()

	return nil
}

func (a *TaskReminderAdapter) MarkAsSent(ctx context.Context, tx *gorm.DB, reminderID int64, sentAt int64) error {
	db := a.getDB(tx)

	updates := map[string]interface{}{
		"reminder_status": "SENT",
		"sent_at_ms":      sentAt,
	}

	if err := db.WithContext(ctx).Model(&model.TaskReminderModel{}).
		Where("id = ?", reminderID).
		Updates(updates).Error; err != nil {
		return fmt.Errorf("failed to mark reminder as sent: %w", err)
	}

	return nil
}

func (a *TaskReminderAdapter) MarkAsRead(ctx context.Context, tx *gorm.DB, reminderID int64, readAt int64) error {
	db := a.getDB(tx)

	updates := map[string]interface{}{
		"is_read":    true,
		"read_at_ms": readAt,
	}

	if err := db.WithContext(ctx).Model(&model.TaskReminderModel{}).
		Where("id = ?", reminderID).
		Updates(updates).Error; err != nil {
		return fmt.Errorf("failed to mark reminder as read: %w", err)
	}

	return nil
}

func (a *TaskReminderAdapter) SnoozeReminder(ctx context.Context, tx *gorm.DB, reminderID int64, snoozeUntil int64) error {
	db := a.getDB(tx)

	updates := map[string]interface{}{
		"is_snoozed":        true,
		"snooze_until_ms":   snoozeUntil,
		"scheduled_time_ms": snoozeUntil,
		"reminder_status":   "PENDING",
	}

	if err := db.WithContext(ctx).Model(&model.TaskReminderModel{}).
		Where("id = ?", reminderID).
		Updates(updates).Error; err != nil {
		return fmt.Errorf("failed to snooze reminder: %w", err)
	}

	return nil
}

func (a *TaskReminderAdapter) DismissReminder(ctx context.Context, tx *gorm.DB, reminderID int64) error {
	db := a.getDB(tx)

	updates := map[string]interface{}{
		"is_dismissed":    true,
		"reminder_status": "DISMISSED",
	}

	if err := db.WithContext(ctx).Model(&model.TaskReminderModel{}).
		Where("id = ?", reminderID).
		Updates(updates).Error; err != nil {
		return fmt.Errorf("failed to dismiss reminder: %w", err)
	}

	return nil
}

func (a *TaskReminderAdapter) SoftDeleteReminder(ctx context.Context, tx *gorm.DB, id int64) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.TaskReminderModel{}).
		Where("id = ?", id).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to soft delete reminder: %w", err)
	}

	return nil
}

func (a *TaskReminderAdapter) DeleteRemindersByTaskID(ctx context.Context, tx *gorm.DB, taskID int64) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.TaskReminderModel{}).
		Where("task_id = ?", taskID).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to delete reminders by task id: %w", err)
	}

	return nil
}

func (a *TaskReminderAdapter) CountUnreadReminders(ctx context.Context, userID int64) (int64, error) {
	var count int64

	if err := a.db.WithContext(ctx).Model(&model.TaskReminderModel{}).
		Where("user_id = ? AND is_read = ? AND active_status = ?", userID, false, "ACTIVE").
		Count(&count).Error; err != nil {
		return 0, fmt.Errorf("failed to count unread reminders: %w", err)
	}

	return count, nil
}

func (a *TaskReminderAdapter) GetReminderDeliveryStats(ctx context.Context, userID int64, fromTimeMs int64) (*store.ReminderDeliveryStats, error) {
	var stats store.ReminderDeliveryStats

	baseQuery := a.db.WithContext(ctx).Model(&model.TaskReminderModel{}).
		Where("user_id = ? AND active_status = ?", userID, "ACTIVE")

	if fromTimeMs > 0 {
		baseQuery = baseQuery.Where("created_at >= ?", fromTimeMs)
	}

	// Total reminders
	if err := baseQuery.Count(&stats.TotalReminders).Error; err != nil {
		return nil, fmt.Errorf("failed to count total reminders: %w", err)
	}

	// Sent reminders
	if err := a.db.WithContext(ctx).Model(&model.TaskReminderModel{}).
		Where("user_id = ? AND reminder_status = ? AND active_status = ?", userID, "SENT", "ACTIVE").
		Count(&stats.SentReminders).Error; err != nil {
		return nil, fmt.Errorf("failed to count sent reminders: %w", err)
	}

	// Read reminders
	if err := a.db.WithContext(ctx).Model(&model.TaskReminderModel{}).
		Where("user_id = ? AND is_read = ? AND active_status = ?", userID, true, "ACTIVE").
		Count(&stats.ReadReminders).Error; err != nil {
		return nil, fmt.Errorf("failed to count read reminders: %w", err)
	}

	// Snoozed reminders
	if err := a.db.WithContext(ctx).Model(&model.TaskReminderModel{}).
		Where("user_id = ? AND is_snoozed = ? AND active_status = ?", userID, true, "ACTIVE").
		Count(&stats.SnoozedReminders).Error; err != nil {
		return nil, fmt.Errorf("failed to count snoozed reminders: %w", err)
	}

	// Dismissed reminders
	if err := a.db.WithContext(ctx).Model(&model.TaskReminderModel{}).
		Where("user_id = ? AND is_dismissed = ? AND active_status = ?", userID, true, "ACTIVE").
		Count(&stats.DismissedReminders).Error; err != nil {
		return nil, fmt.Errorf("failed to count dismissed reminders: %w", err)
	}

	// Calculate rates
	if stats.TotalReminders > 0 {
		stats.DeliveryRate = float64(stats.SentReminders) / float64(stats.TotalReminders)
	}
	if stats.SentReminders > 0 {
		stats.ReadRate = float64(stats.ReadReminders) / float64(stats.SentReminders)
	}

	return &stats, nil
}

func (a *TaskReminderAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}

func (a *TaskReminderAdapter) buildReminderQuery(userID int64, filter *store.ReminderFilter) *gorm.DB {
	if filter == nil {
		filter = store.NewReminderFilter()
	}

	query := a.db.Where("user_id = ?", userID)

	query = query.Where("active_status = ?", "ACTIVE")

	if len(filter.ReminderTypes) > 0 {
		query = query.Where("reminder_type IN ?", filter.ReminderTypes)
	}

	if len(filter.ReminderStatuses) > 0 {
		query = query.Where("reminder_status IN ?", filter.ReminderStatuses)
	}

	if filter.ScheduledFrom != nil {
		query = query.Where("scheduled_time_ms >= ?", *filter.ScheduledFrom)
	}
	if filter.ScheduledTo != nil {
		query = query.Where("scheduled_time_ms <= ?", *filter.ScheduledTo)
	}

	if filter.IsRead != nil {
		query = query.Where("is_read = ?", *filter.IsRead)
	}
	if filter.IsSnoozed != nil {
		query = query.Where("is_snoozed = ?", *filter.IsSnoozed)
	}
	if filter.IsDismissed != nil {
		query = query.Where("is_dismissed = ?", *filter.IsDismissed)
	}

	if len(filter.Channels) > 0 {
		query = query.Where("channel IN ?", filter.Channels)
	}

	if filter.SortBy != "" && filter.SortOrder != "" {
		query = query.Order(fmt.Sprintf("%s %s", filter.SortBy, filter.SortOrder))
	}

	if filter.Limit > 0 {
		query = query.Limit(filter.Limit)
	}
	if filter.Offset > 0 {
		query = query.Offset(filter.Offset)
	}

	return query
}
