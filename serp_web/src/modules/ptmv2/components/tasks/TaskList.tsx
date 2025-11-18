/**
 * PTM v2 - Task List Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Virtualized task list with filters
 */

'use client';

import { useRef, useMemo, useState } from 'react';
import { useVirtualizer } from '@tanstack/react-virtual';
import { Filter, SortAsc, Search } from 'lucide-react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import { Button } from '@/shared/components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { TaskCard } from './TaskCard';
import { TaskDetail } from './TaskDetail';
import { useGetTasksQuery } from '../../services/taskApi';
import type { Task, TaskStatus, TaskPriority } from '../../types';
import { Skeleton } from '@/shared/components/ui/skeleton';

interface TaskListProps {
  projectId?: string;
  className?: string;
}

type SortOption = 'deadline' | 'priority' | 'created' | 'title';

export function TaskList({ projectId, className }: TaskListProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<TaskStatus | 'ALL'>('ALL');
  const [priorityFilter, setPriorityFilter] = useState<TaskPriority | 'ALL'>(
    'ALL'
  );
  const [sortBy, setSortBy] = useState<SortOption>('deadline');
  const [selectedTaskId, setSelectedTaskId] = useState<string | null>(null);

  const { data: tasks = [], isLoading } = useGetTasksQuery({
    projectId,
  });

  // Filter and sort tasks
  const filteredTasks = useMemo(() => {
    let filtered = [...tasks];

    // Search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(
        (task) =>
          task.title.toLowerCase().includes(query) ||
          task.description?.toLowerCase().includes(query) ||
          task.tags?.some((tag: string) => tag.toLowerCase().includes(query))
      );
    }

    // Status filter
    if (statusFilter !== 'ALL') {
      filtered = filtered.filter((task) => task.status === statusFilter);
    }

    // Priority filter
    if (priorityFilter !== 'ALL') {
      filtered = filtered.filter((task) => task.priority === priorityFilter);
    }

    // Sort
    filtered.sort((a, b) => {
      switch (sortBy) {
        case 'deadline':
          if (!a.deadlineMs) return 1;
          if (!b.deadlineMs) return -1;
          return a.deadlineMs - b.deadlineMs;

        case 'priority': {
          const priorityOrder: Record<TaskPriority, number> = {
            HIGH: 0,
            MEDIUM: 1,
            LOW: 2,
          };
          return priorityOrder[a.priority] - priorityOrder[b.priority];
        }

        case 'created':
          return (
            new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
          );

        case 'title':
          return a.title.localeCompare(b.title);

        default:
          return 0;
      }
    });

    return filtered;
  }, [tasks, searchQuery, statusFilter, priorityFilter, sortBy]);

  // Virtualization setup
  const parentRef = useRef<HTMLDivElement>(null);

  const virtualizer = useVirtualizer({
    count: filteredTasks.length,
    getScrollElement: () => parentRef.current,
    estimateSize: () => 120, // Estimated height of each task card
    overscan: 5, // Render 5 extra items above/below viewport
  });

  if (isLoading) {
    return (
      <Card className={className}>
        <CardHeader>
          <CardTitle>Tasks</CardTitle>
        </CardHeader>
        <CardContent className='space-y-3'>
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className='h-24 w-full' />
          ))}
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle className='flex items-center justify-between'>
          <span>
            Tasks
            {filteredTasks.length > 0 && (
              <span className='ml-2 text-sm font-normal text-muted-foreground'>
                ({filteredTasks.length})
              </span>
            )}
          </span>
        </CardTitle>

        {/* Filters & Search */}
        <div className='space-y-3 pt-3'>
          {/* Search */}
          <div className='relative'>
            <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
            <Input
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder='Search tasks...'
              className='pl-9'
            />
          </div>

          {/* Filters Row */}
          <div className='flex flex-wrap items-center gap-2'>
            <Select
              value={statusFilter}
              onValueChange={(value) =>
                setStatusFilter(value as TaskStatus | 'ALL')
              }
            >
              <SelectTrigger className='w-[140px]'>
                <SelectValue placeholder='Status' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='ALL'>All Status</SelectItem>
                <SelectItem value='TODO'>To Do</SelectItem>
                <SelectItem value='IN_PROGRESS'>In Progress</SelectItem>
                <SelectItem value='DONE'>Done</SelectItem>
                <SelectItem value='CANCELLED'>Cancelled</SelectItem>
              </SelectContent>
            </Select>

            <Select
              value={priorityFilter}
              onValueChange={(value) =>
                setPriorityFilter(value as TaskPriority | 'ALL')
              }
            >
              <SelectTrigger className='w-[140px]'>
                <SelectValue placeholder='Priority' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='ALL'>All Priority</SelectItem>
                <SelectItem value='HIGH'>High</SelectItem>
                <SelectItem value='MEDIUM'>Medium</SelectItem>
                <SelectItem value='LOW'>Low</SelectItem>
              </SelectContent>
            </Select>

            <Select
              value={sortBy}
              onValueChange={(value) => setSortBy(value as SortOption)}
            >
              <SelectTrigger className='w-[140px]'>
                <SortAsc className='mr-2 h-4 w-4' />
                <SelectValue placeholder='Sort by' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='deadline'>Deadline</SelectItem>
                <SelectItem value='priority'>Priority</SelectItem>
                <SelectItem value='created'>Created Date</SelectItem>
                <SelectItem value='title'>Title</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>
      </CardHeader>

      <CardContent>
        {/* Virtualized List */}
        {filteredTasks.length === 0 ? (
          <div className='text-center py-12 text-muted-foreground'>
            <p className='text-lg font-medium'>No tasks found</p>
            <p className='text-sm mt-1'>
              {searchQuery || statusFilter !== 'ALL' || priorityFilter !== 'ALL'
                ? 'Try adjusting your filters'
                : 'Create your first task to get started'}
            </p>
          </div>
        ) : (
          <div
            ref={parentRef}
            className='h-[600px] overflow-auto'
            style={{ contain: 'strict' }}
          >
            <div
              style={{
                height: `${virtualizer.getTotalSize()}px`,
                width: '100%',
                position: 'relative',
              }}
            >
              {virtualizer.getVirtualItems().map((virtualItem) => {
                const task = filteredTasks[virtualItem.index];
                return (
                  <div
                    key={task.id}
                    style={{
                      position: 'absolute',
                      top: 0,
                      left: 0,
                      width: '100%',
                      height: `${virtualItem.size}px`,
                      transform: `translateY(${virtualItem.start}px)`,
                    }}
                  >
                    <div className='px-1 pb-3'>
                      <TaskCard task={task} onClick={setSelectedTaskId} />
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Task Detail Sheet */}
        <TaskDetail
          taskId={selectedTaskId}
          open={!!selectedTaskId}
          onOpenChange={(open) => !open && setSelectedTaskId(null)}
        />
      </CardContent>
    </Card>
  );
}
