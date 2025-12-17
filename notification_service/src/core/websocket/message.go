package websocket

import (
	"encoding/json"
	"time"
)

type WSMessage struct {
	Type string `json:"type"`

	Payload json.RawMessage `json:"payload,omitempty"`

	Timestamp int64 `json:"timestamp"`

	MessageID string `json:"messageId,omitempty"`
}

type AckPayload struct {
	MessageID string `json:"messageId"`
}

type SubscribePayload struct {
	Categories []string `json:"categories"`
}

func mustMarshal(v interface{}) json.RawMessage {
	data, _ := json.Marshal(v)
	return data
}

type ClientInfo struct {
	ID            string    `json:"id"`
	UserID        int64     `json:"userId"`
	TenantID      int64     `json:"tenantId"`
	ConnectedAt   time.Time `json:"connectedAt"`
	LastPingAt    time.Time `json:"lastPingAt"`
	UserAgent     string    `json:"userAgent"`
	RemoteAddr    string    `json:"remoteAddr"`
	Subscriptions []string  `json:"subscriptions"`
}
