package utils

import (
	"fmt"
	"strconv"
	"strings"

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

func ValidatePaginationParams(c *gin.Context) (page int, pageSize int, valid bool) {
	pageStr := c.DefaultQuery("page", "0")
	pageSizeStr := c.DefaultQuery("pageSize", "10")

	page, err := strconv.Atoi(pageStr)
	if err != nil || page < 0 {
		AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, "Page must be non-negative")
		return 0, 0, false
	}

	pageSize, err = strconv.Atoi(pageSizeStr)
	if err != nil || pageSize <= 0 || pageSize > 100 {
		AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, "Page size must be between 1 and 100")
		return 0, 0, false
	}

	return page, pageSize, true
}

func ParseIntQuery(c *gin.Context, paramName string) (*int, bool) {
	paramStr := c.Query(paramName)
	if paramStr == "" {
		return nil, true
	}
	param, err := strconv.Atoi(paramStr)
	if err != nil {
		AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, fmt.Sprintf("Invalid %s format", paramName))
		return nil, false
	}
	return &param, true
}

func ParseInt64Query(c *gin.Context, paramName string) *int64 {
	paramStr := c.Query(paramName)
	if paramStr == "" {
		return nil
	}
	param, err := strconv.ParseInt(paramStr, 10, 64)
	if err != nil {
		return nil
	}
	return &param
}

func ParseStringQuery(c *gin.Context, paramName string) *string {
	paramStr := c.Query(paramName)
	if paramStr == "" {
		return nil
	}
	return &paramStr
}

// ValidateAndParseQueryID parses a numeric query parameter and validates it as positive.
func ValidateAndParseQueryID(c *gin.Context, paramName string) (int64, bool) {
	idStr := c.Query(paramName)
	if idStr == "" {
		AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, fmt.Sprintf("%s query parameter is required", paramName))
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

// ValidateAndParseIDsQuery parses a comma-separated list of IDs from a query parameter.
func ValidateAndParseIDsQuery(c *gin.Context, paramName string) ([]int64, bool) {
	idsStr := c.Query(paramName)
	if idsStr == "" {
		AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, fmt.Sprintf("%s query parameter is required", paramName))
		return nil, false
	}

	parts := strings.Split(idsStr, ",")
	ids := make([]int64, 0, len(parts))
	for _, p := range parts {
		s := strings.TrimSpace(p)
		if s == "" {
			AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, fmt.Sprintf("Invalid %s format", paramName))
			return nil, false
		}
		id, err := strconv.ParseInt(s, 10, 64)
		if err != nil || id <= 0 {
			AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, fmt.Sprintf("Invalid %s format", paramName))
			return nil, false
		}
		ids = append(ids, id)
	}
	return ids, true
}
