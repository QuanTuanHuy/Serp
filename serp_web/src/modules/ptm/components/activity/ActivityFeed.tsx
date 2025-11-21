/**
 * PTM v2 - Activity Feed Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Real-time activity tracking with WebSocket
 */

'use client';

import { useState, useEffect } from 'react';
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
} from 'lucide-react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { cn } from '@/shared/utils';
import { formatDistanceToNow } from 'date-fns';

export type ActivityType =
  | 'task_created'
  | 'task_updated'
  | 'task_completed'
  | 'task_deleted'
  | 'project_created'
  | 'project_updated'
  | 'schedule_created'
  | 'schedule_updated'
  | 'schedule_deleted';

export interface Activity {
  id: string;
  type: ActivityType;
  userId: string;
  userName: string;
  title: string;
  description?: string;
  metadata?: Record<string, any>;
  createdAt: number;
}

interface ActivityFeedProps {
  className?: string;
  maxItems?: number;
}

const MOCK_ACTIVITIES: Activity[] = [
  {
    id: '1',
    type: 'task_completed',
    userId: 'user1',
    userName: 'You',
    title: 'Completed task "Implement Activity Feed"',
    description: 'Phase 3 feature',
    metadata: { taskId: 'task1', duration: 45 },
    createdAt: Date.now() - 1000 * 60 * 5,
  },
  {
    id: '2',
    type: 'task_created',
    userId: 'user1',
    userName: 'You',
    title: 'Created task "Build Analytics Dashboard"',
    metadata: { taskId: 'task2', priority: 'high' },
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
    metadata: { taskId: 'task3', field: 'status', newValue: 'in_progress' },
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
    type: 'task_deleted',
    userId: 'user1',
    userName: 'You',
    title: 'Deleted task "Old feature request"',
    metadata: { taskId: 'task4' },
    createdAt: Date.now() - 1000 * 60 * 90,
  },
  {
    id: '7',
    type: 'schedule_updated',
    userId: 'user1',
    userName: 'You',
    title: 'Rescheduled meeting',
    description: 'Moved from 2 PM to 3 PM',
    metadata: { eventId: 'event2' },
    createdAt: Date.now() - 1000 * 60 * 120,
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
      return <FolderOpen className='h-4 w-4' />;
    case 'project_updated':
      return <Edit className='h-4 w-4' />;
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
      return 'text-green-600 bg-green-50 dark:bg-green-950/20';
    case 'task_updated':
    case 'project_updated':
    case 'schedule_updated':
      return 'text-blue-600 bg-blue-50 dark:bg-blue-950/20';
    case 'task_completed':
      return 'text-purple-600 bg-purple-50 dark:bg-purple-950/20';
    case 'task_deleted':
    case 'schedule_deleted':
      return 'text-red-600 bg-red-50 dark:bg-red-950/20';
    default:
      return 'text-gray-600 bg-gray-50 dark:bg-gray-950/20';
  }
};

export function ActivityFeed({ className, maxItems = 20 }: ActivityFeedProps) {
  const [activities, setActivities] = useState<Activity[]>(MOCK_ACTIVITIES);
  const [filter, setFilter] = useState<ActivityType | 'all'>('all');
  const [isConnected, setIsConnected] = useState(false);

  // Simulate WebSocket connection
  useEffect(() => {
    // Simulate connection delay
    const timer = setTimeout(() => setIsConnected(true), 500);

    // Simulate incoming activity
    const interval = setInterval(() => {
      const newActivity: Activity = {
        id: Date.now().toString(),
        type: 'task_updated',
        userId: 'user1',
        userName: 'You',
        title: 'Updated task (live update)',
        description: 'This is a simulated real-time update',
        createdAt: Date.now(),
      };
      setActivities((prev) => [newActivity, ...prev].slice(0, maxItems));
    }, 30000); // Every 30s

    return () => {
      clearTimeout(timer);
      clearInterval(interval);
    };
  }, [maxItems]);

  const filteredActivities =
    filter === 'all'
      ? activities
      : activities.filter((activity) => activity.type === filter);

  return (
    <div className={cn('space-y-4', className)}>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div className='flex items-center gap-2'>
          <Bell className='h-5 w-5 text-blue-600' />
          <h3 className='font-semibold'>Activity Feed</h3>
          <span
            className={cn(
              'flex items-center gap-1.5 text-xs px-2 py-1 rounded-full',
              isConnected
                ? 'bg-green-100 dark:bg-green-950/20 text-green-700 dark:text-green-400'
                : 'bg-gray-100 dark:bg-gray-800 text-gray-500'
            )}
          >
            <span
              className={cn(
                'w-2 h-2 rounded-full',
                isConnected ? 'bg-green-500 animate-pulse' : 'bg-gray-400'
              )}
            />
            {isConnected ? 'Live' : 'Connecting...'}
          </span>
        </div>

        <Select value={filter} onValueChange={(v) => setFilter(v as any)}>
          <SelectTrigger className='w-[160px]'>
            <Filter className='h-4 w-4 mr-2' />
            <SelectValue placeholder='Filter' />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value='all'>All Activities</SelectItem>
            <SelectItem value='task_created'>Task Created</SelectItem>
            <SelectItem value='task_updated'>Task Updated</SelectItem>
            <SelectItem value='task_completed'>Task Completed</SelectItem>
            <SelectItem value='task_deleted'>Task Deleted</SelectItem>
            <SelectItem value='project_created'>Project Created</SelectItem>
            <SelectItem value='schedule_created'>Schedule Created</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* Feed */}
      <Card>
        <CardContent className='p-0'>
          {filteredActivities.length === 0 ? (
            <div className='p-8 text-center text-muted-foreground'>
              No activities yet
            </div>
          ) : (
            <div className='divide-y'>
              {filteredActivities.map((activity, index) => (
                <div
                  key={activity.id}
                  className={cn(
                    'p-4 hover:bg-muted/50 transition-colors',
                    index === 0 && 'bg-blue-50/50 dark:bg-blue-950/10'
                  )}
                >
                  <div className='flex gap-3'>
                    <div
                      className={cn(
                        'flex items-center justify-center w-8 h-8 rounded-full flex-shrink-0',
                        getActivityColor(activity.type)
                      )}
                    >
                      {getActivityIcon(activity.type)}
                    </div>

                    <div className='flex-1 min-w-0'>
                      <p className='font-medium text-sm'>{activity.title}</p>
                      {activity.description && (
                        <p className='text-sm text-muted-foreground mt-0.5'>
                          {activity.description}
                        </p>
                      )}

                      <div className='flex items-center gap-3 mt-2 text-xs text-muted-foreground'>
                        <span>
                          {formatDistanceToNow(new Date(activity.createdAt), {
                            addSuffix: true,
                          })}
                        </span>
                        {activity.metadata?.duration && (
                          <span className='flex items-center gap-1'>
                            <Clock className='h-3 w-3' />
                            {activity.metadata.duration}m
                          </span>
                        )}
                        {activity.metadata?.priority && (
                          <span
                            className={cn(
                              'px-2 py-0.5 rounded-full text-xs font-medium',
                              activity.metadata.priority === 'high'
                                ? 'bg-red-100 text-red-700 dark:bg-red-950/20 dark:text-red-400'
                                : activity.metadata.priority === 'medium'
                                  ? 'bg-amber-100 text-amber-700 dark:bg-amber-950/20 dark:text-amber-400'
                                  : 'bg-blue-100 text-blue-700 dark:bg-blue-950/20 dark:text-blue-400'
                            )}
                          >
                            {activity.metadata.priority}
                          </span>
                        )}
                      </div>
                    </div>

                    {index === 0 && (
                      <span className='text-xs text-blue-600 font-medium'>
                        New
                      </span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {filteredActivities.length > 0 && (
        <div className='text-center'>
          <Button variant='ghost' size='sm'>
            Load More
          </Button>
        </div>
      )}
    </div>
  );
}
