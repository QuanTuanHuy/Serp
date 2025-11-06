/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package constant

const (
	InvalidQueryParameters = "invalid query parameters"
	InvalidDateRange       = "invalid date range: fromDateMs must be less than or equal to toDateMs"
	InvalidPlanID          = "invalid planID: must be positive"
	InvalidEventID         = "invalid eventID: must be positive"

	// Availability Calendar errors
	AvailabilityUserIDMismatch      = "userId mismatch in availability item"
	AvailabilityInvalidItem         = "invalid availability item: dayOfWeek must be 0-6, time range must be valid"
	AvailabilityItemsOverlap        = "availability items overlap: same day of week with overlapping time ranges"
	AvailabilityOverlapWithExisting = "availability overlaps with existing schedule"
	AvailabilityCalendarNotFound    = "no availability calendar found for user"

	// Calendar Exception errors
	ExceptionUserIDMismatch      = "userId mismatch in exception item"
	ExceptionInvalidItem         = "invalid exception item: dateMs and time range must be valid"
	ExceptionItemsOverlap        = "exception items overlap: same date with overlapping time ranges"
	ExceptionOverlapWithExisting = "exception overlaps with existing exception"

	// Schedule Event errors
	EventNotFound                = "schedule event not found"
	EventPlanIDMismatch          = "planID mismatch in event"
	EventInvalidItem             = "invalid event: scheduleTaskID, dateMs, and time range must be valid"
	EventItemsOverlap            = "events overlap: same plan and date with overlapping time ranges"
	EventOverlapWithExisting     = "event overlaps with existing event in schedule"
	EventInvalidStatus           = "invalid status"
	EventInvalidStatusTransition = "invalid status transition"
	EventInvalidActualTime       = "actualStartMin and actualEndMin required for DONE status"
	EventInvalidActualTimeRange  = "invalid actual time range"
	EventStatusUpdateFailed      = "failed to set status: validation failed"

	// Schedule Plan/Group/Task errors
	SchedulePlanNotFound         = "schedule plan not found"
	ScheduleGroupNotFound        = "schedule group not found"
	DeleteScheduleGroupForbidden = "delete schedule group forbidden"
	ScheduleTaskNotFound         = "schedule task not found"
)
