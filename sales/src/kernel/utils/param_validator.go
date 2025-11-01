/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package utils

import (
	"fmt"
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"
	"github.com/serp/sales/src/core/domain/constant"
)

func ValidateAndBindJSON(c *gin.Context, obj interface{}) bool {
	if err := c.ShouldBindJSON(obj); err != nil {
		if validationErrors, ok := err.(validator.ValidationErrors); ok {
			AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, formatValidationErrors(validationErrors))
		} else {
			AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, "Invalid request body")
		}
		return false
	}
	return true
}

func ValidateAndParseID(c *gin.Context, paramName string) (int64, bool) {
	idStr := c.Param(paramName)
	if idStr == "" {
		AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, fmt.Sprintf("%s parameter is required", paramName))
		return 0, false
	}

	id, err := strconv.ParseInt(idStr, 10, 64)
	if err != nil {
		AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, fmt.Sprintf("Invalid %s format", paramName))
		return 0, false
	}

	if id <= 0 {
		AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, fmt.Sprintf("%s must be positive", paramName))
		return 0, false
	}

	return id, true
}

func formatValidationErrors(errors validator.ValidationErrors) string {
	if len(errors) == 0 {
		return "Validation failed"
	}
	return fmt.Sprintf("Validation failed on field '%s'", errors[0].Field())
}
