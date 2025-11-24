package controller

import (
	"github.com/serp/ptm-task/src/core/domain/mapper"
	"github.com/serp/ptm-task/src/core/usecase"
)

type TaskController struct {
	taskUseCase usecase.ITaskUseCase
	mapper      *mapper.TaskMapper
}

func NewTaskController(taskUseCase usecase.ITaskUseCase) *TaskController {
	return &TaskController{
		taskUseCase: taskUseCase,
		mapper:      mapper.NewTaskMapper(),
	}
}
