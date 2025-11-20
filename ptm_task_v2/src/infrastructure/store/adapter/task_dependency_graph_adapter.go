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

type TaskDependencyGraphAdapter struct {
	db     *gorm.DB
	mapper *mapper.TaskDependencyGraphMapper
}

func NewTaskDependencyGraphAdapter(db *gorm.DB) store.ITaskDependencyGraphPort {
	return &TaskDependencyGraphAdapter{
		db:     db,
		mapper: mapper.NewTaskDependencyGraphMapper(),
	}
}

// CreateDependencyGraph creates a new dependency graph entry
func (a *TaskDependencyGraphAdapter) CreateDependencyGraph(ctx context.Context, tx *gorm.DB, graph *entity.TaskDependencyGraphEntity) error {
	db := a.getDB(tx)
	graphModel := a.mapper.ToModel(graph)

	if err := db.WithContext(ctx).Create(graphModel).Error; err != nil {
		return fmt.Errorf("failed to create dependency graph: %w", err)
	}

	graph.ID = graphModel.ID
	graph.CreatedAt = graphModel.CreatedAt.UnixMilli()
	graph.UpdatedAt = graphModel.UpdatedAt.UnixMilli()

	return nil
}

// CreateDependencyGraphs creates multiple dependency graphs in batch
func (a *TaskDependencyGraphAdapter) CreateDependencyGraphs(ctx context.Context, tx *gorm.DB, graphs []*entity.TaskDependencyGraphEntity) error {
	if len(graphs) == 0 {
		return nil
	}

	db := a.getDB(tx)
	graphModels := a.mapper.ToModels(graphs)

	if err := db.WithContext(ctx).CreateInBatches(graphModels, 100).Error; err != nil {
		return fmt.Errorf("failed to create dependency graphs: %w", err)
	}

	for i, graphModel := range graphModels {
		graphs[i].ID = graphModel.ID
		graphs[i].CreatedAt = graphModel.CreatedAt.UnixMilli()
		graphs[i].UpdatedAt = graphModel.UpdatedAt.UnixMilli()
	}

	return nil
}

// GetDependencyGraphByID retrieves a dependency graph by ID
func (a *TaskDependencyGraphAdapter) GetDependencyGraphByID(ctx context.Context, id int64) (*entity.TaskDependencyGraphEntity, error) {
	var graphModel model.TaskDependencyGraphModel

	if err := a.db.WithContext(ctx).First(&graphModel, id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get dependency graph by id: %w", err)
	}

	return a.mapper.ToEntity(&graphModel), nil
}

// GetDependencyGraphsByTaskID retrieves all dependencies for a task
func (a *TaskDependencyGraphAdapter) GetDependencyGraphsByTaskID(ctx context.Context, taskID int64) ([]*entity.TaskDependencyGraphEntity, error) {
	var graphModels []*model.TaskDependencyGraphModel

	if err := a.db.WithContext(ctx).
		Where("task_id = ? AND active_status = ?", taskID, "ACTIVE").
		Find(&graphModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get dependency graphs by task id: %w", err)
	}

	return a.mapper.ToEntities(graphModels), nil
}

// GetBlockingTasks retrieves tasks that this task depends on
func (a *TaskDependencyGraphAdapter) GetBlockingTasks(ctx context.Context, taskID int64) ([]*entity.TaskDependencyGraphEntity, error) {
	var graphModels []*model.TaskDependencyGraphModel

	if err := a.db.WithContext(ctx).
		Where("task_id = ? AND active_status = ?", taskID, "ACTIVE").
		Find(&graphModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get blocking tasks: %w", err)
	}

	return a.mapper.ToEntities(graphModels), nil
}

// GetBlockedTasks retrieves tasks that depend on this task
func (a *TaskDependencyGraphAdapter) GetBlockedTasks(ctx context.Context, taskID int64) ([]*entity.TaskDependencyGraphEntity, error) {
	var graphModels []*model.TaskDependencyGraphModel

	if err := a.db.WithContext(ctx).
		Where("depends_on_task_id = ? AND active_status = ?", taskID, "ACTIVE").
		Find(&graphModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get blocked tasks: %w", err)
	}

	return a.mapper.ToEntities(graphModels), nil
}

// GetInvalidDependencies retrieves all invalid dependencies for a user
func (a *TaskDependencyGraphAdapter) GetInvalidDependencies(ctx context.Context, userID int64) ([]*entity.TaskDependencyGraphEntity, error) {
	var graphModels []*model.TaskDependencyGraphModel

	if err := a.db.WithContext(ctx).
		Where("user_id = ? AND is_valid = ? AND active_status = ?", userID, false, "ACTIVE").
		Find(&graphModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get invalid dependencies: %w", err)
	}

	return a.mapper.ToEntities(graphModels), nil
}

// UpdateDependencyGraph updates a dependency graph
func (a *TaskDependencyGraphAdapter) UpdateDependencyGraph(ctx context.Context, tx *gorm.DB, graph *entity.TaskDependencyGraphEntity) error {
	db := a.getDB(tx)
	graphModel := a.mapper.ToModel(graph)

	if err := db.WithContext(ctx).Save(graphModel).Error; err != nil {
		return fmt.Errorf("failed to update dependency graph: %w", err)
	}

	graph.UpdatedAt = graphModel.UpdatedAt.UnixMilli()

	return nil
}

// MarkAsInvalid marks a dependency as invalid
func (a *TaskDependencyGraphAdapter) MarkAsInvalid(ctx context.Context, tx *gorm.DB, graphID int64, reason string, circularPath []int64) error {
	db := a.getDB(tx)

	updates := map[string]interface{}{
		"is_valid":             false,
		"invalidation_reason":  reason,
		"circular_path":        circularPath,
		"invalidation_time_ms": getCurrentTimeMs(),
	}

	if err := db.WithContext(ctx).Model(&model.TaskDependencyGraphModel{}).
		Where("id = ?", graphID).
		Updates(updates).Error; err != nil {
		return fmt.Errorf("failed to mark dependency as invalid: %w", err)
	}

	return nil
}

// MarkAsValid marks a dependency as valid
func (a *TaskDependencyGraphAdapter) MarkAsValid(ctx context.Context, tx *gorm.DB, graphID int64) error {
	db := a.getDB(tx)

	updates := map[string]interface{}{
		"is_valid":             true,
		"invalidation_reason":  nil,
		"circular_path":        nil,
		"invalidation_time_ms": nil,
	}

	if err := db.WithContext(ctx).Model(&model.TaskDependencyGraphModel{}).
		Where("id = ?", graphID).
		Updates(updates).Error; err != nil {
		return fmt.Errorf("failed to mark dependency as valid: %w", err)
	}

	return nil
}

// SoftDeleteDependencyGraph soft deletes a dependency graph
func (a *TaskDependencyGraphAdapter) SoftDeleteDependencyGraph(ctx context.Context, tx *gorm.DB, id int64) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.TaskDependencyGraphModel{}).
		Where("id = ?", id).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to soft delete dependency graph: %w", err)
	}

	return nil
}

// DeleteDependenciesByTaskID deletes all dependencies for a task
func (a *TaskDependencyGraphAdapter) DeleteDependenciesByTaskID(ctx context.Context, tx *gorm.DB, taskID int64) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.TaskDependencyGraphModel{}).
		Where("task_id = ? OR depends_on_task_id = ?", taskID, taskID).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to delete dependencies by task id: %w", err)
	}

	return nil
}

// DeleteDependencyBetweenTasks deletes a specific dependency between two tasks
func (a *TaskDependencyGraphAdapter) DeleteDependencyBetweenTasks(ctx context.Context, tx *gorm.DB, taskID int64, dependsOnTaskID int64) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.TaskDependencyGraphModel{}).
		Where("task_id = ? AND depends_on_task_id = ?", taskID, dependsOnTaskID).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to delete dependency between tasks: %w", err)
	}

	return nil
}

// ValidateDependency validates if a dependency can be created
func (a *TaskDependencyGraphAdapter) ValidateDependency(ctx context.Context, taskID int64, dependsOnTaskID int64) (*store.DependencyValidationResult, error) {
	// Check for self-dependency
	if taskID == dependsOnTaskID {
		return &store.DependencyValidationResult{
			IsValid: false,
			Reason:  "task cannot depend on itself",
		}, nil
	}

	// Check for circular dependencies
	circularPaths, err := a.DetectCircularDependencies(ctx, dependsOnTaskID)
	if err != nil {
		return nil, err
	}

	if len(circularPaths) > 0 {
		return &store.DependencyValidationResult{
			IsValid:       false,
			Reason:        "circular dependency detected",
			CircularPaths: circularPaths,
		}, nil
	}

	// Check dependency depth
	depth, err := a.GetDependencyDepth(ctx, dependsOnTaskID)
	if err != nil {
		return nil, err
	}

	const maxDepth = 10 // Maximum dependency depth allowed
	if depth >= maxDepth {
		return &store.DependencyValidationResult{
			IsValid:          false,
			Reason:           "maximum dependency depth exceeded",
			MaxDepth:         depth,
			WouldExceedDepth: true,
		}, nil
	}

	return &store.DependencyValidationResult{
		IsValid:  true,
		MaxDepth: depth,
	}, nil
}

// DetectCircularDependencies detects circular dependencies
func (a *TaskDependencyGraphAdapter) DetectCircularDependencies(ctx context.Context, taskID int64) ([][]int64, error) {
	// This is a placeholder - full implementation requires graph traversal
	// In production, use DFS/BFS to detect cycles
	return [][]int64{}, nil
}

// GetDependencyDepth calculates the maximum dependency depth for a task
func (a *TaskDependencyGraphAdapter) GetDependencyDepth(ctx context.Context, taskID int64) (int, error) {
	// This is a placeholder - full implementation requires recursive depth calculation
	return 0, nil
}

// GetTopologicalOrder returns tasks in topological order
func (a *TaskDependencyGraphAdapter) GetTopologicalOrder(ctx context.Context, userID int64) ([]int64, error) {
	// This is a placeholder - full implementation requires topological sort algorithm
	return []int64{}, nil
}

// GetDependencyStats retrieves dependency statistics for a user
func (a *TaskDependencyGraphAdapter) GetDependencyStats(ctx context.Context, userID int64) (*store.DependencyStats, error) {
	var stats store.DependencyStats

	// Total dependencies
	if err := a.db.WithContext(ctx).Model(&model.TaskDependencyGraphModel{}).
		Where("user_id = ? AND active_status = ?", userID, "ACTIVE").
		Count(&stats.TotalDependencies).Error; err != nil {
		return nil, fmt.Errorf("failed to count total dependencies: %w", err)
	}

	// Valid dependencies
	if err := a.db.WithContext(ctx).Model(&model.TaskDependencyGraphModel{}).
		Where("user_id = ? AND is_valid = ? AND active_status = ?", userID, true, "ACTIVE").
		Count(&stats.ValidDependencies).Error; err != nil {
		return nil, fmt.Errorf("failed to count valid dependencies: %w", err)
	}

	// Invalid dependencies
	if err := a.db.WithContext(ctx).Model(&model.TaskDependencyGraphModel{}).
		Where("user_id = ? AND is_valid = ? AND active_status = ?", userID, false, "ACTIVE").
		Count(&stats.InvalidDependencies).Error; err != nil {
		return nil, fmt.Errorf("failed to count invalid dependencies: %w", err)
	}

	return &stats, nil
}

// GetMostBlockingTasks retrieves tasks that block the most other tasks
func (a *TaskDependencyGraphAdapter) GetMostBlockingTasks(ctx context.Context, userID int64, limit int) ([]*store.BlockingTaskInfo, error) {
	// This requires a complex query with joins - placeholder implementation
	return []*store.BlockingTaskInfo{}, nil
}

// GetMostBlockedTasks retrieves tasks that are blocked by the most dependencies
func (a *TaskDependencyGraphAdapter) GetMostBlockedTasks(ctx context.Context, userID int64, limit int) ([]*store.BlockedTaskInfo, error) {
	// This requires a complex query with joins - placeholder implementation
	return []*store.BlockedTaskInfo{}, nil
}

// Helper functions

func (a *TaskDependencyGraphAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}
