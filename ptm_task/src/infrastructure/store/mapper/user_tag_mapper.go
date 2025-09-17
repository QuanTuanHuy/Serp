/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
)

func ToUserTagModel(userTag *entity.UserTagEntity) *model.UserTagModel {
	if userTag == nil {
		return nil
	}

	return &model.UserTagModel{
		BaseModel: model.BaseModel{
			ID: userTag.ID,
		},
		Name:         userTag.Name,
		Color:        userTag.Color,
		Weight:       userTag.Weight,
		ActiveStatus: string(userTag.ActiveStatus),
		UserID:       userTag.UserID,
	}
}

func ToUserTagEntity(userTagModel *model.UserTagModel) *entity.UserTagEntity {
	if userTagModel == nil {
		return nil
	}

	return &entity.UserTagEntity{
		BaseEntity: entity.BaseEntity{
			ID:        userTagModel.ID,
			CreatedAt: userTagModel.CreatedAt.UnixMilli(),
			UpdatedAt: userTagModel.UpdatedAt.UnixMilli(),
		},
		Name:         userTagModel.Name,
		Color:        userTagModel.Color,
		Weight:       userTagModel.Weight,
		ActiveStatus: enum.ActiveStatus(userTagModel.ActiveStatus),
		UserID:       userTagModel.UserID,
	}
}

func ToUserTagEntityList(userTagModels []*model.UserTagModel) []*entity.UserTagEntity {
	if userTagModels == nil {
		return nil
	}
	userTags := make([]*entity.UserTagEntity, len(userTagModels))
	for i, userTagModel := range userTagModels {
		userTags[i] = ToUserTagEntity(userTagModel)
	}
	return userTags
}

func ToUserTagModelList(userTags []*entity.UserTagEntity) []*model.UserTagModel {
	if userTags == nil {
		return nil
	}
	userTagModels := make([]*model.UserTagModel, len(userTags))
	for i, userTag := range userTags {
		userTagModels[i] = ToUserTagModel(userTag)
	}
	return userTagModels
}
