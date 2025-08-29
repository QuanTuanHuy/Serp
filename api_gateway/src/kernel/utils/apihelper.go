package utils

import (
	"fmt"
	"net/http"
	"net/url"
	"reflect"
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/core/domain/constant"
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

// BuildQueryParams builds URL query parameters from a struct using reflection
func BuildQueryParams(params interface{}) string {
	if params == nil {
		return ""
	}

	values := url.Values{}
	v := reflect.ValueOf(params)
	t := reflect.TypeOf(params)

	// Handle pointer types
	if v.Kind() == reflect.Ptr {
		if v.IsNil() {
			return ""
		}
		v = v.Elem()
		t = t.Elem()
	}

	for i := 0; i < v.NumField(); i++ {
		field := v.Field(i)
		fieldType := t.Field(i)

		// Get the form tag name
		tag := fieldType.Tag.Get("form")
		if tag == "" || tag == "-" {
			continue
		}

		// Handle different field types
		switch field.Kind() {
		case reflect.String:
			if field.String() != "" {
				values.Add(tag, field.String())
			}
		case reflect.Int, reflect.Int8, reflect.Int16, reflect.Int32, reflect.Int64:
			if field.Int() != 0 {
				values.Add(tag, strconv.FormatInt(field.Int(), 10))
			}
		case reflect.Uint, reflect.Uint8, reflect.Uint16, reflect.Uint32, reflect.Uint64:
			if field.Uint() != 0 {
				values.Add(tag, strconv.FormatUint(field.Uint(), 10))
			}
		case reflect.Float32, reflect.Float64:
			if field.Float() != 0 {
				values.Add(tag, fmt.Sprintf("%g", field.Float()))
			}
		case reflect.Bool:
			values.Add(tag, strconv.FormatBool(field.Bool()))
		case reflect.Ptr:
			if !field.IsNil() {
				elem := field.Elem()
				switch elem.Kind() {
				case reflect.String:
					if elem.String() != "" {
						values.Add(tag, elem.String())
					}
				case reflect.Int, reflect.Int8, reflect.Int16, reflect.Int32, reflect.Int64:
					values.Add(tag, strconv.FormatInt(elem.Int(), 10))
				case reflect.Bool:
					values.Add(tag, strconv.FormatBool(elem.Bool()))
				}
			}
		}
	}

	if len(values) == 0 {
		return ""
	}

	return "?" + values.Encode()
}
