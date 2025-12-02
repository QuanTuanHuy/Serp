/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package utils

import (
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-schedule/src/core/domain/constant"
)

// HandleBusinessError checks if error is a known business error and returns appropriate HTTP status
func HandleBusinessError(c *gin.Context, err error) bool {
	if err == nil {
		return false
	}

	errMsg := err.Error()

	// Validation errors (400 Bad Request)
	validationErrors := []string{
		constant.AvailabilityUserIDMismatch,
		constant.AvailabilityInvalidItem,
		constant.AvailabilityItemsOverlap,
		constant.AvailabilityOverlapWithExisting,
		constant.ExceptionUserIDMismatch,
		constant.ExceptionInvalidItem,
		constant.ExceptionItemsOverlap,
		constant.ExceptionOverlapWithExisting,
		constant.EventPlanIDMismatch,
		constant.EventInvalidItem,
		constant.EventItemsOverlap,
		constant.EventOverlapWithExisting,
		constant.EventInvalidStatus,
		constant.EventInvalidStatusTransition,
		constant.EventInvalidActualTime,
		constant.EventInvalidActualTimeRange,
		constant.EventStatusUpdateFailed,
		constant.InvalidDateRange,
		constant.InvalidPlanID,
		constant.InvalidEventID,
		constant.InvalidQueryParameters,
		// Plan state errors
		constant.PlanNotProposed,
		constant.PlanNotArchived,
		constant.PlanCannotBeDiscarded,
		constant.PlanAlreadyProcessing,
		constant.OptimisticLockConflict,
		constant.PlanVersionMismatch,
	}

	for _, validationErr := range validationErrors {
		if strings.Contains(errMsg, validationErr) {
			AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, errMsg)
			return true
		}
	}

	// Not found errors (404 Not Found)
	notFoundErrors := []string{
		constant.EventNotFound,
		constant.SchedulePlanNotFound,
		constant.ScheduleGroupNotFound,
		constant.ScheduleTaskNotFound,
		constant.AvailabilityCalendarNotFound,
	}

	for _, notFoundErr := range notFoundErrors {
		if strings.Contains(errMsg, notFoundErr) {
			AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, errMsg)
			return true
		}
	}

	// Forbidden errors (403 Forbidden)
	forbiddenErrors := []string{
		constant.DeleteScheduleGroupForbidden,
		constant.ForbiddenAccess,
	}

	for _, forbiddenErr := range forbiddenErrors {
		if strings.Contains(errMsg, forbiddenErr) {
			AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, errMsg)
			return true
		}
	}

	// Unknown error - return false to let caller handle as internal server error
	return false
}
