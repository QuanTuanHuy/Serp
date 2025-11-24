/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type TaskPriority string

const (
	PriorityLow    TaskPriority = "LOW"
	PriorityMedium TaskPriority = "MEDIUM"
	PriorityHigh   TaskPriority = "HIGH"
)

func (p TaskPriority) IsValid() bool {
	switch p {
	case PriorityLow, PriorityMedium, PriorityHigh:
		return true
	}
	return false
}

func (p TaskPriority) GetScore() float64 {
	switch p {
	case PriorityHigh:
		return 1.0
	case PriorityMedium:
		return 0.66
	case PriorityLow:
		return 0.33
	default:
		return 0.5
	}
}
