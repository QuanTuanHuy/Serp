/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM global search dropdown
 */

'use client';

import { Search } from 'lucide-react';

import type { PMGlobalSearchGroup, PMGlobalSearchItem } from '../../types/api';

interface PMGlobalSearchDropdownProps {
  groups: PMGlobalSearchGroup[];
  isLoading: boolean;
  isError: boolean;
  query: string;
  onSelect: (item: PMGlobalSearchItem) => void;
  onViewAll: () => void;
}

export function PMGlobalSearchDropdown({
  groups,
  isLoading,
  isError,
  query,
  onSelect,
  onViewAll,
}: PMGlobalSearchDropdownProps) {
  const hasResults = groups.some((group) => group.items.length > 0);

  return (
    <div className='absolute left-0 right-0 top-full z-50 mt-2 overflow-hidden rounded-md border bg-background shadow-lg'>
      {isLoading && (
        <div className='px-3 py-2 text-sm text-muted-foreground'>
          Searching...
        </div>
      )}

      {isError && !isLoading && (
        <div className='px-3 py-2 text-sm text-destructive'>
          Search failed. Try again.
        </div>
      )}

      {!isLoading && !isError && !hasResults && (
        <div className='px-3 py-2 text-sm text-muted-foreground'>
          No results for "{query}"
        </div>
      )}

      {!isLoading &&
        !isError &&
        groups.map((group) =>
          group.items.length ? (
            <div key={group.type} className='border-b last:border-b-0'>
              <div className='bg-muted/50 px-3 py-1.5 text-xs font-medium uppercase tracking-wide text-muted-foreground'>
                {group.title}
              </div>
              <div className='py-1'>
                {group.items.map((item) => (
                  <button
                    key={`${group.type}-${item.id}`}
                    type='button'
                    className='flex w-full items-start gap-2 px-3 py-2 text-left text-sm hover:bg-muted'
                    onMouseDown={(event) => event.preventDefault()}
                    onClick={() => onSelect(item)}
                  >
                    <Search className='mt-0.5 h-4 w-4 text-muted-foreground' />
                    <span className='min-w-0 flex-1'>
                      <span className='block truncate font-medium'>
                        {item.title}
                      </span>
                      {item.subtitle && (
                        <span className='block truncate text-xs text-muted-foreground'>
                          {item.subtitle}
                        </span>
                      )}
                    </span>
                  </button>
                ))}
              </div>
            </div>
          ) : null
        )}

      <button
        type='button'
        className='w-full px-3 py-2 text-left text-sm font-medium text-primary hover:bg-muted'
        onMouseDown={(event) => event.preventDefault()}
        onClick={onViewAll}
      >
        View all results
      </button>
    </div>
  );
}
