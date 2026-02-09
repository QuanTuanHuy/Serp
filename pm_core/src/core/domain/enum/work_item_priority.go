/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type WorkItemPriority string

const (
	PriorityLowest  WorkItemPriority = "LOWEST"
	PriorityLow     WorkItemPriority = "LOW"
	PriorityMedium  WorkItemPriority = "MEDIUM"
	PriorityHigh    WorkItemPriority = "HIGH"
	PriorityHighest WorkItemPriority = "HIGHEST"
)

func (p WorkItemPriority) IsValid() bool {
	switch p {
	case PriorityLowest, PriorityLow, PriorityMedium, PriorityHigh, PriorityHighest:
		return true
	}
	return false
}

func (p WorkItemPriority) GetScore() int {
	switch p {
	case PriorityLowest:
		return 1
	case PriorityLow:
		return 2
	case PriorityMedium:
		return 3
	case PriorityHigh:
		return 4
	case PriorityHighest:
		return 5
	}
	return 0
}
