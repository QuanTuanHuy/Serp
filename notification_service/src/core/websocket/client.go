/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package websocket

import (
	"encoding/json"
	"fmt"
	"sync"
	"time"

	"github.com/google/uuid"
	"github.com/gorilla/websocket"
	"go.uber.org/zap"
)

// Connection timeouts
const (
	// Time allowed to write a message
	writeWait = 10 * time.Second

	// Time allowed to read pong from client
	pongWait = 60 * time.Second

	// Ping interval (must be less than pongWait)
	pingPeriod = (pongWait * 9) / 10 // 54 seconds

	// Maximum message size
	maxMessageSize = 4096 // 4KB

	// Send buffer size
	sendBufferSize = 256
)

type Client struct {
	// Unique client ID
	id string

	// Reference to hub
	hub IWebSocketHub

	// WebSocket connection
	conn *websocket.Conn

	userID   int64
	tenantID int64

	// Outbound message buffer
	send chan []byte

	// Category subscriptions
	subscriptions map[string]bool
	subMutex      sync.RWMutex

	connectedAt time.Time
	lastPingAt  time.Time
	userAgent   string
	remoteAddr  string

	logger *zap.Logger
}

func NewClient(
	hub IWebSocketHub,
	conn *websocket.Conn,
	userID, tenantID int64,
	userAgent, remoteAddr string,
	logger *zap.Logger,
) *Client {
	return &Client{
		id:            uuid.NewString(),
		hub:           hub,
		conn:          conn,
		userID:        userID,
		tenantID:      tenantID,
		send:          make(chan []byte, sendBufferSize),
		subscriptions: make(map[string]bool),
		connectedAt:   time.Now(),
		userAgent:     userAgent,
		remoteAddr:    remoteAddr,
		logger:        logger,
	}
}

// ReadPump handles incoming messages from client
// Runs in its own goroutine per client
func (c *Client) ReadPump() {
	defer func() {
		c.hub.UnregisterClient(c)
		c.conn.Close()
		c.logger.Info("ReadPump closed",
			zap.String("clientID", c.id),
			zap.Int64("userID", c.userID),
		)
	}()

	c.conn.SetReadLimit(maxMessageSize)
	c.conn.SetReadDeadline(time.Now().Add(pongWait))

	c.conn.SetPongHandler(func(appData string) error {
		c.conn.SetReadDeadline(time.Now().Add(pongWait))
		c.lastPingAt = time.Now()
		return nil
	})

	for {
		messageType, message, err := c.conn.ReadMessage()
		if err != nil {
			if websocket.IsUnexpectedCloseError(err,
				websocket.CloseGoingAway,
				websocket.CloseAbnormalClosure,
				websocket.CloseNormalClosure,
			) {
				c.logger.Error("Unexpected close error",
					zap.Error(err),
					zap.String("clientID", c.id),
				)
			}
			break
		}

		if messageType == websocket.TextMessage {
			c.handleMessage(message)
		}
	}
}

// WritePump handles outgoing messages to client
// Runs in its own goroutine per client
func (c *Client) WritePump() {
	ticker := time.NewTicker(pingPeriod)

	defer func() {
		ticker.Stop()
		c.conn.Close()
		c.logger.Info("WritePump closed",
			zap.String("clientID", c.id),
			zap.Int64("userID", c.userID),
		)
	}()

	for {
		select {
		case message, ok := <-c.send:
			c.conn.SetWriteDeadline(time.Now().Add(writeWait))

			if !ok {
				c.conn.WriteMessage(websocket.CloseMessage, []byte{})
				return
			}

			if err := c.conn.WriteMessage(websocket.TextMessage, message); err != nil {
				c.logger.Error("Write error",
					zap.Error(err),
					zap.String("clientID", c.id),
				)
				return
			}

			n := len(c.send)
			for i := 0; i < n; i++ {
				if err := c.conn.WriteMessage(websocket.TextMessage, <-c.send); err != nil {
					return
				}
			}

		case <-ticker.C:
			c.conn.SetWriteDeadline(time.Now().Add(writeWait))
			if err := c.conn.WriteMessage(websocket.PingMessage, nil); err != nil {
				return
			}
		}
	}
}

func (c *Client) handleMessage(message []byte) {
	var msg WSMessage
	if err := json.Unmarshal(message, &msg); err != nil {
		c.logger.Warn("Invalid message format",
			zap.Error(err),
			zap.String("clientID", c.id),
		)
		return
	}

	switch msg.Type {
	case "PING":
		c.handlePing()

	case "ACK":
		c.handleAck(&msg)

	case "SUBSCRIBE":
		c.handleSubscribe(&msg)

	case "UNSUBSCRIBE":
		c.handleUnsubscribe(&msg)

	default:
		c.logger.Warn("Unknown message type",
			zap.String("type", msg.Type),
			zap.String("clientID", c.id),
		)
	}
}

func (c *Client) handlePing() {
	response := WSMessage{
		Type:      "PONG",
		Timestamp: time.Now().UnixMilli(),
		MessageID: uuid.NewString(),
	}

	data, _ := json.Marshal(response)

	select {
	case c.send <- data:
	default:
		c.logger.Warn("Send buffer full on PONG",
			zap.String("clientID", c.id),
		)
	}
}

func (c *Client) handleAck(msg *WSMessage) {
	var payload AckPayload
	if err := json.Unmarshal(msg.Payload, &payload); err != nil {
		return
	}

	c.logger.Debug("Message acknowledged",
		zap.String("messageID", payload.MessageID),
		zap.String("clientID", c.id),
	)
}

func (c *Client) handleSubscribe(msg *WSMessage) {
	var payload SubscribePayload
	if err := json.Unmarshal(msg.Payload, &payload); err != nil {
		return
	}

	c.subMutex.Lock()
	for _, category := range payload.Categories {
		c.subscriptions[category] = true
	}
	c.subMutex.Unlock()

	c.logger.Info("Client subscribed",
		zap.Strings("categories", payload.Categories),
		zap.String("clientID", c.id),
	)

	c.sendSubscriptionConfirmation(payload.Categories, true)
}

func (c *Client) handleUnsubscribe(msg *WSMessage) {
	var payload SubscribePayload
	if err := json.Unmarshal(msg.Payload, &payload); err != nil {
		return
	}

	c.subMutex.Lock()
	for _, category := range payload.Categories {
		delete(c.subscriptions, category)
	}
	c.subMutex.Unlock()

	c.logger.Info("Client unsubscribed",
		zap.Strings("categories", payload.Categories),
		zap.String("clientID", c.id),
	)

	c.sendSubscriptionConfirmation(payload.Categories, false)
}

func (c *Client) sendSubscriptionConfirmation(categories []string, subscribed bool) {
	action := "SUBSCRIBED"
	if !subscribed {
		action = "UNSUBSCRIBED"
	}

	response := WSMessage{
		Type:      action,
		Timestamp: time.Now().UnixMilli(),
		MessageID: uuid.NewString(),
		Payload:   MustMarshal(SubscribePayload{Categories: categories}),
	}

	data, _ := json.Marshal(response)
	c.send <- data
}

func (c *Client) IsSubscribed(category string) bool {
	c.subMutex.RLock()
	defer c.subMutex.RUnlock()

	// Empty subscriptions means subscribed to all
	if len(c.subscriptions) == 0 {
		return true
	}

	return c.subscriptions[category]
}

func (c *Client) GetSubscriptions() []string {
	c.subMutex.RLock()
	defer c.subMutex.RUnlock()

	categories := make([]string, 0, len(c.subscriptions))
	for cat := range c.subscriptions {
		categories = append(categories, cat)
	}
	return categories
}

func (c *Client) GetInfo() ClientInfo {
	return ClientInfo{
		ID:            c.id,
		UserID:        c.userID,
		TenantID:      c.tenantID,
		ConnectedAt:   c.connectedAt,
		LastPingAt:    c.lastPingAt,
		UserAgent:     c.userAgent,
		RemoteAddr:    c.remoteAddr,
		Subscriptions: c.GetSubscriptions(),
	}
}

func (c *Client) ConnectionDuration() time.Duration {
	return time.Since(c.connectedAt)
}

func (c *Client) Send(message []byte) error {
	select {
	case c.send <- message:
		return nil
	default:
		return fmt.Errorf("send buffer full for client ID %s", c.id)
	}
}

func (c *Client) UserID() int64 {
	return c.userID
}

func (c *Client) TenantID() int64 {
	return c.tenantID
}

func (c *Client) ID() string {
	return c.id
}
