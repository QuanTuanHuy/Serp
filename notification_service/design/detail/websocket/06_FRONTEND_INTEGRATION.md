# Frontend WebSocket Integration

**Module:** serp_web  
**Ngày tạo:** 2025-12-13

---

## 1. WebSocket Hook

```typescript
// serp_web/src/shared/hooks/useNotificationWebSocket.ts

import { useEffect, useRef, useCallback, useState } from 'react';
import { useDispatch } from 'react-redux';
import { 
  addNotification, 
  updateUnreadCount,
  markNotificationsAsRead,
  removeNotifications 
} from '@/modules/notification/store/notificationSlice';
import { useAuth } from '@/shared/hooks/useAuth';
import { toast } from '@/shared/components/ui/toast';

interface WSMessage {
  type: string;
  payload: any;
  timestamp: number;
  messageId?: string;
}

interface UseNotificationWebSocketOptions {
  autoConnect?: boolean;
  onConnect?: () => void;
  onDisconnect?: () => void;
  onError?: (error: Event) => void;
}

const WS_URL = process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:8080';
const RECONNECT_INTERVAL = 3000;
const MAX_RECONNECT_ATTEMPTS = 10;
const PING_INTERVAL = 30000;

export function useNotificationWebSocket(options: UseNotificationWebSocketOptions = {}) {
  const { autoConnect = true, onConnect, onDisconnect, onError } = options;
  
  const dispatch = useDispatch();
  const { accessToken, isAuthenticated } = useAuth();
  
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectAttempts = useRef(0);
  const reconnectTimeout = useRef<NodeJS.Timeout>();
  const pingInterval = useRef<NodeJS.Timeout>();
  
  const [isConnected, setIsConnected] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);

  // Message handler
  const handleMessage = useCallback((event: MessageEvent) => {
    try {
      const message: WSMessage = JSON.parse(event.data);
      
      switch (message.type) {
        case 'NEW_NOTIFICATION':
          dispatch(addNotification(message.payload));
          showNotificationToast(message.payload);
          break;
          
        case 'UNREAD_COUNT_UPDATE':
          setUnreadCount(message.payload.totalUnread);
          dispatch(updateUnreadCount(message.payload));
          break;
          
        case 'NOTIFICATION_READ':
          dispatch(markNotificationsAsRead(message.payload.notificationIds));
          setUnreadCount(message.payload.remainingUnread);
          break;
          
        case 'NOTIFICATION_DELETED':
          dispatch(removeNotifications(message.payload.notificationIds));
          setUnreadCount(message.payload.remainingUnread);
          break;
          
        case 'SYSTEM_ANNOUNCEMENT':
          showSystemAnnouncement(message.payload);
          break;
          
        case 'PONG':
          // Heartbeat received
          break;
          
        default:
          console.log('Unknown message type:', message.type);
      }
      
      // Send ACK for important messages
      if (message.messageId && message.type === 'NEW_NOTIFICATION') {
        sendAck(message.messageId);
      }
    } catch (error) {
      console.error('Failed to parse WebSocket message:', error);
    }
  }, [dispatch]);

  // Show toast for new notification
  const showNotificationToast = (notification: any) => {
    const variant = getToastVariant(notification.type);
    
    toast({
      title: notification.title,
      description: notification.message,
      variant,
      action: notification.actionUrl ? {
        label: 'View',
        onClick: () => window.location.href = notification.actionUrl,
      } : undefined,
    });
  };

  // Show system announcement
  const showSystemAnnouncement = (announcement: any) => {
    toast({
      title: announcement.title,
      description: announcement.message,
      variant: announcement.severity === 'warning' ? 'warning' : 'info',
      duration: 10000,
    });
  };

  // Get toast variant based on notification type
  const getToastVariant = (type: string) => {
    switch (type) {
      case 'SUCCESS': return 'success';
      case 'WARNING': return 'warning';
      case 'ERROR': return 'destructive';
      default: return 'default';
    }
  };

  // Send ACK
  const sendAck = (messageId: string) => {
    send({
      type: 'ACK',
      payload: { messageId },
      timestamp: Date.now(),
    });
  };

  // Send message
  const send = useCallback((message: WSMessage) => {
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      wsRef.current.send(JSON.stringify(message));
    }
  }, []);

  // Send ping
  const sendPing = useCallback(() => {
    send({
      type: 'PING',
      payload: {},
      timestamp: Date.now(),
    });
  }, [send]);

  // Subscribe to categories
  const subscribe = useCallback((categories: string[]) => {
    send({
      type: 'SUBSCRIBE',
      payload: { categories },
      timestamp: Date.now(),
    });
  }, [send]);

  // Unsubscribe from categories
  const unsubscribe = useCallback((categories: string[]) => {
    send({
      type: 'UNSUBSCRIBE',
      payload: { categories },
      timestamp: Date.now(),
    });
  }, [send]);

  // Connect
  const connect = useCallback(() => {
    if (!isAuthenticated || !accessToken) {
      return;
    }

    if (wsRef.current?.readyState === WebSocket.OPEN) {
      return;
    }

    const url = `${WS_URL}/notification/ws?token=${accessToken}`;
    const ws = new WebSocket(url);

    ws.onopen = () => {
      console.log('WebSocket connected');
      setIsConnected(true);
      reconnectAttempts.current = 0;
      onConnect?.();
      
      // Start ping interval
      pingInterval.current = setInterval(sendPing, PING_INTERVAL);
    };

    ws.onmessage = handleMessage;

    ws.onclose = (event) => {
      console.log('WebSocket closed:', event.code, event.reason);
      setIsConnected(false);
      onDisconnect?.();
      
      // Clear ping interval
      if (pingInterval.current) {
        clearInterval(pingInterval.current);
      }
      
      // Attempt reconnect
      if (reconnectAttempts.current < MAX_RECONNECT_ATTEMPTS) {
        const delay = RECONNECT_INTERVAL * Math.pow(1.5, reconnectAttempts.current);
        reconnectTimeout.current = setTimeout(() => {
          reconnectAttempts.current++;
          connect();
        }, delay);
      }
    };

    ws.onerror = (error) => {
      console.error('WebSocket error:', error);
      onError?.(error);
    };

    wsRef.current = ws;
  }, [isAuthenticated, accessToken, handleMessage, onConnect, onDisconnect, onError, sendPing]);

  // Disconnect
  const disconnect = useCallback(() => {
    if (reconnectTimeout.current) {
      clearTimeout(reconnectTimeout.current);
    }
    if (pingInterval.current) {
      clearInterval(pingInterval.current);
    }
    if (wsRef.current) {
      wsRef.current.close(1000, 'User disconnect');
      wsRef.current = null;
    }
    setIsConnected(false);
  }, []);

  // Auto connect on mount
  useEffect(() => {
    if (autoConnect && isAuthenticated) {
      connect();
    }
    
    return () => {
      disconnect();
    };
  }, [autoConnect, isAuthenticated, connect, disconnect]);

  // Reconnect on token change
  useEffect(() => {
    if (isAuthenticated && accessToken && wsRef.current?.readyState !== WebSocket.OPEN) {
      connect();
    }
  }, [accessToken, isAuthenticated, connect]);

  return {
    isConnected,
    unreadCount,
    connect,
    disconnect,
    subscribe,
    unsubscribe,
    send,
  };
}
```

---

## 2. Notification Provider

```typescript
// serp_web/src/modules/notification/providers/NotificationProvider.tsx

import React, { createContext, useContext, ReactNode } from 'react';
import { useNotificationWebSocket } from '@/shared/hooks/useNotificationWebSocket';

interface NotificationContextValue {
  isConnected: boolean;
  unreadCount: number;
  subscribe: (categories: string[]) => void;
  unsubscribe: (categories: string[]) => void;
}

const NotificationContext = createContext<NotificationContextValue | undefined>(undefined);

export function NotificationProvider({ children }: { children: ReactNode }) {
  const ws = useNotificationWebSocket({
    autoConnect: true,
    onConnect: () => console.log('Notification WS connected'),
    onDisconnect: () => console.log('Notification WS disconnected'),
  });

  return (
    <NotificationContext.Provider value={ws}>
      {children}
    </NotificationContext.Provider>
  );
}

export function useNotification() {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotification must be used within NotificationProvider');
  }
  return context;
}
```

---

## 3. Notification Bell Component

```typescript
// serp_web/src/modules/notification/components/NotificationBell.tsx

import { useState } from 'react';
import { Bell } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { 
  Popover, 
  PopoverContent, 
  PopoverTrigger 
} from '@/shared/components/ui/popover';
import { useNotification } from '../providers/NotificationProvider';
import { NotificationList } from './NotificationList';

export function NotificationBell() {
  const { unreadCount, isConnected } = useNotification();
  const [open, setOpen] = useState(false);

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button variant="ghost" size="icon" className="relative">
          <Bell className="h-5 w-5" />
          
          {/* Connection indicator */}
          <span 
            className={`absolute top-0 right-0 w-2 h-2 rounded-full ${
              isConnected ? 'bg-green-500' : 'bg-red-500'
            }`}
          />
          
          {/* Unread badge */}
          {unreadCount > 0 && (
            <span className="absolute -top-1 -right-1 flex items-center justify-center min-w-[18px] h-[18px] text-xs font-bold text-white bg-red-500 rounded-full px-1">
              {unreadCount > 99 ? '99+' : unreadCount}
            </span>
          )}
        </Button>
      </PopoverTrigger>
      
      <PopoverContent className="w-80 p-0" align="end">
        <NotificationList onClose={() => setOpen(false)} />
      </PopoverContent>
    </Popover>
  );
}
```

---

## 4. Redux Slice

```typescript
// serp_web/src/modules/notification/store/notificationSlice.ts

import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface Notification {
  id: number;
  title: string;
  message: string;
  type: string;
  category: string;
  priority: string;
  actionUrl?: string;
  isRead: boolean;
  createdAt: number;
}

interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  unreadByCategory: Record<string, number>;
  hasUrgent: boolean;
}

const initialState: NotificationState = {
  notifications: [],
  unreadCount: 0,
  unreadByCategory: {},
  hasUrgent: false,
};

const notificationSlice = createSlice({
  name: 'notification',
  initialState,
  reducers: {
    addNotification: (state, action: PayloadAction<Notification>) => {
      // Add to beginning
      state.notifications.unshift(action.payload);
      
      // Keep max 100 notifications
      if (state.notifications.length > 100) {
        state.notifications = state.notifications.slice(0, 100);
      }
      
      // Update unread count
      if (!action.payload.isRead) {
        state.unreadCount++;
      }
    },
    
    updateUnreadCount: (state, action: PayloadAction<{
      totalUnread: number;
      byCategory: Record<string, number>;
      hasUrgent: boolean;
    }>) => {
      state.unreadCount = action.payload.totalUnread;
      state.unreadByCategory = action.payload.byCategory;
      state.hasUrgent = action.payload.hasUrgent;
    },
    
    markNotificationsAsRead: (state, action: PayloadAction<number[]>) => {
      const ids = new Set(action.payload);
      state.notifications.forEach(n => {
        if (ids.has(n.id)) {
          n.isRead = true;
        }
      });
    },
    
    removeNotifications: (state, action: PayloadAction<number[]>) => {
      const ids = new Set(action.payload);
      state.notifications = state.notifications.filter(n => !ids.has(n.id));
    },
    
    setNotifications: (state, action: PayloadAction<Notification[]>) => {
      state.notifications = action.payload;
    },
    
    clearNotifications: (state) => {
      state.notifications = [];
      state.unreadCount = 0;
    },
  },
});

export const {
  addNotification,
  updateUnreadCount,
  markNotificationsAsRead,
  removeNotifications,
  setNotifications,
  clearNotifications,
} = notificationSlice.actions;

export default notificationSlice.reducer;
```

---

## 5. Reconnection Strategy

```typescript
// Exponential backoff with jitter
function getReconnectDelay(attempt: number): number {
  const baseDelay = 1000;
  const maxDelay = 30000;
  
  // Exponential backoff
  const exponentialDelay = baseDelay * Math.pow(2, attempt);
  
  // Add jitter (±25%)
  const jitter = exponentialDelay * 0.25 * (Math.random() * 2 - 1);
  
  // Cap at max delay
  return Math.min(exponentialDelay + jitter, maxDelay);
}

// Usage in reconnect logic
if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
  const delay = getReconnectDelay(reconnectAttempts);
  console.log(`Reconnecting in ${delay}ms (attempt ${reconnectAttempts + 1})`);
  
  setTimeout(() => {
    reconnectAttempts++;
    connect();
  }, delay);
}
```
