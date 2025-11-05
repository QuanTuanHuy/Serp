/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package constant

// HTTP Error Codes
const (
	GeneralInternalServerError = 500

	GeneralBadRequest = 400

	GeneralUnauthorized = 401

	GeneralForbidden = 403

	GeneralNotFound = 404

	GeneralSuccess = 200
)

const (
	MessageOK = "OK"
)

const (
	HttpStatusSuccess = "success"
	HttpStatusError   = "error"
)

const (
	TaskNotFound        = "Task not found"
	GetTaskForbidden    = "You don't have permission to access this task"
	UpdateTaskForbidden = "You don't have permission to update this task"
	DeleteTaskForbidden = "You don't have permission to delete this task"
)

const (
	ProjectNotFound        = "Project not found"
	GetProjectForbidden    = "You don't have permission to access this project"
	UpdateProjectForbidden = "You don't have permission to update this project"
	DeleteProjectForbidden = "You don't have permission to delete this project"
)
