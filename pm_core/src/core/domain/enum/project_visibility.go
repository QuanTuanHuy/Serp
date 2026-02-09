/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type ProjectVisibility string

const (
	VisibilityPublic  ProjectVisibility = "PUBLIC"
	VisibilityPrivate ProjectVisibility = "PRIVATE"
)

func (v ProjectVisibility) IsValid() bool {
	switch v {
	case VisibilityPublic, VisibilityPrivate:
		return true
	}
	return false
}
