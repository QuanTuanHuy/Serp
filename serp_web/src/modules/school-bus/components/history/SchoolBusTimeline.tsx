'use client';

import * as React from 'react';
import {
  Ban,
  BusFront,
  CheckCircle2,
  ChevronDown,
  ChevronUp,
  FilePlus,
  Flag,
  MapPinned,
  PauseCircle,
  Pencil,
  PlayCircle,
  RefreshCw,
  Route,
  Settings,
  XCircle,
  type LucideIcon,
} from 'lucide-react';
import { SchoolBusStatusBadge } from '../SchoolBusStatusBadge';
import { schoolBusUi } from '../../theme';

// ─── Types ──────────────────────────────────────────────────────────────────

export interface TimelineEvent {
  id: string | number;
  occurredAt: string;
  title: string;
  description?: string;
  actorName?: string;
  actorRole?: string;
  source?: string;
  /** Used for icon/color mapping */
  category?: string;
  status?: string;
  badgeLabel?: string;
  /** Extra key-value pairs rendered in an expandable section */
  metadata?: Record<string, string | number | boolean | null | undefined>;
}

export type TimelineMode = 'full' | 'compact';

// ─── Icon / colour mapping ─────────────────────────────────────────────────

interface EventStyle {
  icon: LucideIcon;
  ringColor: string;
  bgColor: string;
  iconColor: string;
}

const EVENT_STYLES: Record<string, EventStyle> = {
  CREATE:  { icon: FilePlus,     ringColor: 'ring-blue-200',    bgColor: 'bg-blue-50',    iconColor: 'text-blue-600' },
  CREATED: { icon: FilePlus,     ringColor: 'ring-blue-200',    bgColor: 'bg-blue-50',    iconColor: 'text-blue-600' },
  UPDATE:  { icon: Pencil,       ringColor: 'ring-violet-200',  bgColor: 'bg-violet-50',  iconColor: 'text-violet-600' },
  CHANGED: { icon: Pencil,       ringColor: 'ring-violet-200',  bgColor: 'bg-violet-50',  iconColor: 'text-violet-600' },
  APPROVE: { icon: CheckCircle2, ringColor: 'ring-emerald-200', bgColor: 'bg-emerald-50', iconColor: 'text-emerald-600' },
  APPROVED:{ icon: CheckCircle2, ringColor: 'ring-emerald-200', bgColor: 'bg-emerald-50', iconColor: 'text-emerald-600' },
  REJECT:  { icon: XCircle,      ringColor: 'ring-rose-200',    bgColor: 'bg-rose-50',    iconColor: 'text-rose-600' },
  REJECTED:{ icon: XCircle,      ringColor: 'ring-rose-200',    bgColor: 'bg-rose-50',    iconColor: 'text-rose-600' },
  CANCEL:  { icon: XCircle,      ringColor: 'ring-rose-200',    bgColor: 'bg-rose-50',    iconColor: 'text-rose-600' },
  CANCELLED:{ icon: XCircle,     ringColor: 'ring-rose-200',    bgColor: 'bg-rose-50',    iconColor: 'text-rose-600' },
  PAUSE:   { icon: PauseCircle,  ringColor: 'ring-amber-200',   bgColor: 'bg-amber-50',   iconColor: 'text-amber-600' },
  PAUSED:  { icon: PauseCircle,  ringColor: 'ring-amber-200',   bgColor: 'bg-amber-50',   iconColor: 'text-amber-600' },
  RESUME:  { icon: PlayCircle,   ringColor: 'ring-emerald-200', bgColor: 'bg-emerald-50', iconColor: 'text-emerald-600' },
  RESUMED: { icon: PlayCircle,   ringColor: 'ring-emerald-200', bgColor: 'bg-emerald-50', iconColor: 'text-emerald-600' },
  STOP:    { icon: Ban,          ringColor: 'ring-rose-200',    bgColor: 'bg-rose-50',    iconColor: 'text-rose-600' },
  STOPPED: { icon: Ban,          ringColor: 'ring-rose-200',    bgColor: 'bg-rose-50',    iconColor: 'text-rose-600' },
  RENEW:   { icon: RefreshCw,    ringColor: 'ring-blue-200',    bgColor: 'bg-blue-50',    iconColor: 'text-blue-600' },
  RENEWED: { icon: RefreshCw,    ringColor: 'ring-blue-200',    bgColor: 'bg-blue-50',    iconColor: 'text-blue-600' },
  ASSIGN:  { icon: BusFront,     ringColor: 'ring-sky-200',     bgColor: 'bg-sky-50',     iconColor: 'text-sky-600' },
  ASSIGNED:{ icon: BusFront,     ringColor: 'ring-sky-200',     bgColor: 'bg-sky-50',     iconColor: 'text-sky-600' },
  COMPLETE:{ icon: Flag,         ringColor: 'ring-emerald-200', bgColor: 'bg-emerald-50', iconColor: 'text-emerald-600' },
  COMPLETED:{ icon: Flag,        ringColor: 'ring-emerald-200', bgColor: 'bg-emerald-50', iconColor: 'text-emerald-600' },
  TRIP:    { icon: MapPinned,    ringColor: 'ring-emerald-200', bgColor: 'bg-emerald-50', iconColor: 'text-emerald-600' },
  ROUTE:   { icon: Route,        ringColor: 'ring-sky-200',     bgColor: 'bg-sky-50',     iconColor: 'text-sky-600' },
  SYSTEM:  { icon: Settings,     ringColor: 'ring-slate-200',   bgColor: 'bg-slate-50',   iconColor: 'text-slate-500' },
};

const DEFAULT_STYLE: EventStyle = {
  icon: Settings,
  ringColor: 'ring-slate-200',
  bgColor: 'bg-slate-50',
  iconColor: 'text-slate-500',
};

function resolveStyle(event: TimelineEvent): EventStyle {
  const key = (event.category || event.status || event.title || '').toUpperCase().replace(/[\s_-]+/g, '_');
  // Try exact match first, then try each word
  if (EVENT_STYLES[key]) return EVENT_STYLES[key];
  for (const word of key.split('_')) {
    if (EVENT_STYLES[word]) return EVENT_STYLES[word];
  }
  return DEFAULT_STYLE;
}

// ─── Date helpers ───────────────────────────────────────────────────────────

function formatTime(iso: string): string {
  try {
    return new Date(iso).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
  } catch {
    return iso;
  }
}

function formatFullDateTime(iso: string): string {
  try {
    return new Date(iso).toLocaleString('en-US', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  } catch {
    return iso;
  }
}

function dayLabel(iso: string): string {
  try {
    const d = new Date(iso);
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const target = new Date(d.getFullYear(), d.getMonth(), d.getDate());
    const diff = (today.getTime() - target.getTime()) / 86400000;
    if (diff === 0) return 'Today';
    if (diff === 1) return 'Yesterday';
    return d.toLocaleDateString('en-US', { weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric' });
  } catch {
    return iso;
  }
}

function groupByDay(events: TimelineEvent[]): { label: string; events: TimelineEvent[] }[] {
  const groups: Record<string, TimelineEvent[]> = {};
  for (const e of events) {
    const key = new Date(e.occurredAt).toDateString();
    if (!groups[key]) groups[key] = [];
    groups[key].push(e);
  }
  return Object.entries(groups).map(([, items]) => ({
    label: dayLabel(items[0].occurredAt),
    events: items,
  }));
}

// ─── Timeline Item ──────────────────────────────────────────────────────────

function TimelineNode({ style }: { style: EventStyle }) {
  const Icon = style.icon;
  return (
    <div className={`relative z-10 flex h-9 w-9 shrink-0 items-center justify-center rounded-full ring-4 ${style.ringColor} ${style.bgColor} shadow-sm`}>
      <Icon className={`h-4.5 w-4.5 ${style.iconColor}`} />
    </div>
  );
}

function TimelineCard({
  event,
  mode,
}: {
  event: TimelineEvent;
  mode: TimelineMode;
}) {
  const [expanded, setExpanded] = React.useState(false);
  const style = resolveStyle(event);
  const hasMeta = event.metadata && Object.keys(event.metadata).length > 0;

  if (mode === 'compact') {
    return (
      <div className='flex items-start gap-3'>
        <TimelineNode style={style} />
        <div className='min-w-0 flex-1 pt-1'>
          <div className='flex flex-wrap items-center gap-2'>
            <span className='text-sm font-semibold text-slate-900'>{event.title}</span>
            {event.badgeLabel && <SchoolBusStatusBadge status={event.badgeLabel} />}
            {event.status && !event.badgeLabel && <SchoolBusStatusBadge status={event.status} />}
          </div>
          {event.description && (
            <p className='mt-0.5 text-xs leading-5 text-slate-500'>{event.description}</p>
          )}
          <p className='mt-1 text-[11px] text-slate-400'>
            {formatFullDateTime(event.occurredAt)}
            {event.actorName ? ` · ${event.actorName}` : ''}
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className='group flex gap-4'>
      {/* Time column — hidden on mobile */}
      <div className='hidden w-[100px] shrink-0 pt-2.5 text-right md:block'>
        <p className='text-xs font-medium text-slate-500'>{formatTime(event.occurredAt)}</p>
      </div>

      {/* Node */}
      <div className='relative flex flex-col items-center'>
        <TimelineNode style={style} />
        {/* Vertical connector is drawn by the parent */}
      </div>

      {/* Card */}
      <div className='mb-6 min-w-0 flex-1'>
        <div className='rounded-2xl border border-slate-200 bg-white p-4 shadow-[0_8px_24px_rgba(15,23,42,0.04)] transition group-hover:border-rose-200 group-hover:shadow-[0_12px_32px_rgba(225,29,72,0.08)]'>
          <div className='flex flex-wrap items-start justify-between gap-2'>
            <div className='flex flex-wrap items-center gap-2'>
              <span className='text-sm font-semibold text-slate-900'>{event.title}</span>
              {event.badgeLabel && <SchoolBusStatusBadge status={event.badgeLabel} />}
              {event.status && !event.badgeLabel && <SchoolBusStatusBadge status={event.status} />}
            </div>
            <p className='text-[11px] text-slate-400 md:hidden'>{formatFullDateTime(event.occurredAt)}</p>
          </div>

          {event.description && (
            <p className='mt-1.5 text-sm leading-6 text-slate-600'>{event.description}</p>
          )}

          <div className='mt-2 flex flex-wrap items-center gap-3 text-xs text-slate-400'>
            {event.actorName && (
              <span>
                👤 {event.actorName}
                {event.actorRole ? ` (${event.actorRole})` : ''}
              </span>
            )}
            {event.source && <span>📌 {event.source}</span>}
          </div>

          {hasMeta && (
            <div className='mt-2'>
              <button
                type='button'
                onClick={() => setExpanded(!expanded)}
                className='inline-flex items-center gap-1 text-xs font-medium text-rose-600 hover:text-rose-700'
              >
                {expanded ? 'Hide details' : 'Show details'}
                {expanded ? <ChevronUp className='h-3 w-3' /> : <ChevronDown className='h-3 w-3' />}
              </button>
              {expanded && (
                <div className='mt-2 space-y-1 rounded-xl border border-slate-100 bg-slate-50/80 p-3'>
                  {Object.entries(event.metadata!).map(([k, v]) =>
                    v != null ? (
                      <div key={k} className='flex gap-2 text-xs'>
                        <span className='font-medium text-slate-600'>{k}:</span>
                        <span className='text-slate-500'>{String(v)}</span>
                      </div>
                    ) : null
                  )}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

// ─── Empty / Loading / Error states ─────────────────────────────────────────

function TimelineEmpty() {
  return (
    <div className='flex flex-col items-center justify-center py-12 text-center'>
      <div className='flex h-14 w-14 items-center justify-center rounded-full bg-slate-100'>
        <Settings className='h-6 w-6 text-slate-400' />
      </div>
      <p className='mt-4 text-sm font-semibold text-slate-700'>No history available</p>
      <p className='mt-1 text-xs text-slate-400'>Events will appear when changes occur.</p>
    </div>
  );
}

function TimelineLoading() {
  return (
    <div className='space-y-6'>
      {[1, 2, 3].map((i) => (
        <div key={i} className='flex gap-4 animate-pulse'>
          <div className='hidden w-[100px] md:block'>
            <div className='ml-auto h-3 w-14 rounded bg-slate-200' />
          </div>
          <div className='flex flex-col items-center'>
            <div className='h-9 w-9 rounded-full bg-slate-200' />
          </div>
          <div className='flex-1'>
            <div className='rounded-2xl border border-slate-100 bg-slate-50 p-4'>
              <div className='h-4 w-2/3 rounded bg-slate-200' />
              <div className='mt-2 h-3 w-1/2 rounded bg-slate-200' />
              <div className='mt-3 h-2.5 w-1/4 rounded bg-slate-200' />
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

function TimelineError({ message }: { message?: string }) {
  return (
    <div className='flex flex-col items-center justify-center py-12 text-center'>
      <div className='flex h-14 w-14 items-center justify-center rounded-full bg-rose-100'>
        <XCircle className='h-6 w-6 text-rose-500' />
      </div>
      <p className='mt-4 text-sm font-semibold text-rose-700'>Failed to load history</p>
      <p className='mt-1 text-xs text-slate-400'>{message || 'Please try again later.'}</p>
    </div>
  );
}

// ─── Main timeline component ────────────────────────────────────────────────

export interface SchoolBusTimelineProps {
  events: TimelineEvent[];
  mode?: TimelineMode;
  isLoading?: boolean;
  isError?: boolean;
  errorMessage?: string;
  /** Group events by day — default true for full mode */
  groupByDate?: boolean;
  className?: string;
  maxHeight?: string;
}

export function SchoolBusTimeline({
  events,
  mode = 'full',
  isLoading = false,
  isError = false,
  errorMessage,
  groupByDate,
  className = '',
  maxHeight,
}: SchoolBusTimelineProps) {
  if (isLoading) return <TimelineLoading />;
  if (isError) return <TimelineError message={errorMessage} />;
  if (events.length === 0) return <TimelineEmpty />;

  const shouldGroup = groupByDate ?? mode === 'full';
  const containerStyle = maxHeight ? { maxHeight, overflowY: 'auto' as const } : {};

  if (mode === 'compact') {
    return (
      <div className={`relative space-y-4 ${className}`} style={containerStyle}>
        {/* Vertical connector line */}
        <div className='absolute left-[17px] top-0 bottom-0 w-px bg-slate-200' />
        {events.map((event) => (
          <TimelineCard key={event.id} event={event} mode='compact' />
        ))}
      </div>
    );
  }

  // Full mode
  if (!shouldGroup) {
    return (
      <div className={`relative ${className}`} style={containerStyle}>
        {/* Vertical connector */}
        <div className='absolute left-[calc(100px+18px)] top-0 bottom-0 hidden w-px bg-slate-200 md:block' />
        <div className='absolute left-[17px] top-0 bottom-0 w-px bg-slate-200 md:hidden' />
        {events.map((event) => (
          <TimelineCard key={event.id} event={event} mode='full' />
        ))}
      </div>
    );
  }

  const groups = groupByDay(events);

  return (
    <div className={`space-y-6 ${className}`} style={containerStyle}>
      {groups.map((group) => (
        <div key={group.label}>
          <div className='mb-3 flex items-center gap-3'>
            <div className='h-px flex-1 bg-slate-200' />
            <span className='shrink-0 rounded-full border border-slate-200 bg-white px-3 py-1 text-[11px] font-semibold uppercase tracking-wide text-slate-500 shadow-sm'>
              {group.label}
            </span>
            <div className='h-px flex-1 bg-slate-200' />
          </div>
          <div className='relative'>
            {/* Vertical connector */}
            <div className='absolute left-[calc(100px+18px)] top-0 bottom-0 hidden w-px bg-slate-200 md:block' />
            <div className='absolute left-[17px] top-0 bottom-0 w-px bg-slate-200 md:hidden' />
            {group.events.map((event) => (
              <TimelineCard key={event.id} event={event} mode='full' />
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
