/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type TaskDependencyGraphEntity struct {
	BaseEntity

	UserID          int64 `json:"userId"`
	TaskID          int64 `json:"taskId"`
	DependsOnTaskID int64 `json:"dependsOnTaskId"`

	IsValid         bool    `json:"isValid"`
	ValidationError *string `json:"validationError,omitempty"`

	DependencyDepth int `json:"dependencyDepth"`
}

func NewTaskDependencyGraphEntity(userID, taskID, dependsOnTaskID int64) *TaskDependencyGraphEntity {
	return &TaskDependencyGraphEntity{
		UserID:          userID,
		TaskID:          taskID,
		DependsOnTaskID: dependsOnTaskID,
		IsValid:         true,
		DependencyDepth: 0,
	}
}

func (t *TaskDependencyGraphEntity) MarkAsInvalid(errorMsg string) {
	t.IsValid = false
	t.ValidationError = &errorMsg
}

func (t *TaskDependencyGraphEntity) MarkAsValid() {
	t.IsValid = true
	t.ValidationError = nil
}

func (t *TaskDependencyGraphEntity) SetDepth(depth int) {
	t.DependencyDepth = depth
}

type DependencyPath struct {
	TaskIDs []int64 `json:"taskIds"`
	Depth   int     `json:"depth"`
}

func NewDependencyPath(taskID int64) *DependencyPath {
	return &DependencyPath{
		TaskIDs: []int64{taskID},
		Depth:   1,
	}
}

func (p *DependencyPath) AddTask(taskID int64) {
	p.TaskIDs = append(p.TaskIDs, taskID)
	p.Depth++
}

func (p *DependencyPath) ContainsTask(taskID int64) bool {
	for _, id := range p.TaskIDs {
		if id == taskID {
			return true
		}
	}
	return false
}

func (p *DependencyPath) GetPathString() string {
	result := ""
	for i, id := range p.TaskIDs {
		if i > 0 {
			result += " -> "
		}
		result += int64ToString(id)
	}
	return result
}

func int64ToString(n int64) string {
	if n == 0 {
		return "0"
	}

	negative := n < 0
	if negative {
		n = -n
	}

	digits := ""
	for n > 0 {
		digit := n % 10
		digits = string(rune('0'+digit)) + digits
		n /= 10
	}

	if negative {
		return "-" + digits
	}
	return digits
}

type CircularDependencyError struct {
	TaskID   int64  `json:"taskId"`
	Path     string `json:"path"`
	ErrorMsg string `json:"errorMsg"`
}

func NewCircularDependencyError(taskID int64, path *DependencyPath) *CircularDependencyError {
	pathStr := path.GetPathString()
	return &CircularDependencyError{
		TaskID:   taskID,
		Path:     pathStr,
		ErrorMsg: "Circular dependency detected: " + pathStr + " -> " + int64ToString(taskID),
	}
}
