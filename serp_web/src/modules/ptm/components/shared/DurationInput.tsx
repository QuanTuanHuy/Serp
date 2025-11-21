/**
 * PTM v2 - Duration Input Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Duration input with hours/minutes
 */

'use client';

import { useState, useEffect } from 'react';
import { Clock } from 'lucide-react';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { cn } from '@/shared/utils';

interface DurationInputProps {
  value: number; // In hours (e.g., 2.5 = 2h 30m)
  onChange: (hours: number) => void;
  label?: string;
  className?: string;
}

export function DurationInput({
  value,
  onChange,
  label,
  className,
}: DurationInputProps) {
  const [hours, setHours] = useState(Math.floor(value));
  const [minutes, setMinutes] = useState(Math.round((value % 1) * 60));

  useEffect(() => {
    setHours(Math.floor(value));
    setMinutes(Math.round((value % 1) * 60));
  }, [value]);

  const handleHoursChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const h = parseInt(e.target.value) || 0;
    setHours(h);
    onChange(h + minutes / 60);
  };

  const handleMinutesChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const m = parseInt(e.target.value) || 0;
    const normalizedMinutes = Math.min(59, Math.max(0, m));
    setMinutes(normalizedMinutes);
    onChange(hours + normalizedMinutes / 60);
  };

  return (
    <div className={cn('space-y-2', className)}>
      {label && <Label>{label}</Label>}

      <div className='flex items-center gap-2'>
        <Clock className='h-4 w-4 text-muted-foreground' />

        <div className='flex items-center gap-2'>
          <Input
            type='number'
            min='0'
            max='23'
            value={hours}
            onChange={handleHoursChange}
            className='w-16 text-center'
          />
          <span className='text-sm text-muted-foreground'>h</span>
        </div>

        <div className='flex items-center gap-2'>
          <Input
            type='number'
            min='0'
            max='59'
            step='15'
            value={minutes}
            onChange={handleMinutesChange}
            className='w-16 text-center'
          />
          <span className='text-sm text-muted-foreground'>m</span>
        </div>
      </div>

      <p className='text-xs text-muted-foreground'>
        Total: {value.toFixed(1)} hours
      </p>
    </div>
  );
}
