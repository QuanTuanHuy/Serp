/**
 * PTM v2 - Calendar View Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Calendar view with drag-drop support
 */

'use client';

import { useCallback, useMemo, useState } from 'react';
import {
  Calendar,
  momentLocalizer,
  View,
  Views,
  EventProps,
} from 'react-big-calendar';
import withDragAndDrop from 'react-big-calendar/lib/addons/dragAndDrop';
import moment from 'moment';
import 'react-big-calendar/lib/css/react-big-calendar.css';
import 'react-big-calendar/lib/addons/dragAndDrop/styles.css';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { cn } from '@/shared/utils';
import {
  useGetScheduleEventsQuery,
  useUpdateScheduleEventMutation,
} from '../../services/scheduleApi';
import type { ScheduleEvent } from '../../types';
import { toast } from 'sonner';

const localizer = momentLocalizer(moment);
const DnDCalendar = withDragAndDrop<CalendarEvent, object>(Calendar);

interface CalendarViewProps {
  className?: string;
}

interface CalendarEvent {
  id: string;
  title: string;
  start: Date;
  end: Date;
  resource: ScheduleEvent;
}

export function CalendarView({ className }: CalendarViewProps) {
  const [view, setView] = useState<View>(Views.WEEK);
  const [date, setDate] = useState(new Date());

  // Calculate date range based on current view
  const dateRange = useMemo(() => {
    const start = moment(date).startOf(view === Views.MONTH ? 'month' : 'week');
    const end = moment(date).endOf(view === Views.MONTH ? 'month' : 'week');
    return {
      startDateMs: start.valueOf(),
      endDateMs: end.valueOf(),
    };
  }, [date, view]);

  const { data: events = [], isLoading } = useGetScheduleEventsQuery(dateRange);
  const [updateEvent] = useUpdateScheduleEventMutation();

  // Transform events for react-big-calendar
  const calendarEvents = useMemo<CalendarEvent[]>(() => {
    return events.map((event: ScheduleEvent) => ({
      id: event.id,
      title: event.title || 'Untitled Event',
      start: new Date(event.dateMs + event.startMin * 60 * 1000),
      end: new Date(event.dateMs + event.endMin * 60 * 1000),
      resource: event,
    }));
  }, [events]);

  // Event style customization
  const eventStyleGetter = useCallback((event: CalendarEvent) => {
    const scheduleEvent = event.resource;

    let backgroundColor = '#3B82F6'; // default blue
    let borderColor = '#2563EB';

    // Focus time - purple
    if (scheduleEvent.isDeepWork) {
      backgroundColor = '#8B5CF6';
      borderColor = '#7C3AED';
    }

    // Priority-based colors
    if (scheduleEvent.scheduleTaskId) {
      // This would normally come from task data, using placeholder
      backgroundColor = '#F59E0B'; // amber for tasks
      borderColor = '#D97706';
    }

    return {
      style: {
        backgroundColor,
        borderColor,
        borderLeft: `4px solid ${borderColor}`,
        color: 'white',
        borderRadius: '4px',
        fontSize: '13px',
        padding: '2px 6px',
      },
    };
  }, []);

  // Handle event resize/move
  const handleEventDrop = useCallback(
    async ({
      event,
      start,
      end,
    }: {
      event: CalendarEvent;
      start: string | Date;
      end: string | Date;
    }) => {
      const startDate = typeof start === 'string' ? new Date(start) : start;
      const endDate = typeof end === 'string' ? new Date(end) : end;

      const startMinutes = startDate.getHours() * 60 + startDate.getMinutes();
      const endMinutes = endDate.getHours() * 60 + endDate.getMinutes();
      const dateMs = new Date(startDate).setHours(0, 0, 0, 0);

      try {
        await updateEvent({
          id: event.id,
          dateMs,
          startMin: startMinutes,
          endMin: endMinutes,
          dateRange, // Pass dateRange for optimistic update
        }).unwrap();

        toast.success('Event updated!');
      } catch (error) {
        console.error('Failed to update event:', error);
        toast.error('Failed to update event');
      }
    },
    [updateEvent, dateRange]
  );

  // Custom event component
  const EventComponent = ({ event }: { event: CalendarEvent }) => {
    const scheduleEvent = event.resource;

    return (
      <div className='flex items-center gap-1 text-xs'>
        {scheduleEvent.isDeepWork && <span>🔒</span>}
        {scheduleEvent.scheduleTaskId && <span>📋</span>}
        <span className='truncate font-medium'>{event.title}</span>
      </div>
    );
  };

  if (isLoading) {
    return (
      <Card className={className}>
        <CardHeader>
          <CardTitle>Calendar</CardTitle>
        </CardHeader>
        <CardContent>
          <div className='h-[600px] flex items-center justify-center'>
            <div className='text-muted-foreground'>Loading calendar...</div>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className={className}>
      <CardHeader>
        <div className='flex items-center justify-between'>
          <CardTitle>Calendar</CardTitle>
          <div className='flex gap-2'>
            <Button
              variant={view === Views.WEEK ? 'default' : 'outline'}
              size='sm'
              onClick={() => setView(Views.WEEK)}
            >
              Week
            </Button>
            <Button
              variant={view === Views.MONTH ? 'default' : 'outline'}
              size='sm'
              onClick={() => setView(Views.MONTH)}
            >
              Month
            </Button>
            <Button
              variant={view === Views.DAY ? 'default' : 'outline'}
              size='sm'
              onClick={() => setView(Views.DAY)}
            >
              Day
            </Button>
          </div>
        </div>
      </CardHeader>

      <CardContent>
        <div className='calendar-wrapper' style={{ height: 600 }}>
          <DnDCalendar
            localizer={localizer}
            events={calendarEvents}
            view={view}
            onView={setView}
            date={date}
            onNavigate={setDate}
            startAccessor='start'
            endAccessor='end'
            style={{ height: '100%' }}
            eventPropGetter={eventStyleGetter}
            components={{
              event: EventComponent,
            }}
            selectable
            popup
            views={[Views.MONTH, Views.WEEK, Views.DAY]}
            step={30}
            showMultiDayTimes
            defaultDate={new Date()}
            // Enable drag & drop
            draggableAccessor={() => true}
            resizable
            onEventDrop={handleEventDrop}
            onEventResize={handleEventDrop}
          />
        </div>

        {/* Legend */}
        <div className='flex flex-wrap items-center gap-4 mt-4 pt-4 border-t text-sm'>
          <div className='flex items-center gap-2'>
            <div className='w-3 h-3 rounded bg-purple-500' />
            <span className='text-muted-foreground'>Focus Time</span>
          </div>
          <div className='flex items-center gap-2'>
            <div className='w-3 h-3 rounded bg-amber-500' />
            <span className='text-muted-foreground'>Task</span>
          </div>
          <div className='flex items-center gap-2'>
            <div className='w-3 h-3 rounded bg-blue-500' />
            <span className='text-muted-foreground'>Event</span>
          </div>
        </div>
      </CardContent>

      <style jsx global>{`
        .calendar-wrapper .rbc-calendar {
          font-family: inherit;
        }

        .calendar-wrapper .rbc-header {
          padding: 12px 8px;
          font-weight: 600;
          font-size: 13px;
          border-bottom: 2px solid hsl(var(--border));
          background-color: hsl(var(--muted));
          color: hsl(var(--foreground));
        }

        .calendar-wrapper .rbc-today {
          background-color: hsl(var(--accent) / 0.5);
        }

        .calendar-wrapper .rbc-event {
          transition: all 0.2s ease;
          cursor: move;
        }

        .calendar-wrapper .rbc-event:hover {
          transform: scale(1.02);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
          z-index: 10;
        }

        .calendar-wrapper .rbc-event.rbc-selected {
          box-shadow: 0 0 0 3px hsl(var(--primary) / 0.3);
        }

        .calendar-wrapper .rbc-addons-dnd .rbc-addons-dnd-resizable {
          position: relative;
        }

        .calendar-wrapper .rbc-addons-dnd-resize-anchor {
          position: absolute;
          width: 100%;
          height: 10px;
          bottom: 0;
          cursor: ns-resize;
          background: linear-gradient(
            to bottom,
            transparent,
            rgba(0, 0, 0, 0.1)
          );
          opacity: 0;
          transition: opacity 0.2s;
        }

        .calendar-wrapper .rbc-event:hover .rbc-addons-dnd-resize-anchor {
          opacity: 1;
        }

        .calendar-wrapper .rbc-time-slot {
          border-top: 1px solid hsl(var(--border) / 0.5);
        }

        .calendar-wrapper .rbc-current-time-indicator {
          background-color: hsl(var(--destructive));
          height: 2px;
        }

        .calendar-wrapper .rbc-event {
          padding: 2px 6px;
          border-radius: 4px;
          cursor: pointer;
        }

        .calendar-wrapper .rbc-event:hover {
          opacity: 0.85;
        }

        .calendar-wrapper .rbc-selected {
          background-color: hsl(var(--primary)) !important;
        }

        .calendar-wrapper .rbc-time-slot {
          min-height: 40px;
        }

        .calendar-wrapper .rbc-current-time-indicator {
          background-color: hsl(var(--destructive));
          height: 2px;
        }

        .calendar-wrapper .rbc-toolbar {
          padding: 12px;
          margin-bottom: 12px;
          border-bottom: 1px solid hsl(var(--border));
        }

        .calendar-wrapper .rbc-toolbar button {
          padding: 6px 12px;
          border: 1px solid hsl(var(--border));
          border-radius: 6px;
          background: hsl(var(--background));
          color: hsl(var(--foreground));
          font-size: 13px;
          cursor: pointer;
        }

        .calendar-wrapper .rbc-toolbar button:hover {
          background: hsl(var(--accent));
        }

        .calendar-wrapper .rbc-toolbar button.rbc-active {
          background: hsl(var(--primary));
          color: hsl(var(--primary-foreground));
          border-color: hsl(var(--primary));
        }

        .calendar-wrapper .rbc-month-view,
        .calendar-wrapper .rbc-time-view {
          border: 1px solid hsl(var(--border));
          border-radius: 8px;
          overflow: hidden;
        }

        .calendar-wrapper .rbc-time-content {
          border-top: 1px solid hsl(var(--border));
        }

        .calendar-wrapper .rbc-day-slot .rbc-time-slot {
          border-top: 1px solid hsl(var(--border) / 0.3);
        }
      `}</style>
    </Card>
  );
}
