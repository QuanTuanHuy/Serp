/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Module Badges Component
 */

'use client';

import React from 'react';
import { Badge } from '@/shared/components/ui';
import { getModuleIcon } from '@/shared/constants/moduleIcons';
import { cn } from '@/shared/utils';
import { Package } from 'lucide-react';

interface ModuleBadge {
  code: string;
  name: string;
  isIncluded?: boolean;
}

interface ModuleBadgesProps {
  modules: ModuleBadge[];
  maxDisplay?: number;
  showIcons?: boolean;
  size?: 'sm' | 'md' | 'lg';
  variant?: 'default' | 'outline' | 'secondary';
}

export function ModuleBadges({
  modules,
  maxDisplay = 4,
  showIcons = true,
  size = 'sm',
  variant = 'outline',
}: ModuleBadgesProps) {
  const displayedModules = modules.slice(0, maxDisplay);
  const remainingCount = modules.length - maxDisplay;

  const sizeClasses = {
    sm: 'text-xs px-2 py-0.5',
    md: 'text-sm px-2.5 py-1',
    lg: 'text-base px-3 py-1.5',
  };

  const iconSizes = {
    sm: 'w-3 h-3',
    md: 'w-3.5 h-3.5',
    lg: 'w-4 h-4',
  };

  return (
    <div className='flex flex-wrap gap-1.5'>
      {displayedModules.map((module) => {
        const iconConfig = getModuleIcon(module.code);
        const Icon = iconConfig?.icon || Package;

        return (
          <Badge
            key={module.code}
            variant={variant}
            className={cn('gap-1.5 font-medium', sizeClasses[size])}
          >
            {showIcons && (
              <Icon
                className={cn(
                  iconSizes[size],
                  iconConfig?.color || 'text-muted-foreground'
                )}
              />
            )}
            <span>{module.name}</span>
          </Badge>
        );
      })}
      {remainingCount > 0 && (
        <Badge
          variant='secondary'
          className={cn('font-medium', sizeClasses[size])}
        >
          +{remainingCount} more
        </Badge>
      )}
    </div>
  );
}
