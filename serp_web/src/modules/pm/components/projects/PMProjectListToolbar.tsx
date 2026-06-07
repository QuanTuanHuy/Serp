/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project list toolbar
 */

'use client';

import type React from 'react';
import { Grid2X2, List, Search, SlidersHorizontal, X } from 'lucide-react';
import {
  Badge,
  Button,
  Input,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type {
  PMProjectSort,
  PMProjectViewMode,
} from '../../types/project-list.types';

export type PMProjectStatusFilter = 'ALL' | 'ACTIVE' | 'ARCHIVED';
export type PMProjectCategoryFilter = 'ALL' | string;

interface PMProjectCategoryOption {
  id: string;
  name: string;
}

interface PMProjectListToolbarProps {
  searchQuery: string;
  onSearchQueryChange: (value: string) => void;
  categoryFilter: PMProjectCategoryFilter;
  categoryOptions: PMProjectCategoryOption[];
  statusFilter: PMProjectStatusFilter;
  sortBy: PMProjectSort;
  onSortByChange: (value: PMProjectSort) => void;
  viewMode: PMProjectViewMode;
  onViewModeChange: (value: PMProjectViewMode) => void;
  activeFilterCount: number;
  onOpenFilters: () => void;
  hasActiveFilters: boolean;
  onClearFilters: () => void;
  resultCount: number;
  totalCount: number;
}

export function PMProjectListToolbar({
  searchQuery,
  onSearchQueryChange,
  categoryFilter,
  categoryOptions,
  statusFilter,
  sortBy,
  onSortByChange,
  viewMode,
  onViewModeChange,
  activeFilterCount,
  onOpenFilters,
  hasActiveFilters,
  onClearFilters,
  resultCount,
  totalCount,
}: PMProjectListToolbarProps) {
  const activeCategory = categoryOptions.find(
    (category) => category.id === categoryFilter
  );
  const filterLabel =
    activeFilterCount === 0
      ? 'Filters'
      : [
          statusFilter !== 'ALL' ? statusFilter.toLowerCase() : undefined,
          activeCategory?.name,
        ]
          .filter(Boolean)
          .join(' / ');

  return (
    <div className='flex flex-col gap-4 rounded-xl border border-border/80 bg-card/60 p-4 shadow-sm backdrop-blur-md xl:flex-row xl:items-center xl:justify-between'>
      <div className='flex flex-1 flex-col gap-3 sm:flex-row sm:items-center sm:gap-4'>
        <div className='relative w-full sm:max-w-xs'>
          <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
          <Input
            value={searchQuery}
            onChange={(event) => onSearchQueryChange(event.target.value)}
            placeholder='Search projects, keys, leads...'
            className='pl-9 h-10 bg-background/50 border-border/70 focus-visible:ring-primary/30'
            aria-label='Search projects'
          />
        </div>

        <div className='flex flex-wrap items-center gap-2.5 text-xs text-muted-foreground'>
          <Badge
            variant='outline'
            className='rounded-full px-2.5 py-0.5 border-border/50 text-[10px] uppercase tracking-wide bg-background/40'
          >
            Software
          </Badge>
          <span>
            Showing{' '}
            <span className='font-semibold text-foreground'>{resultCount}</span>{' '}
            of{' '}
            <span className='font-semibold text-foreground'>{totalCount}</span>
          </span>
        </div>
      </div>

      <div className='flex flex-wrap items-center gap-3'>
        <Button
          type='button'
          variant='outline'
          onClick={onOpenFilters}
          className='h-10 gap-2 rounded-lg bg-background/50 border-border/70'
        >
          <SlidersHorizontal className='h-4 w-4' />
          <span className='max-w-[150px] truncate text-sm'>{filterLabel}</span>
          {activeFilterCount > 0 ? (
            <Badge variant='secondary' className='ml-0.5'>
              {activeFilterCount}
            </Badge>
          ) : null}
        </Button>

        <div className='w-[180px]'>
          <Select
            value={sortBy}
            onValueChange={(value) => onSortByChange(value as PMProjectSort)}
          >
            <SelectTrigger className='h-10 bg-background/50 border-border/70 focus:ring-primary/30'>
              <SelectValue placeholder='Sort by' />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='recentlyUpdated'>Recently updated</SelectItem>
              <SelectItem value='name'>Name</SelectItem>
              <SelectItem value='createdDate'>Created date</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className='flex h-10 rounded-lg border border-border/70 bg-background/50 p-1'>
          <ViewModeButton
            label='List view'
            active={viewMode === 'list'}
            onClick={() => onViewModeChange('list')}
          >
            <List className='h-4 w-4' />
          </ViewModeButton>
          <ViewModeButton
            label='Grid view'
            active={viewMode === 'grid'}
            onClick={() => onViewModeChange('grid')}
          >
            <Grid2X2 className='h-4 w-4' />
          </ViewModeButton>
        </div>

        {hasActiveFilters && (
          <Button
            type='button'
            variant='ghost'
            size='icon'
            onClick={onClearFilters}
            className='h-10 w-10 text-muted-foreground hover:text-foreground hover:bg-muted/50 rounded-xl'
            title='Clear filters'
          >
            <X className='h-4 w-4' />
          </Button>
        )}
      </div>
    </div>
  );
}

function ViewModeButton({
  label,
  active,
  onClick,
  children,
}: {
  label: string;
  active: boolean;
  onClick: () => void;
  children: React.ReactNode;
}) {
  return (
    <Button
      type='button'
      variant='ghost'
      size='icon'
      onClick={onClick}
      className={cn(
        'h-8 w-8 rounded-md text-muted-foreground hover:text-foreground',
        active && 'bg-card text-foreground shadow-sm'
      )}
      aria-label={label}
      title={label}
    >
      {children}
    </Button>
  );
}
