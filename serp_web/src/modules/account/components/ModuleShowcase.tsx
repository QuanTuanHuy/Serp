/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Module showcase with grid/list toggle
 */

'use client';

import React, { useState } from 'react';
import { Grid3x3, List, Loader2 } from 'lucide-react';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { ModuleCard } from './ModuleCard';
import type { ModuleDisplayItem } from '../types';

interface ModuleShowcaseProps {
  modules: ModuleDisplayItem[];
  isLoading?: boolean;
  className?: string;
}

export const ModuleShowcase: React.FC<ModuleShowcaseProps> = ({
  modules,
  isLoading = false,
  className,
}) => {
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');

  if (isLoading) {
    return (
      <div className='flex items-center justify-center py-20'>
        <Loader2 className='w-8 h-8 animate-spin text-primary' />
      </div>
    );
  }

  if (!modules || modules.length === 0) {
    return (
      <div className='text-center py-20'>
        <p className='text-muted-foreground text-lg'>
          No modules available. Please subscribe to a plan.
        </p>
      </div>
    );
  }

  return (
    <div className={cn('space-y-6', className)}>
      {/* Header with View Toggle */}
      <div className='flex items-center justify-between'>
        <div>
          <h2 className='text-2xl font-bold'>Your Modules</h2>
          <p className='text-muted-foreground'>
            {modules.length} module{modules.length !== 1 ? 's' : ''} available
          </p>
        </div>

        {/* View Mode Toggle */}
        <div className='flex items-center gap-2 border rounded-lg p-1'>
          <Button
            variant={viewMode === 'grid' ? 'default' : 'ghost'}
            size='sm'
            onClick={() => setViewMode('grid')}
            className='gap-2'
          >
            <Grid3x3 className='w-4 h-4' />
            Grid
          </Button>
          <Button
            variant={viewMode === 'list' ? 'default' : 'ghost'}
            size='sm'
            onClick={() => setViewMode('list')}
            className='gap-2'
          >
            <List className='w-4 h-4' />
            List
          </Button>
        </div>
      </div>

      {/* Module Display */}
      {viewMode === 'grid' ? (
        <div className='grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-6'>
          {modules.map((module) => (
            <ModuleCard key={module.code} module={module} variant='grid' />
          ))}
        </div>
      ) : (
        <div className='space-y-4'>
          {modules.map((module) => (
            <ModuleCard key={module.code} module={module} variant='list' />
          ))}
        </div>
      )}
    </div>
  );
};
