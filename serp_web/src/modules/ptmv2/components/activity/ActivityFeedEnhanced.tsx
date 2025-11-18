/**
 * PTM v2 - Enhanced Activity Feed Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Advanced activity tracking with grouping and insights
 */

'use client';

import { useState, useEffect, useMemo } from 'react';
import {
  CheckCircle2,
  Circle,
  Clock,
  Edit,
  Trash2,
  Plus,
  Filter,
  Calendar,
  Tag,
  FolderOpen,
  Bell,
  Search,
  ExternalLink,
  Sparkles,
  TrendingUp,
  Activity as ActivityIcon,
} from 'lucide-react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Badge } from '@/shared/components/ui/badge';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import { cn } from '@/shared/utils';
import {
  format,
  formatDistanceToNow,
  isToday,
  isYesterday,
  startOfDay,
  isThisWeek,
} from 'date-fns';
import type { Activity, ActivityType } from './ActivityFeed';

interface ActivityFeedEnhancedProps {
  className?: string;
  maxItems?: number;
}

// Extended mock data with more variety
const EXTENDED_MOCK_ACTIVITIES: Activity[] = [
  {
    id: '1',
    type: 'task_completed',
    userId: 'user1',
    userName: 'You',
    title: 'Completed task "Implement Activity Feed"',
    description: 'Phase 3 feature',
    metadata: { taskId: 'task1', duration: 45, projectName: 'PTM v2' },
    createdAt: Date.now() - 1000 * 60 * 5,
  },
  {
    id: '2',
    type: 'task_created',
    userId: 'user1',
    userName: 'You',
    title: 'Created task "Build Analytics Dashboard"',
    metadata: { taskId: 'task2', priority: 'high', projectName: 'Analytics' },
    createdAt: Date.now() - 1000 * 60 * 15,
  },
  {
    id: '3',
    type: 'schedule_created',
    userId: 'user1',
    userName: 'You',
    title: 'Scheduled focus time',
    description: '9:00 AM - 11:00 AM',
    metadata: { eventId: 'event1', isDeepWork: true },
    createdAt: Date.now() - 1000 * 60 * 30,
  },
  {
    id: '4',
    type: 'task_updated',
    userId: 'user1',
    userName: 'You',
    title: 'Updated task "Calendar Drag & Drop"',
    description: 'Changed status to In Progress',
    metadata: {
      taskId: 'task3',
      field: 'status',
      newValue: 'in_progress',
      projectName: 'PTM v2',
    },
    createdAt: Date.now() - 1000 * 60 * 45,
  },
  {
    id: '5',
    type: 'project_created',
    userId: 'user1',
    userName: 'You',
    title: 'Created project "PTM v2 Phase 3"',
    metadata: { projectId: 'proj1' },
    createdAt: Date.now() - 1000 * 60 * 60,
  },
  {
    id: '6',
    type: 'task_completed',
    userId: 'user1',
    userName: 'You',
    title: 'Completed task "Fix authentication bug"',
    metadata: { taskId: 'task5', duration: 30, projectName: 'Bug Fixes' },
    createdAt: Date.now() - 1000 * 60 * 60 * 2,
  },
  {
    id: '7',
    type: 'task_created',
    userId: 'user1',
    userName: 'You',
    title: 'Created task "Write documentation"',
    metadata: { taskId: 'task6', priority: 'medium', projectName: 'Docs' },
    createdAt: Date.now() - 1000 * 60 * 60 * 3,
  },
  {
    id: '8',
    type: 'schedule_updated',
    userId: 'user1',
    userName: 'You',
    title: 'Rescheduled team meeting',
    description: 'Moved from 2 PM to 3 PM',
    metadata: { eventId: 'event2' },
    createdAt: Date.now() - 1000 * 60 * 60 * 24, // Yesterday
  },
  {
    id: '9',
    type: 'task_completed',
    userId: 'user1',
    userName: 'You',
    title: 'Completed task "Database migration"',
    metadata: { taskId: 'task7', duration: 120, projectName: 'Backend' },
    createdAt: Date.now() - 1000 * 60 * 60 * 24,
  },
  {
    id: '10',
    type: 'project_updated',
    userId: 'user1',
    userName: 'You',
    title: 'Updated project "Mobile App"',
    description: 'Changed due date',
    metadata: { projectId: 'proj2' },
    createdAt: Date.now() - 1000 * 60 * 60 * 24 * 2,
  },
];

const getActivityIcon = (type: ActivityType) => {
  switch (type) {
    case 'task_created':
      return <Plus className='h-4 w-4' />;
    case 'task_updated':
      return <Edit className='h-4 w-4' />;
    case 'task_completed':
      return <CheckCircle2 className='h-4 w-4' />;
    case 'task_deleted':
      return <Trash2 className='h-4 w-4' />;
    case 'project_created':
    case 'project_updated':
      return <FolderOpen className='h-4 w-4' />;
    case 'schedule_created':
      return <Calendar className='h-4 w-4' />;
    case 'schedule_updated':
      return <Clock className='h-4 w-4' />;
    case 'schedule_deleted':
      return <Trash2 className='h-4 w-4' />;
    default:
      return <Circle className='h-4 w-4' />;
  }
};

const getActivityColor = (type: ActivityType) => {
  switch (type) {
    case 'task_created':
    case 'project_created':
    case 'schedule_created':
      return 'text-green-600 bg-green-50 dark:bg-green-950/20 border-green-200';
    case 'task_updated':
    case 'project_updated':
    case 'schedule_updated':
      return 'text-blue-600 bg-blue-50 dark:bg-blue-950/20 border-blue-200';
    case 'task_completed':
      return 'text-purple-600 bg-purple-50 dark:bg-purple-950/20 border-purple-200';
    case 'task_deleted':
    case 'schedule_deleted':
      return 'text-red-600 bg-red-50 dark:bg-red-950/20 border-red-200';
    default:
      return 'text-gray-600 bg-gray-50 dark:bg-gray-950/20 border-gray-200';
  }
};

const getDateGroupLabel = (date: Date) => {
  if (isToday(date)) return 'Today';
  if (isYesterday(date)) return 'Yesterday';
  if (isThisWeek(date)) return format(date, 'EEEE'); // Monday, Tuesday, etc.
  return format(date, 'MMM d, yyyy');
};

export function ActivityFeedEnhanced({
  className,
  maxItems = 50,
}: ActivityFeedEnhancedProps) {
  const [activities, setActivities] = useState<Activity[]>(
    EXTENDED_MOCK_ACTIVITIES
  );
  const [searchQuery, setSearchQuery] = useState('');
  const [typeFilter, setTypeFilter] = useState<ActivityType | 'all'>('all');
  const [timeRange, setTimeRange] = useState<
    'today' | 'week' | 'month' | 'all'
  >('all');
  const [isConnected, setIsConnected] = useState(false);

  // Simulate WebSocket connection
  useEffect(() => {
    const timer = setTimeout(() => setIsConnected(true), 500);
    return () => clearTimeout(timer);
  }, []);

  // Filter activities
  const filteredActivities = useMemo(() => {
    let filtered = activities;

    // Search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(
        (activity) =>
          activity.title.toLowerCase().includes(query) ||
          activity.description?.toLowerCase().includes(query) ||
          activity.metadata?.projectName?.toLowerCase().includes(query)
      );
    }

    // Type filter
    if (typeFilter !== 'all') {
      filtered = filtered.filter((activity) => activity.type === typeFilter);
    }

    // Time range filter
    if (timeRange !== 'all') {
      const now = Date.now();
      const dayMs = 1000 * 60 * 60 * 24;
      let cutoff: number;

      switch (timeRange) {
        case 'today':
          cutoff = startOfDay(now).getTime();
          break;
        case 'week':
          cutoff = now - dayMs * 7;
          break;
        case 'month':
          cutoff = now - dayMs * 30;
          break;
        default:
          cutoff = 0;
      }

      filtered = filtered.filter((activity) => activity.createdAt >= cutoff);
    }

    return filtered.slice(0, maxItems);
  }, [activities, searchQuery, typeFilter, timeRange, maxItems]);

  // Group activities by date
  const groupedActivities = useMemo(() => {
    const groups: Record<string, Activity[]> = {};

    filteredActivities.forEach((activity) => {
      const date = new Date(activity.createdAt);
      const dayKey = format(startOfDay(date), 'yyyy-MM-dd');

      if (!groups[dayKey]) {
        groups[dayKey] = [];
      }
      groups[dayKey].push(activity);
    });

    return Object.entries(groups).sort(([a], [b]) => b.localeCompare(a));
  }, [filteredActivities]);

  // Calculate insights
  const insights = useMemo(() => {
    const completedTasks = activities.filter(
      (a) => a.type === 'task_completed'
    ).length;
    const totalTime = activities
      .filter((a) => a.metadata?.duration)
      .reduce((sum, a) => sum + (a.metadata?.duration || 0), 0);

    return {
      completedTasks,
      totalTime,
      averageTaskTime: completedTasks > 0 ? totalTime / completedTasks : 0,
      mostActiveDay: 'Today',
    };
  }, [activities]);

  const handleActivityClick = (activity: Activity) => {
    // Navigate to related item
    if (activity.metadata?.taskId) {
      console.log('Navigate to task:', activity.metadata.taskId);
      // router.push(`/ptmv2/tasks/${activity.metadata.taskId}`);
    } else if (activity.metadata?.projectId) {
      console.log('Navigate to project:', activity.metadata.projectId);
    } else if (activity.metadata?.eventId) {
      console.log('Navigate to schedule:', activity.metadata.eventId);
    }
  };

  return (
    <div className={cn('space-y-4', className)}>
      {/* AI Insights Card */}
      <Card className='bg-gradient-to-br from-purple-50 to-blue-50 dark:from-purple-950/20 dark:to-blue-950/20 border-purple-200 dark:border-purple-800'>
        <CardContent className='pt-6'>
          <div className='flex items-start gap-3'>
            <Sparkles className='h-5 w-5 text-purple-600 flex-shrink-0 mt-0.5' />
            <div className='space-y-2'>
              <p className='font-medium text-purple-900 dark:text-purple-100'>
                💡 Productivity Insights
              </p>
              <div className='grid grid-cols-2 md:grid-cols-4 gap-4 text-sm'>
                <div>
                  <p className='text-purple-700 dark:text-purple-300'>
                    {insights.completedTasks} tasks completed
                  </p>
                </div>
                <div>
                  <p className='text-purple-700 dark:text-purple-300'>
                    {Math.floor(insights.totalTime / 60)}h{' '}
                    {insights.totalTime % 60}m total time
                  </p>
                </div>
                <div>
                  <p className='text-purple-700 dark:text-purple-300'>
                    {insights.averageTaskTime.toFixed(0)}m avg per task
                  </p>
                </div>
                <div>
                  <p className='text-purple-700 dark:text-purple-300'>
                    Most active: {insights.mostActiveDay}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Filters */}
      <Card>
        <CardHeader>
          <div className='flex items-center justify-between'>
            <div className='flex items-center gap-2'>
              <Bell className='h-5 w-5 text-blue-600' />
              <CardTitle>Activity Timeline</CardTitle>
              <Badge
                variant='outline'
                className={cn(
                  'ml-2',
                  isConnected
                    ? 'bg-green-100 dark:bg-green-950/20 text-green-700 border-green-200'
                    : 'bg-gray-100'
                )}
              >
                <span
                  className={cn(
                    'w-2 h-2 rounded-full mr-1.5',
                    isConnected ? 'bg-green-500 animate-pulse' : 'bg-gray-400'
                  )}
                />
                {isConnected ? 'Live' : 'Connecting...'}
              </Badge>
            </div>
          </div>
          <CardDescription>
            Track all changes and updates across your workspace
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-4'>
          {/* Search and Filters */}
          <div className='flex flex-col md:flex-row gap-3'>
            <div className='relative flex-1'>
              <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
              <Input
                placeholder='Search activities...'
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className='pl-9'
              />
            </div>

            <Select
              value={typeFilter}
              onValueChange={(v) => setTypeFilter(v as any)}
            >
              <SelectTrigger className='w-full md:w-[180px]'>
                <Filter className='h-4 w-4 mr-2' />
                <SelectValue placeholder='Type' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>All Types</SelectItem>
                <SelectItem value='task_created'>Task Created</SelectItem>
                <SelectItem value='task_updated'>Task Updated</SelectItem>
                <SelectItem value='task_completed'>Task Completed</SelectItem>
                <SelectItem value='task_deleted'>Task Deleted</SelectItem>
                <SelectItem value='project_created'>Project Created</SelectItem>
                <SelectItem value='schedule_created'>
                  Schedule Created
                </SelectItem>
              </SelectContent>
            </Select>

            <Tabs
              value={timeRange}
              onValueChange={(v: any) => setTimeRange(v)}
              className='w-full md:w-auto'
            >
              <TabsList className='grid w-full grid-cols-4'>
                <TabsTrigger value='today'>Today</TabsTrigger>
                <TabsTrigger value='week'>Week</TabsTrigger>
                <TabsTrigger value='month'>Month</TabsTrigger>
                <TabsTrigger value='all'>All</TabsTrigger>
              </TabsList>
            </Tabs>
          </div>

          {/* Timeline */}
          {filteredActivities.length === 0 ? (
            <div className='py-12 text-center'>
              <ActivityIcon className='h-12 w-12 text-muted-foreground mx-auto mb-4 opacity-50' />
              <p className='text-lg font-medium text-muted-foreground'>
                No activities found
              </p>
              <p className='text-sm text-muted-foreground mt-1'>
                {searchQuery
                  ? 'Try adjusting your search or filters'
                  : 'Activities will appear here as you work'}
              </p>
            </div>
          ) : (
            <div className='space-y-6'>
              {groupedActivities.map(([dateKey, dateActivities]) => {
                const date = new Date(dateKey);
                return (
                  <div key={dateKey}>
                    {/* Date Header */}
                    <div className='flex items-center gap-3 mb-4'>
                      <h3 className='font-semibold text-sm'>
                        {getDateGroupLabel(date)}
                      </h3>
                      <div className='flex-1 h-px bg-border' />
                      <Badge variant='secondary' className='text-xs'>
                        {dateActivities.length}
                      </Badge>
                    </div>

                    {/* Activities */}
                    <div className='space-y-3 pl-4 border-l-2 border-border'>
                      {dateActivities.map((activity, index) => (
                        <div
                          key={activity.id}
                          onClick={() => handleActivityClick(activity)}
                          className={cn(
                            'group relative -ml-4 pl-4',
                            activity.metadata?.taskId ||
                              activity.metadata?.projectId ||
                              activity.metadata?.eventId
                              ? 'cursor-pointer'
                              : ''
                          )}
                        >
                          {/* Timeline dot */}
                          <div
                            className={cn(
                              'absolute left-0 top-2 -translate-x-1/2 w-3 h-3 rounded-full border-2 border-background',
                              getActivityColor(activity.type).split(' ')[1]
                            )}
                          />

                          {/* Activity Card */}
                          <div
                            className={cn(
                              'rounded-lg border p-4 transition-all',
                              'hover:shadow-sm hover:border-primary/50',
                              index === 0 &&
                                isToday(date) &&
                                'bg-blue-50/50 dark:bg-blue-950/10'
                            )}
                          >
                            <div className='flex gap-3'>
                              <div
                                className={cn(
                                  'flex items-center justify-center w-8 h-8 rounded-full flex-shrink-0 border',
                                  getActivityColor(activity.type)
                                )}
                              >
                                {getActivityIcon(activity.type)}
                              </div>

                              <div className='flex-1 min-w-0'>
                                <div className='flex items-start justify-between gap-2'>
                                  <p className='font-medium text-sm'>
                                    {activity.title}
                                  </p>
                                  {(activity.metadata?.taskId ||
                                    activity.metadata?.projectId ||
                                    activity.metadata?.eventId) && (
                                    <ExternalLink className='h-3.5 w-3.5 text-muted-foreground opacity-0 group-hover:opacity-100 transition-opacity flex-shrink-0' />
                                  )}
                                </div>

                                {activity.description && (
                                  <p className='text-sm text-muted-foreground mt-1'>
                                    {activity.description}
                                  </p>
                                )}

                                <div className='flex flex-wrap items-center gap-3 mt-2'>
                                  <span className='text-xs text-muted-foreground'>
                                    {formatDistanceToNow(
                                      new Date(activity.createdAt),
                                      { addSuffix: true }
                                    )}
                                  </span>

                                  {activity.metadata?.duration && (
                                    <Badge
                                      variant='outline'
                                      className='text-xs gap-1'
                                    >
                                      <Clock className='h-3 w-3' />
                                      {activity.metadata.duration}m
                                    </Badge>
                                  )}

                                  {activity.metadata?.priority && (
                                    <Badge
                                      variant='outline'
                                      className={cn(
                                        'text-xs',
                                        activity.metadata.priority === 'high'
                                          ? 'border-red-200 text-red-700 bg-red-50 dark:bg-red-950/20'
                                          : activity.metadata.priority ===
                                              'medium'
                                            ? 'border-amber-200 text-amber-700 bg-amber-50 dark:bg-amber-950/20'
                                            : 'border-blue-200 text-blue-700 bg-blue-50 dark:bg-blue-950/20'
                                      )}
                                    >
                                      {activity.metadata.priority}
                                    </Badge>
                                  )}

                                  {activity.metadata?.projectName && (
                                    <Badge
                                      variant='outline'
                                      className='text-xs gap-1'
                                    >
                                      <FolderOpen className='h-3 w-3' />
                                      {activity.metadata.projectName}
                                    </Badge>
                                  )}

                                  {activity.metadata?.isDeepWork && (
                                    <Badge
                                      variant='outline'
                                      className='text-xs gap-1 border-purple-200 text-purple-700 bg-purple-50 dark:bg-purple-950/20'
                                    >
                                      🔥 Focus
                                    </Badge>
                                  )}
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                );
              })}
            </div>
          )}

          {/* Load More */}
          {filteredActivities.length >= maxItems && (
            <div className='pt-4 border-t text-center'>
              <Button variant='outline' size='sm'>
                Load More Activities
              </Button>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
