/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"fmt"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/gorilla/websocket"
	ws "github.com/serp/notification-service/src/core/websocket"
	"github.com/serp/notification-service/src/kernel/utils"
	"go.uber.org/zap"
)

type WebSocketController struct {
	hub     ws.IWebSocketHub
	upgrade websocket.Upgrader
	logger  *zap.Logger
}

func NewWebSocketController(hub ws.IWebSocketHub, logger *zap.Logger) *WebSocketController {
	return &WebSocketController{
		hub:    hub,
		logger: logger,
		upgrade: websocket.Upgrader{
			ReadBufferSize:  1024,
			WriteBufferSize: 1024,
			CheckOrigin: func(r *http.Request) bool {
				return true
			},
		},
	}
}

func (w *WebSocketController) HandleWebSocket(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	tenantID, err := utils.GetTenantIDFromContext(c)
	if err != nil {
		return
	}

	conn, err := w.upgrade.Upgrade(c.Writer, c.Request, nil)
	if err != nil {
		w.logger.Error("failed to upgrade to websocket", zap.Error(err))
		return
	}

	client := ws.NewClient(
		w.hub,
		conn,
		userID,
		tenantID,
		c.GetHeader("User-Agent"),
		c.ClientIP(),
		w.logger,
	)
	w.hub.RegisterClient(client)

	w.logger.Info("WebSocket client connected",
		zap.Int64("userID", userID),
		zap.Int64("tenantID", tenantID),
	)

	go client.WritePump()
	go client.ReadPump()

	w.sendInitialData(client)

}

func (w *WebSocketController) sendInitialData(client *ws.Client) {
	go func() {
		unreadCount, err := w.getUnreadCount(client.UserID())
		if err != nil {
			w.logger.Error("Failed to get unread count", zap.Error(err))
			return
		}

		msg := ws.WSMessage{
			Type:      "INITIAL_DATA",
			Payload:   ws.MustMarshal(unreadCount),
			Timestamp: time.Now().UnixMilli(),
			MessageID: fmt.Sprintf("init-%d", time.Now().UnixNano()),
		}

		// Send to client
		client.Send(ws.MustMarshal(msg))
	}()
}

func (w *WebSocketController) getUnreadCount(userID int64) (*ws.UnreadCountPayload, error) {
	// Simplified for illustration
	return &ws.UnreadCountPayload{
		TotalUnread: 0,
		ByCategory:  make(map[string]int64),
		HasUrgent:   false,
	}, nil
}
