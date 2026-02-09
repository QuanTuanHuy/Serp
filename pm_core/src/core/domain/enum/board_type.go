/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type BoardType string

const (
	BoardKanban BoardType = "KANBAN"
	BoardScrum  BoardType = "SCRUM"
)

func (b BoardType) IsValid() bool {
	switch b {
	case BoardKanban, BoardScrum:
		return true
	}
	return false
}
