/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package websocket

import (
	"sync"

	"go.uber.org/zap"
)

type IWebSocketHub interface {
	// Client management
	RegisterClient(client *Client)
	UnregisterClient(client *Client)

	// Send to specific user (all devices)
	SendToUser(userID int64, message []byte) error

	// Send with category filter
	SendToUserWithCategory(userID int64, message []byte, category string) error

	// Broadcast to all users in a tenant
	BroadcastToTenant(tenantID int64, message []byte) error

	// Broadcast to all users
	BroadcastToAll(message []byte) error

	// Connection status
	IsUserConnected(userID int64) bool
	GetConnectedUserCount() int
	GetTotalConnections() int64

	// Metrics
	GetMetrics() HubMetrics
}

type BroadcastMessage struct {
	TargetType string // "user", "tenant", "all"
	TargetID   int64  // userID or tenantID
	Message    []byte
	Category   string // Optional: filter by category subscription
}

type HubMetrics struct {
	TotalConnections  int64 `json:"totalConnections"`
	PeakConnections   int64 `json:"peakConnections"`
	UniqueUsers       int64 `json:"uniqueUsers"`
	UniqueTenants     int64 `json:"uniqueTenants"`
	MessagesDelivered int64 `json:"messagesDelivered"`
}

type Hub struct {
	// Client connections: userID -> set of clients
	// Supports multiple devices per user
	clients map[int64]map[*Client]bool

	// Tenant grouping for broadcast
	tenants map[int64]map[*Client]bool

	// Channels operations
	register   chan *Client
	unregister chan *Client
	broadcast  chan *BroadcastMessage

	mutex sync.RWMutex

	logger *zap.Logger

	// Metrics
	totalConnections  int64
	peakConnections   int64
	messagesDelivered int64
}

func NewHub(logger *zap.Logger) IWebSocketHub {
	hub := &Hub{
		clients:    make(map[int64]map[*Client]bool),
		tenants:    make(map[int64]map[*Client]bool),
		register:   make(chan *Client),
		unregister: make(chan *Client),
		broadcast:  make(chan *BroadcastMessage),
		logger:     logger,
	}
	go hub.Run()
	return hub
}

func (h *Hub) Run() {
	h.logger.Info("WebSocket Hub started")

	for {
		select {
		case client := <-h.register:
			h.RegisterClient(client)
		case client := <-h.unregister:
			h.UnregisterClient(client)
		case message := <-h.broadcast:
			h.broadcastMessage(message)
		}
	}
}

func (h *Hub) RegisterClient(client *Client) {
	h.mutex.Lock()
	defer h.mutex.Unlock()

	if h.clients[client.userID] == nil {
		h.clients[client.userID] = make(map[*Client]bool)
	}
	h.clients[client.userID][client] = true

	if h.tenants[client.tenantID] == nil {
		h.tenants[client.tenantID] = make(map[*Client]bool)
	}
	h.tenants[client.tenantID][client] = true

	h.totalConnections++
	if h.totalConnections > h.peakConnections {
		h.peakConnections = h.totalConnections
	}

	h.logger.Info("Client registered",
		zap.Int64("userID", client.userID),
		zap.Int64("tenantID", client.tenantID),
		zap.String("clientID", client.id),
		zap.Int64("totalConnections", h.totalConnections),
	)
}

func (h *Hub) UnregisterClient(client *Client) {
	h.mutex.Lock()
	defer h.mutex.Unlock()

	if clients, ok := h.clients[client.userID]; ok {
		if _, exists := clients[client]; exists {
			delete(clients, client)
			close(client.send)

			if len(clients) == 0 {
				delete(h.clients, client.userID)
			}
		}
	}

	if clients, ok := h.tenants[client.tenantID]; ok {
		if _, exists := clients[client]; exists {
			delete(clients, client)
			if len(clients) == 0 {
				delete(h.tenants, client.tenantID)
			}
		}
	}

	h.totalConnections--

	h.logger.Info("Client unregistered",
		zap.Int64("userID", client.userID),
		zap.String("clientID", client.id),
	)
}

func (h *Hub) broadcastMessage(msg *BroadcastMessage) {
	switch msg.TargetType {
	case "user":
		h.sendToUser(msg.TargetID, msg.Message, msg.Category)
	case "tenant":
		h.sendToTenant(msg.TargetID, msg.Message, msg.Category)
	case "all":
		h.sendToAll(msg.Message)
	}
}

func (h *Hub) sendToUser(userID int64, message []byte, category string) {
	h.mutex.RLock()
	defer h.mutex.RUnlock()

	clients, ok := h.clients[userID]
	if !ok {
		return
	}

	for client := range clients {
		if category != "" && !client.IsSubscribed(category) {
			continue
		}

		select {
		case client.send <- message:
			h.messagesDelivered++
		default:
			h.logger.Warn("Client buffer full",
				zap.Int64("userID", client.userID),
				zap.String("clientID", client.id),
			)
		}
	}
}

func (h *Hub) sendToTenant(tenantID int64, message []byte, category string) {
	h.mutex.RLock()
	defer h.mutex.RUnlock()

	clients, ok := h.tenants[tenantID]
	if !ok {
		return
	}
	for client := range clients {
		if category != "" && !client.IsSubscribed(category) {
			continue
		}
		select {
		case client.send <- message:
			h.messagesDelivered++
		default:
			h.logger.Warn("Client buffer full",
				zap.Int64("userID", client.userID),
				zap.String("clientID", client.id),
			)
		}
	}
}

func (h *Hub) sendToAll(message []byte) {
	h.mutex.RLock()
	defer h.mutex.RUnlock()

	for _, clients := range h.clients {
		for client := range clients {

			select {
			case client.send <- message:
				h.messagesDelivered++
			default:
				// Skip slow clients
			}
		}
	}
}

// Implements IWebSocketHub.

func (h *Hub) BroadcastToAll(message []byte) error {
	h.broadcast <- &BroadcastMessage{
		TargetType: "all",
		Message:    message,
	}
	return nil
}

func (h *Hub) BroadcastToTenant(tenantID int64, message []byte) error {
	h.broadcast <- &BroadcastMessage{
		TargetType: "tenant",
		TargetID:   tenantID,
		Message:    message,
	}
	return nil
}

func (h *Hub) GetConnectedUserCount() int {
	h.mutex.RLock()
	defer h.mutex.RUnlock()

	return len(h.clients)
}

func (h *Hub) GetMetrics() HubMetrics {
	h.mutex.RLock()
	defer h.mutex.RUnlock()

	return HubMetrics{
		TotalConnections:  h.totalConnections,
		PeakConnections:   h.peakConnections,
		UniqueUsers:       int64(len(h.clients)),
		UniqueTenants:     int64(len(h.tenants)),
		MessagesDelivered: h.messagesDelivered,
	}
}

func (h *Hub) GetTotalConnections() int64 {
	h.mutex.RLock()
	defer h.mutex.RUnlock()

	return h.totalConnections
}

func (h *Hub) IsUserConnected(userID int64) bool {
	h.mutex.RLock()
	defer h.mutex.RUnlock()

	clients, ok := h.clients[userID]
	return ok && len(clients) > 0
}

func (h *Hub) SendToUser(userID int64, message []byte) error {
	h.broadcast <- &BroadcastMessage{
		TargetType: "user",
		TargetID:   userID,
		Message:    message,
	}
	return nil
}

func (h *Hub) SendToUserWithCategory(userID int64, message []byte, category string) error {
	h.broadcast <- &BroadcastMessage{
		TargetType: "user",
		TargetID:   userID,
		Message:    message,
		Category:   category,
	}
	return nil
}
