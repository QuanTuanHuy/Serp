/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/kernel/utils"
)

func CreateUserTagMapper(req *request.CreateTagDTO, userID int64) *entity.UserTagEntity {
	return &entity.UserTagEntity{
		UserID: userID,
		Name:   req.Name,
		Color:  utils.StringValueWithDefault(req.Color, "indigo"),
		Weight: utils.Float64ValueWithDefault(req.Weight, 1),
	}
}

func UpdateUserTagMapper(tag *entity.UserTagEntity, req *request.UpdateTagDTO) *entity.UserTagEntity {
	tag.Name = req.Name
	tag.Color = req.Color
	tag.Weight = req.Weight
	return tag
}
