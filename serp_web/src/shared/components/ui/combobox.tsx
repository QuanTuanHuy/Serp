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
  id?: string;
  value?: string | number;
  onChange: (value: string | number | undefined) => void;
  items: ComboboxItem[];
  placeholder?: string;
  emptyText?: string;
  disabled?: boolean;
  loading?: boolean;
  clearable?: boolean;
  clearText?: string;
  onSearch?: (query: string) => void; // if provided, caller controls filtering via items
  className?: string;
  'aria-describedby'?: string;
  'aria-invalid'?: React.AriaAttributes['aria-invalid'];
}

export const Combobox: React.FC<ComboboxProps> = ({
  id,
  value,
  onChange,
  items,
  placeholder = 'Search...',
  emptyText = 'No items found',
  disabled,
  loading,
  clearable = true,
  clearText = 'Clear selection',
  onSearch,
  className,
  'aria-describedby': ariaDescribedBy,
  'aria-invalid': ariaInvalid,
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
          id={id}
          type='button'
          variant='outline'
          role='combobox'
          aria-expanded={open}
          aria-describedby={ariaDescribedBy}
          aria-invalid={ariaInvalid}
          className={cn('w-full justify-between', className)}
          disabled={disabled}
        >
          <span
            className={cn(
              'min-w-0 truncate text-left',
              !selected && 'text-muted-foreground'
            )}
          >
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
                  value={clearText}
                  onSelect={() => {
                    setQuery('');
                    handleSelect('');
                  }}
                >
                  {clearText}
                </CommandItem>
              )}
              {items.map((item) => (
                <CommandItem
                  key={item.value}
                  value={`${item.label} ${item.value}`}
                  onSelect={() => handleSelect(String(item.value))}
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
