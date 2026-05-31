'use client';

import * as React from 'react';
import { Check, ChevronDown, X } from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui/popover';

export interface FilterSelectOption {
  label: string;
  value: string;
  count?: number;
  disabled?: boolean;
}

export interface SchoolBusFilterSelectProps {
  value: string;
  onChange: (value: string) => void;
  options: FilterSelectOption[];
  placeholder: string;
  label?: string;
  icon?: React.ComponentType<{ className?: string }>;
  size?: 'sm' | 'md';
  clearable?: boolean;
  className?: string;
}

export function SchoolBusFilterSelect({
  value,
  onChange,
  options,
  placeholder,
  label,
  icon: Icon,
  size = 'sm',
  clearable = true,
  className,
}: SchoolBusFilterSelectProps) {
  const [open, setOpen] = React.useState(false);

  const selectedOption = options.find((opt) => opt.value === value);
  const displayLabel = selectedOption?.label || placeholder;
  const hasValue = Boolean(value);

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <button
          type='button'
          aria-label={label || placeholder}
          className={cn(
            'inline-flex items-center gap-1.5 rounded-lg border bg-slate-50 text-slate-700 outline-none transition-all',
            'hover:border-slate-300 hover:bg-white',
            'focus:border-slate-900 focus:ring-1 focus:ring-slate-900/10',
            hasValue && 'border-slate-300 bg-slate-100 text-slate-950 font-semibold',
            size === 'sm' && 'px-2.5 py-1.5 text-xs',
            size === 'md' && 'px-3 py-2 text-sm',
            className,
          )}
        >
          {Icon && <Icon className='h-3 w-3 shrink-0 opacity-60' />}
          <span className='truncate max-w-[120px]'>{displayLabel}</span>
          {clearable && hasValue ? (
            <X
              className='h-3 w-3 shrink-0 opacity-50 hover:opacity-100'
              onClick={(e) => { e.stopPropagation(); onChange(''); setOpen(false); }}
            />
          ) : (
            <ChevronDown className={cn('h-3 w-3 shrink-0 opacity-50 transition-transform', open && 'rotate-180')} />
          )}
        </button>
      </PopoverTrigger>
      <PopoverContent
        align='start'
        className='w-auto min-w-[160px] max-w-[220px] rounded-xl border border-slate-200 bg-white p-1 shadow-lg'
      >
        <div className='flex flex-col gap-0.5'>
          {/* Reset / All option */}
          <FilterItem
            label={placeholder}
            selected={!value}
            size={size}
            onClick={() => { onChange(''); setOpen(false); }}
          />
          <div className='my-0.5 h-px bg-slate-100' />
          {options.map((opt) => (
            <FilterItem
              key={opt.value}
              label={opt.label}
              count={opt.count}
              selected={opt.value === value}
              disabled={opt.disabled}
              size={size}
              onClick={() => { onChange(opt.value); setOpen(false); }}
            />
          ))}
        </div>
      </PopoverContent>
    </Popover>
  );
}

function FilterItem({
  label,
  count,
  selected,
  disabled,
  size = 'sm',
  onClick,
}: {
  label: string;
  count?: number;
  selected?: boolean;
  disabled?: boolean;
  size?: 'sm' | 'md';
  onClick: () => void;
}) {
  return (
    <button
      type='button'
      disabled={disabled}
      onClick={onClick}
      className={cn(
        'flex w-full items-center gap-2 rounded-lg px-2.5 text-left transition-colors',
        size === 'sm' && 'py-1.5 text-xs',
        size === 'md' && 'py-2 text-sm',
        selected
          ? 'bg-slate-100 font-semibold text-slate-900'
          : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900',
        disabled && 'pointer-events-none opacity-40',
      )}
    >
      <span className={cn('flex h-3.5 w-3.5 shrink-0 items-center justify-center', !selected && 'invisible')}>
        <Check className='h-3 w-3' />
      </span>
      <span className='flex-1 truncate'>{label}</span>
      {count !== undefined && (
        <span className={cn(
          'ml-auto rounded-full px-1.5 py-0.5 text-[10px] font-medium leading-none',
          selected ? 'bg-slate-200 text-slate-800' : 'bg-slate-100 text-slate-500',
        )}>
          {count}
        </span>
      )}
    </button>
  );
}
