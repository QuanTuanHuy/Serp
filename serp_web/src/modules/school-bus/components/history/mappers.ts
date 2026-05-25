'use client';

import type {
  SchoolBusTransportRequestHistory,
  SchoolBusSubscriptionHistory,
  SchoolBusSubscriptionPausePeriod,
  SchoolBusDemoEvent,
  SchoolBusTripHistory,
} from '../../types';
import type { TimelineEvent } from './SchoolBusTimeline';

// ─── Transport Request History ──────────────────────────────────────────────

export function mapRequestHistoryToTimeline(
  items: SchoolBusTransportRequestHistory[]
): TimelineEvent[] {
  return items.map((h) => ({
    id: h.id,
    occurredAt: h.changedAt,
    title: buildRequestTitle(h.newStatus),
    description: h.notes || undefined,
    actorName: h.changedBy ? `User #${h.changedBy}` : undefined,
    category: h.newStatus || undefined,
    status: h.newStatus || undefined,
    badgeLabel: h.newStatus || undefined,
    metadata: buildStatusTransition(h.oldStatus, h.newStatus, h.reason),
  }));
}

function buildRequestTitle(newStatus: string | null | undefined): string {
  switch (newStatus?.toUpperCase()) {
    case 'SUBMITTED': return 'Yêu cầu đã được gửi';
    case 'APPROVED':  return 'Yêu cầu đã được duyệt';
    case 'REJECTED':  return 'Yêu cầu bị từ chối';
    case 'CANCELLED': return 'Yêu cầu đã bị hủy';
    case 'DRAFT':     return 'Bản nháp đã tạo';
    default:          return `Trạng thái: ${newStatus || 'N/A'}`;
  }
}

function buildStatusTransition(
  oldStatus: string | null | undefined,
  newStatus: string | null | undefined,
  reason: string | null | undefined
): Record<string, string> | undefined {
  const meta: Record<string, string> = {};
  if (oldStatus) meta['Trạng thái cũ'] = oldStatus;
  if (newStatus) meta['Trạng thái mới'] = newStatus;
  if (reason)    meta['Lý do'] = reason;
  return Object.keys(meta).length > 0 ? meta : undefined;
}

// ─── Subscription History ───────────────────────────────────────────────────

export function mapSubscriptionHistoryToTimeline(
  items: SchoolBusSubscriptionHistory[]
): TimelineEvent[] {
  return items.map((h) => ({
    id: h.id,
    occurredAt: h.changedAt,
    title: buildSubTitle(h.changeType),
    description: h.notes || undefined,
    actorName: h.changedBy ? `User #${h.changedBy}` : undefined,
    category: h.changeType || undefined,
    status: h.newStatus || undefined,
    badgeLabel: h.newStatus || undefined,
    metadata: buildSubMeta(h),
  }));
}

function buildSubTitle(changeType: string): string {
  switch (changeType?.toUpperCase()) {
    case 'CREATED':   return 'Đăng ký mới được tạo';
    case 'CHANGED':   return 'Đăng ký đã được cập nhật';
    case 'PAUSED':    return 'Đăng ký tạm dừng';
    case 'RESUMED':   return 'Đăng ký đã khôi phục';
    case 'STOPPED':   return 'Đăng ký đã dừng';
    case 'RENEWED':   return 'Đăng ký đã gia hạn';
    case 'EXPIRED':   return 'Đăng ký đã hết hạn';
    default:          return `Thay đổi: ${changeType}`;
  }
}

function buildSubMeta(h: SchoolBusSubscriptionHistory): Record<string, string> | undefined {
  const meta: Record<string, string> = {};
  if (h.oldStatus) meta['Trạng thái cũ'] = h.oldStatus;
  if (h.newStatus) meta['Trạng thái mới'] = h.newStatus;
  if (h.oldTripOption) meta['Trip option cũ'] = h.oldTripOption;
  if (h.newTripOption) meta['Trip option mới'] = h.newTripOption;
  if (h.reason) meta['Lý do'] = h.reason;
  return Object.keys(meta).length > 0 ? meta : undefined;
}

// ─── Subscription Pause Period ──────────────────────────────────────────────

export function mapPausePeriodsToTimeline(
  items: SchoolBusSubscriptionPausePeriod[]
): TimelineEvent[] {
  return items.map((p) => ({
    id: p.id,
    occurredAt: p.createdAt || p.pauseFrom,
    title: `Tạm dừng: ${p.pauseFrom}${p.pauseTo ? ` → ${p.pauseTo}` : ''}`,
    description: p.reason || undefined,
    category: 'PAUSED',
    status: p.status,
    badgeLabel: p.status,
  }));
}

// ─── Trip History ───────────────────────────────────────────────────────────

export function mapTripHistoryToTimeline(
  items: SchoolBusTripHistory[]
): TimelineEvent[] {
  return items.map((t) => ({
    id: t.id,
    occurredAt: t.completedAt || t.startedAt || t.createdAt || '',
    title: `Chuyến ${t.routeCode || '#' + t.id}`,
    description: t.driverName ? `Tài xế: ${t.driverName}` : undefined,
    category: t.status === 'COMPLETED' ? 'COMPLETED' : 'TRIP',
    status: t.status,
    badgeLabel: t.status,
    metadata: {
      'Ngày phục vụ': t.serviceDate || 'N/A',
      'Trạng thái': t.status,
      ...(t.busPlateNumber ? { 'Biển số': t.busPlateNumber } : {}),
    },
  }));
}

// ─── Demo Events ────────────────────────────────────────────────────────────

export function mapDemoEventsToTimeline(
  items: SchoolBusDemoEvent[]
): TimelineEvent[] {
  return items.map((e) => ({
    id: e.id ?? `demo-${e.eventType}-${e.eventTime}`,
    occurredAt: e.eventTime,
    title: e.eventType || 'Demo event',
    description: undefined,
    category: 'SYSTEM',
    source: 'Demo',
    metadata: e.payloadJson ? { payload: e.payloadJson } : undefined,
  }));
}
