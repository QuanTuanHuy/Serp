/**
 * PTM v2 - Smart Event Card Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Enhanced calendar event display with utility scores
 */

'use client';

import { Clock, Target, Flame, CheckCircle2, AlertCircle } from 'lucide-react';
import { Badge } from '@/shared/components/ui/badge';
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/shared/components/ui/tooltip';
import { cn } from '@/shared/utils';
import type { ScheduleEvent } from '../../types';

interface SmartEventCardProps {
  event: ScheduleEvent;
  onClick?: () => void;
  className?: string;
}

export function SmartEventCard({
  event,
  onClick,
  className,
}: SmartEventCardProps) {
  const formatTime = (minutes: number) => {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    const period = hours >= 12 ? 'PM' : 'AM';
    const displayHours = hours % 12 || 12;
    return `${displayHours}:${mins.toString().padStart(2, '0')} ${period}`;
  };

  const formatDuration = (minutes: number) => {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0 && mins > 0) return `${hours}h ${mins}m`;
    if (hours > 0) return `${hours}h`;
    return `${mins}m`;
  };

  const getPriorityIcon = (priority?: string) => {
    switch (priority) {
      case 'HIGH':
        return (
          <AlertCircle className='h-3 w-3 text-red-600 dark:text-red-400' />
        );
      case 'MEDIUM':
        return (
          <Target className='h-3 w-3 text-amber-600 dark:text-amber-400' />
        );
      case 'LOW':
        return (
          <CheckCircle2 className='h-3 w-3 text-blue-600 dark:text-blue-400' />
        );
      default:
        return null;
    }
  };

  const getUtilityColor = (utility: number) => {
    if (utility >= 80) return 'text-green-600 dark:text-green-400';
    if (utility >= 60) return 'text-amber-600 dark:text-amber-400';
    return 'text-muted-foreground';
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'completed':
        return (
          <CheckCircle2 className='h-3 w-3 text-green-600 dark:text-green-400' />
        );
      case 'scheduled':
        return <Clock className='h-3 w-3 text-blue-600 dark:text-blue-400' />;
      case 'cancelled':
        return (
          <AlertCircle className='h-3 w-3 text-red-600 dark:text-red-400' />
        );
      default:
        return null;
    }
  };

  return (
    <TooltipProvider delayDuration={200}>
      <Tooltip>
        <TooltipTrigger asChild>
          <div
            onClick={onClick}
            className={cn(
              'group relative cursor-pointer transition-all h-full',
              className
            )}
          >
            {/* Simplified Event Content */}
            <div className='flex flex-col gap-0.5 h-full p-1.5'>
              {/* Time */}
              <div className='flex items-center gap-1 text-[10px] opacity-80'>
                <Clock className='h-2.5 w-2.5' />
                <span>{formatTime(event.startMin)}</span>
              </div>

              {/* Title with icons */}
              <div className='flex items-start gap-1 flex-1'>
                {event.isDeepWork && (
                  <Flame className='h-3 w-3 flex-shrink-0 mt-0.5' />
                )}
                <p className='font-medium text-xs leading-tight line-clamp-2'>
                  {event.title || 'Untitled Event'}
                </p>
              </div>

              {/* Bottom indicators */}
              {(event.priority === 'HIGH' || event.totalParts > 1) && (
                <div className='flex items-center gap-1 text-[9px] opacity-70'>
                  {event.priority === 'HIGH' && (
                    <span className='font-medium'>High Priority</span>
                  )}
                  {event.totalParts > 1 && (
                    <span>
                      • {event.taskPart}/{event.totalParts}
                    </span>
                  )}
                </div>
              )}
            </div>
          </div>
        </TooltipTrigger>

        {/* Rich Tooltip */}
        <TooltipContent
          side='right'
          className='max-w-sm p-4 space-y-3'
          sideOffset={5}
        >
          {/* Title & Status */}
          <div className='space-y-1'>
            <div className='flex items-center gap-2'>
              <h4 className='font-semibold'>
                {event.title || 'Untitled Event'}
              </h4>
              <Badge variant='outline' className='text-xs capitalize'>
                {event.status}
              </Badge>
            </div>
            {event.priority && (
              <Badge
                variant='secondary'
                className={cn(
                  'text-xs',
                  event.priority === 'HIGH' && 'bg-red-500/10 text-red-600',
                  event.priority === 'MEDIUM' &&
                    'bg-amber-500/10 text-amber-600',
                  event.priority === 'LOW' && 'bg-blue-500/10 text-blue-600'
                )}
              >
                {event.priority} Priority
              </Badge>
            )}
          </div>

          {/* Time Details */}
          <div className='space-y-1 text-sm'>
            <div className='flex items-center gap-2 text-muted-foreground'>
              <Clock className='h-3.5 w-3.5' />
              <span>
                {formatTime(event.startMin)} - {formatTime(event.endMin)} (
                {formatDuration(event.durationMin)})
              </span>
            </div>
            {event.isDeepWork && (
              <div className='flex items-center gap-2 text-red-600 dark:text-red-400'>
                <Flame className='h-3.5 w-3.5' />
                <span className='text-xs font-medium'>Deep Work Session</span>
              </div>
            )}
            {event.totalParts > 1 && (
              <div className='flex items-center gap-2 text-muted-foreground'>
                <Target className='h-3.5 w-3.5' />
                <span className='text-xs'>
                  Part {event.taskPart} of {event.totalParts}
                </span>
              </div>
            )}
          </div>

          {/* Utility Breakdown */}
          {event.utilityBreakdown && (
            <div className='space-y-2 pt-2 border-t'>
              <div className='flex items-center justify-between'>
                <span className='text-xs font-semibold text-muted-foreground'>
                  AI Utility Score
                </span>
                <span
                  className={cn(
                    'text-sm font-bold',
                    getUtilityColor(event.utility)
                  )}
                >
                  {Math.round(event.utility)} pts
                </span>
              </div>

              <div className='space-y-1 text-xs'>
                <div className='flex justify-between'>
                  <span className='text-muted-foreground'>Priority Score</span>
                  <span className='font-medium'>
                    +{event.utilityBreakdown.priorityScore}
                  </span>
                </div>
                <div className='flex justify-between'>
                  <span className='text-muted-foreground'>Deadline Score</span>
                  <span className='font-medium'>
                    +{event.utilityBreakdown.deadlineScore}
                  </span>
                </div>
                {event.utilityBreakdown.focusTimeBonus > 0 && (
                  <div className='flex justify-between text-green-600 dark:text-green-400'>
                    <span>Focus Time Bonus</span>
                    <span className='font-medium'>
                      +{event.utilityBreakdown.focusTimeBonus}
                    </span>
                  </div>
                )}
                {event.utilityBreakdown.contextSwitchPenalty < 0 && (
                  <div className='flex justify-between text-red-600 dark:text-red-400'>
                    <span>Context Switch</span>
                    <span className='font-medium'>
                      {event.utilityBreakdown.contextSwitchPenalty}
                    </span>
                  </div>
                )}
              </div>

              {event.utilityBreakdown.reason && (
                <p className='text-xs text-muted-foreground italic pt-1 border-t'>
                  {event.utilityBreakdown.reason}
                </p>
              )}
            </div>
          )}

          {/* Manual Override Indicator */}
          {event.isManualOverride && (
            <div className='pt-2 border-t'>
              <Badge variant='outline' className='text-xs'>
                ✋ Manual Override
              </Badge>
            </div>
          )}
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );
}
