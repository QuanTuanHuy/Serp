/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type DependencyType string

const (
	DependencyBlocks      DependencyType = "BLOCKS"
	DependencyIsBlockedBy DependencyType = "IS_BLOCKED_BY"
	DependencyRelatesTo   DependencyType = "RELATES_TO"
)

func (d DependencyType) IsValid() bool {
	switch d {
	case DependencyBlocks, DependencyIsBlockedBy, DependencyRelatesTo:
		return true
	}
	return false
}
