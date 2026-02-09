/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type MethodologyType string

const (
	MethodologyScrum     MethodologyType = "SCRUM"
	MethodologyKanban    MethodologyType = "KANBAN"
	MethodologyWaterfall MethodologyType = "WATERFALL"
	MethodologyHybrid    MethodologyType = "HYBRID"
)

func (m MethodologyType) IsValid() bool {
	switch m {
	case MethodologyScrum, MethodologyKanban, MethodologyWaterfall, MethodologyHybrid:
		return true
	}
	return false
}
