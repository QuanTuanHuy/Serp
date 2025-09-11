/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-schedule/src/core/domain/dto/request"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	"github.com/serp/ptm-schedule/src/kernel/utils"
)

func ToScheduleGroupEntity(req *request.CreateScheduleGroupDto) *entity.ScheduleGroupEntity {
	priorityStr := utils.StringSliceFromPointers(req.Priority)
	priorityEnum := utils.ToPriorityEnum(priorityStr)

	repeatList := utils.StringSliceFromPointers(req.Repeat)

	return &entity.ScheduleGroupEntity{
		ProjectID:       req.ProjectID,
		GroupTaskID:     req.GroupTaskID,
		Title:           utils.StringValue(req.Title),
		Priority:        priorityEnum,
		Status:          enum.Status(utils.StringValueWithDefault(req.Status, string(enum.ToDo))),
		StartHour:       req.StartHour,
		StartMinute:     req.StartMinute,
		EndHour:         req.EndHour,
		EndMinute:       req.EndMinute,
		Duration:        utils.Float64Value(req.Duration),
		PreferenceLevel: utils.ConvertPriority(priorityEnum),
		Repeat:          utils.ConvertWeekday(repeatList),
		IsNotify:        utils.BoolValue(req.IsNotify),
		ActiveStatus:    enum.ActiveStatus(utils.StringValueWithDefault(req.ActiveStatus, string(enum.Active))),
		RotationDay:     req.RotationDay,
		IsFailed:        false,
	}
}
