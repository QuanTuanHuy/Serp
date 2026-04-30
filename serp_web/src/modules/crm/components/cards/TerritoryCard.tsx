'use client';

import React from 'react';
import { cn } from '@/shared/utils';
import {
  Card,
  CardContent,
  Button,
  Badge,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/ui';
import {
  MapPin,
  MoreHorizontal,
  ExternalLink,
  Edit,
  Building,
  Users,
} from 'lucide-react';
import type { Territory } from '../../types';

export interface TerritoryCardProps {
  territory: Territory;
  onClick?: () => void;
  onEdit?: () => void;
  onActivate?: () => void;
  onDeactivate?: () => void;
  className?: string;
  variant?: 'default' | 'compact';
}

const getLevelBadge = (level: string) => {
  switch (level) {
    case 'PROVINCE_CITY':
      return {
        label: 'Province/City',
        color:
          'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400',
      };
    default:
      return {
        label: level,
        color:
          'bg-gray-100 text-gray-700 dark:bg-gray-800/50 dark:text-gray-400',
      };
  }
};

export const TerritoryCard: React.FC<TerritoryCardProps> = ({
  territory,
  onClick,
  onEdit,
  onActivate,
  onDeactivate,
  className,
  variant = 'default',
}) => {
  const levelBadge = getLevelBadge(territory.territoryLevel);

  if (variant === 'compact') {
    return (
      <Card
        className={cn(
          'group flex flex-row items-center gap-3 p-3',
          'hover:shadow-md hover:border-primary/20 transition-all duration-200 cursor-pointer',
          className
        )}
        onClick={onClick}
      >
        <div className='flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-amber-500/20 to-amber-500/5 text-amber-600'>
          <MapPin className='h-5 w-5' />
        </div>
        <div className='flex-1 min-w-0'>
          <p className='font-medium text-sm truncate'>
            {territory.territoryName}
          </p>
          <p className='text-xs text-muted-foreground truncate'>
            {territory.territoryCode}
          </p>
        </div>
        <div
          className={cn(
            'h-2 w-2 rounded-full',
            territory.active ? 'bg-emerald-500' : 'bg-slate-400'
          )}
        />
      </Card>
    );
  }

  return (
    <Card
      className={cn(
        'group relative overflow-hidden',
        'hover:shadow-lg hover:border-primary/20 transition-all duration-200 cursor-pointer',
        className
      )}
      onClick={onClick}
    >
      <div className='relative h-2 bg-gradient-to-r from-amber-500/60 via-amber-500/40 to-amber-500/20' />

      <CardContent className='p-5'>
        <div className='flex items-start justify-between mb-4'>
          <div className='flex items-center gap-3'>
            <div className='relative'>
              <div className='flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-br from-amber-500/20 to-amber-500/5 text-amber-600 shadow-sm'>
                <MapPin className='h-6 w-6' />
              </div>
              <div
                className={cn(
                  'absolute -bottom-0.5 -right-0.5 h-3.5 w-3.5 rounded-full border-2 border-card',
                  territory.active ? 'bg-emerald-500' : 'bg-slate-400'
                )}
              />
            </div>
            <div className='min-w-0'>
              <h3 className='font-semibold text-foreground truncate'>
                {territory.territoryName}
              </h3>
              <p className='text-xs text-muted-foreground'>
                {territory.territoryCode}
              </p>
            </div>
          </div>

          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button
                variant='ghost'
                size='icon'
                className='h-8 w-8 opacity-0 group-hover:opacity-100 transition-opacity'
                onClick={(e) => e.stopPropagation()}
              >
                <MoreHorizontal className='h-4 w-4 text-muted-foreground' />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align='end'>
              {onClick && (
                <DropdownMenuItem
                  onClick={(e) => {
                    e.stopPropagation();
                    onClick();
                  }}
                >
                  <ExternalLink className='h-4 w-4 mr-2' />
                  View Details
                </DropdownMenuItem>
              )}
              {onEdit && (
                <DropdownMenuItem
                  onClick={(e) => {
                    e.stopPropagation();
                    onEdit();
                  }}
                >
                  <Edit className='h-4 w-4 mr-2' />
                  Edit
                </DropdownMenuItem>
              )}
              {territory.active && onDeactivate && (
                <DropdownMenuItem
                  onClick={(e) => {
                    e.stopPropagation();
                    onDeactivate();
                  }}
                >
                  Deactivate
                </DropdownMenuItem>
              )}
              {!territory.active && onActivate && (
                <DropdownMenuItem
                  onClick={(e) => {
                    e.stopPropagation();
                    onActivate();
                  }}
                >
                  Activate
                </DropdownMenuItem>
              )}
            </DropdownMenuContent>
          </DropdownMenu>
        </div>

        <div className='flex items-center gap-2 mb-4'>
          <Badge variant='secondary' className={cn('gap-1', levelBadge.color)}>
            {levelBadge.label}
          </Badge>
          <Badge
            variant='outline'
            className={cn(
              territory.active
                ? 'border-emerald-500 text-emerald-600'
                : 'border-slate-400 text-slate-500'
            )}
          >
            {territory.active ? 'Active' : 'Inactive'}
          </Badge>
        </div>

        <div className='space-y-2 mb-4'>
          <div className='flex items-center gap-2 text-sm'>
            <Building className='h-4 w-4 text-muted-foreground shrink-0' />
            <span className='text-muted-foreground'>Country:</span>
            <span className='font-medium'>{territory.countryCode}</span>
          </div>
          {territory.assignedTeamName && (
            <div className='flex items-center gap-2 text-sm'>
              <Users className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='text-muted-foreground'>Team:</span>
              <span className='font-medium'>{territory.assignedTeamName}</span>
            </div>
          )}
        </div>

        {!territory.assignedTeamName && (
          <div className='pt-4 border-t'>
            <p className='text-sm text-muted-foreground italic'>Unassigned</p>
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default TerritoryCard;
