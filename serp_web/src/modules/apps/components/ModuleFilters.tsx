/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Module Filters Component
 */

'use client';

import { Grid, List, X } from 'lucide-react';
import {
  Button,
  Badge,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import type { ViewMode, PricingModel, ModuleStatus } from '../types';

interface ModuleFiltersProps {
  viewMode: ViewMode;
  onViewModeChange: (mode: ViewMode) => void;
  categories: string[];
  selectedCategory?: string;
  onCategoryChange: (category?: string) => void;
  selectedPricingModel?: PricingModel;
  onPricingModelChange: (model?: PricingModel) => void;
  selectedStatus?: ModuleStatus;
  onStatusChange: (status?: ModuleStatus) => void;
  hasActiveFilters: boolean;
  onClearFilters: () => void;
  resultCount: number;
}

export function ModuleFilters({
  viewMode,
  onViewModeChange,
  categories,
  selectedCategory,
  onCategoryChange,
  selectedPricingModel,
  onPricingModelChange,
  selectedStatus,
  onStatusChange,
  hasActiveFilters,
  onClearFilters,
  resultCount,
}: ModuleFiltersProps) {
  return (
    <div className='flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 py-4'>
      {/* Left side - Filters */}
      <div className='flex flex-wrap items-center gap-3'>
        {/* Category Filter */}
        {categories.length > 0 && (
          <Select
            value={selectedCategory || 'all'}
            onValueChange={(value) =>
              onCategoryChange(value === 'all' ? undefined : value)
            }
          >
            <SelectTrigger className='w-[160px]'>
              <SelectValue placeholder='All Categories' />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='all'>All Categories</SelectItem>
              {categories.map((category) => (
                <SelectItem key={category} value={category}>
                  {category}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        )}

        {/* Pricing Model Filter */}
        <Select
          value={selectedPricingModel || 'all'}
          onValueChange={(value) =>
            onPricingModelChange(
              value === 'all' ? undefined : (value as PricingModel)
            )
          }
        >
          <SelectTrigger className='w-[160px]'>
            <SelectValue placeholder='All Pricing' />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value='all'>All Pricing</SelectItem>
            <SelectItem value='FREE'>Free</SelectItem>
            <SelectItem value='FIXED'>Fixed Price</SelectItem>
            <SelectItem value='PER_USER'>Per User</SelectItem>
            <SelectItem value='TIERED'>Tiered</SelectItem>
          </SelectContent>
        </Select>

        {/* Status Filter */}
        <Select
          value={selectedStatus || 'all'}
          onValueChange={(value) =>
            onStatusChange(
              value === 'all' ? undefined : (value as ModuleStatus)
            )
          }
        >
          <SelectTrigger className='w-[160px]'>
            <SelectValue placeholder='All Status' />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value='all'>All Status</SelectItem>
            <SelectItem value='ACTIVE'>Active</SelectItem>
            <SelectItem value='BETA'>Beta</SelectItem>
            <SelectItem value='DISABLED'>Coming Soon</SelectItem>
          </SelectContent>
        </Select>

        {/* Clear Filters */}
        {hasActiveFilters && (
          <Button
            variant='ghost'
            size='sm'
            onClick={onClearFilters}
            className='gap-1'
          >
            <X className='w-4 h-4' />
            Clear
          </Button>
        )}

        {/* Result count */}
        <Badge variant='secondary' className='ml-2'>
          {resultCount} {resultCount === 1 ? 'app' : 'apps'}
        </Badge>
      </div>

      {/* Right side - View toggle */}
      <div className='flex items-center gap-1 border rounded-lg p-1'>
        <Button
          variant={viewMode === 'grid' ? 'secondary' : 'ghost'}
          size='sm'
          onClick={() => onViewModeChange('grid')}
          className='gap-2'
        >
          <Grid className='w-4 h-4' />
          <span className='hidden sm:inline'>Grid</span>
        </Button>
        <Button
          variant={viewMode === 'list' ? 'secondary' : 'ghost'}
          size='sm'
          onClick={() => onViewModeChange('list')}
          className='gap-2'
        >
          <List className='w-4 h-4' />
          <span className='hidden sm:inline'>List</span>
        </Button>
      </div>
    </div>
  );
}
