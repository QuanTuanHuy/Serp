/*
Author: QuanTuanHuy
Description: Part of Serp Project - Search bar component with debounced input
*/

'use client';

import { useState, useEffect, useCallback } from 'react';
import { Search, X, Filter } from 'lucide-react';
import { Input } from '@/shared/components/ui/input';
import { Button } from '@/shared/components/ui/button';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui/popover';
import type { SearchFilters, MessageType } from '../types';

interface SearchBarProps {
  value: string;
  onChange: (value: string) => void;
  filters?: SearchFilters;
  onFiltersChange?: (filters: SearchFilters) => void;
  placeholder?: string;
  className?: string;
}

const DEBOUNCE_DELAY = 500; // 500ms debounce

export function SearchBar({
  value,
  onChange,
  filters = {},
  onFiltersChange,
  placeholder = 'Search messages...',
  className = '',
}: SearchBarProps) {
  const [localValue, setLocalValue] = useState(value);
  const [filterOpen, setFilterOpen] = useState(false);

  // Debounced search effect
  useEffect(() => {
    const timer = setTimeout(() => {
      if (localValue !== value) {
        onChange(localValue);
      }
    }, DEBOUNCE_DELAY);

    return () => clearTimeout(timer);
  }, [localValue]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setLocalValue(e.target.value);
  };

  const handleClear = useCallback(() => {
    setLocalValue('');
    onChange('');
  }, [onChange]);

  const handleFilterChange = (key: keyof SearchFilters, value: any) => {
    const newFilters = { ...filters, [key]: value };
    onFiltersChange?.(newFilters);
  };

  const activeFilterCount = Object.values(filters).filter(Boolean).length;

  return (
    <div className={`relative ${className}`}>
      <div className='relative flex items-center gap-2'>
        {/* Search Input */}
        <div className='relative flex-1'>
          <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
          <Input
            type='text'
            value={localValue}
            onChange={handleInputChange}
            placeholder={placeholder}
            autoFocus
            className='pl-10 pr-10 h-11 bg-muted/50 border-border focus:border-primary/50 focus:bg-muted transition-all duration-200'
          />
          <div className='absolute right-2 top-1/2 -translate-y-1/2 flex items-center gap-1'>
            {/* Clear button */}
            {localValue && (
              <Button
                variant='ghost'
                size='sm'
                onClick={handleClear}
                className='h-6 w-6 p-0 hover:bg-accent'
              >
                <X className='h-4 w-4' />
              </Button>
            )}
          </div>
        </div>

        {/* Filter button */}
        <Popover open={filterOpen} onOpenChange={setFilterOpen}>
          <PopoverTrigger asChild>
            <Button
              variant='outline'
              size='sm'
              className='relative h-11 px-4 bg-muted/50 border-border hover:bg-accent hover:border-primary/50'
            >
              <Filter className='h-4 w-4 mr-2' />
              Filters
              {activeFilterCount > 0 && (
                <span className='ml-2 flex h-5 w-5 items-center justify-center rounded-full bg-violet-500 text-[10px] font-semibold text-white'>
                  {activeFilterCount}
                </span>
              )}
            </Button>
          </PopoverTrigger>
          <PopoverContent align='end' className='w-80 p-4 bg-popover border'>
            <div className='space-y-4'>
              <div className='flex items-center justify-between'>
                <h3 className='font-semibold text-sm text-foreground'>
                  Search Filters
                </h3>
                <Button
                  variant='ghost'
                  size='sm'
                  onClick={() => onFiltersChange?.({})}
                  className='h-8 px-2 text-xs text-muted-foreground hover:text-foreground'
                >
                  Clear all
                </Button>
              </div>

              {/* Date range */}
              <div className='space-y-2'>
                <label className='text-xs font-medium text-muted-foreground'>
                  Date Range
                </label>
                <div className='grid grid-cols-2 gap-2'>
                  <Input
                    type='date'
                    value={filters.dateFrom || ''}
                    onChange={(e) =>
                      handleFilterChange('dateFrom', e.target.value)
                    }
                    className='h-9 bg-muted/50 border-border text-xs'
                  />
                  <Input
                    type='date'
                    value={filters.dateTo || ''}
                    onChange={(e) =>
                      handleFilterChange('dateTo', e.target.value)
                    }
                    className='h-9 bg-muted/50 border-border text-xs'
                  />
                </div>
              </div>

              {/* Message type filter */}
              <div className='space-y-2'>
                <label className='text-xs font-medium text-muted-foreground'>
                  Message Type
                </label>
                <div className='flex flex-wrap gap-2'>
                  {(['TEXT', 'IMAGE', 'FILE'] as MessageType[]).map((type) => (
                    <Button
                      key={type}
                      variant={
                        filters.messageType === type ? 'default' : 'outline'
                      }
                      size='sm'
                      onClick={() =>
                        handleFilterChange(
                          'messageType',
                          filters.messageType === type ? undefined : type
                        )
                      }
                      className={`h-8 px-3 text-xs ${
                        filters.messageType === type
                          ? 'bg-primary text-primary-foreground border-0'
                          : 'bg-muted/50 border-border'
                      }`}
                    >
                      {type}
                    </Button>
                  ))}
                </div>
              </div>

              {/* Has attachments toggle */}
              <div className='flex items-center justify-between'>
                <label className='text-xs font-medium text-muted-foreground'>
                  Has Attachments
                </label>
                <Button
                  variant={filters.hasAttachments ? 'default' : 'outline'}
                  size='sm'
                  onClick={() =>
                    handleFilterChange(
                      'hasAttachments',
                      !filters.hasAttachments
                    )
                  }
                  className={`h-8 px-3 text-xs ${
                    filters.hasAttachments
                      ? 'bg-primary text-primary-foreground border-0'
                      : 'bg-muted/50 border-border'
                  }`}
                >
                  {filters.hasAttachments ? 'Yes' : 'No'}
                </Button>
              </div>
            </div>
          </PopoverContent>
        </Popover>
      </div>
    </div>
  );
}
