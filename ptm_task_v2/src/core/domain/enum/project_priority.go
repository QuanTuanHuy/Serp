/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type ProjectPriority string

const (
	Low    ProjectPriority = "LOW"
	Medium ProjectPriority = "MEDIUM"
	High   ProjectPriority = "HIGH"
)

func (p ProjectPriority) IsValid() bool {
	switch p {
	case Low, Medium, High:
		return true
	}
	return false
}
