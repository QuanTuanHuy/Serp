/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package constant

// Event types are owned by notification_service.
// Other services should publish messages with these meta.type values.
const (
	// EventNotificationCreateRequested requests creation of a single notification.
	EventNotificationCreateRequested = "notification.create.requested"

	// EventNotificationBulkCreateRequested requests creation of notifications for many users.
	EventNotificationBulkCreateRequested = "notification.bulk_create.requested"
)
