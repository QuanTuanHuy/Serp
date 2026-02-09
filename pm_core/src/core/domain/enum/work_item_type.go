/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type WorkItemType string

const (
	WorkItemEpic    WorkItemType = "EPIC"
	WorkItemStory   WorkItemType = "STORY"
	WorkItemTask    WorkItemType = "TASK"
	WorkItemBug     WorkItemType = "BUG"
	WorkItemSubtask WorkItemType = "SUBTASK"
)

func (t WorkItemType) IsValid() bool {
	switch t {
	case WorkItemEpic, WorkItemStory, WorkItemTask, WorkItemBug, WorkItemSubtask:
		return true
	}
	return false
}
