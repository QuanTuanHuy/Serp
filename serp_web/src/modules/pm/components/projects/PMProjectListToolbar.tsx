/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project list toolbar
 */

'use client';

import { Search, X, SlidersHorizontal } from 'lucide-react';
import {
  Badge,
  Button,
  Input,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Tabs,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import type { PMProjectSort } from '../../types/project-list.types';

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
  onCategoryFilterChange: (value: PMProjectCategoryFilter) => void;
  categoryOptions: PMProjectCategoryOption[];
  statusFilter: PMProjectStatusFilter;
  onStatusFilterChange: (value: PMProjectStatusFilter) => void;
  sortBy: PMProjectSort;
  onSortByChange: (value: PMProjectSort) => void;
  hasActiveFilters: boolean;
  onClearFilters: () => void;
  resultCount: number;
  totalCount: number;
}

export function PMProjectListToolbar({
  searchQuery,
  onSearchQueryChange,
  categoryFilter,
  onCategoryFilterChange,
  categoryOptions,
  statusFilter,
  onStatusFilterChange,
  sortBy,
  onSortByChange,
  hasActiveFilters,
  onClearFilters,
  resultCount,
  totalCount,
}: PMProjectListToolbarProps) {
  return (
    <div className='flex flex-col gap-4 rounded-2xl border border-border/80 bg-card/60 p-4 shadow-sm backdrop-blur-md xl:flex-row xl:items-center xl:justify-between'>
      {/* Left: Search & Info */}
      <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:gap-4 flex-1'>
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

        <div className='flex items-center gap-2.5 text-xs text-muted-foreground'>
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

      {/* Right: Filters & Clear */}
      <div className='flex flex-wrap items-center gap-3'>
        {/* Status Segmented Tabs */}
        <Tabs
          value={statusFilter}
          onValueChange={(value) =>
            onStatusFilterChange(value as PMProjectStatusFilter)
          }
          className='w-auto'
        >
          <TabsList className='h-10 bg-muted/40 border border-border/60 p-1'>
            <TabsTrigger value='ALL' className='px-3.5 text-xs h-8'>
              All
            </TabsTrigger>
            <TabsTrigger value='ACTIVE' className='px-3.5 text-xs h-8'>
              Active
            </TabsTrigger>
            <TabsTrigger value='ARCHIVED' className='px-3.5 text-xs h-8'>
              Archived
            </TabsTrigger>
          </TabsList>
        </Tabs>

        {/* Category Dropdown */}
        <div className='w-[180px]'>
          <Select
            value={categoryFilter}
            onValueChange={(value) =>
              onCategoryFilterChange(value as PMProjectCategoryFilter)
            }
          >
            <SelectTrigger className='h-10 bg-background/50 border-border/70 focus:ring-primary/30'>
              <SelectValue placeholder='Category' />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='ALL'>All categories</SelectItem>
              {categoryOptions.map((category) => (
                <SelectItem key={category.id} value={category.id}>
                  {category.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        {/* Sort By Dropdown */}
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

        {/* Clear Filters */}
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
