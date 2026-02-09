/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package store

import (
	"context"

	"github.com/serp/pm-core/src/core/domain/entity"
	"gorm.io/gorm"
)

type IProjectMemberPort interface {
	CreateMember(ctx context.Context, tx *gorm.DB, member *entity.ProjectMemberEntity) (*entity.ProjectMemberEntity, error)
	GetMemberByProjectAndUser(ctx context.Context, projectID, userID int64) (*entity.ProjectMemberEntity, error)
	GetMembersByProjectID(ctx context.Context, projectID int64) ([]*entity.ProjectMemberEntity, error)
	DeleteMember(ctx context.Context, tx *gorm.DB, memberID int64) error
}
