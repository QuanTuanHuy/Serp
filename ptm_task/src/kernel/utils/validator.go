package utils

import (
	"fmt"
	"reflect"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"
	"github.com/serp/ptm-task/src/core/domain/constant"
)

var validate *validator.Validate

func init() {
	validate = validator.New()

	// Register custom tag name function to use json tag names in error messages
	validate.RegisterTagNameFunc(func(fld reflect.StructField) string {
		name := strings.SplitN(fld.Tag.Get("json"), ",", 2)[0]
		if name == "-" {
			return ""
		}
		return name
	})
}

// ValidationError represents a field validation error
type ValidationError struct {
	Field   string `json:"field"`
	Tag     string `json:"tag"`
	Value   string `json:"value"`
	Message string `json:"message"`
}

// ValidateStruct validates a struct and returns detailed error information
func ValidateStruct(s interface{}) []ValidationError {
	var validationErrors []ValidationError

	err := validate.Struct(s)
	if err != nil {
		for _, err := range err.(validator.ValidationErrors) {
			validationErrors = append(validationErrors, ValidationError{
				Field:   err.Field(),
				Tag:     err.Tag(),
				Value:   fmt.Sprintf("%v", err.Value()),
				Message: getErrorMessage(err),
			})
		}
	}

	return validationErrors
}

// ValidateAndBindJSON validates JSON request and returns validation errors
func ValidateAndBindJSON(c *gin.Context, req interface{}) bool {
	if err := c.ShouldBindJSON(req); err != nil {
		AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, "Invalid JSON format: "+err.Error())
		return false
	}

	if validationErrors := ValidateStruct(req); len(validationErrors) > 0 {
		AbortValidationError(c, validationErrors)
		return false
	}

	return true
}

// AbortValidationError handles validation errors
func AbortValidationError(c *gin.Context, validationErrors []ValidationError) {
	c.JSON(400, gin.H{
		"code":    constant.GeneralBadRequest,
		"status":  constant.HttpStatusError,
		"message": "Validation failed",
		"errors":  validationErrors,
		"data":    nil,
	})
}

// getErrorMessage returns user-friendly error messages
func getErrorMessage(fe validator.FieldError) string {
	switch fe.Tag() {
	case "required":
		return fmt.Sprintf("%s is required", fe.Field())
	case "email":
		return fmt.Sprintf("%s must be a valid email", fe.Field())
	case "min":
		return fmt.Sprintf("%s must be at least %s characters", fe.Field(), fe.Param())
	case "max":
		return fmt.Sprintf("%s must not exceed %s characters", fe.Field(), fe.Param())
	case "len":
		return fmt.Sprintf("%s must be exactly %s characters", fe.Field(), fe.Param())
	case "oneof":
		return fmt.Sprintf("%s must be one of: %s", fe.Field(), fe.Param())
	default:
		return fmt.Sprintf("%s is invalid", fe.Field())
	}
}
