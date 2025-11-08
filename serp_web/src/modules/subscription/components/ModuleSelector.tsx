/*
Author: QuanTuanHuy
Description: Part of Serp Project - Module Selection Component
*/

'use client';

import React, { useState, useMemo } from 'react';
import { Module } from '../types';
import { Card, Checkbox, Badge, Input, Button } from '@/shared/components/ui';
import { Search, Package, X } from 'lucide-react';
import { cn } from '@/shared/utils';
import { getModuleIcon, MODULE_ICONS } from '@/shared/constants/moduleIcons';

interface ModuleSelectorProps {
  modules: Module[];
  selectedModules: number[];
  onModuleToggle: (moduleId: number) => void;
  isLoading?: boolean;
}

const PRICING_COLORS: Record<string, string> = {
  FREE: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
  FIXED: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
  PER_USER:
    'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200',
  TIERED:
    'bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200',
};

const PRICING_LABELS: Record<string, string> = {
  FREE: 'Free',
  FIXED: 'Fixed Price',
  PER_USER: 'Per User',
  TIERED: 'Tiered Pricing',
};

export const ModuleSelector: React.FC<ModuleSelectorProps> = ({
  modules,
  selectedModules,
  onModuleToggle,
  isLoading,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);

  const categories = useMemo(() => {
    const cats = new Set<string>();
    modules.forEach((m) => m.category && cats.add(m.category));
    return Array.from(cats).sort();
  }, [modules]);

  const filteredModules = useMemo(() => {
    return modules.filter((module) => {
      const matchesSearch =
        searchQuery === '' ||
        module.moduleName.toLowerCase().includes(searchQuery.toLowerCase()) ||
        module.description?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        module.code?.toLowerCase().includes(searchQuery.toLowerCase());

      const matchesCategory =
        selectedCategory === null || module.category === selectedCategory;

      return matchesSearch && matchesCategory;
    });
  }, [modules, searchQuery, selectedCategory]);

  const selectedCount = selectedModules.length;
  const freeModulesCount = modules.filter(
    (m) => m.isFree && selectedModules.includes(m.id)
  ).length;

  return (
    <div className='space-y-4'>
      {/* Header Stats */}
      <div className='flex items-center justify-between'>
        <div className='flex items-center gap-4'>
          <div className='flex items-center gap-2'>
            <Package className='h-4 w-4 text-muted-foreground' />
            <span className='text-sm font-medium'>
              {selectedCount} module{selectedCount !== 1 ? 's' : ''} selected
            </span>
          </div>
          {freeModulesCount > 0 && (
            <Badge variant='secondary' className='text-xs'>
              {freeModulesCount} free
            </Badge>
          )}
        </div>
      </div>

      {/* Search and Filter */}
      <div className='flex flex-col sm:flex-row gap-3'>
        <div className='relative flex-1'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
          <Input
            placeholder='Search modules by name, code, or description...'
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className='pl-10'
          />
        </div>
      </div>

      {/* Category Filters */}
      {categories.length > 0 && (
        <div className='flex flex-wrap gap-2'>
          <Button
            variant={selectedCategory === null ? 'default' : 'outline'}
            size='sm'
            onClick={() => setSelectedCategory(null)}
          >
            All Categories
          </Button>
          {categories.map((category) => {
            const iconConfig = getModuleIcon(category);
            const Icon = iconConfig?.icon || Package;
            return (
              <Button
                key={category}
                variant={selectedCategory === category ? 'default' : 'outline'}
                size='sm'
                onClick={() => setSelectedCategory(category)}
                className='gap-1.5'
              >
                <Icon className='h-3.5 w-3.5' />
                {category}
              </Button>
            );
          })}
        </div>
      )}

      {/* Modules Grid */}
      <div className='space-y-2'>
        {isLoading ? (
          <div className='flex justify-center items-center py-12'>
            <div className='animate-spin rounded-full h-8 w-8 border-b-2 border-primary' />
          </div>
        ) : filteredModules.length === 0 ? (
          <div className='text-center py-12 text-muted-foreground'>
            <Package className='h-12 w-12 mx-auto mb-3 opacity-30' />
            <p className='text-sm'>No modules found</p>
            {searchQuery && (
              <Button
                variant='link'
                size='sm'
                onClick={() => setSearchQuery('')}
                className='mt-2'
              >
                Clear search
              </Button>
            )}
          </div>
        ) : (
          filteredModules.map((module) => {
            const isSelected = selectedModules.includes(module.id);
            const isFree = module.isFree;
            const iconConfig = getModuleIcon(module.code);
            const ModuleIcon = iconConfig?.icon || Package;

            return (
              <Card
                key={module.id}
                className={cn(
                  'p-4 cursor-pointer transition-all hover:shadow-md',
                  isSelected && 'ring-2 ring-primary bg-primary/5'
                )}
                onClick={() => onModuleToggle(module.id)}
              >
                <div className='flex items-start gap-3'>
                  <Checkbox
                    id={`module-${module.id}`}
                    checked={isSelected}
                    onCheckedChange={() => onModuleToggle(module.id)}
                    onClick={(e) => e.stopPropagation()}
                  />

                  {/* Module Icon */}
                  <div
                    className={cn(
                      'flex items-center justify-center w-10 h-10 rounded-lg flex-shrink-0',
                      iconConfig?.bgColor || 'bg-gray-50 dark:bg-gray-950'
                    )}
                  >
                    <ModuleIcon
                      className={cn(
                        'h-5 w-5',
                        iconConfig?.color || 'text-gray-600'
                      )}
                    />
                  </div>

                  <div className='flex-1 min-w-0'>
                    <div className='flex items-start justify-between gap-2 mb-2'>
                      <label
                        htmlFor={`module-${module.id}`}
                        className='text-sm font-semibold leading-none cursor-pointer'
                      >
                        {module.moduleName}
                      </label>
                      <div className='flex items-center gap-1 flex-shrink-0'>
                        {isFree && (
                          <Badge variant='secondary' className='text-xs'>
                            Free
                          </Badge>
                        )}
                        <Badge
                          variant='outline'
                          className={cn(
                            'text-xs',
                            PRICING_COLORS[module.pricingModel]
                          )}
                        >
                          {PRICING_LABELS[module.pricingModel]}
                        </Badge>
                      </div>
                    </div>

                    {module.description && (
                      <p className='text-xs text-muted-foreground mb-2 line-clamp-2'>
                        {module.description}
                      </p>
                    )}

                    <div className='flex items-center gap-3 text-xs text-muted-foreground'>
                      {module.category && (
                        <div className='flex items-center gap-1.5'>
                          <span>{module.category}</span>
                        </div>
                      )}
                      <div className='flex items-center gap-1'>
                        <span className='font-mono font-medium'>
                          {module.code}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </Card>
            );
          })
        )}
      </div>

      {/* Selection Summary */}
      {selectedCount > 0 && (
        <Card className='p-3 bg-muted/50'>
          <div className='flex items-center justify-between'>
            <div className='flex items-center gap-2 text-sm'>
              <Package className='h-4 w-4' />
              <span className='font-medium'>
                {selectedCount} module{selectedCount !== 1 ? 's' : ''} selected
              </span>
              {freeModulesCount > 0 && (
                <span className='text-muted-foreground'>
                  ({freeModulesCount} free)
                </span>
              )}
            </div>
            <Button
              variant='ghost'
              size='sm'
              onClick={() => selectedModules.forEach(onModuleToggle)}
            >
              <X className='h-3 w-3 mr-1' />
              Clear all
            </Button>
          </div>
        </Card>
      )}
    </div>
  );
};
