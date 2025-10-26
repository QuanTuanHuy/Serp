'use client';

import * as React from 'react';
import { Check, ChevronsUpDown } from 'lucide-react';
import { Button } from './button';
import { cn } from '@/shared/utils';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui/popover';
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/shared/components/ui/command';

export type ComboboxItem = {
  value: string | number;
  label: string;
};

export interface ComboboxProps {
  value?: string | number;
  onChange: (value: string | number | undefined) => void;
  items: ComboboxItem[];
  placeholder?: string;
  emptyText?: string;
  disabled?: boolean;
  loading?: boolean;
  clearable?: boolean;
  onSearch?: (query: string) => void; // if provided, caller controls filtering via items
  className?: string;
}

export const Combobox: React.FC<ComboboxProps> = ({
  value,
  onChange,
  items,
  placeholder = 'Search...',
  emptyText = 'No items found',
  disabled,
  loading,
  clearable = true,
  onSearch,
  className,
}) => {
  const [open, setOpen] = React.useState(false);
  const [query, setQuery] = React.useState('');

  const selected = React.useMemo(
    () => items.find((i) => String(i.value) === String(value)),
    [items, value]
  );

  const handleSelect = (val: string) => {
    const found = items.find((i) => String(i.value) === val);
    onChange(found ? found.value : undefined);
    setOpen(false);
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          type='button'
          variant='outline'
          role='combobox'
          aria-expanded={open}
          className={cn('w-full justify-between', className)}
          disabled={disabled}
        >
          <span className={cn(!selected && 'text-muted-foreground')}>
            {selected ? selected.label : placeholder}
          </span>
          <ChevronsUpDown className='ml-2 h-4 w-4 shrink-0 opacity-50' />
        </Button>
      </PopoverTrigger>
      <PopoverContent className='w-[var(--radix-popover-trigger-width)] p-0'>
        <Command shouldFilter={!onSearch}>
          <CommandInput
            value={query}
            placeholder={placeholder}
            onValueChange={(q) => {
              setQuery(q);
              onSearch?.(q);
            }}
          />
          <CommandList>
            <CommandEmpty>{loading ? 'Loading...' : emptyText}</CommandEmpty>
            <CommandGroup>
              {clearable && value !== undefined && (
                <CommandItem
                  value='__clear__'
                  onSelect={() => {
                    setQuery('');
                    handleSelect('');
                  }}
                >
                  Clear selection
                </CommandItem>
              )}
              {items.map((item) => (
                <CommandItem
                  key={item.value}
                  value={String(item.value)}
                  onSelect={handleSelect}
                >
                  <Check
                    className={cn(
                      'mr-2 h-4 w-4',
                      String(item.value) === String(value)
                        ? 'opacity-100'
                        : 'opacity-0'
                    )}
                  />
                  {item.label}
                </CommandItem>
              ))}
            </CommandGroup>
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  );
};
