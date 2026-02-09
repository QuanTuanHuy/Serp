/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

import "slices"

type MilestoneStatus string

const (
	MilestonePending    MilestoneStatus = "PENDING"
	MilestoneInProgress MilestoneStatus = "IN_PROGRESS"
	MilestoneCompleted  MilestoneStatus = "COMPLETED"
	MilestoneMissed     MilestoneStatus = "MISSED"
)

func (m MilestoneStatus) IsValid() bool {
	switch m {
	case MilestonePending, MilestoneInProgress, MilestoneCompleted, MilestoneMissed:
		return true
	}
	return false
}

var validMilestoneTransitions = map[MilestoneStatus][]MilestoneStatus{
	MilestonePending:    {MilestoneInProgress},
	MilestoneInProgress: {MilestoneCompleted, MilestoneMissed},
	MilestoneCompleted:  {},
	MilestoneMissed:     {},
}

func (m MilestoneStatus) CanTransitionTo(target MilestoneStatus) bool {
	allowed, ok := validMilestoneTransitions[m]
	if !ok {
		return false
	}
	return slices.Contains(allowed, target)
}

func (m MilestoneStatus) IsTerminal() bool {
	return m == MilestoneCompleted || m == MilestoneMissed
}
