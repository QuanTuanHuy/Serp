/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package websocket

import (
	"context"
	"sync"
	"sync/atomic"

	"go.uber.org/zap"
)

// Target type constants for BroadcastMessage
const (
	TargetTypeUser   = "user"
	TargetTypeTenant = "tenant"
	TargetTypeAll    = "all"
)

type IWebSocketHub interface {
	// Lifecycle
	Start()
	Stop()

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
	TargetType string
	TargetID   int64 // userID or tenantID
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

	// Broadcast channel for async message dispatch
	broadcast chan *BroadcastMessage

	// Shutdown control
	ctx    context.Context
	cancel context.CancelFunc

	mutex sync.RWMutex

	logger *zap.Logger

	// Metrics
	totalConnections  atomic.Int64
	peakConnections   atomic.Int64
	messagesDelivered atomic.Int64
}

func NewHub(logger *zap.Logger) IWebSocketHub {
	ctx, cancel := context.WithCancel(context.Background())
	return &Hub{
		clients:   make(map[int64]map[*Client]bool),
		tenants:   make(map[int64]map[*Client]bool),
		broadcast: make(chan *BroadcastMessage, 256),
		ctx:       ctx,
		cancel:    cancel,
		logger:    logger,
	}
}

// Start begins the broadcast processing loop.
func (h *Hub) Start() {
	go h.run()
	h.logger.Info("WebSocket Hub started")
}

// Stop gracefully shuts down the hub, closing all client connections.
func (h *Hub) Stop() {
	h.cancel()
	h.logger.Info("WebSocket Hub stopping")

	h.mutex.Lock()
	defer h.mutex.Unlock()

	for userID, clients := range h.clients {
		for client := range clients {
			client.Close()
		}
		delete(h.clients, userID)
	}

	for tenantID := range h.tenants {
		delete(h.tenants, tenantID)
	}
}

// run is the internal broadcast processing loop.
// It only handles message dispatch — registration uses direct mutex calls.
func (h *Hub) run() {
	for {
		select {
		case <-h.ctx.Done():
			h.logger.Info("WebSocket Hub broadcast loop exiting")
			return
		case message := <-h.broadcast:
			h.dispatchBroadcast(message)
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

	total := h.totalConnections.Add(1)
	for {
		peak := h.peakConnections.Load()
		if total <= peak || h.peakConnections.CompareAndSwap(peak, total) {
			break
		}
	}

	h.logger.Info("Client registered",
		zap.Int64("userID", client.userID),
		zap.Int64("tenantID", client.tenantID),
		zap.String("clientID", client.id),
		zap.Int64("totalConnections", total),
	)
}

func (h *Hub) UnregisterClient(client *Client) {
	h.mutex.Lock()
	defer h.mutex.Unlock()

	if clients, ok := h.clients[client.userID]; ok {
		if _, exists := clients[client]; exists {
			delete(clients, client)
			client.Close()

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

	h.totalConnections.Add(-1)

	h.logger.Info("Client unregistered",
		zap.Int64("userID", client.userID),
		zap.String("clientID", client.id),
	)
}

func (h *Hub) dispatchBroadcast(msg *BroadcastMessage) {
	switch msg.TargetType {
	case TargetTypeUser:
		h.sendToUser(msg.TargetID, msg.Message, msg.Category)
	case TargetTypeTenant:
		h.sendToTenant(msg.TargetID, msg.Message, msg.Category)
	case TargetTypeAll:
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

		if err := client.Send(message); err != nil {
			h.logger.Warn("Client buffer full",
				zap.Int64("userID", client.userID),
				zap.String("clientID", client.id),
			)
		} else {
			h.messagesDelivered.Add(1)
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
		if err := client.Send(message); err != nil {
			h.logger.Warn("Client buffer full",
				zap.Int64("userID", client.userID),
				zap.String("clientID", client.id),
			)
		} else {
			h.messagesDelivered.Add(1)
		}
	}
}

func (h *Hub) sendToAll(message []byte) {
	h.mutex.RLock()
	defer h.mutex.RUnlock()

	for _, clients := range h.clients {
		for client := range clients {
			if err := client.Send(message); err == nil {
				h.messagesDelivered.Add(1)
			}
		}
	}
}

// Interface method implementations that route through the broadcast channel.

func (h *Hub) BroadcastToAll(message []byte) error {
	select {
	case h.broadcast <- &BroadcastMessage{
		TargetType: TargetTypeAll,
		Message:    message,
	}:
		return nil
	case <-h.ctx.Done():
		return h.ctx.Err()
	}
}

func (h *Hub) BroadcastToTenant(tenantID int64, message []byte) error {
	select {
	case h.broadcast <- &BroadcastMessage{
		TargetType: TargetTypeTenant,
		TargetID:   tenantID,
		Message:    message,
	}:
		return nil
	case <-h.ctx.Done():
		return h.ctx.Err()
	}
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
		TotalConnections:  h.totalConnections.Load(),
		PeakConnections:   h.peakConnections.Load(),
		UniqueUsers:       int64(len(h.clients)),
		UniqueTenants:     int64(len(h.tenants)),
		MessagesDelivered: h.messagesDelivered.Load(),
	}
}

func (h *Hub) GetTotalConnections() int64 {
	return h.totalConnections.Load()
}

func (h *Hub) IsUserConnected(userID int64) bool {
	h.mutex.RLock()
	defer h.mutex.RUnlock()

	clients, ok := h.clients[userID]
	return ok && len(clients) > 0
}

func (h *Hub) SendToUser(userID int64, message []byte) error {
	select {
	case h.broadcast <- &BroadcastMessage{
		TargetType: TargetTypeUser,
		TargetID:   userID,
		Message:    message,
	}:
		return nil
	case <-h.ctx.Done():
		return h.ctx.Err()
	}
}

func (h *Hub) SendToUserWithCategory(userID int64, message []byte, category string) error {
	select {
	case h.broadcast <- &BroadcastMessage{
		TargetType: TargetTypeUser,
		TargetID:   userID,
		Message:    message,
		Category:   category,
	}:
		return nil
	case <-h.ctx.Done():
		return h.ctx.Err()
	}
}
