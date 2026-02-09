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
