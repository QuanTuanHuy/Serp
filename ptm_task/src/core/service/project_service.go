/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"
	"fmt"
	"math"
	"sort"
	"strings"

	"github.com/agnivade/levenshtein"
	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/dto/response"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/core/domain/mapper"
	port2 "github.com/serp/ptm-task/src/core/port/client"
	port "github.com/serp/ptm-task/src/core/port/store"
)

type IProjectService interface {
	CreateProject(ctx context.Context, userID int64, request *request.CreateProjectDTO) (*entity.ProjectEntity, error)
	UpdateProject(ctx context.Context, userID, projectID int64, request *request.UpdateProjectDTO) (*entity.ProjectEntity, error)
	GetProjectByID(ctx context.Context, ID int64) (*entity.ProjectEntity, error)
	GetProjects(ctx context.Context, params *request.GetProjectParams) ([]*entity.ProjectEntity, int64, error)
	GetProjectsByName(ctx context.Context, userID int64, searchName string, maxDistance int, limit int) ([]*entity.ProjectEntity, error)
	GetProjectByName(ctx context.Context, userID int64, searchName string) (*entity.ProjectEntity, error)
	GetProjectsByUserID(ctx context.Context, userID int64) ([]*entity.ProjectEntity, error)
	ArchiveProject(ctx context.Context, userID, projectID int64) error
}

type ProjectService struct {
	projectPort   port.IProjectPort
	groupTaskPort port.IGroupTaskPort
	redisPort     port2.IRedisPort
	dbTx          port.IDBTransactionPort
}

func (p *ProjectService) GetProjectsByUserID(ctx context.Context, userID int64) ([]*entity.ProjectEntity, error) {
	var projects []*entity.ProjectEntity
	var err error
	cacheKey := fmt.Sprintf(constant.ProjectsByUserID, userID)

	if err = p.redisPort.GetFromRedis(ctx, cacheKey, &projects); err != nil {
		log.Info(ctx, "Cache miss for projects by user ID ", userID, " error: ")
	}
	if projects != nil {
		return projects, nil
	}

	projects, err = p.projectPort.GetProjectsByUserID(ctx, userID)
	if err != nil {
		log.Error(ctx, "Failed to get projects by user ID ", userID, " error: ", err)
		return nil, err
	}

	go func() {
		if len(projects) > 0 {
			err = p.redisPort.SetToRedis(ctx, cacheKey, projects, constant.DefaultTTL)
			if err != nil {
				log.Error(ctx, "Failed to set projects to Redis cache for user ID ", userID, " error: ", err)
			}
		}
	}()
	return projects, nil
}

func (p *ProjectService) GetProjectsByName(ctx context.Context, userID int64, searchName string, maxDistance int, limit int) ([]*entity.ProjectEntity, error) {
	projects, err := p.projectPort.GetProjectsByUserID(ctx, userID)
	if err != nil {
		log.Error(ctx, "Failed to get projects by user ID ", userID, " error: ", err)
		return nil, err
	}
	if len(projects) == 0 {
		return nil, nil
	}

	var matchProject []*response.ProjectMatchDTO
	searchNameLower := strings.ToLower(searchName)

	for _, project := range projects {
		projectNameLower := strings.ToLower(project.Name)

		if strings.Contains(projectNameLower, searchNameLower) {
			matchProject = append(matchProject, &response.ProjectMatchDTO{
				Project:  project,
				Distance: 0,
			})
			continue
		}

		fullDistance := levenshtein.ComputeDistance(searchNameLower, projectNameLower)
		if fullDistance <= maxDistance {
			matchProject = append(matchProject, &response.ProjectMatchDTO{
				Project:  project,
				Distance: fullDistance,
			})
			continue
		}

		words := strings.Fields(projectNameLower)
		for _, word := range words {
			wordDistance := levenshtein.ComputeDistance(searchNameLower, word)
			if wordDistance <= maxDistance {
				matchProject = append(matchProject, &response.ProjectMatchDTO{
					Project:  project,
					Distance: wordDistance,
				})
				break
			}
		}
	}
	sort.Slice(matchProject, func(i, j int) bool {
		return matchProject[i].Distance < matchProject[j].Distance
	})

	result := make([]*entity.ProjectEntity, 0, limit)
	for i, match := range matchProject {
		if i >= limit {
			break
		}
		result = append(result, match.Project)
	}
	return result, nil
}

func (p *ProjectService) GetProjectByName(ctx context.Context, userID int64, searchName string) (*entity.ProjectEntity, error) {
	projects, err := p.GetProjectsByName(ctx, userID, searchName, math.MaxInt, 1)
	if err != nil {
		return nil, err
	}
	if len(projects) == 0 {
		return nil, nil
	}
	return projects[0], nil
}

func (p *ProjectService) GetProjects(ctx context.Context, params *request.GetProjectParams) ([]*entity.ProjectEntity, int64, error) {
	projects, count, err := p.projectPort.GetProjects(ctx, params)
	if err != nil {
		log.Error(ctx, "Failed to get projects: ", err)
		return nil, 0, err
	}
	return projects, count, nil
}

func (p *ProjectService) GetProjectByID(ctx context.Context, ID int64) (*entity.ProjectEntity, error) {
	project, err := p.projectPort.GetProjectByID(ctx, ID)
	if err != nil {
		log.Error(ctx, "Failed to get project by ID ", ID)
		return nil, err
	}
	if project == nil {
		return nil, errors.New(constant.ProjectNotFound)
	}
	return project, nil
}

func (p *ProjectService) CreateProject(ctx context.Context, userID int64, request *request.CreateProjectDTO) (*entity.ProjectEntity, error) {
	var err error
	hasDefaultProject, err := p.CheckDefaultProject(ctx, userID)
	if err != nil {
		return nil, err
	}
	if !hasDefaultProject {
		request.IsDefault = true
	}

	project := mapper.CreateProjectMapper(request, userID)

	tx := p.dbTx.StartTransaction()
	defer func() {
		if r := recover(); r != nil {
			log.Error(ctx, "Panic occurred while creating project ", "error ", r)
			p.dbTx.Rollback(tx)
		}
		if err != nil {
			log.Error(ctx, "Failed to create project ", "error ", err)
			p.dbTx.Rollback(tx)
			return
		}
	}()

	project, err = p.projectPort.CreateProject(ctx, tx, project)
	if err != nil {
		log.Error(ctx, "Failed to create project ", "error ", err)
		return nil, err
	}
	err = p.dbTx.Commit(tx)
	if err != nil {
		log.Error(ctx, "Failed to commit transaction for project creation ", "error ", err)
		return nil, err
	}

	go func() {
		err = p.redisPort.DeleteKeyFromRedis(ctx, fmt.Sprintf(constant.ProjectsByUserID, userID))
		if err != nil {
			log.Error(ctx, "Failed to delete projects cache when create new project for user ID ", userID, " error: ", err)
		}
	}()

	return project, nil
}

func (p *ProjectService) UpdateProject(ctx context.Context, userID, projectID int64, request *request.UpdateProjectDTO) (*entity.ProjectEntity, error) {
	var err error
	project, err := p.projectPort.GetProjectByID(ctx, projectID)
	if err != nil {
		return nil, err
	}
	if project == nil {
		return nil, errors.New(constant.ProjectNotFound)
	}
	if project.OwnerID != userID {
		return nil, errors.New(constant.UpdateProjectForbidden)
	}

	project = request.ToProjectEntity(project)
	project, err = p.updateProject(ctx, projectID, project)
	if err != nil {
		log.Error(ctx, "Failed to update project ", projectID, " error: ", err)
		return nil, err
	}
	go func() {
		err = p.redisPort.DeleteKeyFromRedis(ctx, fmt.Sprintf(constant.ProjectsByUserID, userID))
		if err != nil {
			log.Error(ctx, "Failed to delete projects cache when update project for user ID ", userID, " error: ", err)
		}
	}()
	return project, nil
}

func (p *ProjectService) CheckDefaultProject(ctx context.Context, userID int64) (bool, error) {
	project, err := p.projectPort.GetDefaultProjectByUserID(ctx, userID)
	if err != nil {
		log.Error(ctx, "Failed to get default project by user ID", "userID", userID, "error", err)
		return false, err
	}
	return project != nil, nil
}

func (p *ProjectService) ArchiveProject(ctx context.Context, userID int64, projectID int64) error {
	var err error
	project, err := p.projectPort.GetProjectByID(ctx, projectID)
	if err != nil {
		log.Error(ctx, "Failed to get project by ID ", projectID, " error: ", err)
		return err
	}
	if project == nil {
		return errors.New(constant.ProjectNotFound)
	}
	if project.OwnerID != userID {
		return errors.New(constant.ArchiveProjectForbidden)
	}
	if project.ActiveStatus == enum.Inactive || project.Status == enum.Archived {
		return nil
	}

	project.ActiveStatus = enum.Inactive
	project.Status = enum.Archived

	_, err = p.updateProject(ctx, projectID, project)
	if err != nil {
		log.Error(ctx, "Failed to archive project ", projectID, " error: ", err)
		return err
	}

	go func() {
		err = p.redisPort.DeleteKeyFromRedis(ctx, fmt.Sprintf(constant.ProjectsByUserID, userID))
		if err != nil {
			log.Error(ctx, "Failed to delete projects cache when archive project for user ID ", userID, " error: ", err)
		}
		err = p.redisPort.DeleteKeyFromRedis(ctx, fmt.Sprintf(constant.GroupTasksByProjectID, projectID))
		if err != nil {
			log.Error(ctx, "Failed to delete project cache by ID ", projectID, " error: ", err)
		}
	}()

	return nil
}

func (p *ProjectService) updateProject(ctx context.Context, projectID int64, project *entity.ProjectEntity) (*entity.ProjectEntity, error) {
	var err error
	tx := p.dbTx.StartTransaction()
	defer func() {
		if r := recover(); r != nil {
			log.Error(ctx, "Panic occurred while updating project ", "error ", r)
			p.dbTx.Rollback(tx)
		}
		if err != nil {
			log.Error(ctx, "Failed to update project ", "error ", err)
			p.dbTx.Rollback(tx)
		}
	}()

	project, err = p.projectPort.UpdateProject(ctx, tx, projectID, project)
	if err != nil {
		log.Error(ctx, "Failed to update project ", "error ", err)
		return nil, err
	}
	err = p.dbTx.Commit(tx)
	if err != nil {
		log.Error(ctx, "Failed to commit transaction for project update ", "error ", err)
		return nil, err
	}
	return project, nil
}

func NewProjectService(
	projectPort port.IProjectPort,
	groupTaskPort port.IGroupTaskPort,
	redisPort port2.IRedisPort,
	dbTx port.IDBTransactionPort) IProjectService {
	return &ProjectService{
		projectPort:   projectPort,
		groupTaskPort: groupTaskPort,
		redisPort:     redisPort,
		dbTx:          dbTx,
	}
}
