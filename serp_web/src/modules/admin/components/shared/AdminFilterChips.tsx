'use client';

/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Active filter chip bar
 */

import { X } from 'lucide-react';
import { Badge, Button } from '@/shared/components/ui';

export interface AdminFilterChip {
  id: string;
  label: string;
  onRemove: () => void;
}

export interface AdminFilterChipsProps {
  chips: AdminFilterChip[];
  onClearAll: () => void;
}

export function AdminFilterChips({ chips, onClearAll }: AdminFilterChipsProps) {
  if (chips.length === 0) {
    return null;
  }

  return (
    <div className='flex flex-wrap items-center gap-2'>
      {chips.map((chip) => (
        <Badge
          key={chip.id}
          variant='outline'
          className='gap-1.5 rounded-full px-3 py-1 text-xs font-medium'
        >
          <span>{chip.label}</span>
          <button
            type='button'
            onClick={chip.onRemove}
            className='inline-flex h-4 w-4 items-center justify-center rounded-full text-muted-foreground hover:bg-muted hover:text-foreground'
          >
            <X className='h-3 w-3' />
            <span className='sr-only'>Remove filter</span>
          </button>
        </Badge>
      ))}
      <Button
        type='button'
        variant='ghost'
        size='sm'
        onClick={onClearAll}
        className='h-7 px-2 text-xs'
      >
        Clear all
      </Button>
    </div>
  );
}
