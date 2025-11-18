/**
 * PTM v2 - Recurring Task Configuration Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Configure recurring task patterns
 */

'use client';

import { useState, useEffect } from 'react';
import { Calendar, Repeat, X } from 'lucide-react';
import { Label } from '@/shared/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Input } from '@/shared/components/ui/input';
import { Button } from '@/shared/components/ui/button';
import { Badge } from '@/shared/components/ui/badge';
import { cn } from '@/shared/utils';

export interface RepeatConfig {
  frequency: 'daily' | 'weekly' | 'monthly';
  interval: number;
  endDate?: string;
  daysOfWeek?: number[]; // 0=Sunday, 6=Saturday
}

interface RecurringTaskConfigProps {
  value?: RepeatConfig | null;
  onChange: (config: RepeatConfig | null) => void;
  className?: string;
}

const DAYS_OF_WEEK = [
  { value: 0, label: 'Sun', fullLabel: 'Sunday' },
  { value: 1, label: 'Mon', fullLabel: 'Monday' },
  { value: 2, label: 'Tue', fullLabel: 'Tuesday' },
  { value: 3, label: 'Wed', fullLabel: 'Wednesday' },
  { value: 4, label: 'Thu', fullLabel: 'Thursday' },
  { value: 5, label: 'Fri', fullLabel: 'Friday' },
  { value: 6, label: 'Sat', fullLabel: 'Saturday' },
];

export function RecurringTaskConfig({
  value,
  onChange,
  className,
}: RecurringTaskConfigProps) {
  const [isEnabled, setIsEnabled] = useState(!!value);
  const [frequency, setFrequency] = useState<'daily' | 'weekly' | 'monthly'>(
    value?.frequency || 'daily'
  );
  const [interval, setInterval] = useState(value?.interval || 1);
  const [endDate, setEndDate] = useState(value?.endDate || '');
  const [selectedDays, setSelectedDays] = useState<number[]>(
    value?.daysOfWeek || []
  );

  // Update parent when config changes
  useEffect(() => {
    if (!isEnabled) {
      onChange(null);
      return;
    }

    const config: RepeatConfig = {
      frequency,
      interval,
      endDate: endDate || undefined,
      daysOfWeek: frequency === 'weekly' ? selectedDays : undefined,
    };

    onChange(config);
  }, [isEnabled, frequency, interval, endDate, selectedDays]);

  const toggleDay = (day: number) => {
    setSelectedDays((prev) =>
      prev.includes(day) ? prev.filter((d) => d !== day) : [...prev, day].sort()
    );
  };

  const handleDisable = () => {
    setIsEnabled(false);
    onChange(null);
  };

  const getRecurrencePreview = (): string => {
    if (!isEnabled) return '';

    const intervalText = interval === 1 ? '' : `every ${interval} `;

    if (frequency === 'daily') {
      return interval === 1
        ? 'Repeats daily'
        : `Repeats every ${interval} days`;
    }

    if (frequency === 'weekly') {
      if (selectedDays.length === 0) {
        return interval === 1
          ? 'Repeats weekly'
          : `Repeats every ${interval} weeks`;
      }

      const dayNames = selectedDays
        .map((d) => DAYS_OF_WEEK.find((day) => day.value === d)?.label)
        .join(', ');

      return interval === 1
        ? `Repeats weekly on ${dayNames}`
        : `Repeats every ${interval} weeks on ${dayNames}`;
    }

    if (frequency === 'monthly') {
      return interval === 1
        ? 'Repeats monthly'
        : `Repeats every ${interval} months`;
    }

    return '';
  };

  if (!isEnabled) {
    return (
      <div className={cn('space-y-2', className)}>
        <Button
          type='button'
          variant='outline'
          size='sm'
          onClick={() => setIsEnabled(true)}
          className='gap-2'
        >
          <Repeat className='h-4 w-4' />
          Make Recurring
        </Button>
      </div>
    );
  }

  return (
    <div
      className={cn('space-y-4 p-4 border rounded-lg bg-muted/30', className)}
    >
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div className='flex items-center gap-2'>
          <Repeat className='h-4 w-4 text-primary' />
          <Label className='font-semibold'>Recurring Task</Label>
        </div>
        <Button
          type='button'
          variant='ghost'
          size='icon'
          onClick={handleDisable}
          className='h-6 w-6'
        >
          <X className='h-4 w-4' />
        </Button>
      </div>

      {/* Frequency Selection */}
      <div className='grid grid-cols-2 gap-4'>
        <div className='space-y-2'>
          <Label htmlFor='frequency'>Repeat</Label>
          <Select
            value={frequency}
            onValueChange={(val: 'daily' | 'weekly' | 'monthly') =>
              setFrequency(val)
            }
          >
            <SelectTrigger id='frequency'>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='daily'>Daily</SelectItem>
              <SelectItem value='weekly'>Weekly</SelectItem>
              <SelectItem value='monthly'>Monthly</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className='space-y-2'>
          <Label htmlFor='interval'>Every</Label>
          <div className='flex items-center gap-2'>
            <Input
              id='interval'
              type='number'
              min='1'
              max='365'
              value={interval}
              onChange={(e) => setInterval(parseInt(e.target.value) || 1)}
              className='w-20'
            />
            <span className='text-sm text-muted-foreground'>
              {frequency === 'daily'
                ? interval === 1
                  ? 'day'
                  : 'days'
                : frequency === 'weekly'
                  ? interval === 1
                    ? 'week'
                    : 'weeks'
                  : interval === 1
                    ? 'month'
                    : 'months'}
            </span>
          </div>
        </div>
      </div>

      {/* Weekly - Day Selection */}
      {frequency === 'weekly' && (
        <div className='space-y-2'>
          <Label>Repeat on</Label>
          <div className='flex gap-2'>
            {DAYS_OF_WEEK.map((day) => (
              <Button
                key={day.value}
                type='button'
                variant={
                  selectedDays.includes(day.value) ? 'default' : 'outline'
                }
                size='sm'
                onClick={() => toggleDay(day.value)}
                className='w-12 h-10 p-0'
              >
                {day.label}
              </Button>
            ))}
          </div>
          {selectedDays.length === 0 && (
            <p className='text-xs text-amber-600 dark:text-amber-400'>
              Select at least one day
            </p>
          )}
        </div>
      )}

      {/* End Date (Optional) */}
      <div className='space-y-2'>
        <Label htmlFor='endDate'>End Date (Optional)</Label>
        <div className='flex items-center gap-2'>
          <Calendar className='h-4 w-4 text-muted-foreground' />
          <Input
            id='endDate'
            type='date'
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            className='flex-1'
            min={new Date().toISOString().split('T')[0]}
          />
          {endDate && (
            <Button
              type='button'
              variant='ghost'
              size='icon'
              onClick={() => setEndDate('')}
              className='h-8 w-8'
            >
              <X className='h-4 w-4' />
            </Button>
          )}
        </div>
        {!endDate && (
          <p className='text-xs text-muted-foreground'>
            Leave empty to repeat indefinitely
          </p>
        )}
      </div>

      {/* Preview */}
      <div className='pt-2 border-t'>
        <Badge variant='secondary' className='text-xs'>
          {getRecurrencePreview()}
          {endDate && ` until ${new Date(endDate).toLocaleDateString()}`}
        </Badge>
      </div>
    </div>
  );
}
