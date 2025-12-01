package message

type BaseKafkaMessage struct {
	Meta MessageMetadata `json:"meta"`
	Data any             `json:"data"`
}

func (m BaseKafkaMessage) GetEventType() string {
	return m.Meta.GetEventType()
}

func (m BaseKafkaMessage) GetEventID() string {
	return m.Meta.GetEventID()
}

func (m BaseKafkaMessage) GetSource() string {
	return m.Meta.GetSource()
}

func (m BaseKafkaMessage) GetData() any {
	return m.Data
}

func (m BaseKafkaMessage) GetMeta() MessageMetadata {
	return m.Meta
}

type MessageMetadata struct {
	EventID   string `json:"id"`
	EventType string `json:"type"`
	Source    string `json:"source"`
	Version   string `json:"v"`
	Timestamp int64  `json:"ts"`
	TraceID   string `json:"traceId"`
}

func (m MessageMetadata) GetEventType() string {
	return m.EventType
}

func (m MessageMetadata) GetEventID() string {
	return m.EventID
}

func (m MessageMetadata) GetSource() string {
	return m.Source
}
