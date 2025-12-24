package constant

import "net/http"

const (
	TaskNotFound                   = "task not found"
	TaskTitleRequired              = "task title is required"
	TaskTitleTooLong               = "task title is too long (max 500 characters)"
	InvalidTaskPriority            = "invalid task priority"
	InvalidTaskStatus              = "invalid task status"
	InvalidStatusTransition        = "invalid status transition"
	InvalidDeadline                = "deadline must be after earliest start time"
	InvalidDuration                = "estimated duration must be positive"
	InvalidRecurrencePattern       = "invalid recurrence pattern"
	InvalidQuality                 = "quality must be between 1 and 5"
	GetTaskForbidden               = "you don't have permission to access this task"
	UpdateTaskForbidden            = "you don't have permission to update this task"
	DeleteTaskForbidden            = "you don't have permission to delete this task"
	CannotCompleteTaskWithSubtasks = "cannot complete task with incomplete subtasks"
	ParentTaskDoesNotBelongToUser  = "parent task does not belong to user"
	ParentTaskNotFound             = "parent task not found"
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
	NoteNotFound                  = "note not found"
	NoteContentRequired           = "note content is required"
	NoteContentTooLong            = "note content is too long"
	NoteMustAttachToTaskOrProject = "note must attach to either task or project"
	NoteCannotAttachToBoth        = "note cannot attach to both task and project"
	NoteAttachmentTooLarge        = "attachment size exceeds limit"
	NoteTooManyAttachments        = "too many attachments"
	GetNoteForbidden              = "you don't have permission to access this note"
	UpdateNoteForbidden           = "you don't have permission to update this note"
	DeleteNoteForbidden           = "you don't have permission to delete this note"
)

const (
	ProjectNotFound                = "project not found"
	ProjectTitleRequired           = "project title is required"
	ProjectTitleTooLong            = "project title is too long (max 500 characters)"
	InvalidProjectPriority         = "invalid project priority"
	InvalidProjectStatus           = "invalid project status"
	InvalidProjectStatusTransition = "invalid project status transition"
	InvalidProjectDeadline         = "deadline must be after start date"
	InvalidProjectProgress         = "progress percentage must be between 0 and 100"
	GetProjectForbidden            = "you don't have permission to access this project"
	UpdateProjectForbidden         = "you don't have permission to update this project"
	DeleteProjectForbidden         = "you don't have permission to delete this project"
	ProjectHasTasks                = "cannot delete project with existing tasks"
	ProjectCannotComplete          = "cannot complete project with incomplete tasks"
)

const (
	ActivityEventTypeNotValid = "activity event type is not valid"
	ActivityEventNotFound     = "activity event not found"
)

const (
	UserIDInvalid = "user ID is invalid"
)

type BusinessErrorResponse struct {
	HTTPCode int
	Message  string
}

var BusinessErrorResponseMap = map[string]BusinessErrorResponse{

	TaskNotFound:                   {HTTPCode: http.StatusNotFound, Message: TaskNotFound},
	TaskTitleRequired:              {HTTPCode: http.StatusBadRequest, Message: TaskTitleRequired},
	TaskTitleTooLong:               {HTTPCode: http.StatusBadRequest, Message: TaskTitleTooLong},
	InvalidTaskPriority:            {HTTPCode: http.StatusBadRequest, Message: InvalidTaskPriority},
	InvalidTaskStatus:              {HTTPCode: http.StatusBadRequest, Message: InvalidTaskStatus},
	InvalidStatusTransition:        {HTTPCode: http.StatusBadRequest, Message: InvalidStatusTransition},
	InvalidDeadline:                {HTTPCode: http.StatusBadRequest, Message: InvalidDeadline},
	InvalidDuration:                {HTTPCode: http.StatusBadRequest, Message: InvalidDuration},
	InvalidRecurrencePattern:       {HTTPCode: http.StatusBadRequest, Message: InvalidRecurrencePattern},
	InvalidQuality:                 {HTTPCode: http.StatusBadRequest, Message: InvalidQuality},
	GetTaskForbidden:               {HTTPCode: http.StatusForbidden, Message: GetTaskForbidden},
	UpdateTaskForbidden:            {HTTPCode: http.StatusForbidden, Message: UpdateTaskForbidden},
	DeleteTaskForbidden:            {HTTPCode: http.StatusForbidden, Message: DeleteTaskForbidden},
	CannotCompleteTaskWithSubtasks: {HTTPCode: http.StatusBadRequest, Message: CannotCompleteTaskWithSubtasks},
	ParentTaskDoesNotBelongToUser:  {HTTPCode: http.StatusBadRequest, Message: ParentTaskDoesNotBelongToUser},
	ParentTaskNotFound:             {HTTPCode: http.StatusNotFound, Message: ParentTaskNotFound},

	TemplateNotFound:            {HTTPCode: http.StatusNotFound, Message: TemplateNotFound},
	TemplateNameRequired:        {HTTPCode: http.StatusBadRequest, Message: TemplateNameRequired},
	TemplateNameTooLong:         {HTTPCode: http.StatusBadRequest, Message: TemplateNameTooLong},
	TemplateTitleRequired:       {HTTPCode: http.StatusBadRequest, Message: TemplateTitleRequired},
	InvalidTemplatePriority:     {HTTPCode: http.StatusBadRequest, Message: InvalidTemplatePriority},
	GetTemplateForbidden:        {HTTPCode: http.StatusForbidden, Message: GetTemplateForbidden},
	UpdateTemplateForbidden:     {HTTPCode: http.StatusForbidden, Message: UpdateTemplateForbidden},
	DeleteTemplateForbidden:     {HTTPCode: http.StatusForbidden, Message: DeleteTemplateForbidden},
	TemplateDoesNotBelongToUser: {HTTPCode: http.StatusForbidden, Message: TemplateDoesNotBelongToUser},

	DependencyTaskNotFound:        {HTTPCode: http.StatusNotFound, Message: DependencyTaskNotFound},
	DependencyCannotSelfDepend:    {HTTPCode: http.StatusBadRequest, Message: DependencyCannotSelfDepend},
	DependencyDifferentUsers:      {HTTPCode: http.StatusBadRequest, Message: DependencyDifferentUsers},
	DependencyOnCompletedTask:     {HTTPCode: http.StatusBadRequest, Message: DependencyOnCompletedTask},
	DependencyCircularDetected:    {HTTPCode: http.StatusBadRequest, Message: DependencyCircularDetected},
	DependencyTaskNotBelongToUser: {HTTPCode: http.StatusForbidden, Message: DependencyTaskNotBelongToUser},

	CompletionTaskIDRequired:  {HTTPCode: http.StatusBadRequest, Message: CompletionTaskIDRequired},
	CompletionUserIDRequired:  {HTTPCode: http.StatusBadRequest, Message: CompletionUserIDRequired},
	CompletionInvalidDuration: {HTTPCode: http.StatusBadRequest, Message: CompletionInvalidDuration},
	CompletionInvalidQuality:  {HTTPCode: http.StatusBadRequest, Message: CompletionInvalidQuality},

	DatabaseConnectionNil: {HTTPCode: http.StatusInternalServerError, Message: DatabaseConnectionNil},

	NoteNotFound:                  {HTTPCode: http.StatusNotFound, Message: NoteNotFound},
	NoteContentRequired:           {HTTPCode: http.StatusBadRequest, Message: NoteContentRequired},
	NoteContentTooLong:            {HTTPCode: http.StatusBadRequest, Message: NoteContentTooLong},
	NoteMustAttachToTaskOrProject: {HTTPCode: http.StatusBadRequest, Message: NoteMustAttachToTaskOrProject},
	NoteCannotAttachToBoth:        {HTTPCode: http.StatusBadRequest, Message: NoteCannotAttachToBoth},
	NoteAttachmentTooLarge:        {HTTPCode: http.StatusBadRequest, Message: NoteAttachmentTooLarge},
	NoteTooManyAttachments:        {HTTPCode: http.StatusBadRequest, Message: NoteTooManyAttachments},
	GetNoteForbidden:              {HTTPCode: http.StatusForbidden, Message: GetNoteForbidden},
	UpdateNoteForbidden:           {HTTPCode: http.StatusForbidden, Message: UpdateNoteForbidden},
	DeleteNoteForbidden:           {HTTPCode: http.StatusForbidden, Message: DeleteNoteForbidden},

	ProjectNotFound:                {HTTPCode: http.StatusNotFound, Message: ProjectNotFound},
	ProjectTitleRequired:           {HTTPCode: http.StatusBadRequest, Message: ProjectTitleRequired},
	ProjectTitleTooLong:            {HTTPCode: http.StatusBadRequest, Message: ProjectTitleTooLong},
	InvalidProjectPriority:         {HTTPCode: http.StatusBadRequest, Message: InvalidProjectPriority},
	InvalidProjectStatus:           {HTTPCode: http.StatusBadRequest, Message: InvalidProjectStatus},
	InvalidProjectStatusTransition: {HTTPCode: http.StatusBadRequest, Message: InvalidProjectStatusTransition},
	InvalidProjectDeadline:         {HTTPCode: http.StatusBadRequest, Message: InvalidProjectDeadline},
	InvalidProjectProgress:         {HTTPCode: http.StatusBadRequest, Message: InvalidProjectProgress},
	GetProjectForbidden:            {HTTPCode: http.StatusForbidden, Message: GetProjectForbidden},
	UpdateProjectForbidden:         {HTTPCode: http.StatusForbidden, Message: UpdateProjectForbidden},
	DeleteProjectForbidden:         {HTTPCode: http.StatusForbidden, Message: DeleteProjectForbidden},
	ProjectHasTasks:                {HTTPCode: http.StatusBadRequest, Message: ProjectHasTasks},
	ProjectCannotComplete:          {HTTPCode: http.StatusBadRequest, Message: ProjectCannotComplete},

	ActivityEventTypeNotValid: {HTTPCode: http.StatusBadRequest, Message: ActivityEventTypeNotValid},
	ActivityEventNotFound:     {HTTPCode: http.StatusNotFound, Message: ActivityEventNotFound},

	UserIDInvalid: {HTTPCode: http.StatusBadRequest, Message: UserIDInvalid},
}
