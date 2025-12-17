package enum

type NotificationStatus string

const (
	NotificationUnread   NotificationStatus = "UNREAD"
	NotificationRead     NotificationStatus = "READ"
	NotificationArchived NotificationStatus = "ARCHIVED"
)
