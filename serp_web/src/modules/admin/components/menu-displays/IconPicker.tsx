/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Icon Picker component for menu displays
 */

'use client';

import React, { useState, useMemo } from 'react';
import * as LucideIcons from 'lucide-react';
import { Input } from '@/shared/components/ui/input';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui/popover';
import { Button } from '@/shared/components/ui/button';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import { Search, X } from 'lucide-react';
import { cn } from '@/shared/utils';
import { useDebounce } from '@/shared/hooks/use-debounce';

// Popular icons for quick access
const POPULAR_ICONS = [
  'Home',
  'Users',
  'Settings',
  'FileText',
  'Calendar',
  'Mail',
  'Bell',
  'Heart',
  'Star',
  'Shield',
  'Lock',
  'Key',
  'Database',
  'Server',
  'Cloud',
  'Package',
  'ShoppingCart',
  'CreditCard',
  'DollarSign',
  'TrendingUp',
  'BarChart',
  'PieChart',
  'Layout',
  'Grid',
  'List',
  'Folder',
  'File',
  'Image',
  'Video',
  'Music',
];

const MAX_ICONS_DISPLAY = 180;

interface IconPickerProps {
  value?: string;
  onChange: (iconName: string) => void;
  className?: string;
}

export const IconPicker: React.FC<IconPickerProps> = ({
  value,
  onChange,
  className,
}) => {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState('');

  const debouncedSearch = useDebounce(search, 300);

  const allIconNames = useMemo(() => {
    return Object.keys(LucideIcons).filter(
      (key) =>
        key !== 'default' &&
        key !== 'createLucideIcon' &&
        !key.startsWith('Lucide') &&
        typeof (LucideIcons as any)[key] === 'object' &&
        (LucideIcons as any)[key]?.$$typeof
    );
  }, []);

  // Filter icons based on search with limit
  const filteredIcons = useMemo(() => {
    if (!debouncedSearch) return POPULAR_ICONS;

    const searchLower = debouncedSearch.toLowerCase();
    let filtered = allIconNames.filter((name) =>
      name.toLowerCase().includes(searchLower)
    );

    if (filtered.length === 0) {
      filtered = POPULAR_ICONS.filter((name) =>
        name.toLowerCase().includes(searchLower)
      );
    }

    return filtered.slice(0, MAX_ICONS_DISPLAY);
  }, [debouncedSearch, allIconNames]);

  // Check if results were truncated
  const hasMoreResults = useMemo(() => {
    if (!debouncedSearch) return false;

    const searchLower = debouncedSearch.toLowerCase();
    const totalMatches = allIconNames.filter((name) =>
      name.toLowerCase().includes(searchLower)
    ).length;

    return totalMatches > MAX_ICONS_DISPLAY;
  }, [debouncedSearch, allIconNames]);

  const getIconComponent = (iconName: string) => {
    const IconComponent = (LucideIcons as any)[iconName];
    return IconComponent || LucideIcons.HelpCircle;
  };

  const SelectedIcon = value ? getIconComponent(value) : null;

  const handleSelect = (iconName: string) => {
    onChange(iconName);
    setOpen(false);
    setSearch('');
  };

  const handleClear = (e: React.MouseEvent) => {
    e.stopPropagation();
    onChange('');
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          variant='outline'
          role='combobox'
          aria-expanded={open}
          className={cn('w-full justify-between', className)}
        >
          {value ? (
            <div className='flex items-center gap-2'>
              {SelectedIcon && <SelectedIcon className='h-4 w-4' />}
              <span className='truncate'>{value}</span>
            </div>
          ) : (
            <span className='text-muted-foreground'>Select icon...</span>
          )}
          <div className='flex items-center gap-1'>
            {value && (
              <X
                className='h-4 w-4 opacity-50 hover:opacity-100'
                onClick={handleClear}
              />
            )}
            <LucideIcons.ChevronsUpDown className='ml-2 h-4 w-4 shrink-0 opacity-50' />
          </div>
        </Button>
      </PopoverTrigger>
      <PopoverContent className='w-80 p-0' align='start'>
        <div className='flex flex-col'>
          {/* Search */}
          <div className='p-3 border-b'>
            <div className='relative'>
              <Search className='absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground' />
              <Input
                placeholder='Search icons...'
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className='pl-8'
              />
            </div>
          </div>

          {/* Icon Grid */}
          <ScrollArea className='h-[360px]'>
            {filteredIcons.length === 0 ? (
              <div className='flex flex-col items-center justify-center p-8 text-center min-h-[360px]'>
                <LucideIcons.Search className='h-8 w-8 text-muted-foreground mb-2' />
                <p className='text-sm text-muted-foreground'>
                  No icons found matching &ldquo;{search}&rdquo;
                </p>
              </div>
            ) : (
              <div className='p-2 pb-4'>
                {!debouncedSearch && (
                  <div className='px-2 py-1.5 text-xs font-medium text-muted-foreground'>
                    Popular Icons
                  </div>
                )}
                {debouncedSearch && hasMoreResults && (
                  <div className='px-2 py-1.5 mb-2 text-xs text-amber-600 bg-amber-50 dark:bg-amber-950 dark:text-amber-400 rounded'>
                    Showing first {MAX_ICONS_DISPLAY} results. Try a more
                    specific search.
                  </div>
                )}
                <div className='grid grid-cols-6 gap-1'>
                  {filteredIcons.map((iconName) => {
                    const IconComponent = getIconComponent(iconName);
                    const isSelected = value === iconName;

                    return (
                      <button
                        key={iconName}
                        onClick={() => handleSelect(iconName)}
                        className={cn(
                          'flex flex-col items-center justify-center p-2 rounded-md hover:bg-accent hover:text-accent-foreground transition-colors group',
                          isSelected && 'bg-accent text-accent-foreground'
                        )}
                        title={iconName}
                      >
                        <IconComponent className='h-5 w-5' />
                        <span className='text-[10px] mt-1 truncate w-full text-center opacity-0 group-hover:opacity-100 transition-opacity'>
                          {iconName}
                        </span>
                      </button>
                    );
                  })}
                </div>
              </div>
            )}
          </ScrollArea>

          {/* Footer */}
          <div className='border-t p-2 text-xs text-muted-foreground text-center'>
            {filteredIcons.length} icon{filteredIcons.length !== 1 ? 's' : ''}{' '}
            {debouncedSearch && 'found'}
            {hasMoreResults && ' (limited)'}
          </div>
        </div>
      </PopoverContent>
    </Popover>
  );
};
