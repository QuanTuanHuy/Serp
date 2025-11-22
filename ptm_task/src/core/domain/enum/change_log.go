/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type ChangeType string

const (
	ChangeCreated       ChangeType = "created"
	ChangeUpdated       ChangeType = "updated"
	ChangeStatusChanged ChangeType = "status_changed"
	ChangeRescheduled   ChangeType = "rescheduled"
	ChangeDeleted       ChangeType = "deleted"
)

func (c ChangeType) IsValid() bool {
	switch c {
	case ChangeCreated, ChangeUpdated, ChangeStatusChanged, ChangeRescheduled, ChangeDeleted:
		return true
	}
	return false
}

type ChangeSource string

const (
	SourceUser        ChangeSource = "user"
	SourceAlgorithm   ChangeSource = "algorithm"
	SourceSystem      ChangeSource = "system"
	SourceIntegration ChangeSource = "integration"
)

func (s ChangeSource) IsValid() bool {
	switch s {
	case SourceUser, SourceAlgorithm, SourceSystem, SourceIntegration:
		return true
	}
	return false
}
