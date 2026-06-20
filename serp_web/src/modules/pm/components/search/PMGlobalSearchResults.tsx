/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM global search grouped results
 */

'use client';

import Link from 'next/link';
import { Search } from 'lucide-react';

import type { PMGlobalSearchGroup } from '../../types/api';

interface PMGlobalSearchResultsProps {
  groups: PMGlobalSearchGroup[];
}

export function PMGlobalSearchResults({ groups }: PMGlobalSearchResultsProps) {
  const visibleGroups = groups.filter((group) => group.items.length > 0);

  if (!visibleGroups.length) {
    return (
      <div className='rounded-md border border-dashed p-8 text-center text-sm text-muted-foreground'>
        No results found.
      </div>
    );
  }

  return (
    <div className='space-y-6'>
      {visibleGroups.map((group) => (
        <section key={group.type} className='space-y-3'>
          <div className='flex items-center justify-between'>
            <h2 className='text-base font-semibold'>{group.title}</h2>
            <span className='text-sm text-muted-foreground'>
              {group.total} result{group.total === 1 ? '' : 's'}
            </span>
          </div>
          <div className='divide-y rounded-md border'>
            {group.items.map((item) => (
              <Link
                key={`${group.type}-${item.id}`}
                href={item.url}
                className='flex items-start gap-3 p-4 transition-colors hover:bg-muted/60'
              >
                <Search className='mt-1 h-4 w-4 text-muted-foreground' />
                <span className='min-w-0 flex-1'>
                  <span className='block font-medium'>{item.title}</span>
                  {item.subtitle && (
                    <span className='mt-1 block text-sm text-muted-foreground'>
                      {item.subtitle}
                    </span>
                  )}
                </span>
              </Link>
            ))}
          </div>
        </section>
      ))}
    </div>
  );
}
