/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type TaskTemplateEntity struct {
	BaseEntity

	UserID   int64 `json:"userId"`
	TenantID int64 `json:"tenantId"`

	TemplateName string  `json:"templateName"`
	Description  *string `json:"description,omitempty"`

	TitleTemplate        string   `json:"titleTemplate"`
	EstimatedDurationMin int      `json:"estimatedDurationMin"`
	Priority             string   `json:"priority"`
	Category             *string  `json:"category,omitempty"`
	Tags                 []string `json:"tags,omitempty"`
	IsDeepWork           bool     `json:"isDeepWork"`

	PreferredTimeOfDay *string `json:"preferredTimeOfDay,omitempty"`
	PreferredDays      []int   `json:"preferredDays,omitempty"`

	RecurrencePattern *string `json:"recurrencePattern,omitempty"`
	RecurrenceConfig  *string `json:"recurrenceConfig,omitempty"`

	UsageCount int    `json:"usageCount"`
	LastUsedAt *int64 `json:"lastUsedAt,omitempty"`
	IsFavorite bool   `json:"isFavorite"`

	ActiveStatus string `json:"activeStatus"`
}

func NewTaskTemplateEntity() *TaskTemplateEntity {
	return &TaskTemplateEntity{
		Priority:      "MEDIUM",
		ActiveStatus:  "ACTIVE",
		Tags:          []string{},
		PreferredDays: []int{},
		UsageCount:    0,
		IsFavorite:    false,
	}
}

func (t *TaskTemplateEntity) IncrementUsage(currentTimeMs int64) {
	t.UsageCount++
	t.LastUsedAt = &currentTimeMs
}

func (t *TaskTemplateEntity) SubstituteVariables(variables map[string]string) string {
	title := t.TitleTemplate
	for key, value := range variables {
		placeholder := "{{" + key + "}}"
		title = replaceAll(title, placeholder, value)
	}
	return title
}

func replaceAll(s, old, new string) string {
	result := ""
	for len(s) > 0 {
		idx := indexOf(s, old)
		if idx == -1 {
			result += s
			break
		}
		result += s[:idx] + new
		s = s[idx+len(old):]
	}
	return result
}

func indexOf(s, substr string) int {
	for i := 0; i <= len(s)-len(substr); i++ {
		if s[i:i+len(substr)] == substr {
			return i
		}
	}
	return -1
}
