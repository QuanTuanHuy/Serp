/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import (
	"errors"
	"regexp"
	"strings"

	"github.com/serp/pm-core/src/core/domain/enum"
)

var hexColorRegex = regexp.MustCompile(`^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{6})$`)

type LabelEntity struct {
	BaseEntity

	ProjectID int64  `json:"projectId"`
	Name      string `json:"name"`
	Color     string `json:"color"`

	ActiveStatus string `json:"activeStatus"`
}

func NewLabelEntity() *LabelEntity {
	return &LabelEntity{
		ActiveStatus: string(enum.Active),
	}
}

func (l *LabelEntity) Validate() error {
	if strings.TrimSpace(l.Name) == "" {
		return errors.New("label name is required")
	}
	if l.Color != "" && !hexColorRegex.MatchString(l.Color) {
		return errors.New("label color must be a valid hex color code (e.g., #FF0000)")
	}
	return nil
}
