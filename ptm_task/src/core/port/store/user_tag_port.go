/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"gorm.io/gorm"
)

type IUserTagPort interface {
	CreateUserTag(ctx context.Context, tx *gorm.DB, userTag *entity.UserTagEntity) (*entity.UserTagEntity, error)
	UpdateUserTag(ctx context.Context, tx *gorm.DB, ID int64, userTag *entity.UserTagEntity) (*entity.UserTagEntity, error)
	DeleteUserTag(ctx context.Context, tx *gorm.DB, ID int64) error
	GetUserTagByID(ctx context.Context, ID int64) (*entity.UserTagEntity, error)
	GetTagByUserIDAndName(ctx context.Context, userID int64, name string) (*entity.UserTagEntity, error)
	GetUserTagsByUserID(ctx context.Context, userID int64) ([]*entity.UserTagEntity, error)
}
