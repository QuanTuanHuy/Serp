/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import (
	"errors"

	"github.com/serp/pm-core/src/core/domain/enum"
)

var (
	errSelfDependency        = errors.New("work item cannot depend on itself")
	errInvalidDependencyType = errors.New("invalid dependency type")
)

type WorkItemDependencyEntity struct {
	BaseEntity

	WorkItemID      int64  `json:"workItemId"`
	DependsOnItemID int64  `json:"dependsOnItemId"`
	DependencyType  string `json:"dependencyType"`
}

func (d *WorkItemDependencyEntity) IsSelfDependency() bool {
	return d.WorkItemID == d.DependsOnItemID
}

func (d *WorkItemDependencyEntity) Validate() error {
	if d.IsSelfDependency() {
		return errSelfDependency
	}
	if !enum.DependencyType(d.DependencyType).IsValid() {
		return errInvalidDependencyType
	}
	return nil
}

func (d *WorkItemDependencyEntity) GetInverse() *WorkItemDependencyEntity {
	inverseType := enum.DependencyType(d.DependencyType).GetInverseType()
	return &WorkItemDependencyEntity{
		WorkItemID:      d.DependsOnItemID,
		DependsOnItemID: d.WorkItemID,
		DependencyType:  string(inverseType),
	}
}

func (d *WorkItemDependencyEntity) IsBlocking() bool {
	return enum.DependencyType(d.DependencyType) == enum.DependencyBlocks
}
