/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Module Grid Component
 */

'use client';

import { ModuleCard } from './ModuleCard';
import type { Module, ViewMode } from '../types';

interface ModuleGridProps {
  modules: Module[];
  viewMode?: ViewMode;
  onModuleClick?: (module: Module) => void;
  isLoading?: boolean;
}

export function ModuleGrid({
  modules,
  viewMode = 'grid',
  onModuleClick,
  isLoading = false,
}: ModuleGridProps) {
  if (isLoading) {
    return (
      <div
        className={
          viewMode === 'grid'
            ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6'
            : 'space-y-4'
        }
      >
        {[...Array(8)].map((_, i) => (
          <div
            key={i}
            className={`animate-pulse bg-muted rounded-lg ${
              viewMode === 'grid' ? 'h-80' : 'h-32'
            }`}
          />
        ))}
      </div>
    );
  }

  if (modules.length === 0) {
    return (
      <div className='text-center py-16'>
        <div className='w-16 h-16 bg-muted rounded-full flex items-center justify-center mx-auto mb-4'>
          <span className='text-3xl'>🔍</span>
        </div>
        <h3 className='text-lg font-semibold mb-2'>No modules found</h3>
        <p className='text-muted-foreground text-sm'>
          Try adjusting your search or filters to find what you&apos;re looking
          for
        </p>
      </div>
    );
  }

  if (viewMode === 'list') {
    return (
      <div className='space-y-4'>
        {modules.map((module) => (
          <ModuleCard
            key={module.id}
            module={module}
            viewMode='list'
            onDetailsClick={onModuleClick}
          />
        ))}
      </div>
    );
  }

  return (
    <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6'>
      {modules.map((module) => (
        <ModuleCard
          key={module.id}
          module={module}
          viewMode='grid'
          onDetailsClick={onModuleClick}
        />
      ))}
    </div>
  );
}
