'use client';

import { Button } from '@/shared/components/ui';
import type { PagedResponse } from '../types';
import { schoolBusUi } from '../theme';

interface SchoolBusPaginationBarProps {
  page?: PagedResponse<unknown> | null;
  onPageChange: (page: number) => void;
}

export function SchoolBusPaginationBar({
  page,
  onPageChange,
}: SchoolBusPaginationBarProps) {
  if (!page) {
    return null;
  }

  const currentPage = page.page + 1;
  const totalPages = Math.max(page.totalPages, 1);

  return (
    <div className='flex flex-col gap-3 border-t border-border px-4 py-3 text-sm text-muted-foreground sm:flex-row sm:items-center sm:justify-between'>
      <div>
        Page <span className='font-semibold text-foreground'>{currentPage}</span>{' '}
        of <span className='font-semibold text-foreground'>{totalPages}</span>
        {' · '}
        <span>{page.totalElements} records</span>
      </div>
      <div className='flex gap-2'>
        <Button
          type='button'
          variant='outline'
          size='sm'
          className={schoolBusUi.outlineButton}
          disabled={page.first}
          onClick={() => onPageChange(Math.max(page.page - 1, 0))}
        >
          Previous
        </Button>
        <Button
          type='button'
          variant='outline'
          size='sm'
          className={schoolBusUi.outlineButton}
          disabled={page.last}
          onClick={() => onPageChange(page.page + 1)}
        >
          Next
        </Button>
      </div>
    </div>
  );
}
