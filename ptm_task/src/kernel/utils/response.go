package utils

import (
	"net/http"

	"github.com/serp/ptm-task/src/core/domain/constant"
)

type ErrorResponse struct {
	HTTPCode    int
	ServiceCode int
	Message     string
}

var errorResponseMap = map[int]ErrorResponse{
	constant.GeneralInternalServerError: {
		HTTPCode:    http.StatusInternalServerError,
		ServiceCode: constant.GeneralInternalServerError,
		Message:     "Service unavailable",
	},
	constant.GeneralBadRequest: {
		HTTPCode:    http.StatusBadRequest,
		ServiceCode: constant.GeneralBadRequest,
		Message:     "Bad request",
	},
	constant.GeneralUnauthorized: {
		HTTPCode:    http.StatusUnauthorized,
		ServiceCode: constant.GeneralUnauthorized,
		Message:     "Unauthorized",
	},
	constant.GeneralForbidden: {
		HTTPCode:    http.StatusForbidden,
		ServiceCode: constant.GeneralForbidden,
		Message:     "Forbidden",
	},
	constant.GeneralNotFound: {
		HTTPCode:    http.StatusNotFound,
		ServiceCode: constant.GeneralNotFound,
		Message:     "Not found",
	},
}

// GetErrorResponse get error response from code
func GetErrorResponse(code int) ErrorResponse {
	if val, ok := errorResponseMap[code]; ok {
		return val
	}

	// default response
	return ErrorResponse{
		HTTPCode:    http.StatusInternalServerError,
		ServiceCode: code,
		Message:     http.StatusText(http.StatusInternalServerError),
	}
}
