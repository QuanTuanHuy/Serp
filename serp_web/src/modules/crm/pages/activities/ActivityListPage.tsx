// ActivityListPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useState, useMemo, useEffect } from 'react';
import moment from 'moment';
import { DndContext } from '@dnd-kit/core';
import { CRMCalendarGrid } from '../../components/calendar/CRMCalendarGrid';
import {
  toVietnamMoment,
  getVisibleCalendarDays,
  getCalendarDayKey,
} from '../../components/calendar/crmCalendar.utils';
import { toast } from 'sonner';
import { useRouter } from 'next/navigation';
import {
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Input,
  Badge,
} from '@/shared/components/ui';
import {
  Search,
  Plus,
  Calendar,
  List,
  SlidersHorizontal,
  Phone,
  Mail,
  Users,
  FileText,
  CheckCircle2,
  Clock,
  AlertCircle,
  X,
  ChevronLeft,
  ChevronRight,
  Trash2,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { ExportDropdown, CRMDatePicker } from '../../components/shared';
import { QuickAddActivityDialog } from '../../components/dialogs';
import { toLocalDateInputValue } from '../../utils';
import { ACTIVITY_EXPORT_COLUMNS } from '../../utils/export';
import {
  useCreateActivityMutation,
  useSearchActivitiesQuery,
  useGetActivityStatsQuery,
  useBulkActivityOperationsMutation,
  useRescheduleActivityMutation,
} from '../../api/crmApi';
import type {
  Activity,
  ActivityDisplayStatus,
  ActivityType,
  ActivityStatus,
  CreateActivityRequest,
  Priority,
} from '../../types';
import { getActivityDisplayStatus } from '../../utils';

// Activity type configuration
const ACTIVITY_TYPES: {
  type: ActivityType;
  label: string;
  icon: React.ElementType;
  color: string;
}[] = [
  {
    type: 'CALL',
    label: 'Call',
    icon: Phone,
    color: 'text-blue-600 bg-blue-100',
  },
  {
    type: 'EMAIL',
    label: 'Email',
    icon: Mail,
    color: 'text-purple-600 bg-purple-100',
  },
  {
    type: 'MEETING',
    label: 'Meeting',
    icon: Users,
    color: 'text-green-600 bg-green-100',
  },
  {
    type: 'TASK',
    label: 'Task',
    icon: CheckCircle2,
    color: 'text-orange-600 bg-orange-100',
  },
];

const STATUS_CONFIG: Record<
  ActivityDisplayStatus,
  { label: string; color: string }
> = {
  PLANNED: { label: 'Planned', color: 'bg-blue-100 text-blue-700' },
  COMPLETED: { label: 'Completed', color: 'bg-green-100 text-green-700' },
  CANCELLED: {
    label: 'Cancelled',
    color: 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300',
  },
  OVERDUE: { label: 'Overdue', color: 'bg-red-100 text-red-700' },
};

const FILTER_STATUSES: ActivityStatus[] = ['PLANNED', 'COMPLETED', 'CANCELLED'];

const PRIORITY_CONFIG: Record<Priority, { label: string; color: string }> = {
  LOW: { label: 'Low', color: 'bg-green-100 text-green-700' },
  MEDIUM: { label: 'Medium', color: 'bg-yellow-100 text-yellow-700' },
  HIGH: { label: 'High', color: 'bg-orange-100 text-orange-700' },
  URGENT: { label: 'Urgent', color: 'bg-red-100 text-red-700' },
};

interface ActivityListPageProps {
  className?: string;
}

export const ActivityListPage: React.FC<ActivityListPageProps> = ({
  className,
}) => {
  const router = useRouter();

  // State management
  const [searchQuery, setSearchQuery] = useState('');
  const [typeFilter, setTypeFilter] = useState<ActivityType | ''>('');
  const [statusFilter, setStatusFilter] = useState<ActivityStatus | ''>('');
  const [priorityFilter, setPriorityFilter] = useState<Priority | ''>('');
  const [dueDateFrom, setDueDateFrom] = useState('');
  const [dueDateTo, setDueDateTo] = useState('');
  const [sortBy, setSortBy] = useState<string>('dueDate');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [viewMode, setViewMode] = useState<'list' | 'calendar'>('list');
  const [showFilters, setShowFilters] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [showQuickAdd, setShowQuickAdd] = useState(false);
  const [selectedActivityIds, setSelectedActivityIds] = useState<Set<string>>(
    new Set()
  );
  const pageSize = 10;

  // Calendar specific state
  const [calendarDate, setCalendarDate] = useState(new Date());
  const [calendarView, setCalendarView] = useState<'month' | 'week'>('month');
  const [preselectedDate, setPreselectedDate] = useState<string>('');

  useEffect(() => {
    if (viewMode === 'calendar') {
      const anchor = toVietnamMoment(calendarDate);
      const start =
        calendarView === 'month'
          ? anchor.clone().startOf('month').startOf('isoWeek')
          : anchor.clone().startOf('isoWeek');
      const end =
        calendarView === 'month'
          ? anchor.clone().endOf('month').endOf('isoWeek')
          : anchor.clone().endOf('isoWeek');

      setDueDateFrom(start.format('YYYY-MM-DD'));
      setDueDateTo(end.format('YYYY-MM-DD'));
    }
  }, [viewMode, calendarDate, calendarView]);

  const activityQuery = useSearchActivitiesQuery({
    filters: {
      keyword: searchQuery || undefined,
      types: typeFilter ? [typeFilter] : undefined,
      statuses: statusFilter ? [statusFilter] : undefined,
      priorities: priorityFilter ? [priorityFilter] : undefined,
      dueDateFrom: dueDateFrom || undefined,
      dueDateTo: dueDateTo || undefined,
    },
    pagination: {
      page: viewMode === 'calendar' ? 1 : currentPage,
      limit: viewMode === 'calendar' ? 200 : pageSize,
      sortBy: viewMode === 'calendar' ? 'dueDate' : sortBy,
      sortOrder: viewMode === 'calendar' ? 'asc' : sortOrder,
    },
  });
  const { data: statsData } = useGetActivityStatsQuery(undefined, {
    refetchOnMountOrArgChange: true,
  });
  const [createActivity, { isLoading: isCreating }] =
    useCreateActivityMutation();
  const [bulkActivityOperations, { isLoading: isBulkOperating }] =
    useBulkActivityOperationsMutation();
  const [rescheduleActivity] = useRescheduleActivityMutation();

  const handleDragEnd = async (event: any) => {
    const { active, over } = event;
    if (!over) return;

    const activeData = active.data.current;
    const overData = over.data.current;

    if (activeData?.type === 'activity' && overData?.type === 'calendar-day') {
      const activity = activeData.activity;
      const targetDayKey = overData.dayKey;
      const originalScheduledDate = activity.scheduledDate;

      let timeStr = '09:00';
      if (originalScheduledDate) {
        const originalDate = new Date(originalScheduledDate);
        const hours = String(originalDate.getHours()).padStart(2, '0');
        const minutes = String(originalDate.getMinutes()).padStart(2, '0');
        timeStr = `${hours}:${minutes}`;
      }

      const newScheduledDate = new Date(`${targetDayKey}T${timeStr}`);
      const newTimestamp = newScheduledDate.getTime();

      try {
        await rescheduleActivity({
          id: activity.id,
          data: {
            dueDate: newTimestamp,
          },
        }).unwrap();
        toast.success('Activity rescheduled successfully');
      } catch {
        toast.error('Failed to reschedule activity');
      }
    }
  };

  const calendarDays = useMemo(() => {
    return getVisibleCalendarDays(calendarDate, calendarView);
  }, [calendarDate, calendarView]);

  const calendarTitle = useMemo(() => {
    const m = moment(calendarDate);
    return calendarView === 'month'
      ? m.format('MMMM YYYY')
      : `Week of ${m.clone().startOf('isoWeek').format('MMM D, YYYY')}`;
  }, [calendarDate, calendarView]);

  const handleCalendarPrev = () => {
    setCalendarDate((prev) =>
      moment(prev)
        .subtract(1, calendarView === 'month' ? 'month' : 'week')
        .toDate()
    );
  };

  const handleCalendarNext = () => {
    setCalendarDate((prev) =>
      moment(prev)
        .add(1, calendarView === 'month' ? 'month' : 'week')
        .toDate()
    );
  };

  const handleCalendarToday = () => {
    setCalendarDate(new Date());
  };

  const handleCalendarCellClick = (dateStr: string) => {
    setPreselectedDate(dateStr);
    setShowQuickAdd(true);
  };

  const activities = useMemo(
    () => activityQuery.data?.data?.data || [],
    [activityQuery.data]
  );

  const activitiesByDay = useMemo(() => {
    const map = new Map<string, Activity[]>();
    activities.forEach((activity) => {
      const dayKey = getCalendarDayKey(activity.scheduledDate);
      if (dayKey) {
        if (!map.has(dayKey)) {
          map.set(dayKey, []);
        }
        map.get(dayKey)!.push(activity);
      }
    });
    return map;
  }, [activities]);
  const pagination = activityQuery.data?.data?.pagination;
  const total = pagination?.total || 0;
  const totalPages = pagination?.totalPages || 1;

  const stats = useMemo(() => {
    return {
      total: statsData?.data?.total || 0,
      overdue: statsData?.data?.overdue || 0,
      upcoming: statsData?.data?.upcoming || 0,
      completed: statsData?.data?.byStatus?.COMPLETED || 0,
    };
  }, [statsData]);

  const clearFilters = () => {
    setSearchQuery('');
    setTypeFilter('');
    setStatusFilter('');
    setPriorityFilter('');
    setDueDateFrom('');
    setDueDateTo('');
    setSortBy('dueDate');
    setSortOrder('asc');
    setCurrentPage(1);
  };

  const handleSelectAll = () => {
    if (selectedActivityIds.size === activities.length) {
      setSelectedActivityIds(new Set());
    } else {
      setSelectedActivityIds(new Set(activities.map((a: Activity) => a.id)));
    }
  };

  const handleSelectActivity = (activityId: string) => {
    const newSelected = new Set(selectedActivityIds);
    if (newSelected.has(activityId)) {
      newSelected.delete(activityId);
    } else {
      newSelected.add(activityId);
    }
    setSelectedActivityIds(newSelected);
  };

  const handleBulkAction = async (action: 'COMPLETE' | 'CANCEL' | 'DELETE') => {
    if (selectedActivityIds.size === 0) return;

    try {
      const response = await bulkActivityOperations({
        activityIds: Array.from(selectedActivityIds),
        action,
      }).unwrap();

      const result = response.data;
      toast.success(
        `${result.successCount} activities ${action.toLowerCase()}d successfully` +
          (result.failedCount > 0 ? `, ${result.failedCount} failed` : '')
      );
      setSelectedActivityIds(new Set());
    } catch {
      toast.error(`Failed to ${action.toLowerCase()} activities`);
    }
  };

  const handleQuickAddActivity = async (data: CreateActivityRequest) => {
    try {
      await createActivity(data).unwrap();
      toast.success('Activity created successfully');
      setShowQuickAdd(false);
    } catch {
      toast.error('Failed to create activity');
    }
  };

  const handleViewActivity = (activityId: string) => {
    router.push(`/crm/activities/${activityId}`);
  };

  const hasActiveFilters =
    searchQuery ||
    typeFilter ||
    statusFilter ||
    priorityFilter ||
    dueDateFrom ||
    dueDateTo;

  const getActivityIcon = (type: ActivityType) => {
    const config = ACTIVITY_TYPES.find((t) => t.type === type);
    return config?.icon || FileText;
  };

  const getActivityColor = (type: ActivityType) => {
    const config = ACTIVITY_TYPES.find((t) => t.type === type);
    return (
      config?.color ||
      'text-gray-600 dark:text-gray-300 bg-gray-100 dark:bg-gray-800'
    );
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'Not scheduled';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
      weekday: 'short',
      day: 'numeric',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className={cn('space-y-6', className)}>
      {/* Page Header */}
      <div className='flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Activities</h1>
          <p className='text-muted-foreground'>
            Track and manage all your CRM activities
          </p>
        </div>
        <div className='flex items-center gap-2'>
          {/* <ExportDropdown
            data={activities}
            columns={ACTIVITY_EXPORT_COLUMNS}
            filename='activities'
            onExportComplete={(format, count) => {
              console.log(`Exported ${count} activities as ${format}`);
            }}
          /> */}
          <Button
            variant='outline'
            onClick={() => setShowQuickAdd(true)}
            className='gap-2'
          >
            <Plus className='h-4 w-4 mr-2' />
            Quick Add
          </Button>
        </div>
      </div>

      {/* Quick Stats */}
      <div className='grid grid-cols-2 sm:grid-cols-4 gap-4'>
        <Card className='bg-gradient-to-br from-blue-50 to-blue-100/50 dark:from-blue-950 dark:to-blue-900/50 border-blue-200/50 dark:border-blue-800/50'>
          <CardContent className='p-4'>
            <div className='flex items-center gap-3'>
              <div className='p-2 rounded-lg bg-blue-500/20 dark:bg-blue-500/30'>
                <Calendar className='h-5 w-5 text-blue-600' />
              </div>
              <div>
                <p className='text-sm text-muted-foreground'>Total</p>
                <p className='text-2xl font-bold'>{stats.total}</p>
              </div>
            </div>
          </CardContent>
        </Card>
        <Card className='bg-gradient-to-br from-red-50 to-red-100/50 dark:from-red-950 dark:to-red-900/50 border-red-200/50 dark:border-red-800/50'>
          <CardContent className='p-4'>
            <div className='flex items-center gap-3'>
              <div className='p-2 rounded-lg bg-red-500/20 dark:bg-red-500/30'>
                <AlertCircle className='h-5 w-5 text-red-600' />
              </div>
              <div>
                <p className='text-sm text-muted-foreground'>Overdue</p>
                <p className='text-2xl font-bold'>{stats.overdue}</p>
              </div>
            </div>
          </CardContent>
        </Card>
        <Card className='bg-gradient-to-br from-amber-50 to-amber-100/50 dark:from-amber-950 dark:to-amber-900/50 border-amber-200/50 dark:border-amber-800/50'>
          <CardContent className='p-4'>
            <div className='flex items-center gap-3'>
              <div className='p-2 rounded-lg bg-amber-500/20 dark:bg-amber-500/30'>
                <Clock className='h-5 w-5 text-amber-600' />
              </div>
              <div>
                <p className='text-sm text-muted-foreground'>Upcoming</p>
                <p className='text-2xl font-bold'>{stats.upcoming}</p>
              </div>
            </div>
          </CardContent>
        </Card>
        <Card className='bg-gradient-to-br from-green-50 to-green-100/50 dark:from-green-950 dark:to-green-900/50 border-green-200/50 dark:border-green-800/50'>
          <CardContent className='p-4'>
            <div className='flex items-center gap-3'>
              <div className='p-2 rounded-lg bg-green-500/20 dark:bg-green-500/30'>
                <CheckCircle2 className='h-5 w-5 text-green-600' />
              </div>
              <div>
                <p className='text-sm text-muted-foreground'>Completed</p>
                <p className='text-2xl font-bold'>{stats.completed}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Search & Filters */}
      <div className='flex flex-col sm:flex-row gap-3'>
        <div className='relative flex-1'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
          <Input
            placeholder='Search activities...'
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              setCurrentPage(1);
            }}
            className='pl-10 pr-10'
          />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery('')}
              className='absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground'
            >
              <X className='h-4 w-4' />
            </button>
          )}
        </div>

        <Button
          variant={showFilters ? 'secondary' : 'outline'}
          onClick={() => setShowFilters(!showFilters)}
          className='gap-2'
        >
          <SlidersHorizontal className='h-4 w-4' />
          Filters
          {hasActiveFilters && (
            <span className='h-2 w-2 rounded-full bg-primary' />
          )}
        </Button>

        <div className='flex rounded-lg border bg-muted p-1'>
          <button
            onClick={() => setViewMode('list')}
            className={cn(
              'flex items-center justify-center h-8 w-8 rounded-md transition-colors',
              viewMode === 'list'
                ? 'bg-background shadow-sm'
                : 'hover:bg-background/50'
            )}
            title='List view'
          >
            <List className='h-4 w-4' />
          </button>
          <button
            onClick={() => setViewMode('calendar')}
            className={cn(
              'flex items-center justify-center h-8 w-8 rounded-md transition-colors',
              viewMode === 'calendar'
                ? 'bg-background shadow-sm'
                : 'hover:bg-background/50'
            )}
            title='Calendar view'
          >
            <Calendar className='h-4 w-4' />
          </button>
        </div>
      </div>

      {/* Expanded Filters */}
      {showFilters && (
        <Card>
          <CardContent className='p-4'>
            <div className='grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4'>
              <div>
                <label className='text-sm font-medium mb-1.5 block'>Type</label>
                <select
                  value={typeFilter}
                  onChange={(e) => {
                    setTypeFilter(e.target.value as ActivityType | '');
                    setCurrentPage(1);
                  }}
                  className='w-full px-3 py-2 border border-border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring'
                >
                  <option value=''>All Types</option>
                  {ACTIVITY_TYPES.map(({ type, label }) => (
                    <option key={type} value={type}>
                      {label}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Status
                </label>
                <select
                  value={statusFilter}
                  onChange={(e) => {
                    setStatusFilter(e.target.value as ActivityStatus | '');
                    setCurrentPage(1);
                  }}
                  className='w-full px-3 py-2 border border-border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring'
                >
                  <option value=''>All Statuses</option>
                  {FILTER_STATUSES.map((status) => (
                    <option key={status} value={status}>
                      {STATUS_CONFIG[status].label}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Priority
                </label>
                <select
                  value={priorityFilter}
                  onChange={(e) => {
                    setPriorityFilter(e.target.value as Priority | '');
                    setCurrentPage(1);
                  }}
                  className='w-full px-3 py-2 border border-border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring'
                >
                  <option value=''>All Priorities</option>
                  {Object.entries(PRIORITY_CONFIG).map(
                    ([priority, { label }]) => (
                      <option key={priority} value={priority}>
                        {label}
                      </option>
                    )
                  )}
                </select>
              </div>

              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Due Date From
                </label>
                <CRMDatePicker
                  value={dueDateFrom}
                  onChange={(date) => {
                    setDueDateFrom(date ? toLocalDateInputValue(date) : '');
                    setCurrentPage(1);
                  }}
                />
              </div>

              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Due Date To
                </label>
                <CRMDatePicker
                  value={dueDateTo}
                  onChange={(date) => {
                    setDueDateTo(date ? toLocalDateInputValue(date) : '');
                    setCurrentPage(1);
                  }}
                />
              </div>

              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Sort By
                </label>
                <select
                  value={sortBy}
                  onChange={(e) => {
                    setSortBy(e.target.value);
                    setCurrentPage(1);
                  }}
                  className='w-full px-3 py-2 border border-border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring'
                >
                  <option value='dueDate'>Due Date</option>
                  <option value='activityDate'>Activity Date</option>
                  <option value='createdAt'>Created Date</option>
                  <option value='priority'>Priority</option>
                  <option value='status'>Status</option>
                </select>
              </div>

              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Sort Order
                </label>
                <select
                  value={sortOrder}
                  onChange={(e) => {
                    setSortOrder(e.target.value as 'asc' | 'desc');
                    setCurrentPage(1);
                  }}
                  className='w-full px-3 py-2 border border-border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring'
                >
                  <option value='asc'>Ascending</option>
                  <option value='desc'>Descending</option>
                </select>
              </div>
            </div>

            {hasActiveFilters && (
              <div className='mt-4 pt-4 border-t flex items-center justify-between'>
                <p className='text-sm text-muted-foreground'>
                  {total} results found
                </p>
                <Button variant='ghost' size='sm' onClick={clearFilters}>
                  Clear all filters
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* Bulk Action Toolbar */}
      {selectedActivityIds.size > 0 && (
        <Card className='bg-primary/5 border-primary/20'>
          <CardContent className='p-4'>
            <div className='flex items-center justify-between'>
              <div className='flex items-center gap-3'>
                <span className='text-sm font-medium'>
                  {selectedActivityIds.size} selected
                </span>
                <Button
                  variant='outline'
                  size='sm'
                  onClick={() => setSelectedActivityIds(new Set())}
                >
                  Clear selection
                </Button>
              </div>
              <div className='flex items-center gap-2'>
                <Button
                  variant='outline'
                  size='sm'
                  onClick={() => handleBulkAction('COMPLETE')}
                  disabled={isBulkOperating}
                >
                  <CheckCircle2 className='h-4 w-4 mr-2' />
                  Complete
                </Button>
                <Button
                  variant='outline'
                  size='sm'
                  onClick={() => handleBulkAction('CANCEL')}
                  disabled={isBulkOperating}
                >
                  <X className='h-4 w-4 mr-2' />
                  Cancel
                </Button>
                <Button
                  variant='destructive'
                  size='sm'
                  onClick={() => handleBulkAction('DELETE')}
                  disabled={isBulkOperating}
                >
                  <Trash2 className='h-4 w-4 mr-2' />
                  Delete
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Activity List */}
      {viewMode === 'list' && (
        <div className='space-y-3'>
          {activityQuery.isLoading && (
            <Card>
              <CardContent className='py-12 text-center text-muted-foreground'>
                Loading activities...
              </CardContent>
            </Card>
          )}

          {activityQuery.isError && (
            <Card>
              <CardContent className='py-12 text-center text-destructive'>
                Failed to load activities.
              </CardContent>
            </Card>
          )}

          {!activityQuery.isLoading &&
            !activityQuery.isError &&
            activities.length > 0 && (
              <Card className='mb-3'>
                <CardContent className='p-4'>
                  <label className='flex items-center gap-3 cursor-pointer'>
                    <input
                      type='checkbox'
                      checked={selectedActivityIds.size === activities.length}
                      onChange={handleSelectAll}
                      className='h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary'
                    />
                    <span className='text-sm font-medium'>
                      Select all ({activities.length})
                    </span>
                  </label>
                </CardContent>
              </Card>
            )}

          {!activityQuery.isLoading &&
            !activityQuery.isError &&
            activities.map((activity: Activity) => {
              const Icon = getActivityIcon(activity.type);
              const colorClass = getActivityColor(activity.type);
              const displayStatus = getActivityDisplayStatus(activity);
              const statusConfig = STATUS_CONFIG[displayStatus];
              const isSelected = selectedActivityIds.has(activity.id);

              return (
                <Card
                  key={activity.id}
                  className={cn(
                    'hover:shadow-md transition-shadow',
                    isSelected && 'ring-2 ring-primary'
                  )}
                >
                  <CardContent className='p-4'>
                    <div className='flex items-start gap-4'>
                      {/* Checkbox */}
                      <input
                        type='checkbox'
                        checked={isSelected}
                        onChange={(e) => {
                          e.stopPropagation();
                          handleSelectActivity(activity.id);
                        }}
                        onClick={(e) => e.stopPropagation()}
                        className='mt-1 h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary cursor-pointer'
                      />

                      {/* Icon */}
                      <div
                        className={cn(
                          'p-2.5 rounded-xl cursor-pointer',
                          colorClass.split(' ')[1]
                        )}
                        onClick={() => handleViewActivity(activity.id)}
                      >
                        <Icon
                          className={cn('h-5 w-5', colorClass.split(' ')[0])}
                        />
                      </div>

                      {/* Content */}
                      <div
                        className='flex-1 min-w-0 cursor-pointer'
                        onClick={() => handleViewActivity(activity.id)}
                      >
                        <div className='flex items-start justify-between gap-2'>
                          <div>
                            <h3 className='font-semibold text-sm'>
                              {activity.subject}
                            </h3>
                            <p className='text-sm text-muted-foreground mt-0.5 line-clamp-1'>
                              {activity.description}
                            </p>
                          </div>
                          <Badge className={cn('shrink-0', statusConfig.color)}>
                            {statusConfig.label}
                          </Badge>
                        </div>

                        <div className='flex items-center gap-4 mt-3 text-xs text-muted-foreground'>
                          <span className='flex items-center gap-1'>
                            <Calendar className='h-3.5 w-3.5' />
                            {formatDate(activity.scheduledDate)}
                          </span>
                          <span>•</span>
                          <span>
                            {activity.relatedTo.type}: {activity.relatedTo.name}
                          </span>
                          <span>•</span>
                          <span>Assigned: {activity.assignedToName}</span>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              );
            })}

          {!activityQuery.isLoading &&
            !activityQuery.isError &&
            activities.length === 0 && (
              <Card>
                <CardContent className='py-16 text-center'>
                  <div className='mx-auto w-20 h-20 bg-muted rounded-full flex items-center justify-center mb-4'>
                    <Calendar className='w-10 h-10 text-muted-foreground' />
                  </div>
                  <h3 className='text-lg font-semibold mb-2'>
                    No activities found
                  </h3>
                  <p className='text-muted-foreground mb-6 max-w-sm mx-auto'>
                    {hasActiveFilters
                      ? 'Try adjusting your filters.'
                      : 'Start by logging your first activity.'}
                  </p>
                  {hasActiveFilters ? (
                    <Button variant='outline' onClick={clearFilters}>
                      Clear Filters
                    </Button>
                  ) : (
                    <Button>
                      <Plus className='h-4 w-4 mr-2' />
                      Log First Activity
                    </Button>
                  )}
                </CardContent>
              </Card>
            )}
        </div>
      )}

      {/* Calendar View */}
      {viewMode === 'calendar' && (
        <div className='space-y-4'>
          {/* Navigation and views toolbar */}
          <div className='flex items-center justify-between p-4 bg-muted/20 border rounded-lg'>
            <div className='flex items-center gap-2'>
              <div className='flex items-center gap-1'>
                <Button
                  variant='outline'
                  size='icon'
                  className='h-8 w-8'
                  onClick={handleCalendarPrev}
                >
                  <ChevronLeft className='h-4 w-4' />
                </Button>
                <Button
                  variant='outline'
                  size='sm'
                  className='h-8 px-3'
                  onClick={handleCalendarToday}
                >
                  Today
                </Button>
                <Button
                  variant='outline'
                  size='icon'
                  className='h-8 w-8'
                  onClick={handleCalendarNext}
                >
                  <ChevronRight className='h-4 w-4' />
                </Button>
              </div>
              <span className='text-sm font-semibold ml-2'>
                {calendarTitle}
              </span>
            </div>
            <div className='flex items-center gap-1 bg-muted p-0.5 rounded-md'>
              <Button
                variant={calendarView === 'month' ? 'secondary' : 'ghost'}
                size='sm'
                className='h-7 text-xs cursor-pointer'
                onClick={() => setCalendarView('month')}
              >
                Month
              </Button>
              <Button
                variant={calendarView === 'week' ? 'secondary' : 'ghost'}
                size='sm'
                className='h-7 text-xs cursor-pointer'
                onClick={() => setCalendarView('week')}
              >
                Week
              </Button>
            </div>
          </div>

          <DndContext onDragEnd={handleDragEnd}>
            <CRMCalendarGrid
              days={calendarDays}
              view={calendarView}
              activitiesByDay={activitiesByDay}
              onActivityClick={(activity) => handleViewActivity(activity.id)}
              onAddActivity={handleCalendarCellClick}
            />
          </DndContext>
        </div>
      )}

      {/* Pagination */}
      {viewMode === 'list' && total > pageSize && (
        <div className='flex items-center justify-between pt-4'>
          <p className='text-sm text-muted-foreground'>
            Showing {(currentPage - 1) * pageSize + 1} to{' '}
            {Math.min(currentPage * pageSize, total)} of {total} activities
          </p>
          <div className='flex items-center gap-2'>
            <Button
              variant='outline'
              size='sm'
              disabled={currentPage === 1}
              onClick={() => setCurrentPage(currentPage - 1)}
            >
              <ChevronLeft className='h-4 w-4' />
              Previous
            </Button>
            <Button
              variant='outline'
              size='sm'
              disabled={currentPage === totalPages}
              onClick={() => setCurrentPage(currentPage + 1)}
            >
              Next
              <ChevronRight className='h-4 w-4' />
            </Button>
          </div>
        </div>
      )}

      {/* Quick Add Dialog */}
      <QuickAddActivityDialog
        open={showQuickAdd}
        onOpenChange={(isOpen) => {
          setShowQuickAdd(isOpen);
          if (!isOpen) {
            setPreselectedDate('');
          }
        }}
        onSubmit={handleQuickAddActivity}
        isLoading={isCreating}
        preselectedDate={preselectedDate || undefined}
      />
    </div>
  );
};

export default ActivityListPage;
