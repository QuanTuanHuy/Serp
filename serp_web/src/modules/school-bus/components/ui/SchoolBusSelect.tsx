'use client';

import * as React from 'react';
import { Check, ChevronDown, Search, X } from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui/popover';

export type SchoolBusSelectOption = {
  label: string;
  value: string | number;
  description?: string;
  count?: number;
  icon?: React.ComponentType<{ className?: string }>;
  badge?: React.ReactNode;
  disabled?: boolean;
  color?: 'red' | 'blue' | 'green' | 'orange' | 'indigo' | 'violet' | 'slate';
};

export interface SchoolBusSelectProps {
  value?: string | number | null;
  onChange: (value: any) => void;
  options: SchoolBusSelectOption[];
  placeholder?: string;
  label?: string;
  searchable?: boolean;
  clearable?: boolean;
  disabled?: boolean;
  size?: 'sm' | 'md';
  fullWidth?: boolean;
  className?: string;
  searchPlaceholder?: string;
  emptyText?: string;
  icon?: React.ComponentType<{ className?: string }>;
}

const colorMaps = {
  red: {
    dot: 'bg-[#C81E3A]',
    text: 'text-[#C81E3A]',
    bg: 'bg-[#FDECEF]',
  },
  blue: {
    dot: 'bg-blue-600',
    text: 'text-blue-600',
    bg: 'bg-blue-50',
  },
  green: {
    dot: 'bg-emerald-600',
    text: 'text-emerald-600',
    bg: 'bg-emerald-50',
  },
  orange: {
    dot: 'bg-orange-600',
    text: 'text-orange-600',
    bg: 'bg-orange-50',
  },
  indigo: {
    dot: 'bg-indigo-600',
    text: 'text-indigo-600',
    bg: 'bg-indigo-50',
  },
  violet: {
    dot: 'bg-violet-600',
    text: 'text-violet-600',
    bg: 'bg-violet-50',
  },
  slate: {
    dot: 'bg-slate-500',
    text: 'text-slate-600',
    bg: 'bg-slate-50',
  },
};

export function SchoolBusSelect({
  value,
  onChange,
  options,
  placeholder = 'Select option...',
  label,
  searchable = false,
  clearable = false,
  disabled = false,
  size = 'sm',
  fullWidth = false,
  className,
  searchPlaceholder = 'Search...',
  emptyText = 'No options found',
  icon: TriggerIcon,
}: SchoolBusSelectProps) {
  const [open, setOpen] = React.useState(false);
  const [searchQuery, setSearchQuery] = React.useState('');

  // Reset search query when dropdown opens/closes
  React.useEffect(() => {
    if (!open) {
      setSearchQuery('');
    }
  }, [open]);

  const selectedOption = options.find(
    (opt) => String(opt.value) === String(value ?? '')
  );

  const displayLabel = selectedOption ? selectedOption.label : placeholder;
  const hasValue = value !== undefined && value !== null && value !== '';

  // Filter options client-side
  const filteredOptions = React.useMemo(() => {
    if (!searchable || !searchQuery) return options;
    const query = searchQuery.toLowerCase().trim();
    return options.filter(
      (opt) =>
        opt.label.toLowerCase().includes(query) ||
        (opt.description && opt.description.toLowerCase().includes(query))
    );
  }, [options, searchable, searchQuery]);

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <button
          type='button'
          disabled={disabled}
          aria-label={label || placeholder}
          className={cn(
            'inline-flex items-center justify-between gap-2 border bg-white text-slate-700 outline-none transition-all',
            'hover:border-slate-300 hover:bg-slate-50/50',
            'focus:border-slate-400 focus:ring-1 focus:ring-slate-300/30',
            'disabled:opacity-50 disabled:cursor-not-allowed disabled:bg-slate-50 disabled:border-slate-200',
            hasValue && 'border-slate-300 text-slate-900 font-semibold',
            size === 'sm' && 'h-9 px-3 text-xs rounded-lg',
            size === 'md' && 'h-10 px-4.5 text-sm rounded-xl',
            fullWidth && 'w-full',
            className
          )}
        >
          <div className='flex items-center gap-2 min-w-0 flex-1 text-left'>
            {TriggerIcon && (
              <TriggerIcon className='h-3.5 w-3.5 shrink-0 opacity-60 text-slate-500' />
            )}
            {selectedOption?.icon && !TriggerIcon && (
              <selectedOption.icon className='h-3.5 w-3.5 shrink-0 opacity-60 text-slate-500' />
            )}
            {selectedOption?.color && (
              <span
                className={cn(
                  'h-2 w-2 rounded-full shrink-0',
                  colorMaps[selectedOption.color].dot
                )}
              />
            )}
            <span className='truncate'>{displayLabel}</span>
          </div>

          <div className='flex items-center gap-1 shrink-0 ml-1'>
            {clearable && hasValue && !disabled ? (
              <X
                className='h-3.5 w-3.5 opacity-40 hover:opacity-100 hover:text-red-500 transition-colors'
                onClick={(e) => {
                  e.stopPropagation();
                  onChange('');
                  setOpen(false);
                }}
              />
            ) : (
              <ChevronDown
                className={cn(
                  'h-4 w-4 opacity-40 transition-transform duration-200',
                  open && 'rotate-180'
                )}
              />
            )}
          </div>
        </button>
      </PopoverTrigger>

      <PopoverContent
        align='start'
        className='z-[150] w-[var(--radix-popover-trigger-width)] min-w-[200px] max-w-[min(480px,calc(100vw-2rem))] rounded-xl border border-slate-200 bg-white p-1 shadow-lg'
      >
        {searchable && (
          <div className='relative p-1.5 border-b border-slate-100 mb-1'>
            <Search className='absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-slate-400' />
            <input
              type='text'
              placeholder={searchPlaceholder}
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className='w-full h-8 pl-8 pr-3 text-xs bg-slate-50 rounded-lg border border-slate-200 outline-none focus:bg-white focus:border-slate-300 transition-all'
            />
            {searchQuery && (
              <button
                type='button'
                onClick={() => setSearchQuery('')}
                className='absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600'
              >
                <X className='h-3 w-3' />
              </button>
            )}
          </div>
        )}

        <div className='max-h-60 overflow-y-auto flex flex-col gap-0.5 custom-scrollbar'>
          {filteredOptions.length === 0 ? (
            <div className='px-3 py-4 text-center text-xs text-slate-400 font-medium'>
              {emptyText}
            </div>
          ) : (
            filteredOptions.map((opt) => {
              const isSelected = String(opt.value) === String(value ?? '');
              const IconComp = opt.icon;
              const colorInfo = opt.color ? colorMaps[opt.color] : null;

              return (
                <button
                  key={opt.value}
                  type='button'
                  disabled={opt.disabled}
                  onClick={() => {
                    onChange(opt.value);
                    setOpen(false);
                  }}
                  className={cn(
                    'flex w-full items-center gap-2.5 rounded-lg px-2.5 py-2 text-left transition-all',
                    isSelected
                      ? colorInfo
                        ? cn(colorInfo.bg, colorInfo.text, 'font-semibold')
                        : 'bg-[#FDECEF] text-[#C81E3A] font-semibold'
                      : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900',
                    opt.disabled && 'pointer-events-none opacity-40'
                  )}
                >
                  <span
                    className={cn(
                      'flex h-3.5 w-3.5 shrink-0 items-center justify-center',
                      !isSelected && 'invisible'
                    )}
                  >
                    <Check
                      className={cn(
                        'h-3.5 w-3.5',
                        isSelected && !opt.color && 'text-[#C81E3A]'
                      )}
                    />
                  </span>

                  {IconComp && (
                    <IconComp
                      className={cn(
                        'h-3.5 w-3.5 shrink-0 opacity-70',
                        isSelected && colorInfo
                          ? colorInfo.text
                          : 'text-slate-400'
                      )}
                    />
                  )}

                  {opt.color && (
                    <span
                      className={cn(
                        'h-2 w-2 rounded-full shrink-0',
                        colorInfo?.dot
                      )}
                    />
                  )}

                  <div className='flex-1 min-w-0'>
                    <div className='flex items-center gap-1.5'>
                      <span className='truncate text-xs'>{opt.label}</span>
                      {opt.badge}
                    </div>
                    {opt.description && (
                      <p className='truncate text-[10px] text-slate-400 mt-0.5 font-normal'>
                        {opt.description}
                      </p>
                    )}
                  </div>

                  {opt.count !== undefined && (
                    <span
                      className={cn(
                        'ml-auto rounded-full px-1.5 py-0.5 text-[9px] font-medium leading-none',
                        isSelected
                          ? colorInfo
                            ? cn(colorInfo.bg, colorInfo.text)
                            : 'bg-white text-[#C81E3A] border border-[#C81E3A]/20'
                          : 'bg-slate-100 text-slate-500'
                      )}
                    >
                      {opt.count}
                    </span>
                  )}
                </button>
              );
            })
          )}
        </div>
      </PopoverContent>
    </Popover>
  );
}
