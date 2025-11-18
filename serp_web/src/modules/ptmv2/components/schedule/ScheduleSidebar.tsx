/**
 * PTM v2 - Schedule Sidebar Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Collapsible sidebar with unscheduled tasks and filters
 */

'use client';

import { useState } from 'react';
import {
  ChevronLeft,
  ChevronRight,
  Clock,
  Flame,
  CheckSquare,
  Calendar as CalendarIcon,
} from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Card, CardContent, CardHeader } from '@/shared/components/ui/card';
import { Badge } from '@/shared/components/ui/badge';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { Label } from '@/shared/components/ui/label';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import { cn } from '@/shared/utils';
import type { Task, FocusTimeBlock } from '../../types';

interface ScheduleSidebarProps {
  unscheduledTasks: Task[];
  focusBlocks: FocusTimeBlock[];
  onTaskDragStart?: (task: Task) => void;
  onFocusBlockToggle?: (blockId: string) => void;
  className?: string;
}

export function ScheduleSidebar({
  unscheduledTasks,
  focusBlocks,
  onTaskDragStart,
  onFocusBlockToggle,
  className,
}: ScheduleSidebarProps) {
  const [isCollapsed, setIsCollapsed] = useState(false);

  const getDayName = (dayOfWeek: number) => {
    const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    return days[dayOfWeek];
  };

  const formatTime = (minutes: number) => {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    const period = hours >= 12 ? 'PM' : 'AM';
    const displayHours = hours % 12 || 12;
    return `${displayHours}:${mins.toString().padStart(2, '0')} ${period}`;
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'HIGH':
        return 'bg-red-500/10 text-red-600 dark:text-red-400 border-red-500/20';
      case 'MEDIUM':
        return 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20';
      case 'LOW':
        return 'bg-blue-500/10 text-blue-600 dark:text-blue-400 border-blue-500/20';
      default:
        return 'bg-muted text-muted-foreground';
    }
  };

  if (isCollapsed) {
    return (
      <div className={cn('w-12 space-y-2', className)}>
        <Button
          variant='outline'
          size='icon'
          onClick={() => setIsCollapsed(false)}
          className='w-full'
        >
          <ChevronRight className='h-4 w-4' />
        </Button>

        {/* Collapsed icons */}
        <div className='space-y-2'>
          <div className='flex flex-col items-center gap-1 p-2 rounded-lg bg-muted'>
            <CheckSquare className='h-4 w-4 text-muted-foreground' />
            <span className='text-xs font-medium'>
              {unscheduledTasks.length}
            </span>
          </div>
          <div className='flex flex-col items-center gap-1 p-2 rounded-lg bg-muted'>
            <Flame className='h-4 w-4 text-red-600 dark:text-red-400' />
            <span className='text-xs font-medium'>{focusBlocks.length}</span>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={cn('w-80 space-y-4', className)}>
      {/* Collapse Button */}
      <Button
        variant='ghost'
        size='sm'
        onClick={() => setIsCollapsed(true)}
        className='w-full justify-start'
      >
        <ChevronLeft className='h-4 w-4 mr-2' />
        Collapse Sidebar
      </Button>

      {/* Unscheduled Tasks */}
      <Card>
        <CardHeader className='pb-3'>
          <div className='flex items-center justify-between'>
            <div className='flex items-center gap-2'>
              <CheckSquare className='h-4 w-4 text-primary' />
              <h3 className='font-semibold'>Unscheduled Tasks</h3>
            </div>
            <Badge variant='secondary'>{unscheduledTasks.length}</Badge>
          </div>
        </CardHeader>
        <CardContent>
          <ScrollArea className='h-[300px] pr-4'>
            {unscheduledTasks.length === 0 ? (
              <div className='text-center py-8 text-sm text-muted-foreground'>
                <CalendarIcon className='h-8 w-8 mx-auto mb-2 opacity-50' />
                <p>All tasks scheduled!</p>
              </div>
            ) : (
              <div className='space-y-2'>
                {unscheduledTasks.map((task) => (
                  <div
                    key={task.id}
                    draggable
                    onDragStart={(e) => {
                      // Store task ID in window for calendar drop handler
                      (window as any).__draggedTaskId = task.id;
                      e.dataTransfer.effectAllowed = 'move';
                      e.dataTransfer.setData('text/plain', task.id);
                      onTaskDragStart?.(task);
                    }}
                    onDragEnd={() => {
                      // Clean up if drag was cancelled
                      delete (window as any).__draggedTaskId;
                    }}
                    className='p-3 rounded-lg border-2 border-dashed hover:border-primary hover:bg-accent/50 cursor-move transition-all group'
                  >
                    <div className='flex items-start gap-2'>
                      <div className='flex-1 min-w-0'>
                        <div className='flex items-center gap-2 mb-1'>
                          {task.isDeepWork && (
                            <Flame className='h-3 w-3 text-red-600 dark:text-red-400 flex-shrink-0' />
                          )}
                          <p className='font-medium text-sm truncate'>
                            {task.title}
                          </p>
                        </div>
                        <div className='flex items-center gap-2 flex-wrap'>
                          <Badge
                            variant='outline'
                            className={cn(
                              'text-xs',
                              getPriorityColor(task.priority)
                            )}
                          >
                            {task.priority}
                          </Badge>
                          <div className='flex items-center gap-1 text-xs text-muted-foreground'>
                            <Clock className='h-3 w-3' />
                            <span>{task.estimatedDurationHours}h</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </ScrollArea>
        </CardContent>
      </Card>

      {/* Focus Blocks */}
      <Card>
        <CardHeader className='pb-3'>
          <div className='flex items-center justify-between'>
            <div className='flex items-center gap-2'>
              <Flame className='h-4 w-4 text-red-600 dark:text-red-400' />
              <h3 className='font-semibold'>Focus Blocks</h3>
            </div>
            <Badge variant='secondary'>
              {focusBlocks.filter((b) => b.isEnabled).length}
            </Badge>
          </div>
        </CardHeader>
        <CardContent>
          <div className='space-y-2'>
            {focusBlocks.length === 0 ? (
              <div className='text-center py-4 text-sm text-muted-foreground'>
                <p>No focus blocks configured</p>
              </div>
            ) : (
              focusBlocks.map((block) => (
                <div
                  key={block.id}
                  className='flex items-center justify-between p-3 rounded-lg border hover:bg-accent/50 transition-colors'
                >
                  <div className='flex items-center gap-3'>
                    <Checkbox
                      checked={block.isEnabled}
                      onCheckedChange={() => onFocusBlockToggle?.(block.id)}
                    />
                    <div className='space-y-0.5'>
                      <p className='text-sm font-medium'>{block.blockName}</p>
                      <p className='text-xs text-muted-foreground'>
                        {getDayName(block.dayOfWeek)}{' '}
                        {formatTime(block.startMin)} -{' '}
                        {formatTime(block.endMin)}
                      </p>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
