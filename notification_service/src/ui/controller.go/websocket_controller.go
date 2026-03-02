/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/gorilla/websocket"
	"github.com/serp/notification-service/src/core/domain/constant"
	ws "github.com/serp/notification-service/src/core/websocket"
	"github.com/serp/notification-service/src/kernel/properties"
	"github.com/serp/notification-service/src/kernel/utils"
	"go.uber.org/zap"
)

// Timeout for sending initial data to a newly connected client
const initialDataTimeout = 5 * time.Second

type WebSocketController struct {
	hub      ws.IWebSocketHub
	upgrade  websocket.Upgrader
	jwtUtils *utils.JWTUtils
	logger   *zap.Logger
}

func NewWebSocketController(
	hub ws.IWebSocketHub,
	jwtUtils *utils.JWTUtils,
	appProps *properties.AppProperties,
	logger *zap.Logger,
) *WebSocketController {
	allowedOrigins := make(map[string]bool, len(appProps.WebSocket.AllowedOrigins))
	for _, origin := range appProps.WebSocket.AllowedOrigins {
		allowedOrigins[origin] = true
	}

	return &WebSocketController{
		hub:      hub,
		jwtUtils: jwtUtils,
		logger:   logger,
		upgrade: websocket.Upgrader{
			ReadBufferSize:  1024,
			WriteBufferSize: 1024,
			CheckOrigin: func(r *http.Request) bool {
				if len(allowedOrigins) == 0 {
					return false
				}
				origin := r.Header.Get("Origin")
				if origin == "" {
					// No Origin header means same-origin or non-browser client
					return true
				}
				return allowedOrigins[origin]
			},
		},
	}
}

func (w *WebSocketController) HandleWebSocket(c *gin.Context) {
	token := c.Query("token")
	if token == "" {
		utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
		return
	}

	valid, err := w.jwtUtils.ValidateToken(c, token)
	if err != nil || !valid {
		utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
		return
	}

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
		ctx, cancel := context.WithTimeout(context.Background(), initialDataTimeout)
		defer cancel()

		_ = ctx

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

		data, err := json.Marshal(msg)
		if err != nil {
			w.logger.Error("Failed to marshal initial data", zap.Error(err))
			return
		}

		if err := client.Send(data); err != nil {
			w.logger.Warn("Failed to send initial data, client may have disconnected",
				zap.Error(err),
				zap.String("clientID", client.ID()),
			)
		}
	}()
}

func (w *WebSocketController) getUnreadCount(userID int64) (*ws.UnreadCountPayload, error) {
	// TODO: Wire to actual notification service query
	return &ws.UnreadCountPayload{
		TotalUnread: 0,
		ByCategory:  make(map[string]int64),
		HasUrgent:   false,
	}, nil
}
