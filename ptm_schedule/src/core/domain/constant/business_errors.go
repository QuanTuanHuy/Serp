/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package constant

const (
	// General errors
	ForbiddenAccess = "forbidden access"

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
	EventCannotBeModified        = "event cannot be modified in current status"
	EventCannotBeSplit           = "event cannot be split: duration too short or status not allowed"
	EventInvalidSplitPoint       = "invalid split point: would create parts smaller than minimum duration"
	EventInvalidTimeRange        = "invalid time range for event"

	// Schedule Plan/Group/Task errors
	SchedulePlanNotFound = "schedule plan not found"
	ScheduleTaskNotFound = "schedule task not found"

	// Concurrent update errors
	OptimisticLockConflict = "optimistic lock conflict: data was modified by another process"
	PlanVersionMismatch    = "plan version mismatch: please refresh and try again"

	// Plan state transition errors
	PlanAlreadyActive         = "plan is already active"
	PlanNotProposed           = "plan must be in PROPOSED status to apply"
	PlanNotArchived           = "plan must be in ARCHIVED status to revert"
	PlanCannotBeDiscarded     = "only PROPOSED or DRAFT plans can be discarded"
	PlanAlreadyProcessing     = "plan optimization is already in progress"
	PlanOptimizationFailed    = "plan optimization failed"
	PlanNoEventsToSchedule    = "no events to schedule in plan"
	PlanEventCloneFailed      = "failed to clone events from archived plan"
	PlanTaskCloneFailed       = "failed to clone tasks for proposed plan"
	PlanHistoryLimitReached   = "maximum archived plan history limit reached"
	ProposedPlanAlreadyExists = "a proposed plan already exists, please apply or discard it first"
)
