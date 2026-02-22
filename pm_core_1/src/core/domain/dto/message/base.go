package message

type BaseKafkaMessage struct {
	Meta MessageMetadata `json:"meta"`
	Data any             `json:"data"`
}

type MessageMetadata struct {
	EventID   string `json:"id"`
	EventType string `json:"type"`
	Source    string `json:"source"`
	Version   string `json:"v"`
	Timestamp int64  `json:"ts"`
	TraceID   string `json:"traceId"`
}
