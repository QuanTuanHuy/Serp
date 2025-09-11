/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package utils

import (
	"strings"

	"github.com/serp/ptm-schedule/src/core/domain/enum"
)

func ConvertPriority(priorities []enum.Priority) int32 {
	if len(priorities) == 0 {
		return 0
	}
	priority := priorities[0]
	switch priority {
	case enum.Low:
		return 1
	case enum.Medium:
		return 2
	case enum.High:
		return 3
	case enum.Star:
		return 5
	default:
		return 0
	}
}

func ToPriorityEnum(priorities []string) []enum.Priority {
	var result []enum.Priority
	for _, p := range priorities {
		pCopy := strings.ToLower(p)
		switch pCopy {
		case "low":
			result = append(result, enum.Low)
		case "medium":
			result = append(result, enum.Medium)
		case "high":
			result = append(result, enum.High)
		case "star":
			result = append(result, enum.Star)
		}
	}
	return result
}

func ToPriorityString(priorities []enum.Priority) []string {
	var result []string
	for _, priority := range priorities {
		switch priority {
		case enum.Low:
			result = append(result, "LOW")
		case enum.Medium:
			result = append(result, "MEDIUM")
		case enum.High:
			result = append(result, "HIGH")
		case enum.Star:
			result = append(result, "STAR")
		default:
			result = append(result, "UNKNOWN")
		}
	}
	return result
}

var dayMaps = map[string]int32{
	"sunday":    0,
	"monday":    1,
	"tuesday":   2,
	"wednesday": 3,
	"thursday":  4,
	"friday":    5,
	"saturday":  6,
}

func ConvertWeekday(weekdays []string) []int32 {
	var result []int32
	for _, wd := range weekdays {
		wd = strings.ToLower(wd)
		if val, ok := dayMaps[wd]; ok {
			result = append(result, val)
		}
	}
	return result
}
