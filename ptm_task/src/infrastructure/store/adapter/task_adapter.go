/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"fmt"
	"time"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/port/store"
	"github.com/serp/ptm-task/src/infrastructure/store/mapper"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type TaskAdapter struct {
	db     *gorm.DB
	mapper *mapper.TaskMapper
}

func NewTaskAdapter(db *gorm.DB) store.ITaskPort {
	return &TaskAdapter{
		db:     db,
		mapper: mapper.NewTaskMapper(),
	}
}

func (a *TaskAdapter) CreateTask(ctx context.Context, tx *gorm.DB, task *entity.TaskEntity) (*entity.TaskEntity, error) {
	db := a.getDB(tx)
	taskModel := a.mapper.ToModel(task)
	if err := db.WithContext(ctx).Create(taskModel).Error; err != nil {
		return nil, fmt.Errorf("failed to create task: %w", err)
	}
	return a.mapper.ToEntity(taskModel), nil
}

func (a *TaskAdapter) CreateTasks(ctx context.Context, tx *gorm.DB, tasks []*entity.TaskEntity) error {
	if len(tasks) == 0 {
		return nil
	}

	db := a.getDB(tx)
	taskModels := a.mapper.ToModels(tasks)
	if err := db.WithContext(ctx).CreateInBatches(taskModels, 100).Error; err != nil {
		return fmt.Errorf("failed to create tasks: %w", err)
	}
	return nil
}

func (a *TaskAdapter) GetTaskByID(ctx context.Context, id int64) (*entity.TaskEntity, error) {
	var taskModel model.TaskModel
	if err := a.db.WithContext(ctx).First(&taskModel, id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get task by id: %w", err)
	}
	return a.mapper.ToEntity(&taskModel), nil
}

func (a *TaskAdapter) GetTasksByIDs(ctx context.Context, ids []int64) ([]*entity.TaskEntity, error) {
	if len(ids) == 0 {
		return []*entity.TaskEntity{}, nil
	}
	if len(ids) == 1 {
		task, err := a.GetTaskByID(ctx, ids[0])
		if err != nil {
			return nil, err
		}
		if task == nil {
			return []*entity.TaskEntity{}, nil
		}
		return []*entity.TaskEntity{task}, nil
	}
	var taskModels []*model.TaskModel
	if err := a.db.WithContext(ctx).Where("id IN ?", ids).Find(&taskModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get tasks by ids: %w", err)
	}
	return a.mapper.ToEntities(taskModels), nil
}

func (a *TaskAdapter) GetTasksByUserID(ctx context.Context, userID int64, filter *store.TaskFilter) ([]*entity.TaskEntity, error) {
	var taskModels []*model.TaskModel
	query := a.buildTaskQuery(userID, filter)
	if err := query.WithContext(ctx).Find(&taskModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get tasks by user id: %w", err)
	}
	return a.mapper.ToEntities(taskModels), nil
}

func (a *TaskAdapter) CountTasksByUserID(ctx context.Context, userID int64, filter *store.TaskFilter) (int64, error) {
	var count int64
	filter.Limit = 0
	filter.Offset = 0
	query := a.buildTaskQuery(userID, filter)
	if err := query.WithContext(ctx).Model(&model.TaskModel{}).Count(&count).Error; err != nil {
		return 0, fmt.Errorf("failed to count tasks: %w", err)
	}
	return count, nil
}

func (a *TaskAdapter) GetTaskByExternalID(ctx context.Context, externalID string) (*entity.TaskEntity, error) {
	var taskModel model.TaskModel
	if err := a.db.WithContext(ctx).Where("external_id = ?", externalID).First(&taskModel).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get task by external id: %w", err)
	}
	return a.mapper.ToEntity(&taskModel), nil
}

func (a *TaskAdapter) UpdateTask(ctx context.Context, tx *gorm.DB, task *entity.TaskEntity) (*entity.TaskEntity, error) {
	db := a.getDB(tx)
	taskModel := a.mapper.ToModel(task)
	if err := db.WithContext(ctx).Updates(taskModel).Error; err != nil {
		return nil, fmt.Errorf("failed to update task: %w", err)
	}
	return a.mapper.ToEntity(taskModel), nil
}

func (a *TaskAdapter) UpdateTaskStatus(ctx context.Context, tx *gorm.DB, taskID int64, status string) error {
	db := a.getDB(tx)

	updates := map[string]any{
		"status": status,
	}
	if status == "DONE" {
		updates["completed_at"] = getCurrentTimeMs()
	}

	if err := db.WithContext(ctx).Model(&model.TaskModel{}).
		Where("id = ?", taskID).
		Updates(updates).Error; err != nil {
		return fmt.Errorf("failed to update task status: %w", err)
	}

	return nil
}

func (a *TaskAdapter) UpdateTaskPriority(ctx context.Context, tx *gorm.DB, taskID int64, priority string, priorityScore float64) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.TaskModel{}).
		Where("id = ?", taskID).
		Updates(map[string]any{
			"priority":       priority,
			"priority_score": priorityScore,
		}).Error; err != nil {
		return fmt.Errorf("failed to update task priority: %w", err)
	}

	return nil
}

func (a *TaskAdapter) UpdateTaskDuration(ctx context.Context, tx *gorm.DB, taskID int64, actualDurationMin int) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.TaskModel{}).
		Where("id = ?", taskID).
		Update("actual_duration_min", actualDurationMin).Error; err != nil {
		return fmt.Errorf("failed to update task duration: %w", err)
	}

	return nil
}

func (a *TaskAdapter) SoftDeleteTask(ctx context.Context, tx *gorm.DB, taskID int64) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.TaskModel{}).
		Where("id = ?", taskID).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to soft delete task: %w", err)
	}

	return nil
}

func (a *TaskAdapter) SoftDeleteTasks(ctx context.Context, tx *gorm.DB, taskIDs []int64) error {
	if len(taskIDs) == 0 {
		return nil
	}

	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.TaskModel{}).
		Where("id IN ?", taskIDs).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to soft delete tasks: %w", err)
	}

	return nil
}

func (a *TaskAdapter) GetOverdueTasks(ctx context.Context, userID int64, currentTimeMs int64) ([]*entity.TaskEntity, error) {
	var taskModels []*model.TaskModel

	if err := a.db.WithContext(ctx).
		Where("user_id = ? AND deadline_ms < ? AND status NOT IN ? AND active_status = ?",
			userID, currentTimeMs, []string{"DONE", "CANCELLED"}, "ACTIVE").
		Order("deadline_ms ASC").
		Find(&taskModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get overdue tasks: %w", err)
	}

	return a.mapper.ToEntities(taskModels), nil
}

func (a *TaskAdapter) GetTasksByDeadline(ctx context.Context, userID int64, fromMs, toMs int64) ([]*entity.TaskEntity, error) {
	var taskModels []*model.TaskModel

	if err := a.db.WithContext(ctx).
		Where("user_id = ? AND deadline_ms BETWEEN ? AND ? AND active_status = ?",
			userID, fromMs, toMs, "ACTIVE").
		Order("deadline_ms ASC").
		Find(&taskModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get tasks by deadline: %w", err)
	}

	return a.mapper.ToEntities(taskModels), nil
}

func (a *TaskAdapter) GetTasksByCategory(ctx context.Context, userID int64, category string) ([]*entity.TaskEntity, error) {
	var taskModels []*model.TaskModel

	if err := a.db.WithContext(ctx).
		Where("user_id = ? AND category = ? AND active_status = ?", userID, category, "ACTIVE").
		Order("created_at DESC").
		Find(&taskModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get tasks by category: %w", err)
	}

	return a.mapper.ToEntities(taskModels), nil
}

func (a *TaskAdapter) GetDeepWorkTasks(ctx context.Context, userID int64) ([]*entity.TaskEntity, error) {
	var taskModels []*model.TaskModel

	if err := a.db.WithContext(ctx).
		Where("user_id = ? AND is_deep_work = ? AND active_status = ?", userID, true, "ACTIVE").
		Order("priority_score DESC, deadline_ms ASC").
		Find(&taskModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get deep work tasks: %w", err)
	}

	return a.mapper.ToEntities(taskModels), nil
}

func (a *TaskAdapter) GetTasksByParentID(ctx context.Context, parentTaskID int64) ([]*entity.TaskEntity, error) {
	var taskModels []*model.TaskModel

	if err := a.db.WithContext(ctx).
		Where("parent_task_id = ? AND active_status = ?", parentTaskID, "ACTIVE").
		Order("created_at ASC").
		Find(&taskModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get tasks by parent id: %w", err)
	}

	return a.mapper.ToEntities(taskModels), nil
}

func (a *TaskAdapter) GetTasksByRootID(ctx context.Context, rootTaskID int64) ([]*entity.TaskEntity, error) {
	var taskModels []*model.TaskModel
	sql := `
	WITH RECURSIVE task_tree AS (
		SELECT * FROM tasks WHERE id = ?
		UNION ALL
		SELECT t.* FROM tasks t
		INNER JOIN task_tree tt ON t.parent_task_id = tt.id
	)
	SELECT * FROM task_tree WHERE active_status = 'ACTIVE';
	`

	if err := a.db.WithContext(ctx).
		Raw(sql, rootTaskID).
		Scan(&taskModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get tasks by root id: %w", err)
	}

	return a.mapper.ToEntities(taskModels), nil
}

func (a *TaskAdapter) GetTasksByProjectID(ctx context.Context, projectID int64) ([]*entity.TaskEntity, error) {
	var taskModels []*model.TaskModel

	if err := a.db.WithContext(ctx).
		Where("project_id = ? AND active_status = ?", projectID, "ACTIVE").
		Order("created_at DESC").
		Find(&taskModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get tasks by project id: %w", err)
	}

	return a.mapper.ToEntities(taskModels), nil
}

func (a *TaskAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}

func (a *TaskAdapter) buildTaskQuery(userID int64, filter *store.TaskFilter) *gorm.DB {
	if filter == nil {
		filter = store.NewTaskFilter()
	}

	query := a.db.Where("user_id = ?", userID)

	if len(filter.Statuses) > 0 {
		query = query.Where("status IN ?", filter.Statuses)
	}

	if filter.ActiveStatus != nil {
		query = query.Where("active_status = ?", *filter.ActiveStatus)
	} else {
		query = query.Where("active_status = ?", "ACTIVE")
	}

	if len(filter.Priorities) > 0 {
		query = query.Where("priority IN ?", filter.Priorities)
	}
	if filter.MinPriorityScore != nil {
		query = query.Where("priority_score >= ?", *filter.MinPriorityScore)
	}

	if filter.DeadlineFrom != nil {
		query = query.Where("deadline_ms >= ?", *filter.DeadlineFrom)
	}
	if filter.DeadlineTo != nil {
		query = query.Where("deadline_ms <= ?", *filter.DeadlineTo)
	}

	if filter.CreatedFrom != nil {
		query = query.Where("created_at >= ?", *filter.CreatedFrom)
	}
	if filter.CreatedTo != nil {
		query = query.Where("created_at <= ?", *filter.CreatedTo)
	}

	if len(filter.Categories) > 0 {
		query = query.Where("category IN ?", filter.Categories)
	}

	if len(filter.Tags) > 0 {
		for _, tag := range filter.Tags {
			query = query.Where("tags @> ?", fmt.Sprintf(`["%s"]`, tag))
		}
	}

	if filter.ProjectID != nil {
		query = query.Where("project_id = ?", *filter.ProjectID)
	}

	if filter.IsDeepWork != nil {
		query = query.Where("is_deep_work = ?", *filter.IsDeepWork)
	}
	if filter.IsMeeting != nil {
		query = query.Where("is_meeting = ?", *filter.IsMeeting)
	}
	if filter.IsRecurring != nil {
		query = query.Where("is_recurring = ?", *filter.IsRecurring)
	}

	if filter.ParentTaskID != nil {
		query = query.Where("parent_task_id = ?", *filter.ParentTaskID)
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

func getCurrentTimeMs() int64 {
	return time.Now().UnixMilli()
}
