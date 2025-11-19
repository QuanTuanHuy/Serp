/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Activity Item Component
 */

'use client';

import { useRouter } from 'next/navigation';
import { formatDistanceToNow } from 'date-fns';
import {
  CheckCircle2,
  Circle,
  Clock,
  Edit,
  Trash2,
  Plus,
  FolderOpen,
  Sparkles,
  ExternalLink,
  AlertTriangle,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { Badge } from '@/shared/components/ui/badge';
import type { ActivityEvent } from '../../types';
import { AlgorithmUtilityBreakdown } from './AlgorithmUtilityBreakdown';

interface ActivityItemProps {
  activity: ActivityEvent;
  isLatest?: boolean;
  highlightId?: string;
}

const ACTIVITY_ICON_MAP: Record<string, React.ReactNode> = {
  task_created: <Plus className='h-4 w-4' />,
  task_updated: <Edit className='h-4 w-4' />,
  task_completed: <CheckCircle2 className='h-4 w-4' />,
  task_deleted: <Trash2 className='h-4 w-4' />,
  project_created: <FolderOpen className='h-4 w-4' />,
  project_updated: <Edit className='h-4 w-4' />,
  schedule_optimized: <Sparkles className='h-4 w-4' />,
  schedule_conflict_detected: <AlertTriangle className='h-4 w-4' />,
  algorithm_executed: <Sparkles className='h-4 w-4' />,
  deadline_risk_detected: <AlertTriangle className='h-4 w-4' />,
};

const ACTIVITY_COLOR_MAP: Record<string, string> = {
  task_created: 'text-green-600 bg-green-50 dark:bg-green-950/20',
  task_updated: 'text-blue-600 bg-blue-50 dark:bg-blue-950/20',
  task_completed: 'text-purple-600 bg-purple-50 dark:bg-purple-950/20',
  task_deleted: 'text-red-600 bg-red-50 dark:bg-red-950/20',
  project_created: 'text-green-600 bg-green-50 dark:bg-green-950/20',
  schedule_optimized: 'text-purple-600 bg-purple-50 dark:bg-purple-950/20',
  algorithm_executed: 'text-purple-600 bg-purple-50 dark:bg-purple-950/20',
  deadline_risk_detected: 'text-orange-600 bg-orange-50 dark:bg-orange-950/20',
};

export function ActivityItem({
  activity,
  isLatest,
  highlightId,
}: ActivityItemProps) {
  const router = useRouter();

  const isClickable = Boolean(activity.navigationUrl);
  const isHighlighted = highlightId === activity.id;
  const isAlgorithmEvent = activity.algorithmType !== undefined;

  const handleClick = () => {
    if (!isClickable) return;

    const url = activity.navigationUrl!;
    const params = new URLSearchParams();

    if (activity.navigationParams) {
      Object.entries(activity.navigationParams).forEach(([key, value]) => {
        params.set(key, String(value));
      });
    }

    const fullUrl = params.toString() ? `${url}?${params.toString()}` : url;
    router.push(fullUrl);
  };

  const icon = ACTIVITY_ICON_MAP[activity.eventType] || (
    <Circle className='h-4 w-4' />
  );
  const colorClass =
    ACTIVITY_COLOR_MAP[activity.eventType] || 'text-gray-600 bg-gray-50';

  return (
    <div
      id={`activity-${activity.id}`}
      className={cn(
        'p-4 transition-all duration-200',
        isClickable && 'cursor-pointer hover:bg-muted/50',
        isLatest && 'bg-blue-50/50 dark:bg-blue-950/10',
        isHighlighted && 'animate-pulse bg-amber-50 dark:bg-amber-950/20',
        isAlgorithmEvent && 'border-l-4 border-purple-500'
      )}
      onClick={handleClick}
    >
      <div className='flex gap-3'>
        {/* Icon */}
        <div
          className={cn(
            'flex items-center justify-center w-8 h-8 rounded-full flex-shrink-0',
            colorClass
          )}
        >
          {icon}
        </div>

        {/* Content */}
        <div className='flex-1 min-w-0'>
          {/* Title */}
          <p className='font-medium text-sm flex items-center gap-2'>
            {activity.title}
            {isClickable && (
              <ExternalLink className='h-3 w-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition-opacity' />
            )}
          </p>

          {/* Description */}
          {activity.description && (
            <p className='text-sm text-muted-foreground mt-0.5'>
              {activity.description}
            </p>
          )}

          {/* Algorithm Transparency */}
          {isAlgorithmEvent && activity.utilityBreakdown && (
            <AlgorithmUtilityBreakdown
              breakdown={activity.utilityBreakdown}
              algorithmType={activity.algorithmType}
              executionTimeMs={activity.executionTimeMs}
              tasksAffected={activity.tasksAffected}
            />
          )}

          {/* Metadata */}
          <div className='flex items-center gap-3 mt-2 text-xs text-muted-foreground flex-wrap'>
            <span className='flex items-center gap-1'>
              <Clock className='h-3 w-3' />
              {formatDistanceToNow(new Date(activity.createdAt), {
                addSuffix: true,
              })}
            </span>

            {activity.metadata?.priority && (
              <Badge
                variant='outline'
                className={cn(
                  'text-xs',
                  activity.metadata.priority === 'HIGH' &&
                    'border-red-500 text-red-700',
                  activity.metadata.priority === 'MEDIUM' &&
                    'border-amber-500 text-amber-700'
                )}
              >
                {activity.metadata.priority}
              </Badge>
            )}

            {activity.metadata?.duration && (
              <span className='flex items-center gap-1'>
                <Clock className='h-3 w-3' />
                {activity.metadata.duration}m
              </span>
            )}
          </div>
        </div>

        {/* Latest Badge */}
        {isLatest && (
          <Badge variant='default' className='text-xs'>
            New
          </Badge>
        )}
      </div>
    </div>
  );
}
