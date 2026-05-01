// ActivityCalendarView Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import React, { useCallback, useMemo, useState, useRef } from 'react';
import {
  Calendar,
  momentLocalizer,
  View,
  Views,
  EventProps,
  SlotInfo,
} from 'react-big-calendar';
import withDragAndDrop from 'react-big-calendar/lib/addons/dragAndDrop';
import moment from 'moment';
import 'react-big-calendar/lib/css/react-big-calendar.css';
import 'react-big-calendar/lib/addons/dragAndDrop/styles.css';
import './calendar.css';
import { toast } from 'sonner';
import { useRouter } from 'next/navigation';
import {
  Phone,
  Mail,
  Users,
  CheckCircle2,
  Clock,
  MapPin,
  User,
  ExternalLink,
  Check,
  X,
  LinkIcon,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { Button } from '@/shared/components/ui/button';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui/popover';
import { Badge } from '@/shared/components/ui/badge';
import {
  useGetUpcomingActivitiesQuery,
  useRescheduleActivityMutation,
  useUpdateActivityMutation,
  useCompleteActivityMutation,
  useCancelActivityMutation,
} from '../../api/crmApi';
import { ActivityEventCard } from './ActivityEventCard';
import type {
  Activity,
  ActivityType,
  ActivityStatus,
  Priority,
} from '../../types';

const localizer = momentLocalizer(moment);
const DnDCalendar = withDragAndDrop<CRMCalendarEvent, object>(Calendar);

const DEFAULT_DURATION_MINUTES = 60;
const CALENDAR_QUERY_DATE_FORMAT = 'YYYY-MM-DDTHH:mm:ss.SSS';

// ── Types ───────────────────────────────────────────────────────
interface CRMCalendarEvent {
  id: string;
  title: string;
  start: Date;
  end: Date;
  allDay: boolean;
  resource: Activity;
}

interface ActivityCalendarViewProps {
  className?: string;
  date: Date;
  view: View;
  onNavigate: (date: Date) => void;
  onViewChange: (view: View) => void;
  onCreateActivity?: (slotInfo: {
    start: Date;
    end: Date;
    allDay: boolean;
  }) => void;
  filters?: {
    types?: ActivityType[];
    statuses?: ActivityStatus[];
    priorities?: Priority[];
  };
}

// ── Style helpers ───────────────────────────────────────────────
const EVENT_COLORS: Record<
  string,
  { bg: string; border: string; text: string; darkBg: string; darkText: string }
> = {
  CALL: {
    bg: '#dbeafe',
    border: '#3b82f6',
    text: '#1e3a8a',
    darkBg: 'rgba(59, 130, 246, 0.18)',
    darkText: '#93c5fd',
  },
  MEETING: {
    bg: '#dcfce7',
    border: '#22c55e',
    text: '#14532d',
    darkBg: 'rgba(34, 197, 94, 0.18)',
    darkText: '#86efac',
  },
  EMAIL: {
    bg: '#f3e8ff',
    border: '#a855f7',
    text: '#581c87',
    darkBg: 'rgba(168, 85, 247, 0.18)',
    darkText: '#d8b4fe',
  },
  TASK: {
    bg: '#fef3c7',
    border: '#f59e0b',
    text: '#78350f',
    darkBg: 'rgba(245, 158, 11, 0.18)',
    darkText: '#fcd34d',
  },
};

const STATUS_COLORS: Record<string, string> = {
  PLANNED: 'bg-blue-100 text-blue-700',
  COMPLETED: 'bg-green-100 text-green-700',
  IN_PROGRESS: 'bg-amber-100 text-amber-700',
  CANCELLED: 'bg-gray-100 text-gray-600',
  OVERDUE: 'bg-red-100 text-red-700',
};

const ACTIVITY_ICON_MAP: Record<string, React.ElementType> = {
  CALL: Phone,
  EMAIL: Mail,
  MEETING: Users,
  TASK: CheckCircle2,
};

// ── Helpers ─────────────────────────────────────────────────────
function activityToEvent(activity: Activity): CRMCalendarEvent {
  const hasActivityDate = !!activity.scheduledDate;
  const hasDuration = !!activity.duration && activity.duration > 0;

  // Task with only dueDate (no activityDate) → all-day event
  if (!hasActivityDate && activity.type === 'TASK') {
    const dueDate = activity.scheduledDate
      ? new Date(activity.scheduledDate)
      : new Date();
    return {
      id: activity.id,
      title: activity.subject,
      start: dueDate,
      end: dueDate,
      allDay: true,
      resource: activity,
    };
  }

  const start = activity.scheduledDate
    ? new Date(activity.scheduledDate)
    : new Date();
  const durationMs =
    (hasDuration ? activity.duration! : DEFAULT_DURATION_MINUTES) * 60 * 1000;
  const end = new Date(start.getTime() + durationMs);

  return {
    id: activity.id,
    title: activity.subject,
    start,
    end,
    allDay: false,
    resource: activity,
  };
}

function isDarkMode(): boolean {
  if (typeof document === 'undefined') return false;
  return document.documentElement.classList.contains('dark');
}

function formatTime(dateStr?: string): string {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
  });
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleDateString('vi-VN', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
  });
}

// ── Main Component ──────────────────────────────────────────────
export function ActivityCalendarView({
  className,
  date,
  view,
  onNavigate,
  onViewChange,
  onCreateActivity,
  filters,
}: ActivityCalendarViewProps) {
  const router = useRouter();
  const [selectedEvent, setSelectedEvent] =
    useState<CRMCalendarEvent | null>(null);
  const [popoverOpen, setPopoverOpen] = useState(false);
  const popoverAnchorRef = useRef<HTMLDivElement>(null);
  const [popoverPosition, setPopoverPosition] = useState<{
    top: number;
    left: number;
  }>({ top: 0, left: 0 });

  // Calculate date range based on current view
  const dateRange = useMemo(() => {
    const rangeUnit =
      view === Views.MONTH ? 'month' : view === Views.DAY ? 'day' : 'week';
    const start = moment(date).startOf(rangeUnit);
    const end = moment(date).endOf(rangeUnit);

    // Expand range for month view to include overflow weeks
    if (view === Views.MONTH) {
      start.subtract(7, 'days');
      end.add(7, 'days');
    }

    return {
      startDate: start.format(CALENDAR_QUERY_DATE_FORMAT),
      endDate: end.format(CALENDAR_QUERY_DATE_FORMAT),
    };
  }, [date, view]);

  const {
    data: activitiesData,
    isLoading,
  } = useGetUpcomingActivitiesQuery(dateRange);
  const [rescheduleActivity] = useRescheduleActivityMutation();
  const [updateActivity] = useUpdateActivityMutation();
  const [completeActivity] = useCompleteActivityMutation();
  const [cancelActivity] = useCancelActivityMutation();

  // Transform activities to calendar events
  const calendarEvents = useMemo<CRMCalendarEvent[]>(() => {
    const activities: Activity[] = activitiesData?.data || [];

    // Apply client-side filters
    let filtered = activities;
    if (filters?.types && filters.types.length > 0) {
      filtered = filtered.filter((a) => filters.types!.includes(a.type));
    }
    if (filters?.statuses && filters.statuses.length > 0) {
      filtered = filtered.filter((a) =>
        filters.statuses!.includes(a.status)
      );
    }
    if (filters?.priorities && filters.priorities.length > 0) {
      filtered = filtered.filter(
        (a) => a.priority && filters.priorities!.includes(a.priority)
      );
    }

    return filtered.map(activityToEvent);
  }, [activitiesData, filters]);

  // Event style customization
  const eventStyleGetter = useCallback((event: CRMCalendarEvent) => {
    const activity = event.resource;
    const colors =
      EVENT_COLORS[activity.type] || EVENT_COLORS.TASK;
    const dark = isDarkMode();
    const isInactive =
      activity.status === 'COMPLETED' || activity.status === 'CANCELLED';

    return {
      style: {
        background: dark ? colors.darkBg : colors.bg,
        borderLeftColor: isInactive ? '#9ca3af' : colors.border,
        color: dark ? colors.darkText : colors.text,
        opacity: isInactive ? 0.55 : 1,
      },
    };
  }, []);

  // Handle event drag/drop (reschedule)
  const handleEventDrop = useCallback(
    async ({
      event,
      start,
    }: {
      event: CRMCalendarEvent;
      start: string | Date;
    }) => {
      const activity = event.resource;
      if (
        activity.status === 'COMPLETED' ||
        activity.status === 'CANCELLED'
      ) {
        toast.error('Cannot reschedule completed or cancelled activities');
        return;
      }

      const newStart =
        typeof start === 'string' ? new Date(start) : start;
      const newDueDate = newStart.getTime();

      try {
        await rescheduleActivity({
          id: activity.id,
          data: { dueDate: newDueDate },
        }).unwrap();
        toast.success('Activity rescheduled');
      } catch {
        toast.error('Failed to reschedule activity');
      }
    },
    [rescheduleActivity]
  );

  // Handle event resize (update duration)
  const handleEventResize = useCallback(
    async ({
      event,
      start,
      end,
    }: {
      event: CRMCalendarEvent;
      start: string | Date;
      end: string | Date;
    }) => {
      const activity = event.resource;
      if (
        activity.status === 'COMPLETED' ||
        activity.status === 'CANCELLED'
      ) {
        toast.error(
          'Cannot modify completed or cancelled activities'
        );
        return;
      }

      const startDate =
        typeof start === 'string' ? new Date(start) : start;
      const endDate = typeof end === 'string' ? new Date(end) : end;
      const newDuration = Math.round(
        (endDate.getTime() - startDate.getTime()) / 60000
      );

      try {
        await updateActivity({
          id: activity.id,
          data: {
            durationMinutes: newDuration,
            activityDate: startDate.getTime(),
          },
        }).unwrap();
        toast.success('Activity updated');
      } catch {
        toast.error('Failed to update activity');
      }
    },
    [updateActivity]
  );

  // Handle slot selection (click-to-create)
  const handleSelectSlot = useCallback(
    (slotInfo: SlotInfo) => {
      if (onCreateActivity) {
        const isAllDay = view === Views.MONTH;
        onCreateActivity({
          start: slotInfo.start,
          end: slotInfo.end,
          allDay: isAllDay,
        });
      }
    },
    [onCreateActivity, view]
  );

  // Handle event click (popover)
  const handleSelectEvent = useCallback(
    (event: CRMCalendarEvent, e: React.SyntheticEvent) => {
      const nativeEvent = e.nativeEvent as MouseEvent;
      setSelectedEvent(event);
      setPopoverPosition({
        top: nativeEvent.clientY,
        left: nativeEvent.clientX,
      });
      setPopoverOpen(true);
    },
    []
  );

  // Quick actions from popover
  const handleCompleteActivity = useCallback(async () => {
    if (!selectedEvent) return;
    try {
      await completeActivity({
        id: selectedEvent.resource.id,
      }).unwrap();
      toast.success('Activity completed');
      setPopoverOpen(false);
    } catch {
      toast.error('Failed to complete activity');
    }
  }, [selectedEvent, completeActivity]);

  const handleCancelActivity = useCallback(async () => {
    if (!selectedEvent) return;
    try {
      await cancelActivity(selectedEvent.resource.id).unwrap();
      toast.success('Activity cancelled');
      setPopoverOpen(false);
    } catch {
      toast.error('Failed to cancel activity');
    }
  }, [selectedEvent, cancelActivity]);

  const handleViewDetail = useCallback(() => {
    if (!selectedEvent) return;
    router.push(`/crm/activities/${selectedEvent.resource.id}`);
    setPopoverOpen(false);
  }, [selectedEvent, router]);

  // Custom event component
  const EventComponent = useCallback(
    ({ event }: EventProps<CRMCalendarEvent>) => {
      return (
        <ActivityEventCard
          activity={event.resource}
          isMonthView={view === Views.MONTH}
        />
      );
    },
    [view]
  );

  if (isLoading) {
    return (
      <div className={cn('flex items-center justify-center h-[600px]', className)}>
        <div className='text-muted-foreground animate-pulse'>
          Loading calendar...
        </div>
      </div>
    );
  }

  const selectedActivity = selectedEvent?.resource;
  const SelectedIcon = selectedActivity
    ? ACTIVITY_ICON_MAP[selectedActivity.type] || CheckCircle2
    : CheckCircle2;

  return (
    <div className={className}>
      {/* Popover anchor (positioned absolutely) */}
      <div
        ref={popoverAnchorRef}
        style={{
          position: 'fixed',
          top: popoverPosition.top,
          left: popoverPosition.left,
          width: 1,
          height: 1,
          pointerEvents: 'none',
          zIndex: 9999,
        }}
      />

      {/* Activity Detail Popover */}
      <Popover open={popoverOpen} onOpenChange={setPopoverOpen}>
        <PopoverTrigger asChild>
          <span
            style={{
              position: 'fixed',
              top: popoverPosition.top,
              left: popoverPosition.left,
              width: 1,
              height: 1,
              pointerEvents: 'none',
            }}
          />
        </PopoverTrigger>
        <PopoverContent
          className='w-80 p-0'
          side='right'
          align='start'
          sideOffset={8}
        >
          {selectedActivity && (
            <div className='p-4 space-y-3'>
              {/* Header */}
              <div className='flex items-start gap-3'>
                <div
                  className={cn(
                    'p-2 rounded-lg flex-shrink-0',
                    selectedActivity.type === 'CALL' &&
                      'bg-blue-100 text-blue-600',
                    selectedActivity.type === 'EMAIL' &&
                      'bg-purple-100 text-purple-600',
                    selectedActivity.type === 'MEETING' &&
                      'bg-green-100 text-green-600',
                    selectedActivity.type === 'TASK' &&
                      'bg-orange-100 text-orange-600'
                  )}
                >
                  <SelectedIcon className='h-4 w-4' />
                </div>
                <div className='flex-1 min-w-0'>
                  <h4 className='font-semibold text-sm truncate'>
                    {selectedActivity.subject}
                  </h4>
                  {selectedActivity.description && (
                    <p className='text-xs text-muted-foreground line-clamp-2 mt-0.5'>
                      {selectedActivity.description}
                    </p>
                  )}
                </div>
              </div>

              {/* Status & Priority */}
              <div className='flex items-center gap-2'>
                <Badge
                  className={cn(
                    'text-[10px] px-1.5 py-0',
                    STATUS_COLORS[selectedActivity.status] ||
                      STATUS_COLORS.PLANNED
                  )}
                >
                  {selectedActivity.status}
                </Badge>
                {selectedActivity.priority && (
                  <Badge variant='outline' className='text-[10px] px-1.5 py-0'>
                    {selectedActivity.priority}
                  </Badge>
                )}
              </div>

              {/* Details */}
              <div className='space-y-1.5 text-xs text-muted-foreground'>
                {selectedActivity.scheduledDate && (
                  <div className='flex items-center gap-2'>
                    <Clock className='h-3 w-3 flex-shrink-0' />
                    <span>
                      {formatDate(selectedActivity.scheduledDate)}{' '}
                      {formatTime(selectedActivity.scheduledDate)}
                      {selectedActivity.duration &&
                        ` · ${selectedActivity.duration} min`}
                    </span>
                  </div>
                )}
                {selectedActivity.location && (
                  <div className='flex items-center gap-2'>
                    <MapPin className='h-3 w-3 flex-shrink-0' />
                    <span className='truncate'>
                      {selectedActivity.location}
                    </span>
                  </div>
                )}
                {selectedActivity.assignedToName && (
                  <div className='flex items-center gap-2'>
                    <User className='h-3 w-3 flex-shrink-0' />
                    <span>{selectedActivity.assignedToName}</span>
                  </div>
                )}
                {selectedActivity.relatedTo?.name && (
                  <div className='flex items-center gap-2'>
                    <LinkIcon className='h-3 w-3 flex-shrink-0' />
                    <span>
                      {selectedActivity.relatedTo.type}:{' '}
                      {selectedActivity.relatedTo.name}
                    </span>
                  </div>
                )}
              </div>

              {/* Actions */}
              <div className='flex items-center gap-2 pt-2 border-t'>
                {selectedActivity.status === 'PLANNED' && (
                  <>
                    <Button
                      size='sm'
                      variant='outline'
                      className='flex-1 h-7 text-xs gap-1'
                      onClick={handleCompleteActivity}
                    >
                      <Check className='h-3 w-3' />
                      Complete
                    </Button>
                    <Button
                      size='sm'
                      variant='outline'
                      className='flex-1 h-7 text-xs gap-1'
                      onClick={handleCancelActivity}
                    >
                      <X className='h-3 w-3' />
                      Cancel
                    </Button>
                  </>
                )}
                <Button
                  size='sm'
                  variant='default'
                  className='flex-1 h-7 text-xs gap-1'
                  onClick={handleViewDetail}
                >
                  <ExternalLink className='h-3 w-3' />
                  Detail
                </Button>
              </div>
            </div>
          )}
        </PopoverContent>
      </Popover>

      {/* Calendar */}
      <div className='crm-calendar-wrapper' style={{ height: 700 }}>
        <DnDCalendar
          localizer={localizer}
          events={calendarEvents}
          view={view}
          onView={onViewChange}
          date={date}
          onNavigate={onNavigate}
          startAccessor='start'
          endAccessor='end'
          allDayAccessor='allDay'
          style={{ height: '100%' }}
          eventPropGetter={eventStyleGetter}
          components={{
            event: EventComponent,
            toolbar: () => null,
          }}
          selectable
          popup
          views={[Views.MONTH, Views.WEEK, Views.DAY]}
          step={15}
          timeslots={4}
          showMultiDayTimes
          defaultDate={new Date()}
          // Drag & drop
          draggableAccessor={(event: CRMCalendarEvent) =>
            event.resource.status !== 'COMPLETED' &&
            event.resource.status !== 'CANCELLED'
          }
          resizable
          onEventDrop={handleEventDrop}
          onEventResize={handleEventResize}
          onSelectEvent={handleSelectEvent}
          onSelectSlot={handleSelectSlot}
        />
      </div>
    </div>
  );
}
