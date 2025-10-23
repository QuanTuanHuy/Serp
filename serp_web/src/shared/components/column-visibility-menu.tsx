/*
Author: QuanTuanHuy
Description: Part of Serp Project - Column visibility toggle menu
*/

'use client';

import React from 'react';
import { Settings, Eye, EyeOff, RotateCcw } from 'lucide-react';
import { Button } from './ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuTrigger,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuLabel,
} from './ui/dropdown-menu';
import { Checkbox } from './ui/checkbox';
import { ColumnVisibilityMenuProps } from '../types/datatable.types';

export function ColumnVisibilityMenu({
  columns,
  visibility,
  onVisibilityChange,
  onReset,
}: ColumnVisibilityMenuProps & { onReset?: () => void }) {
  const visibleCount = columns.filter(
    (col) => visibility[col.id] !== false
  ).length;
  const hasHidden = visibleCount < columns.length;

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant='outline' size='sm' className='gap-2'>
          <Settings className='h-4 w-4' />
          Columns
          {hasHidden && (
            <span className='ml-1 rounded-full bg-primary px-2 py-0.5 text-xs text-primary-foreground'>
              {visibleCount}/{columns.length}
            </span>
          )}
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align='end' className='w-56'>
        <DropdownMenuLabel className='flex items-center justify-between'>
          <span>Toggle Columns</span>
          {onReset && hasHidden && (
            <Button
              variant='ghost'
              size='sm'
              onClick={(e) => {
                e.stopPropagation();
                onReset();
              }}
              className='h-6 px-2'
            >
              <RotateCcw className='h-3 w-3' />
            </Button>
          )}
        </DropdownMenuLabel>
        <DropdownMenuSeparator />
        <div className='max-h-[300px] overflow-y-auto'>
          {columns.map((column) => {
            const isVisible = visibility[column.id] !== false;
            return (
              <DropdownMenuItem
                key={column.id}
                className='flex items-center gap-2 cursor-pointer'
                onSelect={(e) => {
                  e.preventDefault();
                  onVisibilityChange(column.id, !isVisible);
                }}
              >
                <Checkbox
                  checked={isVisible}
                  onCheckedChange={(checked) =>
                    onVisibilityChange(column.id, checked as boolean)
                  }
                  onClick={(e) => e.stopPropagation()}
                />
                {isVisible ? (
                  <Eye className='h-3.5 w-3.5 text-muted-foreground' />
                ) : (
                  <EyeOff className='h-3.5 w-3.5 text-muted-foreground' />
                )}
                <span className='flex-1'>{column.header}</span>
              </DropdownMenuItem>
            );
          })}
        </div>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
