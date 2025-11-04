/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/ptm-task/src/core/domain/enum"

type PriorityDimension struct {
	Key   string `json:"key"`
	Value int32  `json:"value"`
}

type TaskEntity struct {
	BaseEntity
	Title         string              `json:"title"`
	Description   string              `json:"description"`
	Priority      []enum.Priority     `json:"priority"`
	Status        enum.Status         `json:"status"`
	StartDate     *int64              `json:"startDate"`
	Deadline      *int64              `json:"deadline"`
	Duration      float64             `json:"duration"`
	ActiveStatus  enum.ActiveStatus   `json:"activeStatus"`
	GroupTaskID   int64               `json:"groupTaskId"`
	UserID        int64               `json:"userId"`
	ParentTaskID  *int64              `json:"parentTaskId"`
	Children      []*TaskEntity       `json:"children"`
	PriorityScore *float64            `json:"priorityScore"`
	PriorityDims  []PriorityDimension `json:"priorityDimensions"`
}
