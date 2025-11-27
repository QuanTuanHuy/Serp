package entity

type FailedEventEntity struct {
	BaseEntity
	EventID      string `json:"eventId"`
	EventType    string `json:"eventType"`
	Topic        string `json:"topic"`
	MessageKey   string `json:"messageKey"`
	MessageValue string `json:"messageValue"`
	RetryCount   int    `json:"retryCount"`
	LastError    string `json:"lastError"`
	Status       string `json:"status"`
}
