/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package utils

import (
	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"
	"github.com/serp/ptm-task/src/core/domain/constant"
)

var validate = validator.New()

func ValidateRequest(c *gin.Context, req interface{}) bool {
	if err := c.ShouldBindJSON(req); err != nil {
		AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, err.Error())
		return false
	}

	if err := validate.Struct(req); err != nil {
		AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, err.Error())
		return false
	}

	return true
}

func ValidateURI(c *gin.Context, req interface{}) bool {
	if err := c.ShouldBindUri(req); err != nil {
		AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, err.Error())
		return false
	}

	if err := validate.Struct(req); err != nil {
		AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, err.Error())
		return false
	}

	return true
}

func ValidateQuery(c *gin.Context, req interface{}) bool {
	if err := c.ShouldBindQuery(req); err != nil {
		AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, err.Error())
		return false
	}

	if err := validate.Struct(req); err != nil {
		AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, err.Error())
		return false
	}

	return true
}
