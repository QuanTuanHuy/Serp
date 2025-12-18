/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket actions
 */

import { createAction } from '@reduxjs/toolkit';

export const wsConnect = createAction('websocket/connect');
export const wsDisconnect = createAction('websocket/disconnect');
export const wsConnected = createAction('websocket/connected');
export const wsDisconnected = createAction('websocket/disconnected');
export const wsMessageReceived = createAction<any>('websocket/messageReceived');
export const wsError = createAction<string>('websocket/error');
