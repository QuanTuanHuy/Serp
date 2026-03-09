/*
Author: QuanTuanHuy
Description: Part of Serp Project - WebSocket hook for real-time discuss functionality
Architecture: Single subscription to /user/queue/events with server-side fan-out
*/

'use client';

import { useEffect, useRef, useCallback, useMemo, useState } from 'react';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { useAppSelector, useAppDispatch } from '@/lib/store/hooks';
import { toast } from 'sonner';
import { messageApi } from '../api/messages.api';
import { discussApi } from '../api/discussApi';
import type { Message, MessageReaction, WsEvent } from '../types';
import { transformMessage } from '../api/transformers';

// Helper to find cache entries for a channel - matches the one in messages.api.ts
const findMessagesCacheEntry = (
  state: any,
  channelId: string | number
): { page: number; limit: number } | undefined => {
  const queries = state.api?.queries;
  if (!queries) return undefined;

  const normalizedChannelId = String(channelId);

  for (const key of Object.keys(queries)) {
    if (key.startsWith('getMessages(')) {
      const entry = queries[key];
      if (entry?.originalArgs?.channelId === normalizedChannelId) {
        return {
          page: entry.originalArgs.pagination.page,
          limit: entry.originalArgs.pagination.limit,
        };
      }
    }
  }
  return undefined;
};

export const useDiscussWebSocket = () => {
  const dispatch = useAppDispatch();
  const clientRef = useRef<Client | null>(null);
  const subscriptionRef = useRef<StompSubscription | null>(null);

  const [isConnected, setIsConnected] = useState(false);

  // Channel and callback refs - updated via setter functions
  const channelIdRef = useRef<string | undefined>(undefined);
  const onMessageRef = useRef<((msg: Message) => void) | undefined>(undefined);
  const onTypingUpdateRef = useRef<
    ((userId: string, userName: string, isTyping: boolean) => void) | undefined
  >(undefined);
  const onUserStatusUpdateRef = useRef<
    ((userId: string, isOnline: boolean) => void) | undefined
  >(undefined);
  const onErrorRef = useRef<((error: any) => void) | undefined>(undefined);

  // Setter functions for ChatWindow to register channel-specific behavior
  const setActiveChannel = useCallback((channelId: string | undefined) => {
    channelIdRef.current = channelId;
  }, []);

  const setOnMessage = useCallback(
    (cb: ((msg: Message) => void) | undefined) => {
      onMessageRef.current = cb;
    },
    []
  );

  const setOnTypingUpdate = useCallback(
    (
      cb:
        | ((userId: string, userName: string, isTyping: boolean) => void)
        | undefined
    ) => {
      onTypingUpdateRef.current = cb;
    },
    []
  );

  const setOnError = useCallback((cb: ((error: any) => void) | undefined) => {
    onErrorRef.current = cb;
  }, []);

  const token = useAppSelector((state) => state.account.auth?.token);
  const wsUrl =
    (process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:8080/ws') + '/discuss';

  // Event handler - uses refs to avoid stale closures
  const handleEventRef = useRef<(event: WsEvent) => void>(() => {});

  // Keep handleEvent updated with latest dispatch
  useEffect(() => {
    handleEventRef.current = (event: WsEvent) => {
      const { type, payload: data } = event;

      // Get current state for cache lookups
      let state: any;
      dispatch((_, getState) => {
        state = getState();
        return { type: 'NOOP' };
      });

      const activeChannelId = channelIdRef.current;

      switch (type) {
        case 'MESSAGE_NEW': {
          console.log('[WebSocket] MESSAGE_NEW received:', data);

          if (!data.message) {
            console.error('[WebSocket] MESSAGE_NEW missing message object');
            break;
          }

          // RTK Query cache update - applies to any channel
          const cacheInfo = findMessagesCacheEntry(state, data.channelId);

          if (cacheInfo) {
            const transformedMessage = transformMessage(data.message);
            const normalizedChannelId = String(data.channelId);

            dispatch(
              messageApi.util.updateQueryData(
                'getMessages',
                { channelId: normalizedChannelId, pagination: cacheInfo },
                (draft) => {
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

          // Invalidate channel list for last message preview
          dispatch(
            discussApi.util.invalidateTags([
              { type: 'Channel', id: data.channelId },
              { type: 'Channel', id: 'LIST' },
            ])
          );

          // Callback only for the active channel
          if (
            activeChannelId &&
            String(data.channelId) === activeChannelId &&
            onMessageRef.current
          ) {
            onMessageRef.current(transformMessage(data.message));
          }
          break;
        }

        case 'MESSAGE_UPDATED': {
          console.log('[WebSocket] MESSAGE_UPDATED received:', data);

          if (!data.message || !data.messageId) {
            console.error(
              '[WebSocket] MESSAGE_UPDATED missing required fields'
            );
            break;
          }

          const cacheInfo = findMessagesCacheEntry(state, data.channelId);

          if (cacheInfo) {
            const normalizedChannelId = String(data.channelId);
            dispatch(
              messageApi.util.updateQueryData(
                'getMessages',
                { channelId: normalizedChannelId, pagination: cacheInfo },
                (draft) => {
                  const message = draft.data?.items?.find(
                    (m) => m.id === String(data.messageId)
                  );
                  if (message && data.message) {
                    message.content = data.message.content;
                    message.isEdited = true;
                    message.editedAt =
                      data.message.editedAt || new Date().toISOString();
                  }
                }
              )
            );
          }
          break;
        }

        case 'MESSAGE_DELETED': {
          console.log('[WebSocket] MESSAGE_DELETED received:', data);

          if (!data.messageId) {
            console.error('[WebSocket] MESSAGE_DELETED missing messageId');
            break;
          }

          const cacheInfo = findMessagesCacheEntry(state, data.channelId);

          if (cacheInfo) {
            const normalizedChannelId = String(data.channelId);
            dispatch(
              messageApi.util.updateQueryData(
                'getMessages',
                { channelId: normalizedChannelId, pagination: cacheInfo },
                (draft) => {
                  const message = draft.data?.items?.find(
                    (m) => m.id === String(data.messageId)
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
            const normalizedChannelId = String(data.channelId);
            dispatch(
              messageApi.util.updateQueryData(
                'getMessages',
                { channelId: normalizedChannelId, pagination: cacheInfo },
                (draft) => {
                  const message = draft.data?.items?.find(
                    (m) => m.id === String(data.messageId)
                  );
                  if (message) {
                    const existingReaction = message.reactions.find(
                      (r: MessageReaction) => r.emoji === data.emoji
                    );
                    if (existingReaction) {
                      if (
                        !existingReaction.userIds.includes(String(data.userId))
                      ) {
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
            const normalizedChannelId = String(data.channelId);
            dispatch(
              messageApi.util.updateQueryData(
                'getMessages',
                { channelId: normalizedChannelId, pagination: cacheInfo },
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
                      const userIndex = reaction.userIds.indexOf(
                        String(data.userId)
                      );
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

        case 'TYPING_START':
        case 'TYPING_STOP': {
          console.log('[WebSocket] Typing event:', type, data);
          // Only notify for the active channel
          if (
            activeChannelId &&
            String(data.channelId) === activeChannelId &&
            onTypingUpdateRef.current
          ) {
            onTypingUpdateRef.current(
              String(data.userId),
              String(data.userName || ''),
              type === 'TYPING_START'
            );
          }
          break;
        }

        case 'USER_ONLINE':
        case 'USER_OFFLINE': {
          console.log('[WebSocket] User status update:', type, data);
          if (onUserStatusUpdateRef.current) {
            onUserStatusUpdateRef.current(
              String(data.userId),
              type === 'USER_ONLINE'
            );
          }
          dispatch(discussApi.util.invalidateTags(['Presence']));
          break;
        }

        case 'USER_PRESENCE_CHANGED': {
          console.log('[WebSocket] User presence changed:', data);
          if (onUserStatusUpdateRef.current) {
            onUserStatusUpdateRef.current(String(data.userId), data.online);
          }
          dispatch(discussApi.util.invalidateTags(['Presence']));
          break;
        }

        case 'CHANNEL_CREATED':
        case 'CHANNEL_ARCHIVED': {
          console.log('[WebSocket] Channel event:', type, data);
          dispatch(
            discussApi.util.invalidateTags([{ type: 'Channel', id: 'LIST' }])
          );
          break;
        }

        case 'CHANNEL_UPDATED': {
          console.log('[WebSocket] Channel updated:', data);
          dispatch(
            discussApi.util.invalidateTags([
              { type: 'Channel', id: data.channelId },
              { type: 'Channel', id: 'LIST' },
            ])
          );
          break;
        }

        case 'MEMBER_JOINED':
        case 'MEMBER_LEFT':
        case 'MEMBER_REMOVED':
        case 'MEMBER_ROLE_CHANGED': {
          console.log('[WebSocket] Member event:', type, data);
          dispatch(
            discussApi.util.invalidateTags([
              { type: 'Channel', id: String(data.channelId) },
            ])
          );
          break;
        }

        case 'MESSAGE_READ': {
          console.log('[WebSocket] Message read:', data);
          dispatch(
            discussApi.util.invalidateTags([
              { type: 'Channel', id: String(data.channelId) },
            ])
          );
          break;
        }

        case 'ERROR': {
          console.error('[WebSocket] Received error event:', data);
          toast.error(data.message || 'An error occurred');
          if (onErrorRef.current) {
            onErrorRef.current(data);
          }
          break;
        }

        default:
          console.warn('[WebSocket] Unknown event type:', type);
      }
    };
  }, [dispatch]);

  // Connect to WebSocket server and subscribe to single event queue
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
      console.log('[WebSocket] Connected successfully');
      clientRef.current = client;
      setIsConnected(true);

      // Single subscription to personal event queue
      try {
        const sub = client.subscribe(
          '/user/queue/events',
          (message: IMessage) => {
            try {
              const event: WsEvent = JSON.parse(message.body);
              handleEventRef.current(event);
            } catch (e) {
              console.error('[WebSocket] Failed to parse event:', e);
            }
          }
        );
        subscriptionRef.current = sub;
        console.log('[WebSocket] Subscribed to /user/queue/events');
      } catch (e) {
        console.error('[WebSocket] Failed to subscribe:', e);
      }
    };

    client.onStompError = (frame) => {
      console.error('[WebSocket] STOMP error:', frame.headers['message']);
      console.error('[WebSocket] Error details:', frame.body);
      setIsConnected(false);
    };

    client.onWebSocketClose = () => {
      console.log('[WebSocket] Connection closed');
      setIsConnected(false);
    };

    client.onWebSocketError = (event) => {
      console.error('[WebSocket] WebSocket error:', event);
    };

    client.activate();

    return () => {
      console.log('[WebSocket] Disconnecting...');
      if (subscriptionRef.current) {
        subscriptionRef.current.unsubscribe();
        subscriptionRef.current = null;
      }
      client.deactivate();
      clientRef.current = null;
    };
  }, [token, wsUrl]);

  // Send typing indicator to active channel
  const sendTypingIndicator = useCallback((isTyping: boolean) => {
    const client = clientRef.current;
    const channelId = channelIdRef.current;
    if (!client || !client.connected || !channelId) {
      return;
    }

    try {
      client.publish({
        destination: `/app/channels/${channelId}/typing`,
        body: JSON.stringify({ isTyping }),
      });
    } catch (error) {
      console.error('[WebSocket] Failed to send typing indicator:', error);
    }
  }, []);

  // Mark messages as read in active channel
  const markAsRead = useCallback((messageId: string) => {
    const client = clientRef.current;
    const channelId = channelIdRef.current;
    if (!client || !client.connected || !channelId) {
      return;
    }

    try {
      client.publish({
        destination: `/app/channels/${channelId}/read`,
        body: JSON.stringify({ messageId }),
      });
    } catch (error) {
      console.error('[WebSocket] Failed to mark as read:', error);
    }
  }, []);

  // Send message via WebSocket to active channel
  const sendMessage = useCallback((content: string, parentId?: string) => {
    const client = clientRef.current;
    const channelId = channelIdRef.current;
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
  }, []);

  // Return stable API object
  const api = useMemo(
    () => ({
      isConnected,
      sendMessage,
      sendTypingIndicator,
      markAsRead,
      setActiveChannel,
      setOnMessage,
      setOnTypingUpdate,
      setOnError,
    }),
    [
      isConnected,
      sendMessage,
      sendTypingIndicator,
      markAsRead,
      setActiveChannel,
      setOnMessage,
      setOnTypingUpdate,
      setOnError,
    ]
  );

  return api;
};
