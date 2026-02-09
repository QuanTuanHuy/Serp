/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/pm-core/src/core/domain/enum"

type BoardEntity struct {
	BaseEntity

	ProjectID int64  `json:"projectId"`
	Name      string `json:"name"`
	Type      string `json:"type"`
	IsDefault bool   `json:"isDefault"`

	ActiveStatus string `json:"activeStatus"`

	Columns []*BoardColumnEntity `json:"columns,omitempty"`
}

func NewBoardEntity() *BoardEntity {
	return &BoardEntity{
		Type:         string(enum.BoardKanban),
		IsDefault:    false,
		ActiveStatus: string(enum.Active),
	}
}

func (b *BoardEntity) IsKanban() bool {
	return enum.BoardType(b.Type) == enum.BoardKanban
}

func (b *BoardEntity) IsScrum() bool {
	return enum.BoardType(b.Type) == enum.BoardScrum
}

func (b *BoardEntity) GetColumnCount() int {
	return len(b.Columns)
}
