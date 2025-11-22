package entity

import "github.com/serp/ptm-task/src/core/domain/enum"

type TaggedItemEntity struct {
	BaseEntity
	TagID        int64             `json:"tagId"`
	ResourceType enum.ResourceType `json:"resourceType"`
	ResourceID   int64             `json:"resourceId"`
}
