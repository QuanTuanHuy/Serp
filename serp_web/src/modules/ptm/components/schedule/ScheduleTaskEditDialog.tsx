/*
Author: QuanTuanHuy
Description: Part of Serp Project
Schedule Task Edit Dialog - Edit scheduling constraints for tasks
*/

'use client';

import { useState, useEffect } from 'react';
import { useUpdateScheduleTaskMutation } from '../../api';
import type { ScheduleTask, Priority } from '../../types';
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
import { Input } from '@/shared/components/ui/input';
import { Switch } from '@/shared/components/ui/switch';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Slider } from '@/shared/components/ui/slider';
import { Badge } from '@/shared/components/ui/badge';
import { Separator } from '@/shared/components/ui/separator';
import { Calendar, Clock, Flame, Scissors, Shield } from 'lucide-react';
import { toast } from 'sonner';

interface ScheduleTaskEditDialogProps {
  task: ScheduleTask | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function ScheduleTaskEditDialog({
  task,
  open,
  onOpenChange,
}: ScheduleTaskEditDialogProps) {
  const [updateTask, { isLoading }] = useUpdateScheduleTaskMutation();

  // Form state
  const [formData, setFormData] = useState({
    durationMin: 60,
    priority: 'MEDIUM' as Priority,
    isDeepWork: false,
    deadlineMs: undefined as number | undefined,
    earliestStartMs: undefined as number | undefined,
    preferredStartMs: undefined as number | undefined,
    allowSplit: true,
    minSplitDurationMin: 30,
    maxSplitCount: 4,
    bufferBeforeMin: 0,
    bufferAfterMin: 0,
  });

  // Initialize form when task changes
  useEffect(() => {
    if (task) {
      setFormData({
        durationMin: task.durationMin,
        priority: task.priority,
        isDeepWork: task.isDeepWork,
        deadlineMs: task.deadlineMs,
        earliestStartMs: task.earliestStartMs,
        preferredStartMs: task.preferredStartMs,
        allowSplit: task.allowSplit,
        minSplitDurationMin: task.minSplitDurationMin,
        maxSplitCount: task.maxSplitCount,
        bufferBeforeMin: task.bufferBeforeMin,
        bufferAfterMin: task.bufferAfterMin,
      });
    }
  }, [task]);

  const handleSubmit = async () => {
    if (!task) return;

    try {
      await updateTask({
        id: task.id,
        updates: {
          durationMin: formData.durationMin,
          priority: formData.priority,
          isDeepWork: formData.isDeepWork,
          deadlineMs: formData.deadlineMs,
          earliestStartMs: formData.earliestStartMs,
          preferredStartMs: formData.preferredStartMs,
          allowSplit: formData.allowSplit,
          minSplitDurationMin: formData.minSplitDurationMin,
          maxSplitCount: formData.maxSplitCount,
          bufferBeforeMin: formData.bufferBeforeMin,
          bufferAfterMin: formData.bufferAfterMin,
        },
      }).unwrap();

      toast.success('Task Updated', {
        description: 'Scheduling constraints updated. Reschedule triggered.',
      });
      onOpenChange(false);
    } catch (error) {
      toast.error('Update Failed', {
        description: 'Failed to update task constraints.',
      });
    }
  };

  const formatDuration = (minutes: number) => {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0) {
      return mins > 0 ? `${hours}h ${mins}m` : `${hours}h`;
    }
    return `${mins}m`;
  };

  if (!task) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-2xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle className='flex items-center gap-2'>
            Edit Scheduling Constraints
          </DialogTitle>
          <DialogDescription>
            Configure scheduling parameters for: <strong>{task.title}</strong>
          </DialogDescription>
        </DialogHeader>

        <div className='space-y-6 py-4'>
          {/* Basic Constraints */}
          <div className='space-y-4'>
            <div className='flex items-center gap-2 text-sm font-semibold'>
              <Clock className='h-4 w-4' />
              <span>Basic Constraints</span>
            </div>

            <div className='grid grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='duration'>Duration</Label>
                <div className='flex gap-2'>
                  <Input
                    id='duration'
                    type='number'
                    min={15}
                    max={480}
                    step={15}
                    value={formData.durationMin}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        durationMin: parseInt(e.target.value) || 60,
                      })
                    }
                  />
                  <Badge variant='secondary' className='shrink-0'>
                    {formatDuration(formData.durationMin)}
                  </Badge>
                </div>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='priority'>Priority</Label>
                <Select
                  value={formData.priority}
                  onValueChange={(value) =>
                    setFormData({ ...formData, priority: value as Priority })
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='LOW'>Low</SelectItem>
                    <SelectItem value='MEDIUM'>Medium</SelectItem>
                    <SelectItem value='HIGH'>High</SelectItem>
                    <SelectItem value='URGENT'>Urgent</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className='flex items-center justify-between'>
              <div className='space-y-0.5'>
                <Label htmlFor='deepWork' className='flex items-center gap-2'>
                  <Flame className='h-4 w-4 text-purple-600 dark:text-purple-400' />
                  Deep Work Session
                </Label>
                <p className='text-xs text-muted-foreground'>
                  Requires uninterrupted focus time
                </p>
              </div>
              <Switch
                id='deepWork'
                checked={formData.isDeepWork}
                onCheckedChange={(checked) =>
                  setFormData({ ...formData, isDeepWork: checked })
                }
              />
            </div>
          </div>

          <Separator />

          {/* Time Constraints */}
          <div className='space-y-4'>
            <div className='flex items-center gap-2 text-sm font-semibold'>
              <Calendar className='h-4 w-4' />
              <span>Time Constraints</span>
            </div>

            <div className='space-y-3'>
              <div className='space-y-2'>
                <Label htmlFor='deadline'>Deadline (Optional)</Label>
                <Input
                  id='deadline'
                  type='datetime-local'
                  value={
                    formData.deadlineMs
                      ? new Date(formData.deadlineMs).toISOString().slice(0, 16)
                      : ''
                  }
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      deadlineMs: e.target.value
                        ? new Date(e.target.value).getTime()
                        : undefined,
                    })
                  }
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='earliestStart'>Earliest Start (Optional)</Label>
                <Input
                  id='earliestStart'
                  type='datetime-local'
                  value={
                    formData.earliestStartMs
                      ? new Date(formData.earliestStartMs)
                          .toISOString()
                          .slice(0, 16)
                      : ''
                  }
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      earliestStartMs: e.target.value
                        ? new Date(e.target.value).getTime()
                        : undefined,
                    })
                  }
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='preferredStart'>
                  Preferred Start (Optional)
                </Label>
                <Input
                  id='preferredStart'
                  type='datetime-local'
                  value={
                    formData.preferredStartMs
                      ? new Date(formData.preferredStartMs)
                          .toISOString()
                          .slice(0, 16)
                      : ''
                  }
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      preferredStartMs: e.target.value
                        ? new Date(e.target.value).getTime()
                        : undefined,
                    })
                  }
                />
              </div>
            </div>
          </div>

          <Separator />

          {/* Split Settings */}
          <div className='space-y-4'>
            <div className='flex items-center gap-2 text-sm font-semibold'>
              <Scissors className='h-4 w-4' />
              <span>Split Settings</span>
            </div>

            <div className='flex items-center justify-between'>
              <div className='space-y-0.5'>
                <Label htmlFor='allowSplit'>Allow Splitting</Label>
                <p className='text-xs text-muted-foreground'>
                  Allow task to be split across multiple time slots
                </p>
              </div>
              <Switch
                id='allowSplit'
                checked={formData.allowSplit}
                onCheckedChange={(checked) =>
                  setFormData({ ...formData, allowSplit: checked })
                }
              />
            </div>

            {formData.allowSplit && (
              <div className='space-y-4 pl-4 border-l-2 border-border'>
                <div className='space-y-2'>
                  <Label>
                    Minimum Split Duration: {formData.minSplitDurationMin} min
                  </Label>
                  <Slider
                    value={[formData.minSplitDurationMin]}
                    onValueChange={([value]) =>
                      setFormData({ ...formData, minSplitDurationMin: value })
                    }
                    min={15}
                    max={120}
                    step={15}
                  />
                </div>

                <div className='space-y-2'>
                  <Label>Maximum Parts: {formData.maxSplitCount}</Label>
                  <Slider
                    value={[formData.maxSplitCount]}
                    onValueChange={([value]) =>
                      setFormData({ ...formData, maxSplitCount: value })
                    }
                    min={1}
                    max={10}
                    step={1}
                  />
                </div>
              </div>
            )}
          </div>

          <Separator />

          {/* Buffers */}
          <div className='space-y-4'>
            <div className='flex items-center gap-2 text-sm font-semibold'>
              <Shield className='h-4 w-4' />
              <span>Buffers</span>
            </div>

            <div className='grid grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label>Buffer Before: {formData.bufferBeforeMin} min</Label>
                <Slider
                  value={[formData.bufferBeforeMin]}
                  onValueChange={([value]) =>
                    setFormData({ ...formData, bufferBeforeMin: value })
                  }
                  min={0}
                  max={60}
                  step={5}
                />
              </div>

              <div className='space-y-2'>
                <Label>Buffer After: {formData.bufferAfterMin} min</Label>
                <Slider
                  value={[formData.bufferAfterMin]}
                  onValueChange={([value]) =>
                    setFormData({ ...formData, bufferAfterMin: value })
                  }
                  min={0}
                  max={60}
                  step={5}
                />
              </div>
            </div>
          </div>
        </div>

        <DialogFooter>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button onClick={handleSubmit} disabled={isLoading}>
            {isLoading ? 'Updating...' : 'Update & Reschedule'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
