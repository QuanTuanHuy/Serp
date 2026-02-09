/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type ProjectPriority string

const (
	ProjectPriorityLow      ProjectPriority = "LOW"
	ProjectPriorityMedium   ProjectPriority = "MEDIUM"
	ProjectPriorityHigh     ProjectPriority = "HIGH"
	ProjectPriorityCritical ProjectPriority = "CRITICAL"
)

func (p ProjectPriority) IsValid() bool {
	switch p {
	case ProjectPriorityLow, ProjectPriorityMedium, ProjectPriorityHigh, ProjectPriorityCritical:
		return true
	}
	return false
}
