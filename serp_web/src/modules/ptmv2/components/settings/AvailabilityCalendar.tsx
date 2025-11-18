/**
 * PTM v2 - Availability Calendar Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Weekly availability schedule with focus time tagging
 */

'use client';

import { useState, useEffect } from 'react';
import { Plus, Trash2, Clock, Flame, Sparkles, Save } from 'lucide-react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Label } from '@/shared/components/ui/label';
import { Switch } from '@/shared/components/ui/switch';
import { Input } from '@/shared/components/ui/input';
import { Badge } from '@/shared/components/ui/badge';
import { cn } from '@/shared/utils';
import { toast } from 'sonner';
import type { AvailabilitySlot, AvailabilitySlotType } from '../../types';

interface AvailabilityCalendarProps {
  className?: string;
  onSave?: (slots: AvailabilitySlot[]) => void;
  initialSlots?: AvailabilitySlot[];
}

const DAYS_OF_WEEK = [
  { label: 'Sunday', value: 0, short: 'Sun' },
  { label: 'Monday', value: 1, short: 'Mon' },
  { label: 'Tuesday', value: 2, short: 'Tue' },
  { label: 'Wednesday', value: 3, short: 'Wed' },
  { label: 'Thursday', value: 4, short: 'Thu' },
  { label: 'Friday', value: 5, short: 'Fri' },
  { label: 'Saturday', value: 6, short: 'Sat' },
];

const SLOT_TYPE_CONFIG: Record<
  AvailabilitySlotType,
  {
    label: string;
    icon: any;
    color: string;
    bgColor: string;
    description: string;
  }
> = {
  focus: {
    label: 'Focus Time',
    icon: Flame,
    color: 'text-purple-600',
    bgColor:
      'bg-purple-50 dark:bg-purple-950/20 border-purple-200 dark:border-purple-800',
    description: 'Deep work - AI prioritizes cognitively demanding tasks',
  },
  regular: {
    label: 'Regular',
    icon: Clock,
    color: 'text-blue-600',
    bgColor:
      'bg-blue-50 dark:bg-blue-950/20 border-blue-200 dark:border-blue-800',
    description: 'Standard availability - Any task type',
  },
  flexible: {
    label: 'Flexible',
    icon: Sparkles,
    color: 'text-green-600',
    bgColor:
      'bg-green-50 dark:bg-green-950/20 border-green-200 dark:border-green-800',
    description: 'Low priority - AI uses only if needed',
  },
};

export function AvailabilityCalendar({
  className,
  onSave,
  initialSlots = [],
}: AvailabilityCalendarProps) {
  const [slots, setSlots] = useState<AvailabilitySlot[]>(initialSlots);
  const [hasChanges, setHasChanges] = useState(false);

  useEffect(() => {
    // Initialize with default working hours if empty
    if (slots.length === 0) {
      const defaultSlots: AvailabilitySlot[] = [];
      // Monday to Friday: 9 AM - 5 PM
      for (let day = 1; day <= 5; day++) {
        defaultSlots.push({
          dayOfWeek: day,
          startTime: '09:00',
          endTime: '17:00',
          slotType: 'regular',
          isEnabled: true,
        });
      }
      setSlots(defaultSlots);
    }
  }, []);

  const addSlot = (dayOfWeek: number) => {
    const newSlot: AvailabilitySlot = {
      id: Date.now(),
      dayOfWeek,
      startTime: '09:00',
      endTime: '17:00',
      slotType: 'regular',
      isEnabled: true,
    };
    setSlots([...slots, newSlot]);
    setHasChanges(true);
    toast.success('Time block added');
  };

  const removeSlot = (id: number | undefined, index: number) => {
    const slotToRemove = slots[index];
    setSlots(slots.filter((_, i) => i !== index));
    setHasChanges(true);
    toast.success('Time block removed');
  };

  const updateSlot = (
    index: number,
    field: keyof AvailabilitySlot,
    value: any
  ) => {
    const newSlots = [...slots];
    newSlots[index] = { ...newSlots[index], [field]: value };
    setSlots(newSlots);
    setHasChanges(true);
  };

  const toggleSlotType = (index: number, newType: AvailabilitySlotType) => {
    updateSlot(index, 'slotType', newType);
  };

  const formatTime = (time: string) => {
    const [hours, minutes] = time.split(':').map(Number);
    const period = hours >= 12 ? 'PM' : 'AM';
    const displayHours = hours > 12 ? hours - 12 : hours === 0 ? 12 : hours;
    return `${displayHours}:${minutes.toString().padStart(2, '0')} ${period}`;
  };

  const calculateDuration = (startTime: string, endTime: string) => {
    const [startH, startM] = startTime.split(':').map(Number);
    const [endH, endM] = endTime.split(':').map(Number);
    const totalMinutes = endH * 60 + endM - (startH * 60 + startM);
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    if (minutes > 0) return `${hours}h ${minutes}m`;
    return `${hours}h`;
  };

  const getSlotsByDay = (dayOfWeek: number) => {
    return slots
      .map((slot, index) => ({ ...slot, originalIndex: index }))
      .filter((slot) => slot.dayOfWeek === dayOfWeek && slot.isEnabled)
      .sort((a, b) => a.startTime.localeCompare(b.startTime));
  };

  const handleSave = () => {
    if (onSave) {
      onSave(slots.filter((s) => s.isEnabled));
    }
    setHasChanges(false);
    toast.success('Availability schedule saved!', {
      description: 'AI will use these hours for scheduling tasks.',
    });
  };

  const getDayStats = (dayOfWeek: number) => {
    const daySlots = getSlotsByDay(dayOfWeek);
    const totalMinutes = daySlots.reduce((sum, slot) => {
      const [startH, startM] = slot.startTime.split(':').map(Number);
      const [endH, endM] = slot.endTime.split(':').map(Number);
      return sum + (endH * 60 + endM - (startH * 60 + startM));
    }, 0);
    const focusSlots = daySlots.filter((s) => s.slotType === 'focus').length;
    return { totalMinutes, focusSlots, slotCount: daySlots.length };
  };

  const totalFocusHours =
    slots
      .filter((s) => s.slotType === 'focus' && s.isEnabled)
      .reduce((sum, slot) => {
        const [startH, startM] = slot.startTime.split(':').map(Number);
        const [endH, endM] = slot.endTime.split(':').map(Number);
        return sum + (endH * 60 + endM - (startH * 60 + startM));
      }, 0) / 60;

  return (
    <div className={cn('space-y-6', className)}>
      {/* Header */}
      <div className='flex items-start justify-between'>
        <div>
          <h2 className='text-2xl font-bold flex items-center gap-2'>
            <Clock className='h-6 w-6 text-blue-600' />
            Weekly Availability
          </h2>
          <p className='text-muted-foreground mt-1'>
            Set your working hours and mark focus time for deep work
          </p>
        </div>
        {hasChanges && (
          <Button onClick={handleSave} className='gap-2'>
            <Save className='h-4 w-4' />
            Save Changes
          </Button>
        )}
      </div>

      {/* Stats Summary */}
      <div className='grid grid-cols-1 md:grid-cols-3 gap-4'>
        <Card>
          <CardContent className='pt-6'>
            <div className='text-center'>
              <p className='text-3xl font-bold text-blue-600'>
                {slots.filter((s) => s.isEnabled).length}
              </p>
              <p className='text-sm text-muted-foreground mt-1'>
                Total Time Blocks
              </p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className='pt-6'>
            <div className='text-center'>
              <p className='text-3xl font-bold text-purple-600'>
                {totalFocusHours.toFixed(1)}h
              </p>
              <p className='text-sm text-muted-foreground mt-1'>
                Focus Time / Week
              </p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className='pt-6'>
            <div className='text-center'>
              <p className='text-3xl font-bold text-green-600'>
                {
                  DAYS_OF_WEEK.filter((d) => getSlotsByDay(d.value).length > 0)
                    .length
                }
              </p>
              <p className='text-sm text-muted-foreground mt-1'>Active Days</p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Weekly Schedule */}
      <Card>
        <CardHeader>
          <CardTitle>Schedule by Day</CardTitle>
          <CardDescription>
            Add multiple time blocks per day and mark focus periods
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-6'>
          {DAYS_OF_WEEK.map((day) => {
            const daySlots = getSlotsByDay(day.value);
            const stats = getDayStats(day.value);

            return (
              <div key={day.value} className='space-y-3'>
                {/* Day Header */}
                <div className='flex items-center justify-between'>
                  <div className='flex items-center gap-3'>
                    <Label className='text-base font-semibold min-w-[100px]'>
                      {day.label}
                    </Label>
                    {stats.slotCount > 0 && (
                      <div className='flex items-center gap-2 text-sm text-muted-foreground'>
                        <Badge variant='outline' className='text-xs'>
                          {Math.floor(stats.totalMinutes / 60)}h{' '}
                          {stats.totalMinutes % 60 > 0 &&
                            `${stats.totalMinutes % 60}m`}
                        </Badge>
                        {stats.focusSlots > 0 && (
                          <Badge
                            variant='outline'
                            className='text-xs border-purple-200 text-purple-700 dark:border-purple-800 dark:text-purple-300'
                          >
                            <Flame className='h-3 w-3 mr-1' />
                            {stats.focusSlots} focus
                          </Badge>
                        )}
                      </div>
                    )}
                  </div>
                  <Button
                    variant='outline'
                    size='sm'
                    onClick={() => addSlot(day.value)}
                  >
                    <Plus className='h-4 w-4 mr-1' />
                    Add Block
                  </Button>
                </div>

                {/* Day Slots */}
                {daySlots.length === 0 ? (
                  <p className='text-sm text-muted-foreground pl-4 py-2'>
                    No availability set
                  </p>
                ) : (
                  <div className='space-y-2 pl-4'>
                    {daySlots.map((slot) => {
                      const config = SLOT_TYPE_CONFIG[slot.slotType];
                      const Icon = config.icon;

                      return (
                        <div
                          key={slot.originalIndex}
                          className={cn(
                            'p-4 rounded-lg border transition-all',
                            config.bgColor
                          )}
                        >
                          <div className='flex items-start gap-4'>
                            {/* Icon */}
                            <div className='flex-shrink-0 mt-1'>
                              <Icon className={cn('h-5 w-5', config.color)} />
                            </div>

                            {/* Time Inputs */}
                            <div className='flex-1 space-y-3'>
                              <div className='flex items-center gap-2'>
                                <Input
                                  type='time'
                                  value={slot.startTime}
                                  onChange={(e) =>
                                    updateSlot(
                                      slot.originalIndex!,
                                      'startTime',
                                      e.target.value
                                    )
                                  }
                                  className='w-32'
                                />
                                <span className='text-muted-foreground'>
                                  to
                                </span>
                                <Input
                                  type='time'
                                  value={slot.endTime}
                                  onChange={(e) =>
                                    updateSlot(
                                      slot.originalIndex!,
                                      'endTime',
                                      e.target.value
                                    )
                                  }
                                  className='w-32'
                                />
                                <Badge variant='secondary' className='ml-2'>
                                  {calculateDuration(
                                    slot.startTime,
                                    slot.endTime
                                  )}
                                </Badge>
                              </div>

                              {/* Slot Type Selector */}
                              <div className='flex items-center gap-2'>
                                <Label className='text-sm text-muted-foreground'>
                                  Type:
                                </Label>
                                <div className='flex gap-2'>
                                  {(
                                    Object.keys(
                                      SLOT_TYPE_CONFIG
                                    ) as AvailabilitySlotType[]
                                  ).map((type) => {
                                    const typeConfig = SLOT_TYPE_CONFIG[type];
                                    const TypeIcon = typeConfig.icon;
                                    return (
                                      <Button
                                        key={type}
                                        variant={
                                          slot.slotType === type
                                            ? 'default'
                                            : 'outline'
                                        }
                                        size='sm'
                                        onClick={() =>
                                          toggleSlotType(
                                            slot.originalIndex!,
                                            type
                                          )
                                        }
                                        className='gap-1'
                                      >
                                        <TypeIcon className='h-3 w-3' />
                                        {typeConfig.label}
                                      </Button>
                                    );
                                  })}
                                </div>
                              </div>

                              {/* Description */}
                              <p className='text-xs text-muted-foreground'>
                                {config.description}
                              </p>
                            </div>

                            {/* Delete Button */}
                            <Button
                              variant='ghost'
                              size='icon'
                              onClick={() =>
                                removeSlot(slot.id, slot.originalIndex!)
                              }
                              className='h-8 w-8 flex-shrink-0'
                            >
                              <Trash2 className='h-4 w-4 text-red-600' />
                            </Button>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            );
          })}
        </CardContent>
      </Card>

      {/* Legend */}
      <Card>
        <CardHeader>
          <CardTitle className='text-base'>Slot Types</CardTitle>
        </CardHeader>
        <CardContent>
          <div className='grid grid-cols-1 md:grid-cols-3 gap-4'>
            {(Object.keys(SLOT_TYPE_CONFIG) as AvailabilitySlotType[]).map(
              (type) => {
                const config = SLOT_TYPE_CONFIG[type];
                const Icon = config.icon;
                return (
                  <div key={type} className='flex items-start gap-3'>
                    <div
                      className={cn(
                        'p-2 rounded-lg',
                        config.bgColor.split(' ')[0]
                      )}
                    >
                      <Icon className={cn('h-4 w-4', config.color)} />
                    </div>
                    <div>
                      <p className='font-medium text-sm'>{config.label}</p>
                      <p className='text-xs text-muted-foreground'>
                        {config.description}
                      </p>
                    </div>
                  </div>
                );
              }
            )}
          </div>
        </CardContent>
      </Card>

      {/* Actions */}
      {hasChanges && (
        <div className='flex justify-end gap-2 pt-4 border-t'>
          <Button
            variant='outline'
            onClick={() => {
              setSlots(initialSlots);
              setHasChanges(false);
              toast.info('Changes discarded');
            }}
          >
            Discard Changes
          </Button>
          <Button onClick={handleSave} className='gap-2'>
            <Save className='h-4 w-4' />
            Save Availability
          </Button>
        </div>
      )}
    </div>
  );
}
