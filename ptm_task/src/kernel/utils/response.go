/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package utils

import (
	"net/http"

	"github.com/gin-gonic/gin"
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

func GetErrorResponse(code int) ErrorResponse {
	if val, ok := errorResponseMap[code]; ok {
		return val
	}

	return ErrorResponse{
		HTTPCode:    http.StatusInternalServerError,
		ServiceCode: code,
		Message:     http.StatusText(http.StatusInternalServerError),
	}
}

func AbortErrorHandleCustomMessage(c *gin.Context, errorCode int, customMessage string) {
	errorResponse := GetErrorResponse(errorCode)
	c.JSON(errorResponse.HTTPCode, gin.H{
		"code":    errorResponse.ServiceCode,
		"status":  "error",
		"message": customMessage,
	})
	c.Abort()
}

func AbortErrorHandle(c *gin.Context, errorCode int) {
	errorResponse := GetErrorResponse(errorCode)
	c.JSON(errorResponse.HTTPCode, gin.H{
		"code":    errorResponse.ServiceCode,
		"status":  "error",
		"message": errorResponse.Message,
	})
	c.Abort()
}

func SuccessfulHandle(c *gin.Context, message string, data any) {
	c.JSON(http.StatusOK, gin.H{
		"code":    http.StatusOK,
		"status":  "success",
		"message": message,
		"data":    data,
	})
}

func HandleBusinessError(c *gin.Context, businessError string) {
	if errorResp, ok := constant.BusinessErrorResponseMap[businessError]; ok {
		c.JSON(errorResp.HTTPCode, gin.H{
			"code":    errorResp.HTTPCode,
			"status":  constant.HttpStatusError,
			"message": errorResp.Message,
		})
	} else {
		c.JSON(http.StatusBadRequest, gin.H{
			"code":    http.StatusBadRequest,
			"status":  constant.HttpStatusError,
			"message": businessError,
		})
	}
	c.Abort()
}

func ErrorHandle(c *gin.Context, err error) {
	if err == nil {
		return
	}

	errorMsg := err.Error()

	if errorResp, ok := constant.BusinessErrorResponseMap[errorMsg]; ok {
		c.JSON(errorResp.HTTPCode, gin.H{
			"code":    errorResp.HTTPCode,
			"status":  constant.HttpStatusError,
			"message": errorResp.Message,
		})
		c.Abort()
		return
	}

	c.JSON(http.StatusInternalServerError, gin.H{
		"code":    http.StatusInternalServerError,
		"status":  constant.HttpStatusError,
		"message": errorMsg,
	})
	c.Abort()
}

func ErrorHandleCustomMessage(c *gin.Context, businessError string, customMessage string) {
	var httpCode int
	if errorResp, ok := constant.BusinessErrorResponseMap[businessError]; ok {
		httpCode = errorResp.HTTPCode
	} else {
		httpCode = http.StatusBadRequest
	}

	c.JSON(httpCode, gin.H{
		"code":    httpCode,
		"status":  constant.HttpStatusError,
		"message": customMessage,
	})
	c.Abort()
}
