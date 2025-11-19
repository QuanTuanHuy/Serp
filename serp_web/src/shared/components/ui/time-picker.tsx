'use client';

import { useState, useEffect, useMemo } from 'react';
import { Clock } from 'lucide-react';
import { Popover, PopoverContent, PopoverTrigger } from './popover';
import { Button } from './button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from './select';

interface TimePickerProps {
  value?: string; // "HH:MM"
  onChange?: (value: string) => void;
  minuteStep?: number;
  className?: string;
}

export function TimePicker({
  value,
  onChange,
  minuteStep = 5,
  className,
}: TimePickerProps) {
  const [hour, setHour] = useState(value ? value.split(':')[0] : '09');
  const [minute, setMinute] = useState(value ? value.split(':')[1] : '00');

  const minutes = useMemo(() => {
    const arr: string[] = [];
    for (let i = 0; i < 60; i += minuteStep) {
      arr.push(String(i).padStart(2, '0'));
    }
    return arr;
  }, [minuteStep]);

  const handleUpdate = (h: string, m: string) => {
    const formatted = `${h}:${m}`;
    onChange?.(formatted);
  };

  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button
          variant='outline'
          className={`w-[160px] justify-start ${className}`}
        >
          <Clock className='mr-2 h-4 w-4' />
          {hour}:{minute}
        </Button>
      </PopoverTrigger>

      <PopoverContent className='w-52'>
        <div className='flex items-center justify-between gap-3'>
          {/* Hours */}
          <div className='flex-1'>
            <Select
              value={hour}
              onValueChange={(val) => {
                setHour(val);
                handleUpdate(val, minute);
              }}
            >
              <SelectTrigger>
                <SelectValue placeholder='Hour' />
              </SelectTrigger>
              <SelectContent>
                {Array.from({ length: 24 }).map((_, i) => {
                  const h = String(i).padStart(2, '0');
                  return (
                    <SelectItem key={h} value={h}>
                      {h}
                    </SelectItem>
                  );
                })}
              </SelectContent>
            </Select>
          </div>

          {/* Minutes */}
          <div className='flex-1'>
            <Select
              value={minute}
              onValueChange={(val) => {
                setMinute(val);
                handleUpdate(hour, val);
              }}
            >
              <SelectTrigger>
                <SelectValue placeholder='Min' />
              </SelectTrigger>
              <SelectContent>
                {minutes.map((m) => (
                  <SelectItem key={m} value={m}>
                    {m}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
      </PopoverContent>
    </Popover>
  );
}
