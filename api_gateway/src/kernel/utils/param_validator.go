package utils

import (
	"fmt"
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/core/domain/constant"
)

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

func ValidatePaginationParams(c *gin.Context) (page int64, pageSize int64, valid bool) {
	pageStr := c.DefaultQuery("page", "1")
	pageSizeStr := c.DefaultQuery("pageSize", "10")

	page, err := strconv.ParseInt(pageStr, 10, 64)
	if err != nil || page <= 0 {
		AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, "Page must be a positive number")
		return 0, 0, false
	}

	pageSize, err = strconv.ParseInt(pageSizeStr, 10, 64)
	if err != nil || pageSize <= 0 || pageSize > 100 {
		AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, "Page size must be between 1 and 100")
		return 0, 0, false
	}

	return page, pageSize, true
}
