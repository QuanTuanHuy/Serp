// ActivityListPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useState, useMemo } from 'react';
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
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { ExportDropdown } from '../../components/shared';
import { QuickAddActivityDialog } from '../../components/dialogs';
import { ACTIVITY_EXPORT_COLUMNS } from '../../utils/export';
import { MOCK_ACTIVITIES } from '../../mocks';
import type { Activity, ActivityType, ActivityStatus } from '../../types';

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
    type: 'NOTE',
    label: 'Note',
    icon: FileText,
    color: 'text-gray-600 dark:text-gray-300 bg-gray-100 dark:bg-gray-800',
  },
  {
    type: 'TASK',
    label: 'Task',
    icon: CheckCircle2,
    color: 'text-orange-600 bg-orange-100',
  },
  {
    type: 'FOLLOW_UP',
    label: 'Follow Up',
    icon: Clock,
    color: 'text-amber-600 bg-amber-100',
  },
];

const STATUS_CONFIG: Record<ActivityStatus, { label: string; color: string }> =
  {
    PLANNED: { label: 'Planned', color: 'bg-blue-100 text-blue-700' },
    IN_PROGRESS: { label: 'In Progress', color: 'bg-amber-100 text-amber-700' },
    COMPLETED: { label: 'Completed', color: 'bg-green-100 text-green-700' },
    CANCELLED: {
      label: 'Cancelled',
      color: 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300',
    },
    OVERDUE: { label: 'Overdue', color: 'bg-red-100 text-red-700' },
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
  const [viewMode, setViewMode] = useState<'list' | 'calendar'>('list');
  const [showFilters, setShowFilters] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [showQuickAdd, setShowQuickAdd] = useState(false);
  const pageSize = 10;

  // Filter and sort activities
  const filteredActivities = useMemo(() => {
    let result = [...MOCK_ACTIVITIES];

    // Search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      result = result.filter(
        (activity) =>
          activity.subject.toLowerCase().includes(query) ||
          activity.description?.toLowerCase().includes(query) ||
          activity.relatedTo.name.toLowerCase().includes(query)
      );
    }

    // Type filter
    if (typeFilter) {
      result = result.filter((activity) => activity.type === typeFilter);
    }

    // Status filter
    if (statusFilter) {
      result = result.filter((activity) => activity.status === statusFilter);
    }

    // Sort by scheduled date (most recent first)
    result.sort((a, b) => {
      const dateA = a.scheduledDate ? new Date(a.scheduledDate).getTime() : 0;
      const dateB = b.scheduledDate ? new Date(b.scheduledDate).getTime() : 0;
      return dateB - dateA;
    });

    return result;
  }, [searchQuery, typeFilter, statusFilter]);

  // Paginate
  const paginatedActivities = useMemo(() => {
    const start = (currentPage - 1) * pageSize;
    return filteredActivities.slice(start, start + pageSize);
  }, [filteredActivities, currentPage]);

  const total = filteredActivities.length;
  const totalPages = Math.ceil(total / pageSize);

  // Stats
  const stats = useMemo(() => {
    const today = new Date().toISOString().split('T')[0];
    const todayActivities = MOCK_ACTIVITIES.filter(
      (a) => a.scheduledDate?.split('T')[0] === today
    );
    const overdue = MOCK_ACTIVITIES.filter(
      (a) =>
        a.status === 'OVERDUE' ||
        (a.status === 'PLANNED' &&
          a.scheduledDate &&
          new Date(a.scheduledDate) < new Date())
    );
    const planned = MOCK_ACTIVITIES.filter((a) => a.status === 'PLANNED');
    const completed = MOCK_ACTIVITIES.filter((a) => a.status === 'COMPLETED');

    return {
      today: todayActivities.length,
      overdue: overdue.length,
      planned: planned.length,
      completed: completed.length,
    };
  }, []);

  const clearFilters = () => {
    setSearchQuery('');
    setTypeFilter('');
    setStatusFilter('');
    setCurrentPage(1);
  };

  const handleQuickAddActivity = async (data: unknown) => {
    console.log('Quick adding activity:', data);
    setShowQuickAdd(false);
  };

  const hasActiveFilters = searchQuery || typeFilter || statusFilter;

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
          <ExportDropdown
            data={filteredActivities}
            columns={ACTIVITY_EXPORT_COLUMNS}
            filename='activities'
            onExportComplete={(format, count) => {
              console.log(`Exported ${count} activities as ${format}`);
            }}
          />
          <Button
            variant='outline'
            onClick={() => setShowQuickAdd(true)}
            className='gap-2'
          >
            <Plus className='h-4 w-4' />
            Thêm nhanh
          </Button>
          <Button className='gap-2'>
            <Plus className='h-4 w-4' />
            Log Activity
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
                <p className='text-sm text-muted-foreground'>Today</p>
                <p className='text-2xl font-bold'>{stats.today}</p>
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
                <p className='text-sm text-muted-foreground'>Planned</p>
                <p className='text-2xl font-bold'>{stats.planned}</p>
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
            <div className='grid grid-cols-1 sm:grid-cols-2 gap-4'>
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
                  {Object.entries(STATUS_CONFIG).map(([status, { label }]) => (
                    <option key={status} value={status}>
                      {label}
                    </option>
                  ))}
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

      {/* Activity List */}
      {viewMode === 'list' && (
        <div className='space-y-3'>
          {paginatedActivities.map((activity) => {
            const Icon = getActivityIcon(activity.type);
            const colorClass = getActivityColor(activity.type);
            const statusConfig = STATUS_CONFIG[activity.status];

            return (
              <Card
                key={activity.id}
                className='hover:shadow-md transition-shadow cursor-pointer'
              >
                <CardContent className='p-4'>
                  <div className='flex items-start gap-4'>
                    {/* Icon */}
                    <div
                      className={cn(
                        'p-2.5 rounded-xl',
                        colorClass.split(' ')[1]
                      )}
                    >
                      <Icon
                        className={cn('h-5 w-5', colorClass.split(' ')[0])}
                      />
                    </div>

                    {/* Content */}
                    <div className='flex-1 min-w-0'>
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

          {paginatedActivities.length === 0 && (
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

      {/* Calendar View Placeholder */}
      {viewMode === 'calendar' && (
        <Card>
          <CardContent className='py-16 text-center'>
            <div className='mx-auto w-20 h-20 bg-muted rounded-full flex items-center justify-center mb-4'>
              <Calendar className='w-10 h-10 text-muted-foreground' />
            </div>
            <h3 className='text-lg font-semibold mb-2'>Calendar View</h3>
            <p className='text-muted-foreground max-w-sm mx-auto'>
              Calendar view coming soon. Switch to list view to see all
              activities.
            </p>
          </CardContent>
        </Card>
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
        onOpenChange={setShowQuickAdd}
        onSubmit={handleQuickAddActivity}
      />
    </div>
  );
};

export default ActivityListPage;
