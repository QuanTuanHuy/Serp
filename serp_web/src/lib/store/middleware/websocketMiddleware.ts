/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket Middleware
 */

import { Middleware } from '@reduxjs/toolkit';
import { setTokens, clearAuth } from '@/modules/account/store';
import {
  wsConnect,
  wsDisconnect,
  wsConnected,
  wsDisconnected,
  wsMessageReceived,
  wsError,
} from '../actions/websocketActions';

let socket: WebSocket | null = null;
let reconnectTimer: NodeJS.Timeout | null = null;
let pingInterval: NodeJS.Timeout | null = null;

export const websocketMiddleware: Middleware =
  (store) => (next) => (action: any) => {
    const result = next(action);
    const state = store.getState();
    const token = state.account.auth?.token;

    // Helper to connect
    const connect = () => {
      if (socket) {
        return;
      }

      const wsUrl = process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:8080';
      const url = `${wsUrl}/ws/notifications?token=${token}`;

      try {
        socket = new WebSocket(url);

        socket.onopen = () => {
          store.dispatch(wsConnected());
          if (reconnectTimer) {
            clearTimeout(reconnectTimer);
            reconnectTimer = null;
          }

          // Start ping interval to keep connection alive
          if (pingInterval) {
            clearInterval(pingInterval);
          }
          pingInterval = setInterval(() => {
            if (socket && socket.readyState === WebSocket.OPEN) {
              socket.send(
                JSON.stringify({ type: 'PING', timestamp: Date.now() })
              );
            }
          }, 30000); // Ping every 30 seconds
        };

        socket.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data);
            store.dispatch(wsMessageReceived(data));
          } catch (e) {
            console.error('Failed to parse WS message', e);
          }
        };

        socket.onclose = () => {
          store.dispatch(wsDisconnected());
          socket = null;

          // Clear ping interval
          if (pingInterval) {
            clearInterval(pingInterval);
            pingInterval = null;
          }

          // Reconnect if still authenticated
          const currentToken = store.getState().account.auth?.token;
          if (currentToken && !reconnectTimer) {
            reconnectTimer = setTimeout(() => {
              reconnectTimer = null;
              store.dispatch(wsConnect());
            }, 5000);
          }
        };

        socket.onerror = (error) => {
          store.dispatch(wsError('WebSocket error'));
          // console.error('WebSocket error', error);
        };
      } catch (e) {
        console.error('Failed to create WebSocket', e);
      }
    };

    // Helper to disconnect
    const disconnect = () => {
      if (pingInterval) {
        clearInterval(pingInterval);
        pingInterval = null;
      }
      if (socket) {
        socket.close();
        socket = null;
      }
      if (reconnectTimer) {
        clearTimeout(reconnectTimer);
        reconnectTimer = null;
      }
    };

    // 1. Connect on explicit action or login/rehydrate
    if (
      action.type === wsConnect.type ||
      action.type === setTokens.type ||
      action.type === 'persist/REHYDRATE'
    ) {
      // Wait for state to update (handled by next(action) above)
      // Check if we have a token now
      const currentToken = store.getState().account.auth?.token;
      if (currentToken) {
        connect();
      }
    }

    // 2. Disconnect on explicit action or logout
    if (action.type === wsDisconnect.type || action.type === clearAuth.type) {
      disconnect();
    }

    return result;
  };
