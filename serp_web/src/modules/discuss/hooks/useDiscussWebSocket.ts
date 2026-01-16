/*
Author: QuanTuanHuy
Description: Part of Serp Project - WebSocket hook for real-time discuss functionality
Optimized: Direct cache updates instead of invalidation for better performance
*/

'use client';

import { useEffect, useRef, useCallback } from 'react';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { useAppSelector, useAppDispatch } from '@/lib/store/hooks';
import { messageApi } from '../api/messages.api';
import { discussApi } from '../api/discussApi';
import type { Message, MessageReaction } from '../types';
import { transformMessage } from '../api/transformers';

interface UseDiscussWebSocketOptions {
  channelId?: string;
  onMessage?: (message: Message) => void;
  onTypingUpdate?: (userId: string, isTyping: boolean) => void;
  onUserStatusUpdate?: (userId: string, isOnline: boolean) => void;
}

// Helper to find cache entries for a channel - matches the one in messages.api.ts
const findMessagesCacheEntry = (
  state: any,
  channelId: string
): { page: number; limit: number } | undefined => {
  const queries = state.api?.queries;
  if (!queries) return undefined;

  for (const key of Object.keys(queries)) {
    if (key.startsWith('getMessages(')) {
      const entry = queries[key];
      if (entry?.originalArgs?.channelId === channelId) {
        return {
          page: entry.originalArgs.pagination.page,
          limit: entry.originalArgs.pagination.limit,
        };
      }
    }
  }
  return undefined;
};

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

  const dispatch = useAppDispatch();
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

  // Handle incoming channel messages - Direct cache updates for better performance
  const handleChannelMessage = useCallback(
    (payload: any) => {
      const { type, data } = payload;

      // Get current state to find cache entries - use a custom thunk to access getState
      let state: any;
      dispatch((d, getState) => {
        state = getState();
        return { type: 'NOOP' };
      });

      switch (type) {
        case 'NEW_MESSAGE': {
          console.log('[WebSocket] New message received:', data);
          const cacheInfo = findMessagesCacheEntry(state, data.channelId);

          if (cacheInfo) {
            // Transform and add message directly to cache
            const transformedMessage = transformMessage(data);
            dispatch(
              messageApi.util.updateQueryData(
                'getMessages',
                { channelId: data.channelId, pagination: cacheInfo },
                (draft) => {
                  // Avoid duplicates - check if message already exists
                  const exists = draft.data?.items?.some(
                    (m) => m.id === transformedMessage.id
                  );
                  if (!exists) {
                    draft.data?.items?.push(transformedMessage);
                  }
                }
              )
            );
          }

          // Still update channel list for last message preview
          dispatch(
            discussApi.util.invalidateTags([
              { type: 'Channel', id: data.channelId },
              { type: 'Channel', id: 'LIST' },
            ])
          );

          if (onMessage) {
            onMessage(transformMessage(data));
          }
          break;
        }

        case 'MESSAGE_EDITED': {
          console.log('[WebSocket] Message edited:', data);
          const cacheInfo = findMessagesCacheEntry(state, data.channelId);

          if (cacheInfo) {
            dispatch(
              messageApi.util.updateQueryData(
                'getMessages',
                { channelId: data.channelId, pagination: cacheInfo },
                (draft) => {
                  const message = draft.data?.items?.find(
                    (m) => m.id === String(data.id)
                  );
                  if (message) {
                    message.content = data.content;
                    message.isEdited = true;
                    message.editedAt = data.editedAt || new Date().toISOString();
                  }
                }
              )
            );
          }
          break;
        }

        case 'MESSAGE_DELETED': {
          console.log('[WebSocket] Message deleted:', data);
          const cacheInfo = findMessagesCacheEntry(state, data.channelId);

          if (cacheInfo) {
            dispatch(
              messageApi.util.updateQueryData(
                'getMessages',
                { channelId: data.channelId, pagination: cacheInfo },
                (draft) => {
                  const message = draft.data?.items?.find(
                    (m) => m.id === String(data.id || data.messageId)
                  );
                  if (message) {
                    message.isDeleted = true;
                    message.content = 'This message was deleted';
                    message.deletedAt = new Date().toISOString();
                  }
                }
              )
            );
          }

          // Update channel for last message preview
          dispatch(
            discussApi.util.invalidateTags([
              { type: 'Channel', id: data.channelId },
            ])
          );
          break;
        }

        case 'REACTION_ADDED': {
          console.log('[WebSocket] Reaction added:', data);
          const cacheInfo = findMessagesCacheEntry(state, data.channelId);

          if (cacheInfo) {
            dispatch(
              messageApi.util.updateQueryData(
                'getMessages',
                { channelId: data.channelId, pagination: cacheInfo },
                (draft) => {
                  const message = draft.data?.items?.find(
                    (m) => m.id === String(data.messageId)
                  );
                  if (message) {
                    const existingReaction = message.reactions.find(
                      (r: MessageReaction) => r.emoji === data.emoji
                    );
                    if (existingReaction) {
                      if (!existingReaction.userIds.includes(String(data.userId))) {
                        existingReaction.userIds.push(String(data.userId));
                        existingReaction.count += 1;
                      }
                    } else {
                      message.reactions.push({
                        emoji: data.emoji,
                        userIds: [String(data.userId)],
                        count: 1,
                      });
                    }
                  }
                }
              )
            );
          }
          break;
        }

        case 'REACTION_REMOVED': {
          console.log('[WebSocket] Reaction removed:', data);
          const cacheInfo = findMessagesCacheEntry(state, data.channelId);

          if (cacheInfo) {
            dispatch(
              messageApi.util.updateQueryData(
                'getMessages',
                { channelId: data.channelId, pagination: cacheInfo },
                (draft) => {
                  const message = draft.data?.items?.find(
                    (m) => m.id === String(data.messageId)
                  );
                  if (message) {
                    const reactionIndex = message.reactions.findIndex(
                      (r: MessageReaction) => r.emoji === data.emoji
                    );
                    if (reactionIndex !== -1) {
                      const reaction = message.reactions[reactionIndex];
                      const userIndex = reaction.userIds.indexOf(String(data.userId));
                      if (userIndex !== -1) {
                        reaction.userIds.splice(userIndex, 1);
                        reaction.count -= 1;
                        if (reaction.count <= 0) {
                          message.reactions.splice(reactionIndex, 1);
                        }
                      }
                    }
                  }
                }
              )
            );
          }
          break;
        }

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
