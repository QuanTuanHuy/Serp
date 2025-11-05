package constant

const (
	TaskNotFound             = "task not found"
	TaskTitleRequired        = "task title is required"
	TaskTitleTooLong         = "task title is too long (max 500 characters)"
	InvalidTaskPriority      = "invalid task priority"
	InvalidTaskStatus        = "invalid task status"
	InvalidStatusTransition  = "invalid status transition"
	InvalidDeadline          = "deadline must be after earliest start time"
	InvalidDuration          = "estimated duration must be positive"
	InvalidRecurrencePattern = "invalid recurrence pattern"
	InvalidQuality           = "quality must be between 1 and 5"
	GetTaskForbidden         = "you don't have permission to access this task"
	UpdateTaskForbidden      = "you don't have permission to update this task"
	DeleteTaskForbidden      = "you don't have permission to delete this task"
)

const (
	TemplateNotFound            = "template not found"
	TemplateNameRequired        = "template name is required"
	TemplateNameTooLong         = "template name is too long (max 200 characters)"
	TemplateTitleRequired       = "title template is required"
	InvalidTemplatePriority     = "invalid priority"
	InvalidTemplateDuration     = "estimated duration must be positive"
	GetTemplateForbidden        = "you don't have permission to access this template"
	UpdateTemplateForbidden     = "you don't have permission to update this template"
	DeleteTemplateForbidden     = "you don't have permission to delete this template"
	TemplateDoesNotBelongToUser = "template does not belong to user"
)

const (
	DependencyTaskNotFound        = "dependency task not found"
	DependencyCannotSelfDepend    = "task cannot depend on itself"
	DependencyDifferentUsers      = "tasks must belong to same user"
	DependencyOnCompletedTask     = "cannot depend on completed task"
	DependencyCircularDetected    = "circular dependency detected"
	DependencyTaskNotBelongToUser = "task does not belong to user"
)

const (
	CompletionTaskIDRequired  = "task ID is required"
	CompletionUserIDRequired  = "user ID is required"
	CompletionInvalidDuration = "actual duration must be positive"
	CompletionInvalidQuality  = "completion quality must be between 1 and 5"
)

const (
	DatabaseConnectionNil = "database connection is nil"
)

const (
	ProjectNotFound        = "project not found"
	GetProjectForbidden    = "you don't have permission to access this project"
	UpdateProjectForbidden = "you don't have permission to update this project"
	DeleteProjectForbidden = "you don't have permission to delete this project"
)
