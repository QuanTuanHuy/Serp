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
  Pin,
  PinOff,
  Scissors,
  XCircle,
  Link2,
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
import { SplitEventDialog } from './SplitEventDialog';
import {
  useUpdateScheduleEventMutation,
  useCompleteScheduleEventMutation,
  useSplitScheduleEventMutation,
} from '../../api';

interface EventDetailSheetProps {
  event: ScheduleEvent | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onReschedule?: (eventId: number) => void;
  onRemove?: (eventId: number) => void;
  onViewTask?: (taskId: number) => void;
}

export function EventDetailSheet({
  event,
  open,
  onOpenChange,
  onReschedule,
  onRemove,
  onViewTask,
}: EventDetailSheetProps) {
  const [splitDialogOpen, setSplitDialogOpen] = useState(false);

  const [updateEvent] = useUpdateScheduleEventMutation();
  const [completeEvent] = useCompleteScheduleEventMutation();
  const [splitEvent] = useSplitScheduleEventMutation();

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

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'DONE':
        return 'bg-green-500/10 text-green-600 dark:text-green-400 border-green-500/20';
      case 'PLANNED':
        return 'bg-blue-500/10 text-blue-600 dark:text-blue-400 border-blue-500/20';
      case 'CANCELLED':
      case 'SKIPPED':
        return 'bg-red-500/10 text-red-600 dark:text-red-400 border-red-500/20';
      default:
        return 'bg-muted text-muted-foreground';
    }
  };

  const handleMarkComplete = async () => {
    if (event.status === 'DONE') {
      toast.info('This event is already completed');
      return;
    }
    try {
      await completeEvent({
        id: event.id,
        actualStartMin: event.startMin,
        actualEndMin: event.endMin,
      }).unwrap();
      toast.success('Event marked as completed! 🎉');
      onOpenChange(false);
    } catch (error) {
      toast.error('Failed to mark event as completed');
    }
  };

  const handleTogglePin = async () => {
    try {
      await updateEvent({
        id: event.id,
        isPinned: !event.isPinned,
      }).unwrap();
      toast.success(
        event.isPinned
          ? 'Event unpinned - can be rescheduled'
          : 'Event pinned - locked in place'
      );
    } catch (error) {
      toast.error('Failed to toggle pin');
    }
  };

  const handleMarkSkipped = async () => {
    try {
      await completeEvent({
        id: event.id,
        actualStartMin: event.startMin,
        actualEndMin: event.startMin, // Same as start = skipped
      }).unwrap();
      toast.success('Event marked as skipped');
      onOpenChange(false);
    } catch (error) {
      toast.error('Failed to skip event');
    }
  };

  const handleSplit = async (eventId: number, splitPointMin: number) => {
    try {
      await splitEvent({ id: eventId, splitPointMin }).unwrap();
      toast.success('Event split successfully');
      onOpenChange(false);
    } catch (error) {
      toast.error('Failed to split event');
    }
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
                  {event.isPinned && (
                    <Badge
                      variant='secondary'
                      className='bg-purple-500/10 text-purple-600 dark:text-purple-400 border-purple-500/20'
                    >
                      <Pin className='h-3 w-3 mr-1' />
                      Pinned
                    </Badge>
                  )}
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
                  Duration:{' '}
                  {formatDuration(
                    event.durationMin || event.endMin - event.startMin
                  )}
                </p>
              </div>
            </div>

            {event.totalParts > 1 && (
              <div className='flex items-start gap-3'>
                <div className='p-2 rounded-lg bg-amber-500/10'>
                  <Link2 className='h-4 w-4 text-amber-600 dark:text-amber-400' />
                </div>
                <div className='space-y-2 flex-1'>
                  <p className='text-sm font-medium'>Multi-Part Event</p>
                  <p className='text-sm text-muted-foreground'>
                    Part {event.partIndex + 1} of {event.totalParts}
                  </p>
                  <Progress
                    value={((event.partIndex + 1) / event.totalParts) * 100}
                    className='h-2'
                  />
                  {event.linkedEventId && (
                    <p className='text-xs text-muted-foreground'>
                      Linked to event #{event.linkedEventId}
                    </p>
                  )}
                </div>
              </div>
            )}
          </div>

          <Separator />

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
            {event.taskId && (
              <Button
                variant='outline'
                className='w-full justify-start'
                onClick={() => {
                  onViewTask?.(event.taskId!);
                }}
              >
                <ExternalLink className='h-4 w-4 mr-2' />
                View Full Task Details
              </Button>
            )}

            {/* Pin/Unpin */}
            <Button
              variant='outline'
              className={cn(
                'w-full justify-start',
                event.isPinned &&
                  'text-purple-600 hover:text-purple-700 hover:bg-purple-500/10'
              )}
              onClick={handleTogglePin}
            >
              {event.isPinned ? (
                <>
                  <PinOff className='h-4 w-4 mr-2' />
                  Unpin Event
                </>
              ) : (
                <>
                  <Pin className='h-4 w-4 mr-2' />
                  Pin Event (Lock Position)
                </>
              )}
            </Button>

            {/* Split Event */}
            {event.status === 'PLANNED' && (
              <Button
                variant='outline'
                className='w-full justify-start'
                onClick={() => setSplitDialogOpen(true)}
              >
                <Scissors className='h-4 w-4 mr-2' />
                Split into Multiple Parts
              </Button>
            )}

            {/* Mark Complete */}
            {event.status !== 'DONE' && event.status !== 'SKIPPED' && (
              <Button
                variant='outline'
                className='w-full justify-start text-green-600 hover:text-green-700 hover:bg-green-500/10'
                onClick={handleMarkComplete}
              >
                <CheckCircle2 className='h-4 w-4 mr-2' />
                Mark as Completed
              </Button>
            )}

            {/* Mark Skipped */}
            {event.status !== 'DONE' && event.status !== 'SKIPPED' && (
              <Button
                variant='outline'
                className='w-full justify-start text-amber-600 hover:text-amber-700 hover:bg-amber-500/10'
                onClick={handleMarkSkipped}
              >
                <XCircle className='h-4 w-4 mr-2' />
                Mark as Skipped
              </Button>
            )}

            {/* Reschedule */}
            <Button
              variant='outline'
              className='w-full justify-start'
              onClick={() => {
                onReschedule?.(event.id);
              }}
            >
              <Edit className='h-4 w-4 mr-2' />
              Trigger Reschedule
            </Button>

            {/* Remove */}
            <Button
              variant='outline'
              className='w-full justify-start text-red-600 hover:text-red-700 hover:bg-red-500/10'
              onClick={handleRemove}
            >
              <Trash2 className='h-4 w-4 mr-2' />
              Remove from Schedule
            </Button>
          </div>
        </div>

        {/* Split Dialog */}
        <SplitEventDialog
          event={event}
          open={splitDialogOpen}
          onOpenChange={setSplitDialogOpen}
          onConfirm={handleSplit}
        />
      </SheetContent>
    </Sheet>
  );
}
