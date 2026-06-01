'use client';

import * as React from 'react';
import { Calendar as CalendarIcon, X } from 'lucide-react';
import { Popover, PopoverContent, PopoverTrigger } from '@/shared/components/ui/popover';
import { Calendar } from '@/shared/components/ui/calendar';
import { Button } from '@/shared/components/ui/button';
import { cn } from '@/shared/utils';

export interface SchoolBusDatePickerProps {
  value?: string | null;
  onChange: (value: string) => void;
  placeholder?: string;
  label?: string;
  required?: boolean;
  disabled?: boolean;
  minDate?: string | Date;
  maxDate?: string | Date;
  error?: boolean | string;
  clearable?: boolean;
  size?: 'sm' | 'md';
  fullWidth?: boolean;
  className?: string;
}

const parseDateString = (str: string | null | undefined): Date | undefined => {
  if (!str) return undefined;
  const parts = str.split('-');
  if (parts.length !== 3) return undefined;
  const year = parseInt(parts[0], 10);
  const month = parseInt(parts[1], 10) - 1;
  const day = parseInt(parts[2], 10);
  if (isNaN(year) || isNaN(month) || isNaN(day)) return undefined;
  return new Date(year, month, day);
};

const formatDateToString = (date: Date): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

export const SchoolBusDatePicker = React.forwardRef<HTMLButtonElement, SchoolBusDatePickerProps>(
  (
    {
      value,
      onChange,
      placeholder = 'Select date',
      label,
      required,
      disabled,
      minDate,
      maxDate,
      error,
      clearable = true,
      size = 'md',
      fullWidth = false,
      className,
    },
    ref
  ) => {
    const [open, setOpen] = React.useState(false);
    
    const dateValue = React.useMemo(() => parseDateString(value), [value]);

    const formattedDisplayValue = React.useMemo(() => {
      if (!dateValue) return '';
      const month = String(dateValue.getMonth() + 1).padStart(2, '0');
      const day = String(dateValue.getDate()).padStart(2, '0');
      const year = dateValue.getFullYear();
      return `${month}/${day}/${year}`;
    }, [dateValue]);

    const handleSelect = (selectedDate: Date | undefined) => {
      if (selectedDate) {
        onChange(formatDateToString(selectedDate));
      } else {
        onChange('');
      }
      setOpen(false);
    };

    const handleClear = (e: React.MouseEvent) => {
      e.stopPropagation();
      onChange('');
    };

    const parsedMinDate = React.useMemo(() => {
      if (minDate instanceof Date) return minDate;
      return parseDateString(minDate);
    }, [minDate]);

    const parsedMaxDate = React.useMemo(() => {
      if (maxDate instanceof Date) return maxDate;
      return parseDateString(maxDate);
    }, [maxDate]);

    const isDateDisabled = (date: Date) => {
      const checkDate = new Date(date);
      checkDate.setHours(0, 0, 0, 0);

      if (parsedMinDate) {
        const min = new Date(parsedMinDate);
        min.setHours(0, 0, 0, 0);
        if (checkDate < min) return true;
      }
      if (parsedMaxDate) {
        const max = new Date(parsedMaxDate);
        max.setHours(0, 0, 0, 0);
        if (checkDate > max) return true;
      }
      return false;
    };

    const handleTodayClick = () => {
      const today = new Date();
      if (!isDateDisabled(today)) {
        onChange(formatDateToString(today));
      }
      setOpen(false);
    };

    return (
      <div className={cn('flex flex-col gap-1.5', fullWidth ? 'w-full' : 'w-auto', className)}>
        {label && (
          <label className='text-xs font-semibold text-slate-700'>
            {label}
            {required && <span className='text-red-500 ml-0.5'>*</span>}
          </label>
        )}
        
        <Popover open={open} onOpenChange={setOpen}>
          <PopoverTrigger asChild>
            <Button
              ref={ref}
              type='button'
              variant='outline'
              disabled={disabled}
              className={cn(
                'relative flex items-center justify-between bg-white text-left font-medium transition-all shadow-sm border border-slate-200 text-slate-800 hover:bg-slate-50/50 outline-none',
                size === 'sm' ? 'h-9 px-3 rounded-lg text-xs' : 'h-11 px-4 rounded-xl text-sm',
                fullWidth && 'w-full',
                error && 'border-rose-500 ring-rose-500/20 focus-visible:ring-rose-500/20',
                !value && 'text-slate-400 hover:text-slate-400 font-normal',
                'focus-visible:ring-2 focus-visible:ring-[#C81E3A]/20 focus-visible:ring-offset-0 focus-visible:border-[#C81E3A]'
              )}
            >
              <span className='truncate'>{formattedDisplayValue || placeholder}</span>
              
              <div className='flex items-center gap-1.5 ml-2 text-slate-400 shrink-0'>
                {clearable && value && !disabled && (
                  <button
                    type='button'
                    onClick={handleClear}
                    className='rounded-full p-0.5 hover:bg-slate-100 hover:text-slate-600 transition-colors'
                  >
                    <X className='h-3.5 w-3.5' />
                  </button>
                )}
                <CalendarIcon className={cn('text-slate-400', size === 'sm' ? 'h-4 w-4' : 'h-4.5 w-4.5')} />
              </div>
            </Button>
          </PopoverTrigger>
          <PopoverContent 
            className='w-auto p-0 z-50 rounded-2xl border border-slate-100 bg-white shadow-xl origin-top-left' 
            align='start'
            sideOffset={6}
          >
            <div className='p-3 flex flex-col gap-3'>
              <Calendar
                mode='single'
                selected={dateValue}
                onSelect={handleSelect}
                disabled={isDateDisabled}
                className={cn(
                  'p-0',
                  '[&_button[data-selected-single=true]]:bg-[#C81E3A] [&_button[data-selected-single=true]]:text-white [&_button[data-selected-single=true]]:hover:bg-[#a6172e] [&_button[data-selected-single=true]]:font-semibold',
                  '[&_button[data-range-start=true]]:bg-[#C81E3A] [&_button[data-range-start=true]]:text-white',
                  '[&_button[data-range-end=true]]:bg-[#C81E3A] [&_button[data-range-end=true]]:text-white',
                  '[&_button:hover:not([data-selected-single=true]):not([disabled])]:bg-[#FDECEF]/60 [&_button:hover:not([data-selected-single=true]):not([disabled])]:text-[#C81E3A]',
                  '[&_button[disabled]]:opacity-30 [&_button[disabled]]:cursor-not-allowed [&_button[disabled]]:hover:bg-transparent [&_button[disabled]]:hover:text-slate-400',
                  '[&_.rdp-today_button]:border [&_.rdp-today_button]:border-[#C81E3A]/40 [&_.rdp-today_button]:text-[#C81E3A] [&_.rdp-today_button]:font-semibold',
                  '[&_button]:rounded-lg [&_button]:transition-all'
                )}
              />
              
              <div className='flex items-center justify-between border-t border-slate-100 pt-2 px-1 gap-2'>
                <Button
                  type='button'
                  variant='ghost'
                  size='sm'
                  onClick={handleTodayClick}
                  className='text-xs font-semibold text-[#C81E3A] hover:bg-[#FDECEF]/40 hover:text-[#a6172e] rounded-lg'
                >
                  Today
                </Button>
                {clearable && value && (
                  <Button
                    type='button'
                    variant='ghost'
                    size='sm'
                    onClick={() => {
                      onChange('');
                      setOpen(false);
                    }}
                    className='text-xs font-semibold text-slate-500 hover:bg-slate-50 rounded-lg'
                  >
                    Clear
                  </Button>
                )}
              </div>
            </div>
          </PopoverContent>
        </Popover>

        {error && typeof error === 'string' && (
          <span className='text-xs text-rose-500 mt-0.5'>{error}</span>
        )}
      </div>
    );
  }
);

SchoolBusDatePicker.displayName = 'SchoolBusDatePicker';
