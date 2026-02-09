/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type ActivityEntityType string

const (
	ActivityEntityProject   ActivityEntityType = "PROJECT"
	ActivityEntityWorkItem  ActivityEntityType = "WORK_ITEM"
	ActivityEntitySprint    ActivityEntityType = "SPRINT"
	ActivityEntityMilestone ActivityEntityType = "MILESTONE"
	ActivityEntityComment   ActivityEntityType = "COMMENT"
)

func (a ActivityEntityType) IsValid() bool {
	switch a {
	case ActivityEntityProject, ActivityEntityWorkItem, ActivityEntitySprint, ActivityEntityMilestone, ActivityEntityComment:
		return true
	}
	return false
}
