/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package constant

const (
	InvalidQueryParameters = "invalid query parameters"

	ProjectNotFound         = "project not found"
	UpdateProjectForbidden  = "you do not have permission to update this project"
	ArchiveProjectForbidden = "you do not have permission to archive this project"

	GroupTaskNotFound     = "group task not found"
	GetGroupTaskForbidden = "you do not have permission to access this group task"

	TaskNotFound        = "task not found"
	GetTaskForbidden    = "you do not have permission to access this task"
	UpdateTaskForbidden = "you do not have permission to update this task"
	DeleteTaskForbidden = "you do not have permission to delete this task"

	CreateCommentForbidden = "you do not have permission to create a comment on this task"
	GetCommentForbidden    = "you do not have permission to access comments for this task"
	CommentNotFound        = "comment not found"
	UpdateCommentForbidden = "you do not have permission to update this comment"
	DeleteCommentForbidden = "you do not have permission to delete this comment"

	NoteNotFound         = "note not found"
	DeleteNoteForbidden  = "you do not have permission to delete this note"
	LockNoteForbidden    = "you do not have permission to lock this note"
	NotePasswordMismatch = "note password does not match"
)
