/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/ptm-task/src/core/domain/enum"

type ProjectEntity struct {
	BaseEntity
	Name         string            `json:"name"`
	Description  string            `json:"description"`
	Status       enum.Status       `json:"status"`
	Color        string            `json:"color"`
	OwnerID      int64             `json:"ownerId"`
	IsDefault    bool              `json:"isDefault"`
	ActiveStatus enum.ActiveStatus `json:"activeStatus"`

	GroupTasks []*GroupTaskEntity `json:"groupTasks,omitempty"`
}
