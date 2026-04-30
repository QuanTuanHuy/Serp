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
  Users,
  MapPin,
  MoreHorizontal,
  ExternalLink,
  Edit,
  Trash2,
  UserCircle,
} from 'lucide-react';
import type { Team, TeamStatus } from '../../types';

export interface TeamCardProps {
  team: Team;
  onClick?: () => void;
  onEdit?: () => void;
  onDelete?: () => void;
  className?: string;
  variant?: 'default' | 'compact';
}

const statusStyles: Record<
  TeamStatus,
  { bg: string; text: string; dot: string }
> = {
  ACTIVE: {
    bg: 'bg-emerald-100 dark:bg-emerald-900/30',
    text: 'text-emerald-700 dark:text-emerald-400',
    dot: 'bg-emerald-500',
  },
  INACTIVE: {
    bg: 'bg-slate-100 dark:bg-slate-800/50',
    text: 'text-slate-600 dark:text-slate-400',
    dot: 'bg-slate-400',
  },
};

const getInitials = (name: string): string => {
  return name
    .split(' ')
    .map((n) => n.charAt(0))
    .join('')
    .toUpperCase()
    .slice(0, 2);
};

export const TeamCard: React.FC<TeamCardProps> = ({
  team,
  onClick,
  onEdit,
  onDelete,
  className,
  variant = 'default',
}) => {
  const status = statusStyles[team.status] || statusStyles.ACTIVE;
  const managerName = team.manager?.name;

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
        <div className='flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-blue-500/20 to-blue-500/5 text-blue-600 font-semibold text-sm'>
          {getInitials(team.name)}
        </div>
        <div className='flex-1 min-w-0'>
          <p className='font-medium text-sm truncate'>{team.name}</p>
          <p className='text-xs text-muted-foreground truncate'>
            {team.memberCount ?? 0} members
          </p>
        </div>
        <div className={cn('h-2 w-2 rounded-full', status.dot)} />
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
      <div className='relative h-2 bg-gradient-to-r from-blue-500/60 via-blue-500/40 to-blue-500/20' />

      <CardContent className='p-5'>
        <div className='flex items-start justify-between mb-4'>
          <div className='flex items-center gap-3'>
            <div className='relative'>
              <div className='flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-br from-blue-500/20 to-blue-500/5 text-blue-600 font-semibold text-lg shadow-sm'>
                {getInitials(team.name)}
              </div>
              <div
                className={cn(
                  'absolute -bottom-0.5 -right-0.5 h-3.5 w-3.5 rounded-full border-2 border-card',
                  status.dot
                )}
              />
            </div>
            <div className='min-w-0'>
              <h3 className='font-semibold text-foreground truncate'>
                {team.name}
              </h3>
              {managerName && (
                <p className='text-xs text-muted-foreground truncate'>
                  <UserCircle className='h-3 w-3 inline mr-1' />
                  {managerName}
                </p>
              )}
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
              {onDelete && (
                <DropdownMenuItem
                  onClick={(e) => {
                    e.stopPropagation();
                    onDelete();
                  }}
                  className='text-destructive focus:text-destructive'
                >
                  <Trash2 className='h-4 w-4 mr-2' />
                  Delete
                </DropdownMenuItem>
              )}
            </DropdownMenuContent>
          </DropdownMenu>
        </div>

        <div className='flex items-center gap-2 mb-4'>
          <Badge
            variant='secondary'
            className={cn('gap-1', status.bg, status.text)}
          >
            <span className={cn('h-1.5 w-1.5 rounded-full', status.dot)} />
            {team.status.charAt(0) + team.status.slice(1).toLowerCase()}
          </Badge>
        </div>

        {team.description && (
          <p className='text-sm text-muted-foreground mb-4 line-clamp-2'>
            {team.description}
          </p>
        )}

        <div className='flex items-center justify-between pt-4 border-t'>
          <div className='flex items-center gap-4'>
            <div className='flex items-center gap-1.5 text-sm'>
              <Users className='h-4 w-4 text-muted-foreground' />
              <span className='font-medium'>{team.memberCount ?? 0}</span>
              <span className='text-muted-foreground'>members</span>
            </div>
            <div className='flex items-center gap-1.5 text-sm'>
              <MapPin className='h-4 w-4 text-muted-foreground' />
              <span className='font-medium'>-</span>
              <span className='text-muted-foreground'>territories</span>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default TeamCard;
