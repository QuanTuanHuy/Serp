/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM shared date picker
 */

'use client';

import { forwardRef, useState, type ComponentPropsWithoutRef } from 'react';
import { CalendarDays, X } from 'lucide-react';
import { format } from 'date-fns';
import { Button } from '@/shared/components/ui/button';
import { Calendar } from '@/shared/components/ui/calendar';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui/popover';
import { cn } from '@/shared/utils';
import { parseLocalDateValue } from '../../utils/date';

interface PMDatePickerProps
  extends Omit<
    ComponentPropsWithoutRef<typeof Button>,
    'className' | 'children' | 'onChange' | 'type' | 'disabled' | 'value'
  > {
  value?: number | string | Date | null;
  onChange: (value?: Date) => void;
  placeholder?: string;
  disabled?: boolean;
  showClear?: boolean;
  align?: 'start' | 'center' | 'end';
  className?: string;
  buttonClassName?: string;
  contentClassName?: string;
  clearLabel?: string;
}

interface PMDateRangePickerProps {
  from?: number | string | Date | null;
  to?: number | string | Date | null;
  onFromChange: (value?: Date) => void;
  onToChange: (value?: Date) => void;
  fromLabel?: string;
  toLabel?: string;
  fromPlaceholder?: string;
  toPlaceholder?: string;
  disabled?: boolean;
  className?: string;
}

export const PMDatePicker = forwardRef<HTMLButtonElement, PMDatePickerProps>(
  function PMDatePicker(
    {
      value,
      onChange,
      placeholder = 'Pick a date',
      disabled,
      showClear = true,
      align = 'start',
      className,
      buttonClassName,
      contentClassName,
      clearLabel = 'Clear date',
      ...buttonProps
    },
    ref
  ) {
    const [open, setOpen] = useState(false);
    const selectedDate = parseLocalDateValue(value);
    const hasValue = Boolean(selectedDate);

    return (
      <div className={cn('flex min-w-0 items-center gap-2', className)}>
        <Popover open={open} onOpenChange={setOpen}>
          <PopoverTrigger asChild>
            <Button
              ref={ref}
              type='button'
              variant='outline'
              disabled={disabled}
              {...buttonProps}
              className={cn(
                'min-w-0 flex-1 justify-start gap-2 text-left font-normal',
                !hasValue && 'text-muted-foreground',
                buttonClassName
              )}
            >
              <CalendarDays className='h-4 w-4 shrink-0' />
              <span className='min-w-0 flex-1 truncate'>
                {hasValue ? format(selectedDate!, 'PPP') : placeholder}
              </span>
            </Button>
          </PopoverTrigger>
          <PopoverContent
            align={align}
            className={cn('w-auto p-0', contentClassName)}
          >
            <Calendar
              mode='single'
              selected={selectedDate}
              onSelect={(date) => {
                onChange(date ?? undefined);
                setOpen(false);
              }}
              initialFocus
            />
          </PopoverContent>
        </Popover>

        {showClear && hasValue ? (
          <Button
            type='button'
            variant='ghost'
            size='icon'
            className='h-10 w-10 shrink-0'
            aria-label={clearLabel}
            onClick={() => onChange(undefined)}
            disabled={disabled}
          >
            <X className='h-4 w-4' />
          </Button>
        ) : null}
      </div>
    );
  }
);

export function PMDateRangePicker({
  from,
  to,
  onFromChange,
  onToChange,
  fromLabel = 'From',
  toLabel = 'To',
  fromPlaceholder = 'Start date',
  toPlaceholder = 'End date',
  disabled,
  className,
}: PMDateRangePickerProps) {
  return (
    <div className={cn('grid gap-2 sm:grid-cols-2', className)}>
      <label className='space-y-1'>
        <span className='text-xs font-medium text-muted-foreground'>
          {fromLabel}
        </span>
        <PMDatePicker
          value={from}
          onChange={onFromChange}
          placeholder={fromPlaceholder}
          disabled={disabled}
          showClear
          buttonClassName='flex-1'
        />
      </label>
      <label className='space-y-1'>
        <span className='text-xs font-medium text-muted-foreground'>
          {toLabel}
        </span>
        <PMDatePicker
          value={to}
          onChange={onToChange}
          placeholder={toPlaceholder}
          disabled={disabled}
          showClear
          buttonClassName='flex-1'
        />
      </label>
    </div>
  );
}
