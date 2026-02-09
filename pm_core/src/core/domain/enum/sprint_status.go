/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type SprintStatus string

const (
	SprintPlanning  SprintStatus = "PLANNING"
	SprintActive    SprintStatus = "ACTIVE"
	SprintCompleted SprintStatus = "COMPLETED"
	SprintCancelled SprintStatus = "CANCELLED"
)

func (s SprintStatus) IsValid() bool {
	switch s {
	case SprintPlanning, SprintActive, SprintCompleted, SprintCancelled:
		return true
	}
	return false
}
