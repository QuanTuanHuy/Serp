/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/core/domain/constant"
	service "github.com/serp/api-gateway/src/core/service/ptm"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type ScheduleTaskController struct {
	scheduleTaskService service.IScheduleTaskService
}

func NewScheduleTaskController(scheduleTaskService service.IScheduleTaskService) *ScheduleTaskController {
	return &ScheduleTaskController{
		scheduleTaskService: scheduleTaskService,
	}
}

func (s *ScheduleTaskController) GetTasksByPlanID(c *gin.Context) {
	params := map[string]string{}
	queryParams := c.Request.URL.Query()
	for key, values := range queryParams {
		if len(values) > 0 {
			params[key] = values[0]
		}
	}
	res, err := s.scheduleTaskService.GetScheduleTasksByPlanID(c.Request.Context(), params)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}
