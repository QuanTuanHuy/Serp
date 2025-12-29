/*
Author: QuanTuanHuy
Description: Part of Serp Project
Split Event Dialog - Split a schedule event into multiple parts
*/

'use client';

import { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import { Button } from '@/shared/components/ui/button';
import { Label } from '@/shared/components/ui/label';
import { Slider } from '@/shared/components/ui/slider';
import { Clock, Scissors } from 'lucide-react';
import type { ScheduleEvent } from '../../types';

interface SplitEventDialogProps {
  event: ScheduleEvent | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onConfirm: (eventId: number, splitPointMin: number) => Promise<void>;
}

export function SplitEventDialog({
  event,
  open,
  onOpenChange,
  onConfirm,
}: SplitEventDialogProps) {
  const [isLoading, setIsLoading] = useState(false);
  const totalDuration = event ? event.endMin - event.startMin : 60;
  const [splitAt, setSplitAt] = useState(Math.floor(totalDuration / 2));

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
    if (hours > 0 && mins > 0) return `${hours}h ${mins}m`;
    if (hours > 0) return `${hours}h`;
    return `${mins}m`;
  };

  const handleSplit = async () => {
    setIsLoading(true);
    try {
      const splitPointMin = event.startMin + splitAt;
      await onConfirm(event.id, splitPointMin);
      onOpenChange(false);
    } catch (error) {
      console.error('Failed to split event:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const firstPartDuration = splitAt;
  const secondPartDuration = totalDuration - splitAt;
  const splitPointTime = event.startMin + splitAt;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle className='flex items-center gap-2'>
            <Scissors className='h-5 w-5' />
            Split Event
          </DialogTitle>
          <DialogDescription>
            Divide "{event.title}" into two separate events
          </DialogDescription>
        </DialogHeader>

        <div className='space-y-6 py-4'>
          {/* Event Info */}
          <div className='rounded-lg border bg-muted/50 p-4 space-y-2'>
            <div className='flex items-center justify-between text-sm'>
              <span className='text-muted-foreground'>Original Event</span>
              <span className='font-medium'>
                {formatDuration(totalDuration)}
              </span>
            </div>
            <div className='flex items-center justify-between text-sm'>
              <span className='text-muted-foreground'>Time Range</span>
              <span className='font-medium'>
                {formatTime(event.startMin)} - {formatTime(event.endMin)}
              </span>
            </div>
          </div>

          {/* Split Point Slider */}
          <div className='space-y-4'>
            <Label>Split Point</Label>
            <Slider
              value={[splitAt]}
              onValueChange={([value]) => setSplitAt(value)}
              min={30}
              max={totalDuration - 30}
              step={15}
              className='py-4'
            />
            <div className='flex items-center justify-center'>
              <div className='flex items-center gap-2 text-sm text-muted-foreground'>
                <Clock className='h-4 w-4' />
                <span>Split at {formatTime(splitPointTime)}</span>
              </div>
            </div>
          </div>

          {/* Result Preview */}
          <div className='space-y-3'>
            <Label>Result Preview</Label>
            <div className='grid gap-2'>
              <div className='flex items-center justify-between rounded-lg border bg-blue-50 dark:bg-blue-950/20 p-3'>
                <div>
                  <div className='text-sm font-medium'>Part 1</div>
                  <div className='text-xs text-muted-foreground'>
                    {formatTime(event.startMin)} - {formatTime(splitPointTime)}
                  </div>
                </div>
                <div className='text-sm font-semibold text-blue-600'>
                  {formatDuration(firstPartDuration)}
                </div>
              </div>
              <div className='flex items-center justify-between rounded-lg border bg-green-50 dark:bg-green-950/20 p-3'>
                <div>
                  <div className='text-sm font-medium'>Part 2</div>
                  <div className='text-xs text-muted-foreground'>
                    {formatTime(splitPointTime)} - {formatTime(event.endMin)}
                  </div>
                </div>
                <div className='text-sm font-semibold text-green-600'>
                  {formatDuration(secondPartDuration)}
                </div>
              </div>
            </div>
          </div>

          {/* Warning */}
          <div className='rounded-lg border border-amber-200 bg-amber-50 dark:bg-amber-950/20 p-3 text-xs text-muted-foreground'>
            <strong className='text-foreground'>Note:</strong> Both parts will
            be linked and tracked separately. You can further adjust or
            reschedule them independently.
          </div>
        </div>

        <DialogFooter>
          <Button
            variant='outline'
            onClick={() => onOpenChange(false)}
            disabled={isLoading}
          >
            Cancel
          </Button>
          <Button onClick={handleSplit} disabled={isLoading}>
            {isLoading ? (
              <Clock className='mr-2 h-4 w-4 animate-spin' />
            ) : (
              <Scissors className='mr-2 h-4 w-4' />
            )}
            Split Event
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
