/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package constant

const (
	DefaultTTL = 60 * 60
	ShortTTL   = 5 * 60
)

const (
	UserPriorityWeights = "ptm:users:%d:priority:weights"

	ProjectsByUserID      = "ptm:users:%d:projects"
	GroupTasksByProjectID = "ptm:projects:%d:group-tasks"
)
