package constant

import "net/http"

// Project errors
const (
	ProjectNotFound         = "project not found"
	ProjectNameRequired     = "project name is required"
	ProjectNameTooLong      = "project name is too long (max 200 characters)"
	ProjectKeyRequired      = "project key is required"
	ProjectKeyTooLong       = "project key is too long (max 10 characters)"
	ProjectKeyAlreadyExists = "project key already exists"
	InvalidProjectStatus    = "invalid project status"
	GetProjectForbidden     = "you don't have permission to access this project"
	UpdateProjectForbidden  = "you don't have permission to update this project"
	DeleteProjectForbidden  = "you don't have permission to delete this project"
)

// Project Member errors
const (
	MemberAlreadyExists     = "member already exists in this project"
	MemberNotFound          = "project member not found"
	InsufficientPermissions = "insufficient permissions for this action"
	CannotRemoveOwner       = "cannot remove the project owner"
)

// Work Item errors
const (
	WorkItemNotFound        = "work item not found"
	WorkItemTitleRequired   = "work item title is required"
	InvalidWorkItemType     = "invalid work item type"
	InvalidWorkItemStatus   = "invalid work item status"
	InvalidWorkItemPriority = "invalid work item priority"
	WorkItemAccessForbidden = "you don't have permission to access this work item"
)

// Sprint errors
const (
	SprintNotFound      = "sprint not found"
	SprintNameRequired  = "sprint name is required"
	InvalidSprintStatus = "invalid sprint status"
	SprintAlreadyActive = "there is already an active sprint in this project"
)

// Milestone errors
const (
	MilestoneNotFound     = "milestone not found"
	MilestoneNameRequired = "milestone name is required"
)

// Board errors
const (
	BoardNotFound     = "board not found"
	BoardNameRequired = "board name is required"
)

// Comment errors
const (
	CommentNotFound        = "comment not found"
	CommentContentRequired = "comment content is required"
)

// Label errors
const (
	LabelNotFound      = "label not found"
	LabelNameRequired  = "label name is required"
	LabelAlreadyExists = "label already exists in this project"
)

// Dependency errors
const (
	DependencyNotFound         = "dependency not found"
	DependencyCannotSelfDepend = "work item cannot depend on itself"
	DependencyCircularDetected = "circular dependency detected"
)

// General errors
const (
	DatabaseConnectionNil = "database connection is nil"
	UserIDInvalid         = "user ID is invalid"
)

type BusinessErrorResponse struct {
	HTTPCode int
	Message  string
}

var BusinessErrorResponseMap = map[string]BusinessErrorResponse{
	// Project errors
	ProjectNotFound:         {HTTPCode: http.StatusNotFound, Message: ProjectNotFound},
	ProjectNameRequired:     {HTTPCode: http.StatusBadRequest, Message: ProjectNameRequired},
	ProjectNameTooLong:      {HTTPCode: http.StatusBadRequest, Message: ProjectNameTooLong},
	ProjectKeyRequired:      {HTTPCode: http.StatusBadRequest, Message: ProjectKeyRequired},
	ProjectKeyTooLong:       {HTTPCode: http.StatusBadRequest, Message: ProjectKeyTooLong},
	ProjectKeyAlreadyExists: {HTTPCode: http.StatusConflict, Message: ProjectKeyAlreadyExists},
	InvalidProjectStatus:    {HTTPCode: http.StatusBadRequest, Message: InvalidProjectStatus},
	GetProjectForbidden:     {HTTPCode: http.StatusForbidden, Message: GetProjectForbidden},
	UpdateProjectForbidden:  {HTTPCode: http.StatusForbidden, Message: UpdateProjectForbidden},
	DeleteProjectForbidden:  {HTTPCode: http.StatusForbidden, Message: DeleteProjectForbidden},

	// Member errors
	MemberAlreadyExists:     {HTTPCode: http.StatusConflict, Message: MemberAlreadyExists},
	MemberNotFound:          {HTTPCode: http.StatusNotFound, Message: MemberNotFound},
	InsufficientPermissions: {HTTPCode: http.StatusForbidden, Message: InsufficientPermissions},
	CannotRemoveOwner:       {HTTPCode: http.StatusBadRequest, Message: CannotRemoveOwner},

	// Work Item errors
	WorkItemNotFound:        {HTTPCode: http.StatusNotFound, Message: WorkItemNotFound},
	WorkItemTitleRequired:   {HTTPCode: http.StatusBadRequest, Message: WorkItemTitleRequired},
	InvalidWorkItemType:     {HTTPCode: http.StatusBadRequest, Message: InvalidWorkItemType},
	InvalidWorkItemStatus:   {HTTPCode: http.StatusBadRequest, Message: InvalidWorkItemStatus},
	InvalidWorkItemPriority: {HTTPCode: http.StatusBadRequest, Message: InvalidWorkItemPriority},
	WorkItemAccessForbidden: {HTTPCode: http.StatusForbidden, Message: WorkItemAccessForbidden},

	// Sprint errors
	SprintNotFound:      {HTTPCode: http.StatusNotFound, Message: SprintNotFound},
	SprintNameRequired:  {HTTPCode: http.StatusBadRequest, Message: SprintNameRequired},
	InvalidSprintStatus: {HTTPCode: http.StatusBadRequest, Message: InvalidSprintStatus},
	SprintAlreadyActive: {HTTPCode: http.StatusBadRequest, Message: SprintAlreadyActive},

	// Milestone errors
	MilestoneNotFound:     {HTTPCode: http.StatusNotFound, Message: MilestoneNotFound},
	MilestoneNameRequired: {HTTPCode: http.StatusBadRequest, Message: MilestoneNameRequired},

	// Board errors
	BoardNotFound:     {HTTPCode: http.StatusNotFound, Message: BoardNotFound},
	BoardNameRequired: {HTTPCode: http.StatusBadRequest, Message: BoardNameRequired},

	// Comment errors
	CommentNotFound:        {HTTPCode: http.StatusNotFound, Message: CommentNotFound},
	CommentContentRequired: {HTTPCode: http.StatusBadRequest, Message: CommentContentRequired},

	// Label errors
	LabelNotFound:      {HTTPCode: http.StatusNotFound, Message: LabelNotFound},
	LabelNameRequired:  {HTTPCode: http.StatusBadRequest, Message: LabelNameRequired},
	LabelAlreadyExists: {HTTPCode: http.StatusConflict, Message: LabelAlreadyExists},

	// Dependency errors
	DependencyNotFound:         {HTTPCode: http.StatusNotFound, Message: DependencyNotFound},
	DependencyCannotSelfDepend: {HTTPCode: http.StatusBadRequest, Message: DependencyCannotSelfDepend},
	DependencyCircularDetected: {HTTPCode: http.StatusBadRequest, Message: DependencyCircularDetected},

	// General errors
	DatabaseConnectionNil: {HTTPCode: http.StatusInternalServerError, Message: DatabaseConnectionNil},
	UserIDInvalid:         {HTTPCode: http.StatusBadRequest, Message: UserIDInvalid},
}
