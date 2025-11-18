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
import './calendar.css';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetScheduleEventsQuery,
  useUpdateScheduleEventMutation,
} from '../../services/scheduleApi';
import { SmartEventCard } from './SmartEventCard';
import type { ScheduleEvent } from '../../types';
import { toast } from 'sonner';

const localizer = momentLocalizer(moment);
const DnDCalendar = withDragAndDrop<CalendarEvent, object>(Calendar);

interface CalendarViewProps {
  className?: string;
  onEventSelect?: (event: ScheduleEvent) => void;
  onExternalDrop?: (taskId: string, start: Date, end: Date) => void;
}

interface CalendarEvent {
  id: string;
  title: string;
  start: Date;
  end: Date;
  resource: ScheduleEvent;
}

export function CalendarView({
  className,
  onEventSelect,
  onExternalDrop,
}: CalendarViewProps) {
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

  // Event style customization (Motion-inspired clean colors)
  const eventStyleGetter = useCallback((event: CalendarEvent) => {
    const scheduleEvent = event.resource;

    // Deep Work - Soft Purple
    if (scheduleEvent.isDeepWork) {
      return {
        style: {
          background: '#f3e8ff',
          borderLeftColor: '#a855f7',
          color: '#6b21a8',
        },
      };
    }

    // High Priority Task - Soft Rose/Pink
    if (scheduleEvent.priority === 'HIGH') {
      return {
        style: {
          background: '#ffe4e6',
          borderLeftColor: '#f43f5e',
          color: '#9f1239',
        },
      };
    }

    // Medium Priority - Soft Amber
    if (scheduleEvent.priority === 'MEDIUM') {
      return {
        style: {
          background: '#fef3c7',
          borderLeftColor: '#f59e0b',
          color: '#92400e',
        },
      };
    }

    // Low Priority or Regular Event - Soft Sky Blue (Motion default)
    return {
      style: {
        background: '#e0f2fe',
        borderLeftColor: '#0ea5e9',
        color: '#075985',
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

  // Handle external drag (from sidebar)
  const handleDropFromOutside = useCallback(
    ({ start, end }: { start: string | Date; end: string | Date }) => {
      const startDate = typeof start === 'string' ? new Date(start) : start;
      const endDate = typeof end === 'string' ? new Date(end) : end;

      // Get the dragged task ID from the drag event
      const taskId = (window as any).__draggedTaskId;
      if (!taskId) {
        toast.error('Invalid task drop');
        return;
      }

      if (onExternalDrop) {
        onExternalDrop(taskId, startDate, endDate);
      }

      // Clear the dragged task ID
      delete (window as any).__draggedTaskId;
    },
    [onExternalDrop]
  );

  // Handle drag over (needed for external drops)
  const handleDragOver = useCallback((event: any) => {
    event.preventDefault();
    // Check if we have a dragged task
    if ((window as any).__draggedTaskId) {
      event.dataTransfer.dropEffect = 'move';
    }
  }, []);

  // Provide custom drag from outside item (required by react-big-calendar DnD)
  const customDragFromOutsideItem = useCallback(() => {
    const taskId = (window as any).__draggedTaskId;
    if (!taskId) {
      // Return a dummy event that will be ignored
      return {
        id: 'temp',
        title: 'Temp',
        start: new Date(),
        end: new Date(),
        resource: null as any,
      };
    }

    // Return the dragged task as a calendar event placeholder
    const now = new Date();
    return {
      id: taskId,
      title: `Task ${taskId}`,
      start: now,
      end: new Date(now.getTime() + 60 * 60 * 1000), // 1 hour default
      resource: { id: taskId } as any,
    };
  }, []);

  // Handle event selection (click)
  const handleSelectEvent = useCallback(
    (event: CalendarEvent) => {
      if (onEventSelect) {
        onEventSelect(event.resource);
      }
    },
    [onEventSelect]
  );

  // Custom event component
  const EventComponent = ({ event }: EventProps<CalendarEvent>) => {
    return <SmartEventCard event={event.resource} />;
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
    <Card className={cn('shadow-sm', className)}>
      <CardHeader className='pb-4'>
        <div className='flex items-center justify-between'>
          <div className='flex items-center gap-4'>
            <CardTitle>Calendar</CardTitle>
            <div className='flex items-center gap-2'>
              <Button
                variant='outline'
                size='sm'
                onClick={() =>
                  setDate(
                    moment(date)
                      .subtract(
                        1,
                        view === Views.MONTH
                          ? 'month'
                          : view === Views.WEEK
                            ? 'week'
                            : 'day'
                      )
                      .toDate()
                  )
                }
              >
                <ChevronLeft className='h-4 w-4' />
              </Button>
              <Button
                variant='outline'
                size='sm'
                onClick={() => setDate(new Date())}
              >
                Today
              </Button>
              <Button
                variant='outline'
                size='sm'
                onClick={() =>
                  setDate(
                    moment(date)
                      .add(
                        1,
                        view === Views.MONTH
                          ? 'month'
                          : view === Views.WEEK
                            ? 'week'
                            : 'day'
                      )
                      .toDate()
                  )
                }
              >
                <ChevronRight className='h-4 w-4' />
              </Button>
            </div>
          </div>
          <div className='flex gap-2'>
            <Button
              variant={view === Views.DAY ? 'default' : 'outline'}
              size='sm'
              onClick={() => setView(Views.DAY)}
            >
              Day
            </Button>
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
          </div>
        </div>
      </CardHeader>
      <CardContent className='p-6 pt-0'>
        <div
          className='calendar-wrapper'
          style={{ height: 700 }}
          onDragOver={handleDragOver}
        >
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
              toolbar: () => null,
            }}
            selectable
            popup
            views={[Views.MONTH, Views.WEEK, Views.DAY]}
            step={5}
            timeslots={12}
            showMultiDayTimes
            defaultDate={new Date()}
            // Enable drag & drop
            draggableAccessor={() => true}
            resizable
            onEventDrop={handleEventDrop}
            onEventResize={handleEventDrop}
            onDropFromOutside={handleDropFromOutside}
            onSelectEvent={handleSelectEvent}
            onDragOver={handleDragOver}
            dragFromOutsideItem={customDragFromOutsideItem}
          />
        </div>
      </CardContent>
    </Card>
  );
}
