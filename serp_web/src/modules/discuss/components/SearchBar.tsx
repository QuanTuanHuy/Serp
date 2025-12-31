/*
Author: QuanTuanHuy
Description: Part of Serp Project - Search bar component with debounced input
*/

'use client';

import { useState, useEffect, useCallback } from 'react';
import { Search, X, Clock, Filter, Command } from 'lucide-react';
import { Input } from '@/shared/components/ui/input';
import { Button } from '@/shared/components/ui/button';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui/popover';
import type { SearchFilters, ChannelType, MessageType } from '../types';

interface SearchBarProps {
  value: string;
  onChange: (value: string) => void;
  onSearch: (query: string, filters: SearchFilters) => void;
  filters?: SearchFilters;
  onFiltersChange?: (filters: SearchFilters) => void;
  placeholder?: string;
  recentSearches?: string[];
  onSelectRecent?: (query: string) => void;
  onClearRecent?: () => void;
  className?: string;
}

const DEBOUNCE_DELAY = 500; // 500ms debounce

export function SearchBar({
  value,
  onChange,
  onSearch,
  filters = {},
  onFiltersChange,
  placeholder = 'Search messages...',
  recentSearches = [],
  onSelectRecent,
  onClearRecent,
  className = '',
}: SearchBarProps) {
  const [localValue, setLocalValue] = useState(value);
  const [showRecent, setShowRecent] = useState(false);
  const [filterOpen, setFilterOpen] = useState(false);

  // Debounced search effect
  useEffect(() => {
    const timer = setTimeout(() => {
      if (localValue !== value) {
        onChange(localValue);
        if (localValue.trim()) {
          onSearch(localValue, filters);
        }
      }
    }, DEBOUNCE_DELAY);

    return () => clearTimeout(timer);
  }, [localValue, filters]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setLocalValue(e.target.value);
  };

  const handleClear = useCallback(() => {
    setLocalValue('');
    onChange('');
  }, [onChange]);

  const handleSelectRecent = (query: string) => {
    setLocalValue(query);
    onChange(query);
    onSelectRecent?.(query);
    setShowRecent(false);
    onSearch(query, filters);
  };

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
          <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
          <Input
            type='text'
            value={localValue}
            onChange={handleInputChange}
            onFocus={() => setShowRecent(recentSearches.length > 0)}
            onBlur={() => setTimeout(() => setShowRecent(false), 200)}
            placeholder={placeholder}
            className='pl-10 pr-20 h-11 bg-white/5 border-white/10 focus:border-violet-500/50 focus:bg-white/10 transition-all duration-200'
          />
          <div className='absolute right-2 top-1/2 -translate-y-1/2 flex items-center gap-1'>
            {/* Keyboard shortcut hint */}
            <kbd className='hidden sm:inline-flex h-6 select-none items-center gap-1 rounded border border-white/10 bg-white/5 px-2 font-mono text-[10px] font-medium text-slate-400'>
              <Command className='h-3 w-3' />K
            </kbd>
            {/* Clear button */}
            {localValue && (
              <Button
                variant='ghost'
                size='sm'
                onClick={handleClear}
                className='h-6 w-6 p-0 hover:bg-white/10'
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
              className='relative h-11 px-4 bg-white/5 border-white/10 hover:bg-white/10 hover:border-violet-500/50'
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
          <PopoverContent
            align='end'
            className='w-80 p-4 bg-slate-900 border-white/10'
          >
            <div className='space-y-4'>
              <div className='flex items-center justify-between'>
                <h3 className='font-semibold text-sm'>Search Filters</h3>
                <Button
                  variant='ghost'
                  size='sm'
                  onClick={() => onFiltersChange?.({})}
                  className='h-8 px-2 text-xs text-slate-400 hover:text-white'
                >
                  Clear all
                </Button>
              </div>

              {/* Date range */}
              <div className='space-y-2'>
                <label className='text-xs font-medium text-slate-400'>
                  Date Range
                </label>
                <div className='grid grid-cols-2 gap-2'>
                  <Input
                    type='date'
                    value={filters.dateFrom || ''}
                    onChange={(e) =>
                      handleFilterChange('dateFrom', e.target.value)
                    }
                    className='h-9 bg-white/5 border-white/10 text-xs'
                  />
                  <Input
                    type='date'
                    value={filters.dateTo || ''}
                    onChange={(e) =>
                      handleFilterChange('dateTo', e.target.value)
                    }
                    className='h-9 bg-white/5 border-white/10 text-xs'
                  />
                </div>
              </div>

              {/* Message type filter */}
              <div className='space-y-2'>
                <label className='text-xs font-medium text-slate-400'>
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
                          ? 'bg-gradient-to-r from-violet-600 to-fuchsia-600 border-0'
                          : 'bg-white/5 border-white/10'
                      }`}
                    >
                      {type}
                    </Button>
                  ))}
                </div>
              </div>

              {/* Has attachments toggle */}
              <div className='flex items-center justify-between'>
                <label className='text-xs font-medium text-slate-400'>
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
                      ? 'bg-gradient-to-r from-violet-600 to-fuchsia-600 border-0'
                      : 'bg-white/5 border-white/10'
                  }`}
                >
                  {filters.hasAttachments ? 'Yes' : 'No'}
                </Button>
              </div>
            </div>
          </PopoverContent>
        </Popover>
      </div>

      {/* Recent searches dropdown */}
      {showRecent && recentSearches.length > 0 && (
        <div className='absolute top-full left-0 right-0 mt-2 z-50 rounded-lg border border-white/10 bg-slate-900 shadow-xl'>
          <div className='p-2'>
            <div className='flex items-center justify-between px-2 py-1'>
              <span className='text-xs font-medium text-slate-400'>
                Recent searches
              </span>
              <Button
                variant='ghost'
                size='sm'
                onClick={onClearRecent}
                className='h-6 px-2 text-xs text-slate-400 hover:text-white'
              >
                Clear
              </Button>
            </div>
            <div className='mt-1 space-y-1'>
              {recentSearches.slice(0, 5).map((query, index) => (
                <button
                  key={index}
                  onClick={() => handleSelectRecent(query)}
                  className='w-full flex items-center gap-2 px-2 py-2 rounded-md hover:bg-white/5 transition-colors text-left'
                >
                  <Clock className='h-3.5 w-3.5 text-slate-400 flex-shrink-0' />
                  <span className='text-sm truncate'>{query}</span>
                </button>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
