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
	"Sunday":    0,
	"Monday":    1,
	"Tuesday":   2,
	"Wednesday": 3,
	"Thursday":  4,
	"Friday":    5,
	"Saturday":  6,
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

func ToDayOfWeekString(weekDay int) string {
	for k, v := range dayMaps {
		if int(v) == weekDay {
			return k
		}
	}
	return "Unknown"
}
