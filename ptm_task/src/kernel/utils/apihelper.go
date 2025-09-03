package utils

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-task/src/core/domain/constant"
)

func AbortErrorHandle(c *gin.Context, code int) {
	errorResponse := GetErrorResponse(code)
	c.JSON(errorResponse.HTTPCode, gin.H{
		"code":    errorResponse.ServiceCode,
		"status":  constant.HttpStatusError,
		"message": errorResponse.Message,
		"data":    nil,
	})
}

func AbortErrorHandleCustomMessage(c *gin.Context, code int, message string) {
	errorResponse := GetErrorResponse(code)
	c.JSON(errorResponse.HTTPCode, gin.H{
		"code":    errorResponse.ServiceCode,
		"status":  constant.HttpStatusError,
		"message": message,
		"data":    nil,
	})
}

func AbortErrorResponseHandle(c *gin.Context, errorResponse *ErrorResponse) {
	c.JSON(errorResponse.HTTPCode, gin.H{
		"code":    errorResponse.ServiceCode,
		"status":  constant.HttpStatusError,
		"message": errorResponse.Message,
		"data":    nil,
	})
}

func SuccessfulHandle(c *gin.Context, data interface{}) {
	c.JSON(http.StatusOK, gin.H{
		"code":    constant.GeneralSuccess,
		"status":  constant.HttpStatusSuccess,
		"message": constant.MessageOK,
		"data":    data,
	})
}

func MakeDataResponseWithPagination(data interface{}, total int64) (response gin.H) {
	return gin.H{
		"total": total,
		"data":  data,
	}
}

func BuildResponseListRequestForApp(limit, offset, total int64, objects map[string]interface{}) gin.H {
	response := gin.H{
		"limit":  limit,
		"offset": offset,
		"total":  total,
	}

	for key := range objects {
		response[key] = objects[key]
	}

	return response
}
