/*
Author: QuanTuanHuy
Description: Part of Serp Project - Time Slot Utilities
*/

package algorithm

import "sort"

type TimeSlotUtils struct{}

func NewTimeSlotUtils() *TimeSlotUtils {
	return &TimeSlotUtils{}
}

// CalculateGaps computes available gaps given base windows and booked assignments
func (u *TimeSlotUtils) CalculateGaps(baseWindows []*Window, bookedAssignments []*Assignment) []*Window {
	gaps := make([]*Window, 0)

	sortedAssignments := make([]*Assignment, len(bookedAssignments))
	copy(sortedAssignments, bookedAssignments)
	sort.Slice(sortedAssignments, func(i, j int) bool {
		if sortedAssignments[i].DateMs != sortedAssignments[j].DateMs {
			return sortedAssignments[i].DateMs < sortedAssignments[j].DateMs
		}
		return sortedAssignments[i].StartMin < sortedAssignments[j].StartMin
	})

	for _, baseWin := range baseWindows {
		dateMs := baseWin.DateMs
		currentCursor := baseWin.StartMin
		windowEnd := baseWin.EndMin

		dailyAssignments := u.filterAssignmentsForWindow(sortedAssignments, baseWin)

		for _, assignment := range dailyAssignments {
			assignmentStart := max(baseWin.StartMin, assignment.StartMin)
			assignmentEnd := min(baseWin.EndMin, assignment.EndMin)

			if assignmentStart > currentCursor {
				gaps = append(gaps, &Window{
					DateMs:     dateMs,
					StartMin:   currentCursor,
					EndMin:     assignmentStart,
					IsDeepWork: baseWin.IsDeepWork,
				})
			}

			currentCursor = max(currentCursor, assignmentEnd)
		}

		if currentCursor < windowEnd {
			gaps = append(gaps, &Window{
				DateMs:     dateMs,
				StartMin:   currentCursor,
				EndMin:     windowEnd,
				IsDeepWork: baseWin.IsDeepWork,
			})
		}
	}

	return gaps
}

func (u *TimeSlotUtils) filterAssignmentsForWindow(assignments []*Assignment, window *Window) []*Assignment {
	filtered := make([]*Assignment, 0)
	for _, a := range assignments {
		if a.DateMs == window.DateMs &&
			a.EndMin > window.StartMin &&
			a.StartMin < window.EndMin {
			filtered = append(filtered, a)
		}
	}
	return filtered
}

// MergeAssignments combines existing and new assignments, handling conflicts
func (u *TimeSlotUtils) MergeAssignments(existing, new []*Assignment) []*Assignment {
	result := make([]*Assignment, 0, len(existing)+len(new))

	result = append(result, new...)

	// Add existing that don't conflict with new
	for _, e := range existing {
		hasConflict := false
		for _, n := range new {
			if e.Overlaps(n) {
				hasConflict = true
				break
			}
		}
		if !hasConflict {
			result = append(result, e)
		}
	}

	sort.Slice(result, func(i, j int) bool {
		if result[i].DateMs != result[j].DateMs {
			return result[i].DateMs < result[j].DateMs
		}
		return result[i].StartMin < result[j].StartMin
	})

	return result
}

// FindConflictingAssignments returns assignments that overlap with given time range
func (u *TimeSlotUtils) FindConflictingAssignments(assignments []*Assignment, dateMs int64, startMin, endMin int) []*Assignment {
	conflicts := make([]*Assignment, 0)
	for _, a := range assignments {
		if a.OverlapsTimeRange(dateMs, startMin, endMin) {
			conflicts = append(conflicts, a)
		}
	}
	return conflicts
}

// GetAllAssignmentsForTask returns all parts of a split task
func (u *TimeSlotUtils) GetAllAssignmentsForTask(assignments []*Assignment, scheduleTaskID int64) []*Assignment {
	result := make([]*Assignment, 0)
	for _, a := range assignments {
		if a.ScheduleTaskID == scheduleTaskID {
			result = append(result, a)
		}
	}
	return result
}

// RemoveAssignmentsForTask removes all parts of a task from assignments
func (u *TimeSlotUtils) RemoveAssignmentsForTask(assignments []*Assignment, scheduleTaskID int64) []*Assignment {
	result := make([]*Assignment, 0, len(assignments))
	for _, a := range assignments {
		if a.ScheduleTaskID != scheduleTaskID {
			result = append(result, a)
		}
	}
	return result
}
