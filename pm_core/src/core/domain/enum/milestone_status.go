/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

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
