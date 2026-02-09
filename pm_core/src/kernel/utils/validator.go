/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package utils

import (
	"errors"
	"fmt"
	"reflect"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/gin-gonic/gin/binding"
	"github.com/go-playground/validator/v10"
	"github.com/serp/pm-core/src/core/domain/constant"
)

var validate *validator.Validate

func init() {
	validate = validator.New()

	validate.RegisterTagNameFunc(func(fld reflect.StructField) string {
		name := strings.SplitN(fld.Tag.Get("json"), ",", 2)[0]
		if name == "-" {
			return ""
		}
		return name
	})

	if v, ok := binding.Validator.Engine().(*validator.Validate); ok {
		v.RegisterTagNameFunc(func(fld reflect.StructField) string {
			name := strings.SplitN(fld.Tag.Get("json"), ",", 2)[0]
			if name == "-" {
				return ""
			}
			return name
		})
	}
}

type ValidationError struct {
	Field   string `json:"field"`
	Tag     string `json:"tag"`
	Value   string `json:"value"`
	Message string `json:"message"`
}

func ValidateStruct(s any) []ValidationError {
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

func ValidateAndBindJSON(c *gin.Context, req any) bool {
	if err := c.ShouldBindJSON(req); err != nil {
		var verrs validator.ValidationErrors
		if errors.As(err, &verrs) {
			var validationErrors []ValidationError
			for _, fe := range verrs {
				fieldName := jsonFieldNameFromFieldError(req, fe)
				validationErrors = append(validationErrors, ValidationError{
					Field:   fieldName,
					Tag:     fe.Tag(),
					Value:   fmt.Sprintf("%v", fe.Value()),
					Message: getErrorMessageWithName(fe, fieldName),
				})
			}
			AbortValidationError(c, validationErrors)
			return false
		}

		AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, "Invalid JSON format: "+err.Error())
		return false
	}

	if validationErrors := ValidateStruct(req); len(validationErrors) > 0 {
		AbortValidationError(c, validationErrors)
		return false
	}

	return true
}

func ValidateAndBindQuery(c *gin.Context, req any) bool {
	if err := c.ShouldBindQuery(req); err != nil {
		var verrs validator.ValidationErrors
		if errors.As(err, &verrs) {
			var validationErrors []ValidationError
			for _, fe := range verrs {
				fieldName := jsonFieldNameFromFieldError(req, fe)
				validationErrors = append(validationErrors, ValidationError{
					Field:   fieldName,
					Tag:     fe.Tag(),
					Value:   fmt.Sprintf("%v", fe.Value()),
					Message: getErrorMessageWithName(fe, fieldName),
				})
			}
			AbortValidationError(c, validationErrors)
			return false
		}

		AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, "Invalid query parameters: "+err.Error())
		return false
	}

	if validationErrors := ValidateStruct(req); len(validationErrors) > 0 {
		AbortValidationError(c, validationErrors)
		return false
	}

	return true
}

func AbortValidationError(c *gin.Context, validationErrors []ValidationError) {
	c.JSON(400, gin.H{
		"code":    constant.GeneralBadRequest,
		"status":  constant.HttpStatusError,
		"message": "Validation failed",
		"errors":  validationErrors,
		"data":    nil,
	})
}

func getErrorMessage(fe validator.FieldError) string {
	return getErrorMessageWithName(fe, fe.Field())
}

func getErrorMessageWithName(fe validator.FieldError, field string) string {
	sizeNoun := func(kind reflect.Kind) string {
		switch kind {
		case reflect.String:
			return "characters"
		case reflect.Slice, reflect.Array, reflect.Map:
			return "items"
		default:
			return ""
		}
	}

	formatBoundMsg := func(prefix string) string {
		k := fe.Kind()
		noun := sizeNoun(k)
		if noun != "" {
			return fmt.Sprintf("%s must be %s %s %s", field, prefix, fe.Param(), noun)
		}
		return fmt.Sprintf("%s must be %s %s", field, prefix, fe.Param())
	}

	switch fe.Tag() {
	case "required":
		return fmt.Sprintf("%s is required", field)
	case "required_with":
		return fmt.Sprintf("%s is required when %s is present", field, fe.Param())
	case "required_without":
		return fmt.Sprintf("%s is required when %s is absent", field, fe.Param())
	case "required_with_all":
		return fmt.Sprintf("%s is required when all of [%s] are present", field, fe.Param())
	case "required_without_all":
		return fmt.Sprintf("%s is required when all of [%s] are absent", field, fe.Param())
	case "required_if":
		return fmt.Sprintf("%s is required when %s", field, fe.Param())
	case "required_unless":
		return fmt.Sprintf("%s is required unless %s", field, fe.Param())
	case "email":
		return fmt.Sprintf("%s must be a valid email", field)
	case "url":
		return fmt.Sprintf("%s must be a valid URL", field)
	case "uri":
		return fmt.Sprintf("%s must be a valid URI", field)
	case "uuid", "uuid3", "uuid4", "uuid5", "uuid_rfc4122":
		return fmt.Sprintf("%s must be a valid UUID", field)
	case "ip":
		return fmt.Sprintf("%s must be a valid IP address", field)
	case "ipv4":
		return fmt.Sprintf("%s must be a valid IPv4 address", field)
	case "ipv6":
		return fmt.Sprintf("%s must be a valid IPv6 address", field)
	case "hostname":
		return fmt.Sprintf("%s must be a valid hostname", field)
	case "fqdn":
		return fmt.Sprintf("%s must be a valid FQDN", field)
	case "base64":
		return fmt.Sprintf("%s must be a valid base64 string", field)
	case "boolean":
		return fmt.Sprintf("%s must be a boolean", field)
	case "numeric":
		return fmt.Sprintf("%s must be a number", field)
	case "alpha":
		return fmt.Sprintf("%s must contain only letters", field)
	case "alphanum":
		return fmt.Sprintf("%s must contain only letters and numbers", field)
	case "datetime":
		return fmt.Sprintf("%s must match datetime format '%s'", field, fe.Param())
	case "min":
		noun := sizeNoun(fe.Kind())
		if noun != "" {
			return fmt.Sprintf("%s must be at least %s %s", field, fe.Param(), noun)
		}
		return fmt.Sprintf("%s must be at least %s", field, fe.Param())
	case "max":
		noun := sizeNoun(fe.Kind())
		if noun != "" {
			return fmt.Sprintf("%s must not exceed %s %s", field, fe.Param(), noun)
		}
		return fmt.Sprintf("%s must not exceed %s", field, fe.Param())
	case "len":
		noun := sizeNoun(fe.Kind())
		if noun != "" {
			return fmt.Sprintf("%s must be exactly %s %s", field, fe.Param(), noun)
		}
		return fmt.Sprintf("%s must be exactly %s", field, fe.Param())
	case "gt":
		return formatBoundMsg("greater than")
	case "gte":
		return formatBoundMsg("greater than or equal to")
	case "lt":
		return formatBoundMsg("less than")
	case "lte":
		return formatBoundMsg("less than or equal to")
	case "gtfield":
		return fmt.Sprintf("%s must be greater than %s", field, fe.Param())
	case "gtefield":
		return fmt.Sprintf("%s must be greater than or equal to %s", field, fe.Param())
	case "ltfield":
		return fmt.Sprintf("%s must be less than %s", field, fe.Param())
	case "ltefield":
		return fmt.Sprintf("%s must be less than or equal to %s", field, fe.Param())
	case "eqfield":
		return fmt.Sprintf("%s must be equal to %s", field, fe.Param())
	case "nefield":
		return fmt.Sprintf("%s must not be equal to %s", field, fe.Param())
	case "oneof":
		return fmt.Sprintf("%s must be one of: %s", field, fe.Param())
	case "contains":
		return fmt.Sprintf("%s must contain '%s'", field, fe.Param())
	case "excludes":
		return fmt.Sprintf("%s must not contain '%s'", field, fe.Param())
	case "startswith":
		return fmt.Sprintf("%s must start with '%s'", field, fe.Param())
	case "endswith":
		return fmt.Sprintf("%s must end with '%s'", field, fe.Param())
	default:
		return fmt.Sprintf("%s is invalid", field)
	}
}

func jsonFieldNameFromFieldError(req interface{}, fe validator.FieldError) string {
	t := reflect.TypeOf(req)
	if t == nil {
		return fe.Field()
	}
	if t.Kind() == reflect.Ptr {
		t = t.Elem()
	}

	ns := fe.StructNamespace()
	if ns == "" {
		ns = fe.StructField()
	}
	parts := strings.Split(ns, ".")
	start := 0
	if len(parts) > 0 && parts[0] == t.Name() {
		start = 1
	}

	curr := t
	for i := start; i < len(parts); i++ {
		name := parts[i]
		if curr.Kind() == reflect.Ptr {
			curr = curr.Elem()
		}
		if curr.Kind() != reflect.Struct {
			break
		}
		sf, ok := curr.FieldByName(name)
		if !ok {
			return fe.Field()
		}
		if i == len(parts)-1 {
			tag := sf.Tag.Get("json")
			if tag == "" || tag == "-" {
				return name
			}
			return strings.SplitN(tag, ",", 2)[0]
		}
		curr = sf.Type
	}

	return fe.Field()
}
