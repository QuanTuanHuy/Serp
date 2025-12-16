package constant

import (
	"net/http"
)

const (
	DatabaseConnectionNil = "database connection is nil"
)

const (
	UserIDInvalid = "user ID is invalid"
)

var (
	ErrEmptyTitle      = "notification title cannot be empty"
	ErrTitleTooLong    = "notification title exceeds 255 characters"
	ErrInvalidType     = "invalid notification type"
	ErrInvalidPriority = "invalid notification priority"

	ErrNotifcationNotFound = "notification not found"
)

type BusinessErrorResponse struct {
	HTTPCode int
	Message  string
}

var BusinessErrorResponseMap = map[string]BusinessErrorResponse{

	UserIDInvalid: {HTTPCode: http.StatusBadRequest, Message: UserIDInvalid},

	DatabaseConnectionNil: {HTTPCode: http.StatusInternalServerError, Message: DatabaseConnectionNil},

	ErrEmptyTitle:      {HTTPCode: http.StatusBadRequest, Message: ErrEmptyTitle},
	ErrTitleTooLong:    {HTTPCode: http.StatusBadRequest, Message: ErrTitleTooLong},
	ErrInvalidType:     {HTTPCode: http.StatusBadRequest, Message: ErrInvalidType},
	ErrInvalidPriority: {HTTPCode: http.StatusBadRequest, Message: ErrInvalidPriority},

	ErrNotifcationNotFound: {HTTPCode: http.StatusNotFound, Message: ErrNotifcationNotFound},
}
