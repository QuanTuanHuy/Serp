import { Client, IMessage, StompSubscription } from '@stomp/stompjs';

const WS_URL =
  (process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:8080/ws') + '/school-bus';

const isDev = process.env.NODE_ENV === 'development';

export function connectSchoolBusSocket(token?: string): Client {
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

export function subscribeTripEvents(
  client: Client,
  tripId: number,
  callback: (message: any) => void
): StompSubscription | null {
  const topic = `/topic/school-bus/trips/${tripId}/events`;

  if (client.connected) {
    if (isDev) console.log('[SchoolBus WS] Subscribing trip events:', topic);
    return client.subscribe(topic, (msg: IMessage) => {
      const parsed = JSON.parse(msg.body);
      if (isDev) console.log('[SchoolBus WS] Trip event received:', parsed.eventType || parsed.action);
      callback(parsed);
    });
  }

  const prevOnConnect = client.onConnect;
  let sub: StompSubscription | null = null;
  client.onConnect = (frame) => {
    prevOnConnect?.(frame);
    if (isDev) console.log('[SchoolBus WS] Subscribing trip events (deferred):', topic);
    sub = client.subscribe(topic, (msg: IMessage) => {
      const parsed = JSON.parse(msg.body);
      if (isDev) console.log('[SchoolBus WS] Trip event received:', parsed.eventType || parsed.action);
      callback(parsed);
    });
  };
  return sub;
}
