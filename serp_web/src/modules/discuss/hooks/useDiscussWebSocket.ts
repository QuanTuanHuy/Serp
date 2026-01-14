/*
Author: QuanTuanHuy
Description: Part of Serp Project - WebSocket hook for real-time discuss functionality
*/

'use client';

import { useEffect, useRef, useCallback } from 'react';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { useAppSelector } from '@/lib/store/hooks';
import { discussApi } from '../api/discussApi';
import type { Message } from '../types';
import { useDispatch } from 'react-redux';

interface UseDiscussWebSocketOptions {
  channelId?: string;
  onMessage?: (message: Message) => void;
  onTypingUpdate?: (userId: string, isTyping: boolean) => void;
  onUserStatusUpdate?: (userId: string, isOnline: boolean) => void;
}

export const useDiscussWebSocket = (
  options: UseDiscussWebSocketOptions = {}
) => {
  const { channelId, onMessage, onTypingUpdate, onUserStatusUpdate } = options;

  const dispatch = useDispatch();
  const clientRef = useRef<Client | null>(null);
  const subscriptionRef = useRef<StompSubscription | null>(null);

  const token = useAppSelector((state) => state.account.auth?.token);
  const wsUrl =
    (process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:8080/ws') + '/discuss';

  // Connect to WebSocket server
  useEffect(() => {
    if (!token) {
      console.warn('[WebSocket] No auth token available, skipping connection');
      return;
    }

    console.log('[WebSocket] Connecting to', wsUrl);

    const client = new Client({
      brokerURL: wsUrl,
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      debug: (str) => {
        if (process.env.NODE_ENV === 'development') {
          console.log('[STOMP]', str);
        }
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    });

    client.onConnect = () => {
      console.log('[WebSocket] ✅ Connected successfully');
      clientRef.current = client;
    };

    client.onStompError = (frame) => {
      console.error('[WebSocket] ❌ STOMP error:', frame.headers['message']);
      console.error('[WebSocket] Error details:', frame.body);
    };

    client.onWebSocketClose = () => {
      console.log('[WebSocket] 🔌 Connection closed');
    };

    client.onWebSocketError = (event) => {
      console.error('[WebSocket] ❌ WebSocket error:', event);
    };

    client.activate();

    return () => {
      console.log('[WebSocket] Disconnecting...');
      client.deactivate();
      clientRef.current = null;
    };
  }, [token, wsUrl]);

  // Subscribe to channel when channelId changes
  useEffect(() => {
    const client = clientRef.current;
    if (!client || !client.connected || !channelId) {
      return;
    }

    console.log('[WebSocket] Subscribing to channel', channelId);

    // Subscribe to channel messages
    const subscription = client.subscribe(
      `/topic/channels/${channelId}`,
      (message: IMessage) => {
        try {
          const payload = JSON.parse(message.body);
          console.log('[WebSocket] Received message:', payload);

          // Handle different message types
          handleChannelMessage(payload);
        } catch (error) {
          console.error('[WebSocket] Failed to parse message:', error);
        }
      }
    );

    subscriptionRef.current = subscription;

    return () => {
      console.log('[WebSocket] Unsubscribing from channel', channelId);
      subscription.unsubscribe();
      subscriptionRef.current = null;
    };
  }, [channelId, dispatch, onMessage, onTypingUpdate]);

  // Handle incoming channel messages
  const handleChannelMessage = useCallback(
    (payload: any) => {
      const { type, data } = payload;

      switch (type) {
        case 'NEW_MESSAGE':
          console.log('[WebSocket] New message received:', data);
          // Invalidate messages cache to refetch
          dispatch(
            discussApi.util.invalidateTags([
              { type: 'Message', id: `CHANNEL-${data.channelId}` },
            ])
          );
          if (onMessage) {
            onMessage(data);
          }
          break;

        case 'MESSAGE_EDITED':
          console.log('[WebSocket] Message edited:', data);
          dispatch(
            discussApi.util.invalidateTags([
              { type: 'Message', id: `CHANNEL-${data.channelId}` },
            ])
          );
          break;

        case 'MESSAGE_DELETED':
          console.log('[WebSocket] Message deleted:', data);
          dispatch(
            discussApi.util.invalidateTags([
              { type: 'Message', id: `CHANNEL-${data.channelId}` },
            ])
          );
          break;

        case 'REACTION_ADDED':
        case 'REACTION_REMOVED':
          console.log('[WebSocket] Reaction updated:', data);
          dispatch(
            discussApi.util.invalidateTags([
              { type: 'Message', id: `CHANNEL-${data.channelId}` },
            ])
          );
          break;

        case 'TYPING_INDICATOR':
          console.log('[WebSocket] Typing indicator:', data);
          if (onTypingUpdate) {
            onTypingUpdate(data.userId, data.isTyping);
          }
          break;

        case 'USER_ONLINE':
        case 'USER_OFFLINE':
          console.log('[WebSocket] User status update:', data);
          if (onUserStatusUpdate) {
            onUserStatusUpdate(data.userId, type === 'USER_ONLINE');
          }
          dispatch(discussApi.util.invalidateTags(['Presence']));
          break;

        case 'CHANNEL_UPDATED':
          console.log('[WebSocket] Channel updated:', data);
          dispatch(
            discussApi.util.invalidateTags([
              { type: 'Channel', id: data.channelId },
              { type: 'Channel', id: 'LIST' },
            ])
          );
          break;

        case 'UNREAD_COUNT_UPDATED':
          console.log('[WebSocket] Unread count updated:', data);
          dispatch(
            discussApi.util.invalidateTags([
              { type: 'Channel', id: data.channelId },
            ])
          );
          break;

        default:
          console.warn('[WebSocket] Unknown message type:', type);
      }
    },
    [dispatch, onMessage, onTypingUpdate, onUserStatusUpdate]
  );

  // Send typing indicator
  const sendTypingIndicator = useCallback(
    (isTyping: boolean) => {
      const client = clientRef.current;
      if (!client || !client.connected || !channelId) {
        console.warn(
          '[WebSocket] Cannot send typing indicator: not connected or no channel'
        );
        return;
      }

      try {
        client.publish({
          destination: `/app/channels/${channelId}/typing`,
          body: JSON.stringify({ isTyping }),
        });
        console.log('[WebSocket] Sent typing indicator:', isTyping);
      } catch (error) {
        console.error('[WebSocket] Failed to send typing indicator:', error);
      }
    },
    [channelId]
  );

  // Mark messages as read
  const markAsRead = useCallback(
    (messageId: string) => {
      const client = clientRef.current;
      if (!client || !client.connected || !channelId) {
        console.warn(
          '[WebSocket] Cannot mark as read: not connected or no channel'
        );
        return;
      }

      try {
        client.publish({
          destination: `/app/channels/${channelId}/read`,
          body: JSON.stringify({ messageId }),
        });
        console.log('[WebSocket] Marked message as read:', messageId);
      } catch (error) {
        console.error('[WebSocket] Failed to mark as read:', error);
      }
    },
    [channelId]
  );

  // Send message via WebSocket (alternative to REST API)
  const sendMessage = useCallback(
    (content: string, parentId?: string) => {
      const client = clientRef.current;
      if (!client || !client.connected || !channelId) {
        console.warn(
          '[WebSocket] Cannot send message: not connected or no channel'
        );
        return;
      }

      try {
        client.publish({
          destination: `/app/channels/${channelId}/message`,
          body: JSON.stringify({
            content,
            parentId,
            type: 'STANDARD',
          }),
        });
        console.log('[WebSocket] Sent message via WebSocket');
      } catch (error) {
        console.error('[WebSocket] Failed to send message:', error);
      }
    },
    [channelId]
  );

  return {
    isConnected: clientRef.current?.connected ?? false,
    sendTypingIndicator,
    markAsRead,
    sendMessage,
  };
};
