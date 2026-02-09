/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/pm-core/src/core/domain/enum"

type BoardColumnEntity struct {
	BaseEntity

	BoardID       int64   `json:"boardId"`
	Name          string  `json:"name"`
	Position      int     `json:"position"`
	StatusMapping *string `json:"statusMapping,omitempty"`
	WipLimit      int     `json:"wipLimit"`

	ActiveStatus string `json:"activeStatus"`
}

func NewBoardColumnEntity() *BoardColumnEntity {
	return &BoardColumnEntity{
		WipLimit:     0,
		ActiveStatus: string(enum.Active),
	}
}

func (c *BoardColumnEntity) HasWipLimit() bool {
	return c.WipLimit > 0
}

func (c *BoardColumnEntity) IsWipLimitExceeded(currentCount int) bool {
	if !c.HasWipLimit() {
		return false
	}
	return currentCount >= c.WipLimit
}

func (c *BoardColumnEntity) HasStatusMapping() bool {
	return c.StatusMapping != nil && *c.StatusMapping != ""
}
