/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project list toolbar
 */

'use client';

import { Search, X } from 'lucide-react';
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
import type {
  PMProjectSort,
  PMProjectStatus,
  PMProjectTemplateType,
} from '../../types/project-list.types';

export type PMProjectTemplateFilter = 'ALL' | PMProjectTemplateType;
export type PMProjectStatusFilter = 'ALL' | PMProjectStatus;
export type PMProjectCategoryFilter = 'ALL' | string;
export type PMProjectLeadFilter = 'ALL' | string;

interface PMProjectListToolbarProps {
  searchQuery: string;
  onSearchQueryChange: (value: string) => void;
  categoryFilter: PMProjectCategoryFilter;
  onCategoryFilterChange: (value: PMProjectCategoryFilter) => void;
  categoryOptions: string[];
  leadFilter: PMProjectLeadFilter;
  onLeadFilterChange: (value: PMProjectLeadFilter) => void;
  leadOptions: Array<{ id: string; name: string }>;
  templateFilter: PMProjectTemplateFilter;
  onTemplateFilterChange: (value: PMProjectTemplateFilter) => void;
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
  leadFilter,
  onLeadFilterChange,
  leadOptions,
  templateFilter,
  onTemplateFilterChange,
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
    <div className='space-y-4 rounded-2xl border bg-card p-4 shadow-sm'>
      <div className='flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between'>
        <div className='relative w-full xl:max-w-sm'>
          <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
          <Input
            value={searchQuery}
            onChange={(event) => onSearchQueryChange(event.target.value)}
            placeholder='Search projects, keys, leads...'
            className='pl-9'
            aria-label='Search projects'
          />
        </div>

        <div className='flex flex-wrap items-center gap-2 text-sm text-muted-foreground'>
          <Badge variant='outline' className='rounded-full px-3 py-1'>
            Software projects only
          </Badge>
          <span>
            Showing{' '}
            <span className='font-semibold text-foreground'>{resultCount}</span>{' '}
            of{' '}
            <span className='font-semibold text-foreground'>{totalCount}</span>
          </span>
        </div>
      </div>

      <div className='grid gap-3 md:grid-cols-2 xl:grid-cols-[220px_220px_220px_220px_220px_auto]'>
        <Select
          value={categoryFilter}
          onValueChange={(value) =>
            onCategoryFilterChange(value as PMProjectCategoryFilter)
          }
        >
          <SelectTrigger>
            <SelectValue placeholder='Category' />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value='ALL'>All categories</SelectItem>
            {categoryOptions.map((category) => (
              <SelectItem key={category} value={category}>
                {category}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Select
          value={leadFilter}
          onValueChange={(value) =>
            onLeadFilterChange(value as PMProjectLeadFilter)
          }
        >
          <SelectTrigger>
            <SelectValue placeholder='Lead' />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value='ALL'>All leads</SelectItem>
            {leadOptions.map((lead) => (
              <SelectItem key={lead.id} value={lead.id}>
                {lead.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Select
          value={templateFilter}
          onValueChange={(value) =>
            onTemplateFilterChange(value as PMProjectTemplateFilter)
          }
        >
          <SelectTrigger>
            <SelectValue placeholder='Template' />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value='ALL'>All templates</SelectItem>
            <SelectItem value='BLANK'>Blank</SelectItem>
            <SelectItem value='KANBAN'>Kanban</SelectItem>
            <SelectItem value='SCRUM'>Scrum</SelectItem>
          </SelectContent>
        </Select>

        <Select
          value={statusFilter}
          onValueChange={(value) =>
            onStatusFilterChange(value as PMProjectStatusFilter)
          }
        >
          <SelectTrigger>
            <SelectValue placeholder='Status' />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value='ALL'>All statuses</SelectItem>
            <SelectItem value='ACTIVE'>Active</SelectItem>
            <SelectItem value='COMPLETED'>Completed</SelectItem>
            <SelectItem value='ARCHIVED'>Archived</SelectItem>
          </SelectContent>
        </Select>

        <Select
          value={sortBy}
          onValueChange={(value) => onSortByChange(value as PMProjectSort)}
        >
          <SelectTrigger>
            <SelectValue placeholder='Sort by' />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value='recentlyUpdated'>Recently updated</SelectItem>
            <SelectItem value='name'>Name</SelectItem>
            <SelectItem value='createdDate'>Created date</SelectItem>
          </SelectContent>
        </Select>

        <div className='flex items-center justify-end lg:justify-start'>
          <Button
            type='button'
            variant='ghost'
            className='w-full lg:w-auto'
            onClick={onClearFilters}
            disabled={!hasActiveFilters}
          >
            <X className='mr-2 h-4 w-4' />
            Clear filters
          </Button>
        </div>
      </div>
    </div>
  );
}
