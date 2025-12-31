/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"
	"time"

	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/core/port/store"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

type IProjectService interface {
	ValidateProjectData(project *entity.ProjectEntity) error
	ValidateProjectOwnership(userID int64, project *entity.ProjectEntity) error
	ValidateProjectStatus(currentStatus, newStatus string) error

	CalculateProgressPercentage(totalTasks, completedTasks int) int
	CheckIfOverdue(project *entity.ProjectEntity, currentTimeMs int64) bool
	CheckIfCanBeCompleted(project *entity.ProjectEntity, taskStats *store.ProjectStats) bool

	CreateProject(ctx context.Context, tx *gorm.DB, userID int64, project *entity.ProjectEntity) (*entity.ProjectEntity, error)
	UpdateProject(ctx context.Context, tx *gorm.DB, userID int64, oldProject, newProject *entity.ProjectEntity) error
	UpdateProjectProgress(ctx context.Context, tx *gorm.DB, projectID int64, totalTasks, completedTasks int) error
	DeleteProject(ctx context.Context, tx *gorm.DB, userID int64, projectID int64) error

	GetProjectByID(ctx context.Context, projectID int64) (*entity.ProjectEntity, error)
	GetProjectByUserIDAndID(ctx context.Context, userID, projectID int64) (*entity.ProjectEntity, error)
	GetProjectWithStats(ctx context.Context, projectID int64) (*entity.ProjectEntity, error)
	GetProjectsByUserID(ctx context.Context, userID int64, filter *store.ProjectFilter) ([]*entity.ProjectEntity, error)
	CountProjectsByUserID(ctx context.Context, userID int64, filter *store.ProjectFilter) (int64, error)
	GetProjectsWithStats(ctx context.Context, userID int64, filter *store.ProjectFilter) ([]*entity.ProjectEntity, error)
	GetFavoriteProjects(ctx context.Context, userID int64) ([]*entity.ProjectEntity, error)
	GetOverdueProjects(ctx context.Context, userID int64) ([]*entity.ProjectEntity, error)
}

type projectService struct {
	projectPort store.IProjectPort
	taskPort    store.ITaskPort

	logger *zap.Logger
}

func NewProjectService(
	projectPort store.IProjectPort,
	taskPort store.ITaskPort,
	logger *zap.Logger,
) IProjectService {
	return &projectService{
		projectPort: projectPort,
		taskPort:    taskPort,
		logger:      logger,
	}
}

func (s *projectService) ValidateProjectData(project *entity.ProjectEntity) error {
	if project.Title == "" {
		return errors.New(constant.ProjectTitleRequired)
	}
	if len(project.Title) > 500 {
		return errors.New(constant.ProjectTitleTooLong)
	}
	if !enum.TaskPriority(project.Priority).IsValid() {
		return errors.New(constant.InvalidProjectPriority)
	}
	if !enum.ProjectStatus(project.Status).IsValid() {
		return errors.New(constant.InvalidProjectStatus)
	}
	if project.DeadlineMs != nil && project.StartDateMs != nil {
		if *project.DeadlineMs < *project.StartDateMs {
			return errors.New(constant.InvalidProjectDeadline)
		}
	}
	if project.ProgressPercentage < 0 || project.ProgressPercentage > 100 {
		return errors.New(constant.InvalidProjectProgress)
	}
	return nil
}

func (s *projectService) ValidateProjectOwnership(userID int64, project *entity.ProjectEntity) error {
	if project.UserID != userID {
		return errors.New(constant.UpdateProjectForbidden)
	}
	return nil
}

func (s *projectService) ValidateProjectStatus(currentStatus, newStatus string) error {
	current := enum.ProjectStatus(currentStatus)
	if !current.IsValid() {
		return errors.New(constant.InvalidProjectStatus)
	}
	new := enum.ProjectStatus(newStatus)
	if !new.IsValid() {
		return errors.New(constant.InvalidProjectStatus)
	}
	if !current.CanTransitionTo(new) {
		return errors.New(constant.InvalidProjectStatusTransition)
	}
	return nil
}

func (s *projectService) CalculateProgressPercentage(totalTasks, completedTasks int) int {
	if totalTasks == 0 {
		return 0
	}
	return int(float64(completedTasks) / float64(totalTasks) * 100)
}

func (s *projectService) CheckIfOverdue(project *entity.ProjectEntity, currentTimeMs int64) bool {
	return project.IsOverdue(currentTimeMs)
}

func (s *projectService) CheckIfCanBeCompleted(project *entity.ProjectEntity, taskStats *store.ProjectStats) bool {
	if taskStats.TotalTasks == 0 {
		return true
	}
	return taskStats.CompletedTasks == taskStats.TotalTasks
}

func (s *projectService) CreateProject(ctx context.Context, tx *gorm.DB, userID int64, project *entity.ProjectEntity) (*entity.ProjectEntity, error) {
	project.UserID = userID
	now := time.Now().UnixMilli()
	project.CreatedAt = now
	project.UpdatedAt = now

	if project.Status == "" {
		project.Status = string(enum.ProjectNew)
	}
	if project.ActiveStatus == "" {
		project.ActiveStatus = string(enum.Active)
	}
	if project.Priority == "" {
		project.Priority = string(enum.Medium)
	}
	project.ProgressPercentage = 0

	if err := s.ValidateProjectData(project); err != nil {
		return nil, err
	}

	project, err := s.projectPort.CreateProject(ctx, tx, project)
	if err != nil {
		return nil, err
	}

	return project, nil
}

func (s *projectService) UpdateProject(ctx context.Context, tx *gorm.DB, userID int64, oldProject, newProject *entity.ProjectEntity) error {
	if err := s.ValidateProjectOwnership(userID, newProject); err != nil {
		return err
	}
	if err := s.ValidateProjectData(newProject); err != nil {
		return err
	}

	if oldProject.Status != newProject.Status {
		if err := s.ValidateProjectStatus(oldProject.Status, newProject.Status); err != nil {
			return err
		}

		if newProject.IsCompleted() {
			stats, err := s.projectPort.GetProjectStats(ctx, newProject.ID)
			if err != nil {
				return err
			}
			if !s.CheckIfCanBeCompleted(newProject, stats) {
				return errors.New(constant.ProjectCannotComplete)
			}
		}
	}

	newProject.UpdatedAt = time.Now().UnixMilli()
	return s.projectPort.UpdateProject(ctx, tx, newProject)
}

func (s *projectService) UpdateProjectProgress(ctx context.Context, tx *gorm.DB, projectID int64, totalTasks, completedTasks int) error {
	progressPercentage := s.CalculateProgressPercentage(totalTasks, completedTasks)
	return s.projectPort.UpdateProjectProgress(ctx, tx, projectID, totalTasks, completedTasks, progressPercentage)
}

func (s *projectService) DeleteProject(ctx context.Context, tx *gorm.DB, userID int64, projectID int64) error {
	project, err := s.GetProjectByID(ctx, projectID)
	if err != nil {
		return err
	}
	if err := s.ValidateProjectOwnership(userID, project); err != nil {
		return err
	}

	stats, err := s.projectPort.GetProjectStats(ctx, projectID)
	if err != nil {
		return err
	}
	if stats.TotalTasks > 0 {
		return errors.New(constant.ProjectHasTasks)
	}

	return s.projectPort.SoftDeleteProject(ctx, tx, projectID)
}

func (s *projectService) GetProjectByID(ctx context.Context, projectID int64) (*entity.ProjectEntity, error) {
	project, err := s.projectPort.GetProjectByID(ctx, projectID)
	if err != nil {
		return nil, err
	}
	if project == nil {
		return nil, errors.New(constant.ProjectNotFound)
	}
	return project, nil
}

func (s *projectService) GetProjectByUserIDAndID(ctx context.Context, userID, projectID int64) (*entity.ProjectEntity, error) {
	project, err := s.projectPort.GetProjectByID(ctx, projectID)
	if err != nil {
		return nil, err
	}
	if project == nil || project.UserID != userID {
		return nil, errors.New(constant.ProjectNotFound)
	}
	return project, nil
}

func (s *projectService) GetProjectWithStats(ctx context.Context, projectID int64) (*entity.ProjectEntity, error) {
	project, err := s.GetProjectByID(ctx, projectID)
	if err != nil {
		return nil, err
	}

	stats, err := s.projectPort.GetProjectStats(ctx, projectID)
	if err != nil {
		return nil, err
	}

	if stats.TotalTasks != project.TotalTasks || stats.CompletedTasks != project.CompletedTasks {
		project.UpdateStats(
			stats.TotalTasks,
			stats.CompletedTasks,
			stats.EstimatedDurationMin,
			stats.ActualDurationMin,
		)
		err = s.projectPort.UpdateProject(ctx, nil, project)
		if err != nil {
			s.logger.Error("failed to update project stats", zap.Error(err))
		}
	}

	return project, nil
}

func (s *projectService) GetProjectsByUserID(ctx context.Context, userID int64, filter *store.ProjectFilter) ([]*entity.ProjectEntity, error) {
	return s.projectPort.GetProjectsByUserID(ctx, userID, filter)
}

func (s *projectService) CountProjectsByUserID(ctx context.Context, userID int64, filter *store.ProjectFilter) (int64, error) {
	return s.projectPort.CountProjectsByUserID(ctx, userID, filter)
}

func (s *projectService) GetProjectsWithStats(ctx context.Context, userID int64, filter *store.ProjectFilter) ([]*entity.ProjectEntity, error) {
	return s.projectPort.GetProjectsWithStats(ctx, userID, filter)
}

func (s *projectService) GetFavoriteProjects(ctx context.Context, userID int64) ([]*entity.ProjectEntity, error) {
	return s.projectPort.GetFavoriteProjects(ctx, userID)
}

func (s *projectService) GetOverdueProjects(ctx context.Context, userID int64) ([]*entity.ProjectEntity, error) {
	currentTimeMs := time.Now().UnixMilli()
	return s.projectPort.GetOverdueProjects(ctx, userID, currentTimeMs)
}
