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

func CreateTagMapper(req *request.CreateTagDTO, userID int64) *entity.TagEntity {
	w := utils.Float64ValueWithDefault(req.Weight, 1)
	return &entity.TagEntity{
		UserID: userID,
		Name:   req.Name,
		Color:  utils.StringValueWithDefault(req.Color, "indigo"),
		Weight: utils.Float64Ptr(w),
	}
}

func UpdateTagMapper(tag *entity.TagEntity, req *request.UpdateTagDTO) *entity.TagEntity {
	tag.Name = req.Name
	tag.Color = req.Color
	tag.Weight = utils.Float64Ptr(req.Weight)
	return tag
}
