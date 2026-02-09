/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"fmt"

	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/core/port/store"
	"github.com/serp/pm-core/src/infrastructure/store/mapper"
	"github.com/serp/pm-core/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type ProjectMemberAdapter struct {
	db     *gorm.DB
	mapper *mapper.ProjectMemberMapper
}

func NewProjectMemberAdapter(db *gorm.DB) store.IProjectMemberPort {
	return &ProjectMemberAdapter{
		db:     db,
		mapper: mapper.NewProjectMemberMapper(),
	}
}

func (a *ProjectMemberAdapter) CreateMember(ctx context.Context, tx *gorm.DB, member *entity.ProjectMemberEntity) (*entity.ProjectMemberEntity, error) {
	db := a.getDB(tx)
	memberModel := a.mapper.ToModel(member)
	if err := db.WithContext(ctx).Create(memberModel).Error; err != nil {
		return nil, fmt.Errorf("failed to create project member: %w", err)
	}
	return a.mapper.ToEntity(memberModel), nil
}

func (a *ProjectMemberAdapter) GetMemberByProjectAndUser(ctx context.Context, projectID, userID int64) (*entity.ProjectMemberEntity, error) {
	var memberModel model.ProjectMemberModel

	if err := a.db.WithContext(ctx).
		Where("project_id = ? AND user_id = ?", projectID, userID).
		First(&memberModel).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get project member: %w", err)
	}
	return a.mapper.ToEntity(&memberModel), nil
}

func (a *ProjectMemberAdapter) GetMembersByProjectID(ctx context.Context, projectID int64) ([]*entity.ProjectMemberEntity, error) {
	var memberModels []*model.ProjectMemberModel

	if err := a.db.WithContext(ctx).
		Where("project_id = ?", projectID).
		Find(&memberModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get project members: %w", err)
	}
	return a.mapper.ToEntities(memberModels), nil
}

func (a *ProjectMemberAdapter) DeleteMember(ctx context.Context, tx *gorm.DB, memberID int64) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Delete(&model.ProjectMemberModel{}, memberID).Error; err != nil {
		return fmt.Errorf("failed to delete project member: %w", err)
	}
	return nil
}

func (a *ProjectMemberAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}
