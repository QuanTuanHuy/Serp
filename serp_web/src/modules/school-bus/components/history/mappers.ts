'use client';

import type {
  SchoolBusTransportRequestHistory,
  SchoolBusSubscriptionHistory,
  SchoolBusSubscriptionPausePeriod,
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
    case 'SUBMITTED':
      return 'Request submitted';
    case 'APPROVED':
      return 'Request approved';
    case 'REJECTED':
      return 'Request rejected';
    case 'CANCELLED':
      return 'Request cancelled';
    case 'DRAFT':
      return 'Draft created';
    default:
      return `Status: ${newStatus || 'N/A'}`;
  }
}

function buildStatusTransition(
  oldStatus: string | null | undefined,
  newStatus: string | null | undefined,
  reason: string | null | undefined
): Record<string, string> | undefined {
  const meta: Record<string, string> = {};
  if (oldStatus) meta['Previous status'] = oldStatus;
  if (newStatus) meta['New status'] = newStatus;
  if (reason) meta['Reason'] = reason;
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
    case 'CREATED':
      return 'New subscription created';
    case 'CHANGED':
      return 'Subscription updated';
    case 'PAUSED':
      return 'Subscription paused';
    case 'RESUMED':
      return 'Subscription resumed';
    case 'STOPPED':
      return 'Subscription stopped';
    case 'RENEWED':
      return 'Subscription renewed';
    case 'EXPIRED':
      return 'Subscription expired';
    default:
      return `Change: ${changeType}`;
  }
}

function buildSubMeta(
  h: SchoolBusSubscriptionHistory
): Record<string, string> | undefined {
  const meta: Record<string, string> = {};
  if (h.oldStatus) meta['Previous status'] = h.oldStatus;
  if (h.newStatus) meta['New status'] = h.newStatus;
  if (h.oldTripOption) meta['Previous trip option'] = h.oldTripOption;
  if (h.newTripOption) meta['New trip option'] = h.newTripOption;
  if (h.reason) meta['Reason'] = h.reason;
  return Object.keys(meta).length > 0 ? meta : undefined;
}

// ─── Subscription Pause Period ──────────────────────────────────────────────

export function mapPausePeriodsToTimeline(
  items: SchoolBusSubscriptionPausePeriod[]
): TimelineEvent[] {
  return items.map((p) => ({
    id: p.id,
    occurredAt: p.createdAt || p.pauseFrom,
    title: `Paused: ${p.pauseFrom}${p.pauseTo ? ` → ${p.pauseTo}` : ''}`,
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
    title: `Trip ${t.routeCode || '#' + t.id}`,
    description: t.driverName ? `Driver: ${t.driverName}` : undefined,
    category: t.status === 'COMPLETED' ? 'COMPLETED' : 'TRIP',
    status: t.status,
    badgeLabel: t.status,
    metadata: {
      'Service date': t.serviceDate || 'N/A',
      Status: t.status,
      ...(t.busPlateNumber ? { 'Plate number': t.busPlateNumber } : {}),
    },
  }));
}
