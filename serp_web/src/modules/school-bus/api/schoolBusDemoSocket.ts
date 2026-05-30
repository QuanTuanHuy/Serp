/**
 * WebSocket helper for School Bus Demo Session real-time updates.
 * Uses @stomp/stompjs (already a project dependency).
 *
 * Topics:
 * - /topic/school-bus/demo-sessions/{id}/position  → bus position updates
 * - /topic/school-bus/demo-sessions/{id}/events    → event feed (append)
 * - /topic/school-bus/trips/{tripId}/demo          → trip-level demo state
 */

import { Client, IMessage, StompSubscription } from '@stomp/stompjs';

// ─── Types ────────────────────────────────────────────────────────────

export interface DemoPositionMessage {
  demoSessionId: number;
  tripId: number;
  tripCode: string;
  routeId: number | null;
  routeCode: string | null;
  status: string;
  progressPercent: number;
  currentLatitude: number | null;
  currentLongitude: number | null;
  currentStopOrder: number | null;
  lastTickAt: string | null;
  lastEventType: string | null;
  eventType: string;
  timestamp: string;
}

export interface DemoEventMessage {
  demoSessionId: number;
  tripId: number;
  eventType: string;
  eventTime: string;
  payloadJson: string | null;
  progressPercent: number;
  currentLatitude: number | null;
  currentLongitude: number | null;
  currentStopOrder: number | null;
}

// ─── Connection ───────────────────────────────────────────────────────

const WS_URL =
  (process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:8080/ws') + '/school-bus';

const isDev = process.env.NODE_ENV === 'development';

export function connectSchoolBusDemoSocket(token?: string): Client {
  const client = new Client({
    brokerURL: WS_URL,
    connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    debug: isDev ? (str) => console.debug('[SchoolBus WS]', str) : undefined,
  });

  client.onConnect = () => {
    if (isDev) console.log('[SchoolBus WS] Connected to', WS_URL);
  };

  client.onStompError = (frame) => {
    if (isDev) console.error('[SchoolBus WS] STOMP error:', frame.headers['message']);
  };

  client.activate();
  return client;
}

export function disconnectDemoSocket(client: Client): void {
  if (client.active) {
    client.deactivate();
  }
}

// ─── Subscriptions ────────────────────────────────────────────────────

export function subscribeDemoPosition(
  client: Client,
  demoSessionId: number,
  callback: (message: DemoPositionMessage) => void
): StompSubscription | null {
  const topic = `/topic/school-bus/demo-sessions/${demoSessionId}/position`;

  if (client.connected) {
    if (isDev) console.log('[SchoolBus WS] Subscribing position:', topic);
    return client.subscribe(topic, (msg: IMessage) => {
      const parsed = JSON.parse(msg.body) as DemoPositionMessage;
      if (isDev) console.log('[SchoolBus WS] Position received:', parsed.status, parsed.progressPercent + '%');
      callback(parsed);
    });
  }

  // Not connected yet — subscribe when connected
  const prevOnConnect = client.onConnect;
  let sub: StompSubscription | null = null;
  client.onConnect = (frame) => {
    prevOnConnect?.(frame);
    if (isDev) console.log('[SchoolBus WS] Subscribing position (deferred):', topic);
    sub = client.subscribe(topic, (msg: IMessage) => {
      const parsed = JSON.parse(msg.body) as DemoPositionMessage;
      if (isDev) console.log('[SchoolBus WS] Position received:', parsed.status, parsed.progressPercent + '%');
      callback(parsed);
    });
  };
  return sub;
}

export function subscribeDemoEvents(
  client: Client,
  demoSessionId: number,
  callback: (message: DemoEventMessage) => void
): StompSubscription | null {
  const topic = `/topic/school-bus/demo-sessions/${demoSessionId}/events`;

  if (client.connected) {
    if (isDev) console.log('[SchoolBus WS] Subscribing events:', topic);
    return client.subscribe(topic, (msg: IMessage) => {
      const parsed = JSON.parse(msg.body) as DemoEventMessage;
      if (isDev) console.log('[SchoolBus WS] Event received:', parsed.eventType);
      callback(parsed);
    });
  }

  const prevOnConnect = client.onConnect;
  let sub: StompSubscription | null = null;
  client.onConnect = (frame) => {
    prevOnConnect?.(frame);
    if (isDev) console.log('[SchoolBus WS] Subscribing events (deferred):', topic);
    sub = client.subscribe(topic, (msg: IMessage) => {
      const parsed = JSON.parse(msg.body) as DemoEventMessage;
      if (isDev) console.log('[SchoolBus WS] Event received:', parsed.eventType);
      callback(parsed);
    });
  };
  return sub;
}

// TODO Phase 6: auto-advance stop lifecycle when demo reaches a stop.
// TODO Phase 6: auto-attendance when autoAttendance is enabled.
