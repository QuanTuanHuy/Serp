package constant

import "net/http"

const (
	DatabaseConnectionNil = "database connection is nil"
)

const (
	UserIDInvalid = "user ID is invalid"
)

type BusinessErrorResponse struct {
	HTTPCode int
	Message  string
}

var BusinessErrorResponseMap = map[string]BusinessErrorResponse{

	UserIDInvalid: {HTTPCode: http.StatusBadRequest, Message: UserIDInvalid},
}
