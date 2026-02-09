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

func (t WorkItemType) CanHaveChildren() bool {
	switch t {
	case WorkItemEpic, WorkItemStory, WorkItemTask, WorkItemBug:
		return true
	}
	return false
}

func (t WorkItemType) CanBeChild() bool {
	switch t {
	case WorkItemStory, WorkItemTask, WorkItemBug, WorkItemSubtask:
		return true
	}
	return false
}

// MaxDepth returns the maximum nesting depth for this work item type
func (t WorkItemType) MaxDepth() int {
	switch t {
	case WorkItemEpic:
		return 0
	case WorkItemStory:
		return 1
	case WorkItemTask, WorkItemBug:
		return 2
	case WorkItemSubtask:
		return 3
	}
	return -1
}

func (t WorkItemType) IsTopLevel() bool {
	return t == WorkItemEpic
}
