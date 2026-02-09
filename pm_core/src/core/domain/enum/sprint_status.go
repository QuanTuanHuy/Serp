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

var validSprintTransitions = map[SprintStatus][]SprintStatus{
	SprintPlanning:  {SprintActive, SprintCancelled},
	SprintActive:    {SprintCompleted},
	SprintCompleted: {},
	SprintCancelled: {},
}

func (s SprintStatus) CanTransitionTo(target SprintStatus) bool {
	allowed, ok := validSprintTransitions[s]
	if !ok {
		return false
	}
	for _, a := range allowed {
		if a == target {
			return true
		}
	}
	return false
}

func (s SprintStatus) IsTerminal() bool {
	return s == SprintCompleted || s == SprintCancelled
}

func (s SprintStatus) IsActive() bool {
	return s == SprintActive
}

func (s SprintStatus) IsPlanning() bool {
	return s == SprintPlanning
}

func (s SprintStatus) IsCompleted() bool {
	return s == SprintCompleted
}

func (s SprintStatus) IsCancelled() bool {
	return s == SprintCancelled
}
