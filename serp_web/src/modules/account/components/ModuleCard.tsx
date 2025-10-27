/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Module card component
 */

'use client';

import React from 'react';
import Link from 'next/link';
import { Card, CardContent } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { MODULE_ICONS, getModuleIcon } from '@/shared';
import { ArrowRight, Crown } from 'lucide-react';
import type { ModuleDisplayItem } from '../types';

interface ModuleCardProps {
  module: ModuleDisplayItem;
  variant?: 'grid' | 'list';
}

export const ModuleCard: React.FC<ModuleCardProps> = ({
  module,
  variant = 'grid',
}) => {
  const iconConfig = getModuleIcon(module.code);
  const Icon = iconConfig?.icon;

  if (variant === 'list') {
    return (
      <Link href={module.href} className='block'>
        <Card
          className={cn(
            'group hover:shadow-lg transition-all duration-300 hover:scale-[1.01] cursor-pointer border-2',
            !module.isActive && 'opacity-60 cursor-not-allowed',
            module.isAdmin && 'border-red-200 dark:border-red-800'
          )}
        >
          <CardContent className='p-3'>
            <div className='flex items-center gap-3'>
              {/* Icon */}
              <div
                className={cn(
                  'flex-shrink-0 w-12 h-12 rounded-lg flex items-center justify-center transition-transform group-hover:scale-110',
                  iconConfig?.bgColor || 'bg-gray-50 dark:bg-gray-950'
                )}
              >
                {Icon && (
                  <Icon
                    className={cn(
                      'w-6 h-6',
                      iconConfig?.color || 'text-gray-600'
                    )}
                  />
                )}
              </div>

              {/* Content */}
              <div className='flex-1 min-w-0'>
                <div className='flex items-center gap-2'>
                  <h3 className='font-semibold text-base truncate'>
                    {module.name}
                  </h3>
                  {module.isAdmin && (
                    <Crown className='w-3.5 h-3.5 text-red-600 flex-shrink-0' />
                  )}
                </div>
                <p className='text-xs text-muted-foreground truncate'>
                  {module.description}
                </p>
              </div>

              {/* Arrow */}
              <div className='flex-shrink-0'>
                <ArrowRight className='w-4 h-4 text-muted-foreground group-hover:translate-x-1 transition-transform' />
              </div>
            </div>
          </CardContent>
        </Card>
      </Link>
    );
  }

  // Grid variant
  return (
    <Link href={module.href} className='block h-full'>
      <Card
        className={cn(
          'group hover:shadow-xl transition-all duration-300 hover:scale-105 cursor-pointer h-full border-2',
          !module.isActive && 'opacity-60 cursor-not-allowed',
          module.isAdmin && 'border-red-200 dark:border-red-800'
        )}
      >
        <CardContent className='p-4 flex flex-col items-center text-center h-full'>
          {/* Icon */}
          <div
            className={cn(
              'w-14 h-14 rounded-xl flex items-center justify-center mb-3 transition-transform group-hover:scale-110',
              iconConfig?.bgColor || 'bg-gray-50 dark:bg-gray-950'
            )}
          >
            {Icon && (
              <Icon
                className={cn('w-7 h-7', iconConfig?.color || 'text-gray-600')}
              />
            )}
          </div>

          {/* Title */}
          <div className='flex items-center gap-1.5 mb-1.5'>
            <h3 className='font-semibold text-base'>{module.name}</h3>
            {module.isAdmin && (
              <Crown className='w-3.5 h-3.5 text-red-600 flex-shrink-0' />
            )}
          </div>

          {/* Description */}
          <p className='text-xs text-muted-foreground line-clamp-2 flex-1'>
            {module.description}
          </p>

          {/* Action indicator */}
          <div className='mt-3 opacity-0 group-hover:opacity-100 transition-opacity'>
            <ArrowRight className='w-4 h-4 text-primary' />
          </div>
        </CardContent>
      </Card>
    </Link>
  );
};
