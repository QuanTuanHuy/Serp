/*
Author: QuanTuanHuy
Description: Part of Serp Project - WebSocket Context for Discuss module
*/

'use client';

import React, { createContext, useContext, ReactNode } from 'react';

// WebSocket API interface
export interface WebSocketAPI {
  isConnected: boolean;
  sendMessage: (content: string, parentId?: string) => void;
  sendTypingIndicator: (isTyping: boolean) => void;
  markAsRead: (messageId: string) => void;
}

const WebSocketContext = createContext<WebSocketAPI | null>(null);

export const useWebSocket = () => {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error('useWebSocket must be used within WebSocketProvider');
  }
  return context;
};

// Optional hook that returns null if not in provider (for optional usage)
export const useWebSocketOptional = () => {
  return useContext(WebSocketContext);
};

interface WebSocketProviderProps {
  children: ReactNode;
  value: WebSocketAPI;
}

export const WebSocketProvider: React.FC<WebSocketProviderProps> = ({
  children,
  value,
}) => {
  return (
    <WebSocketContext.Provider value={value}>
      {children}
    </WebSocketContext.Provider>
  );
};
