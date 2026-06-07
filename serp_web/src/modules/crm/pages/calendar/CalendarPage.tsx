// CalendarPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useState, useCallback } from 'react';
import { View, Views } from 'react-big-calendar';
import moment from 'moment';
import { toast } from 'sonner';
import {
  ChevronLeft,
  ChevronRight,
  Plus,
  Calendar as CalendarIcon,
  Phone,
  Mail,
  Users,
  CheckCircle2,
  SlidersHorizontal,
} from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Card, CardContent } from '@/shared/components/ui/card';
import { Badge } from '@/shared/components/ui/badge';
import { cn } from '@/shared/utils';
import { ActivityCalendarView } from '../../components/calendar';
import { QuickAddActivityDialog } from '../../components/dialogs';
import { useCreateActivityMutation } from '../../api/crmApi';
import type {
  ActivityType,
  ActivityStatus,
  CreateActivityRequest,
  Priority,
} from '../../types';

const ACTIVITY_TYPE_FILTERS: {
  type: ActivityType;
  label: string;
  icon: React.ElementType;
  activeColor: string;
}[] = [
  {
    type: 'CALL',
    label: 'Calls',
    icon: Phone,
    activeColor: 'bg-blue-500 text-white hover:bg-blue-600',
  },
  {
    type: 'EMAIL',
    label: 'Emails',
    icon: Mail,
    activeColor: 'bg-purple-500 text-white hover:bg-purple-600',
  },
  {
    type: 'MEETING',
    label: 'Meetings',
    icon: Users,
    activeColor: 'bg-green-500 text-white hover:bg-green-600',
  },
  {
    type: 'TASK',
    label: 'Tasks',
    icon: CheckCircle2,
    activeColor: 'bg-amber-500 text-white hover:bg-amber-600',
  },
];

const STATUS_FILTERS: { status: ActivityStatus; label: string }[] = [
  { status: 'PLANNED', label: 'Planned' },
  { status: 'COMPLETED', label: 'Completed' },
  { status: 'CANCELLED', label: 'Cancelled' },
];

export const CalendarPage: React.FC = () => {
  // Calendar state
  const [date, setDate] = useState(new Date());
  const [view, setView] = useState<View>(Views.WEEK);

  // Filter state
  const [activeTypes, setActiveTypes] = useState<ActivityType[]>([]);
  const [activeStatuses, setActiveStatuses] = useState<ActivityStatus[]>([]);
  const [showFilters, setShowFilters] = useState(false);

  // Quick add dialog state
  const [showQuickAdd, setShowQuickAdd] = useState(false);
  const [quickAddDefaults, setQuickAddDefaults] = useState<{
    scheduledDate?: string;
    scheduledTime?: string;
    duration?: number;
  }>({});

  const [createActivity, { isLoading: isCreating }] =
    useCreateActivityMutation();

  // Navigation helpers
  const navigateUnit =
    view === Views.MONTH ? 'month' : view === Views.WEEK ? 'week' : 'day';

  const goBack = useCallback(() => {
    setDate((prev) => moment(prev).subtract(1, navigateUnit).toDate());
  }, [navigateUnit]);

  const goForward = useCallback(() => {
    setDate((prev) => moment(prev).add(1, navigateUnit).toDate());
  }, [navigateUnit]);

  const goToday = useCallback(() => {
    setDate(new Date());
  }, []);

  // Title based on view
  const getTitle = () => {
    const m = moment(date);
    switch (view) {
      case Views.DAY:
        return m.format('dddd, MMMM D, YYYY');
      case Views.WEEK: {
        const start = m.clone().startOf('week');
        const end = m.clone().endOf('week');
        if (start.month() === end.month()) {
          return `${start.format('MMM D')} – ${end.format('D, YYYY')}`;
        }
        return `${start.format('MMM D')} – ${end.format('MMM D, YYYY')}`;
      }
      case Views.MONTH:
        return m.format('MMMM YYYY');
      default:
        return m.format('MMMM YYYY');
    }
  };

  // Toggle type filter
  const toggleTypeFilter = (type: ActivityType) => {
    setActiveTypes((prev) =>
      prev.includes(type) ? prev.filter((t) => t !== type) : [...prev, type]
    );
  };

  // Toggle status filter
  const toggleStatusFilter = (status: ActivityStatus) => {
    setActiveStatuses((prev) =>
      prev.includes(status)
        ? prev.filter((s) => s !== status)
        : [...prev, status]
    );
  };

  const hasActiveFilters = activeTypes.length > 0 || activeStatuses.length > 0;

  const clearFilters = () => {
    setActiveTypes([]);
    setActiveStatuses([]);
  };

  // Handle click-to-create
  const handleCreateActivity = useCallback(
    (slotInfo: { start: Date; end: Date; allDay: boolean }) => {
      const startMoment = moment(slotInfo.start);
      const endMoment = moment(slotInfo.end);
      const durationMinutes = Math.max(
        15,
        endMoment.diff(startMoment, 'minutes')
      );

      setQuickAddDefaults({
        scheduledDate: startMoment.format('YYYY-MM-DD'),
        scheduledTime: slotInfo.allDay
          ? undefined
          : startMoment.format('HH:mm'),
        duration: slotInfo.allDay ? 60 : durationMinutes,
      });
      setShowQuickAdd(true);
    },
    []
  );

  // Submit new activity
  const handleQuickAddSubmit = async (data: CreateActivityRequest) => {
    try {
      await createActivity(data).unwrap();
      toast.success('Activity created successfully');
      setShowQuickAdd(false);
      setQuickAddDefaults({});
    } catch {
      toast.error('Failed to create activity');
    }
  };

  return (
    <div className='space-y-4'>
      {/* Page Header */}
      <div className='flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Calendar</h1>
          <p className='text-muted-foreground text-sm'>
            View and manage all CRM activities
          </p>
        </div>
        <Button
          className='gap-2'
          onClick={() => {
            setQuickAddDefaults({});
            setShowQuickAdd(true);
          }}
        >
          <Plus className='h-4 w-4' />
          New Activity
        </Button>
      </div>

      {/* Toolbar */}
      <Card>
        <CardContent className='p-3'>
          <div className='flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3'>
            {/* Left: Navigation */}
            <div className='flex items-center gap-2'>
              <div className='flex items-center gap-1'>
                <Button
                  variant='outline'
                  size='icon'
                  className='h-8 w-8'
                  onClick={goBack}
                >
                  <ChevronLeft className='h-4 w-4' />
                </Button>
                <Button
                  variant='outline'
                  size='sm'
                  className='h-8 px-3'
                  onClick={goToday}
                >
                  Today
                </Button>
                <Button
                  variant='outline'
                  size='icon'
                  className='h-8 w-8'
                  onClick={goForward}
                >
                  <ChevronRight className='h-4 w-4' />
                </Button>
              </div>
              <h2 className='text-sm font-semibold ml-2'>{getTitle()}</h2>
            </div>

            {/* Right: View mode + Filters toggle */}
            <div className='flex items-center gap-2'>
              {/* Type filter chips */}
              <div className='hidden md:flex items-center gap-1'>
                {ACTIVITY_TYPE_FILTERS.map(
                  ({ type, label, icon: Icon, activeColor }) => {
                    const isActive = activeTypes.includes(type);
                    return (
                      <Button
                        key={type}
                        variant={isActive ? 'default' : 'outline'}
                        size='sm'
                        className={cn(
                          'h-7 text-xs gap-1 px-2',
                          isActive && activeColor
                        )}
                        onClick={() => toggleTypeFilter(type)}
                      >
                        <Icon className='h-3 w-3' />
                        {label}
                      </Button>
                    );
                  }
                )}
              </div>

              {/* Filters toggle (mobile) */}
              <Button
                variant={showFilters ? 'secondary' : 'outline'}
                size='sm'
                className='h-8 gap-1 md:hidden'
                onClick={() => setShowFilters(!showFilters)}
              >
                <SlidersHorizontal className='h-3.5 w-3.5' />
                Filters
                {hasActiveFilters && (
                  <span className='h-1.5 w-1.5 rounded-full bg-primary' />
                )}
              </Button>

              {/* Divider */}
              <div className='h-6 w-px bg-border hidden sm:block' />

              {/* View switcher */}
              <div className='flex rounded-lg border bg-muted p-0.5'>
                {[
                  { v: Views.DAY, label: 'Day' },
                  { v: Views.WEEK, label: 'Week' },
                  { v: Views.MONTH, label: 'Month' },
                ].map(({ v, label }) => (
                  <button
                    key={v}
                    onClick={() => setView(v)}
                    className={cn(
                      'px-3 py-1 text-xs font-medium rounded-md transition-colors',
                      view === v
                        ? 'bg-background shadow-sm text-foreground'
                        : 'text-muted-foreground hover:text-foreground'
                    )}
                  >
                    {label}
                  </button>
                ))}
              </div>
            </div>
          </div>

          {/* Mobile filters */}
          {showFilters && (
            <div className='mt-3 pt-3 border-t md:hidden space-y-2'>
              <div className='flex flex-wrap gap-1'>
                {ACTIVITY_TYPE_FILTERS.map(
                  ({ type, label, icon: Icon, activeColor }) => {
                    const isActive = activeTypes.includes(type);
                    return (
                      <Button
                        key={type}
                        variant={isActive ? 'default' : 'outline'}
                        size='sm'
                        className={cn(
                          'h-7 text-xs gap-1',
                          isActive && activeColor
                        )}
                        onClick={() => toggleTypeFilter(type)}
                      >
                        <Icon className='h-3 w-3' />
                        {label}
                      </Button>
                    );
                  }
                )}
              </div>
              <div className='flex flex-wrap gap-1'>
                {STATUS_FILTERS.map(({ status, label }) => {
                  const isActive = activeStatuses.includes(status);
                  return (
                    <Button
                      key={status}
                      variant={isActive ? 'secondary' : 'outline'}
                      size='sm'
                      className='h-7 text-xs'
                      onClick={() => toggleStatusFilter(status)}
                    >
                      {label}
                    </Button>
                  );
                })}
              </div>
              {hasActiveFilters && (
                <Button
                  variant='ghost'
                  size='sm'
                  className='h-7 text-xs'
                  onClick={clearFilters}
                >
                  Clear filters
                </Button>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Calendar View */}
      <ActivityCalendarView
        date={date}
        view={view}
        onNavigate={setDate}
        onViewChange={setView}
        onCreateActivity={handleCreateActivity}
        filters={{
          types: activeTypes.length > 0 ? activeTypes : undefined,
          statuses: activeStatuses.length > 0 ? activeStatuses : undefined,
        }}
      />

      {/* Quick Add Activity Dialog */}
      <QuickAddActivityDialog
        open={showQuickAdd}
        onOpenChange={setShowQuickAdd}
        onSubmit={handleQuickAddSubmit}
        isLoading={isCreating}
      />
    </div>
  );
};
