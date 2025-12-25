'use client';

import { useRef, useEffect } from 'react';
import { useVirtualizer } from '@tanstack/react-virtual';
import { Filter, SortAsc, Search } from 'lucide-react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { TaskCard } from './TaskCard';
import { TaskDetail } from './TaskDetail';
import { useTasks, useTaskManagement, useTaskDialogs } from '../../hooks';
import type { TaskStatus, TaskPriority } from '../../types';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { useState } from 'react';
import { EditTaskDialog } from './dialogs/EditTaskDialog';
import { DeleteTaskDialog } from './dialogs/DeleteTaskDialog';

interface TaskListProps {
  projectId?: number | string;
  filterProjectId?: number | string;
  className?: string;
}

export function TaskList({
  projectId,
  filterProjectId,
  className,
}: TaskListProps) {
  const [selectedTaskId, setSelectedTaskId] = useState<number | null>(null);

  const {
    tasks: filteredTasks,
    isLoading,
    searchQuery,
    setSearchQuery,
    statusFilter,
    setStatusFilter,
    priorityFilter,
    setPriorityFilter,
    sortBy,
    setSortBy,
    clearFilters,
  } = useTasks({
    projectId: filterProjectId || projectId,
    initialSortBy: 'deadline',
  });

  // Task CRUD operations
  const { handleToggleComplete, handleStart, handlePause } =
    useTaskManagement();

  // Dialog state management
  const { editDialog, deleteDialog } = useTaskDialogs();

  // Virtualization setup
  const parentRef = useRef<HTMLDivElement>(null);

  const virtualizer = useVirtualizer({
    count: filteredTasks.length,
    getScrollElement: () => parentRef.current,
    estimateSize: () => 120,
    overscan: 5,
  });

  // Keyboard shortcuts for task navigation
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Only handle shortcuts when no task detail is open
      if (selectedTaskId) return;

      // Cmd/Ctrl + F to focus search
      if ((e.metaKey || e.ctrlKey) && e.key === 'f') {
        e.preventDefault();
        const searchInput = document.querySelector(
          'input[placeholder="Search tasks..."]'
        ) as HTMLInputElement;
        searchInput?.focus();
      }

      // Escape to clear filters
      if (e.key === 'Escape') {
        clearFilters();
      }

      // Arrow down/up for task navigation when no input is focused
      const activeElement = document.activeElement as HTMLElement;
      const isInputFocused =
        activeElement?.tagName === 'INPUT' ||
        activeElement?.tagName === 'TEXTAREA';

      if (!isInputFocused && filteredTasks.length > 0) {
        if (
          e.key === 'ArrowDown' ||
          (e.key === 'j' && !e.metaKey && !e.ctrlKey)
        ) {
          e.preventDefault();
          const firstTask = filteredTasks[0];
          if (firstTask) setSelectedTaskId(firstTask.id);
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [selectedTaskId, filteredTasks]);

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
          <div className='hidden md:flex items-center gap-2 text-xs text-muted-foreground'>
            <kbd className='px-2 py-1 bg-muted rounded border'>⌘F</kbd>
            <span>Search</span>
            <kbd className='px-2 py-1 bg-muted rounded border ml-3'>ESC</kbd>
            <span>Clear</span>
          </div>
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
              onValueChange={(value) =>
                setSortBy(
                  value as 'deadline' | 'priority' | 'created' | 'title' | 'id'
                )
              }
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
                    data-index={virtualItem.index}
                    ref={virtualizer.measureElement}
                    style={{
                      position: 'absolute',
                      top: 0,
                      left: 0,
                      width: '100%',
                      transform: `translateY(${virtualItem.start}px)`,
                    }}
                  >
                    <div className='px-1 pb-3'>
                      <TaskCard
                        task={task}
                        onClick={setSelectedTaskId}
                        onToggleComplete={handleToggleComplete}
                        onStart={handleStart}
                        onPause={handlePause}
                        onEdit={editDialog.openEdit}
                        onDelete={(task) =>
                          deleteDialog.openDelete(task.id, task.title)
                        }
                      />
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

        {/* Edit & Delete Dialogs */}
        <EditTaskDialog
          taskId={editDialog.taskId}
          open={editDialog.open}
          onOpenChange={editDialog.onOpenChange}
        />
        <DeleteTaskDialog
          taskId={deleteDialog.taskId}
          taskTitle={deleteDialog.taskTitle}
          open={deleteDialog.open}
          onOpenChange={deleteDialog.onOpenChange}
        />
      </CardContent>
    </Card>
  );
}
