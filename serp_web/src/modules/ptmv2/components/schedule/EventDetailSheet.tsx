/**
 * PTM v2 - Event Detail Sheet Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - View and edit schedule event details
 */

'use client';

import { useState } from 'react';
import {
  Clock,
  Calendar,
  Target,
  Flame,
  CheckCircle2,
  X,
  Edit,
  Trash2,
  ExternalLink,
  TrendingUp,
} from 'lucide-react';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetDescription,
} from '@/shared/components/ui/sheet';
import { Button } from '@/shared/components/ui/button';
import { Badge } from '@/shared/components/ui/badge';
import { Separator } from '@/shared/components/ui/separator';
import { Progress } from '@/shared/components/ui/progress';
import { cn } from '@/shared/utils';
import type { ScheduleEvent } from '../../types';
import { toast } from 'sonner';

interface EventDetailSheetProps {
  event: ScheduleEvent | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onMarkComplete?: (eventId: string) => void;
  onReschedule?: (eventId: string) => void;
  onRemove?: (eventId: string) => void;
  onViewTask?: (taskId: string) => void;
}

export function EventDetailSheet({
  event,
  open,
  onOpenChange,
  onMarkComplete,
  onReschedule,
  onRemove,
  onViewTask,
}: EventDetailSheetProps) {
  if (!event) return null;

  const formatTime = (minutes: number) => {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    const period = hours >= 12 ? 'PM' : 'AM';
    const displayHours = hours % 12 || 12;
    return `${displayHours}:${mins.toString().padStart(2, '0')} ${period}`;
  };

  const formatDuration = (minutes: number) => {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0 && mins > 0)
      return `${hours} hour${hours > 1 ? 's' : ''} ${mins} min`;
    if (hours > 0) return `${hours} hour${hours > 1 ? 's' : ''}`;
    return `${mins} minutes`;
  };

  const formatDate = (timestamp: number) => {
    return new Date(timestamp).toLocaleDateString('en-US', {
      weekday: 'long',
      month: 'long',
      day: 'numeric',
      year: 'numeric',
    });
  };

  const getUtilityColor = (utility: number) => {
    if (utility >= 80) return 'text-green-600 dark:text-green-400';
    if (utility >= 60) return 'text-amber-600 dark:text-amber-400';
    return 'text-red-600 dark:text-red-400';
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'completed':
        return 'bg-green-500/10 text-green-600 dark:text-green-400 border-green-500/20';
      case 'scheduled':
        return 'bg-blue-500/10 text-blue-600 dark:text-blue-400 border-blue-500/20';
      case 'cancelled':
        return 'bg-red-500/10 text-red-600 dark:text-red-400 border-red-500/20';
      default:
        return 'bg-muted text-muted-foreground';
    }
  };

  const handleMarkComplete = () => {
    if (event.status === 'completed') {
      toast.info('This event is already completed');
      return;
    }
    onMarkComplete?.(event.id);
    toast.success('Event marked as completed! 🎉');
    onOpenChange(false);
  };

  const handleRemove = () => {
    if (!confirm('Are you sure you want to remove this event from schedule?'))
      return;
    onRemove?.(event.id);
    toast.success('Event removed from schedule');
    onOpenChange(false);
  };

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className='w-full sm:max-w-lg overflow-y-auto'>
        <SheetHeader>
          <SheetTitle className='sr-only'>Event Details</SheetTitle>
          <SheetDescription className='sr-only'>
            View and manage schedule event details
          </SheetDescription>
        </SheetHeader>

        <div className='space-y-6 py-6'>
          {/* Header */}
          <div className='space-y-3'>
            <div className='flex items-start justify-between gap-4'>
              <div className='space-y-2 flex-1'>
                <div className='flex items-center gap-2 flex-wrap'>
                  {event.isDeepWork && (
                    <Badge
                      variant='secondary'
                      className='bg-red-500/10 text-red-600 dark:text-red-400 border-red-500/20'
                    >
                      <Flame className='h-3 w-3 mr-1' />
                      Deep Work
                    </Badge>
                  )}
                  {event.priority && (
                    <Badge
                      variant='outline'
                      className={cn(
                        event.priority === 'HIGH' &&
                          'bg-red-500/10 text-red-600 border-red-500/20',
                        event.priority === 'MEDIUM' &&
                          'bg-amber-500/10 text-amber-600 border-amber-500/20',
                        event.priority === 'LOW' &&
                          'bg-blue-500/10 text-blue-600 border-blue-500/20'
                      )}
                    >
                      {event.priority}
                    </Badge>
                  )}
                  <Badge
                    variant='outline'
                    className={cn('capitalize', getStatusColor(event.status))}
                  >
                    {event.status}
                  </Badge>
                </div>
                <h2 className='text-2xl font-bold'>
                  {event.title || 'Untitled Event'}
                </h2>
              </div>
            </div>
          </div>

          <Separator />

          {/* Time Details */}
          <div className='space-y-4'>
            <div className='flex items-start gap-3'>
              <div className='p-2 rounded-lg bg-blue-500/10'>
                <Calendar className='h-4 w-4 text-blue-600 dark:text-blue-400' />
              </div>
              <div className='space-y-1'>
                <p className='text-sm font-medium'>Date</p>
                <p className='text-sm text-muted-foreground'>
                  {formatDate(event.dateMs)}
                </p>
              </div>
            </div>

            <div className='flex items-start gap-3'>
              <div className='p-2 rounded-lg bg-purple-500/10'>
                <Clock className='h-4 w-4 text-purple-600 dark:text-purple-400' />
              </div>
              <div className='space-y-1'>
                <p className='text-sm font-medium'>Time</p>
                <p className='text-sm text-muted-foreground'>
                  {formatTime(event.startMin)} - {formatTime(event.endMin)}
                </p>
                <p className='text-xs text-muted-foreground'>
                  Duration: {formatDuration(event.durationMin)}
                </p>
              </div>
            </div>

            {event.totalParts > 1 && (
              <div className='flex items-start gap-3'>
                <div className='p-2 rounded-lg bg-amber-500/10'>
                  <Target className='h-4 w-4 text-amber-600 dark:text-amber-400' />
                </div>
                <div className='space-y-2 flex-1'>
                  <p className='text-sm font-medium'>Task Progress</p>
                  <p className='text-sm text-muted-foreground'>
                    Part {event.taskPart} of {event.totalParts}
                  </p>
                  <Progress
                    value={(event.taskPart / event.totalParts) * 100}
                    className='h-2'
                  />
                </div>
              </div>
            )}
          </div>

          <Separator />

          {/* AI Utility Score */}
          {event.utilityBreakdown && (
            <div className='space-y-4'>
              <div className='flex items-center justify-between'>
                <div className='flex items-center gap-2'>
                  <TrendingUp className='h-4 w-4 text-primary' />
                  <h3 className='font-semibold'>AI Utility Score</h3>
                </div>
                <span
                  className={cn(
                    'text-2xl font-bold',
                    getUtilityColor(event.utility)
                  )}
                >
                  {Math.round(event.utility)}
                </span>
              </div>

              <div className='space-y-3'>
                <div className='flex items-center justify-between text-sm'>
                  <span className='text-muted-foreground'>Priority Score</span>
                  <span className='font-medium'>
                    +{event.utilityBreakdown.priorityScore}
                  </span>
                </div>
                <div className='flex items-center justify-between text-sm'>
                  <span className='text-muted-foreground'>Deadline Score</span>
                  <span className='font-medium'>
                    +{event.utilityBreakdown.deadlineScore}
                  </span>
                </div>
                {event.utilityBreakdown.focusTimeBonus > 0 && (
                  <div className='flex items-center justify-between text-sm text-green-600 dark:text-green-400'>
                    <span>Focus Time Bonus</span>
                    <span className='font-medium'>
                      +{event.utilityBreakdown.focusTimeBonus}
                    </span>
                  </div>
                )}
                {event.utilityBreakdown.contextSwitchPenalty < 0 && (
                  <div className='flex items-center justify-between text-sm text-red-600 dark:text-red-400'>
                    <span>Context Switch Penalty</span>
                    <span className='font-medium'>
                      {event.utilityBreakdown.contextSwitchPenalty}
                    </span>
                  </div>
                )}
              </div>

              {event.utilityBreakdown.reason && (
                <div className='p-3 rounded-lg bg-muted/50 border'>
                  <p className='text-sm text-muted-foreground italic'>
                    "{event.utilityBreakdown.reason}"
                  </p>
                </div>
              )}
            </div>
          )}

          {/* Manual Override Indicator */}
          {event.isManualOverride && (
            <>
              <Separator />
              <div className='p-3 rounded-lg bg-amber-500/10 border border-amber-500/20'>
                <div className='flex items-center gap-2 text-amber-600 dark:text-amber-400'>
                  <span className='text-lg'>✋</span>
                  <div>
                    <p className='text-sm font-medium'>Manual Override</p>
                    <p className='text-xs'>
                      This event was manually moved by you
                    </p>
                  </div>
                </div>
              </div>
            </>
          )}

          <Separator />

          {/* Actions */}
          <div className='space-y-2'>
            {event.scheduleTaskId && (
              <Button
                variant='outline'
                className='w-full justify-start'
                onClick={() => {
                  onViewTask?.(event.scheduleTaskId);
                  toast.info('Opening task details...');
                }}
              >
                <ExternalLink className='h-4 w-4 mr-2' />
                View Full Task Details
              </Button>
            )}

            {event.status !== 'completed' && (
              <Button
                variant='outline'
                className='w-full justify-start text-green-600 hover:text-green-700 hover:bg-green-500/10'
                onClick={handleMarkComplete}
              >
                <CheckCircle2 className='h-4 w-4 mr-2' />
                Mark as Completed
              </Button>
            )}

            <Button
              variant='outline'
              className='w-full justify-start'
              onClick={() => {
                onReschedule?.(event.id);
                toast.info('AI will suggest best time slots...');
              }}
            >
              <Edit className='h-4 w-4 mr-2' />
              Reschedule with AI
            </Button>

            <Button
              variant='outline'
              className='w-full justify-start text-red-600 hover:text-red-700 hover:bg-red-500/10'
              onClick={handleRemove}
            >
              <Trash2 className='h-4 w-4 mr-2' />
              Remove from Schedule
            </Button>
          </div>

          {/* Metadata */}
          <Separator />
          <div className='space-y-2 text-xs text-muted-foreground'>
            <div className='flex justify-between'>
              <span>Created</span>
              <span>{new Date(event.createdAt).toLocaleString()}</span>
            </div>
            <div className='flex justify-between'>
              <span>Last Updated</span>
              <span>{new Date(event.updatedAt).toLocaleString()}</span>
            </div>
            <div className='flex justify-between'>
              <span>Event ID</span>
              <span className='font-mono'>{event.id}</span>
            </div>
          </div>
        </div>
      </SheetContent>
    </Sheet>
  );
}
