/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package utils

import (
	"errors"

	"github.com/gin-gonic/gin"
)

func GetUserIDFromContext(c *gin.Context) (int64, error) {
	userID, exists := c.Get("userID")
	if !exists {
		return 0, errors.New("userID not found in context")
	}

	id, ok := userID.(int64)
	if !ok {
		return 0, errors.New("userID is not of type int64")
	}

	return id, nil
}

func GetTenantIDFromContext(c *gin.Context) (int64, error) {
	tenantID, exists := c.Get("tenantID")
	if !exists {
		return 0, errors.New("tenantID not found in context")
	}

	id, ok := tenantID.(int64)
	if !ok {
		return 0, errors.New("tenantID is not of type int64")
	}

	return id, nil
}

func GetUserEmailFromContext(c *gin.Context) (string, error) {
	email, exists := c.Get("userEmail")
	if !exists {
		return "", errors.New("userEmail not found in context")
	}

	str, ok := email.(string)
	if !ok {
		return "", errors.New("userEmail is not of type string")
	}

	return str, nil
}

func GetTokenFromContext(c *gin.Context) (string, error) {
	token, exists := c.Get("token")
	if !exists {
		return "", errors.New("token not found in context")
	}

	str, ok := token.(string)
	if !ok {
		return "", errors.New("token is not of type string")
	}

	return str, nil
}
