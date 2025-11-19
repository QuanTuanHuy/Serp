/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

'use client';

import { formatDistanceToNow, format } from 'date-fns';
import { Clock, User, Circle } from 'lucide-react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Badge } from '@/shared/components/ui/badge';
import { cn } from '@/shared/utils';
import type { ActivityEvent } from '../../types';

interface Props {
  activities: ActivityEvent[];
  highlightId?: string;
}

const EVENT_COLORS: Record<string, string> = {
  task_created: 'bg-green-500',
  task_updated: 'bg-blue-500',
  task_completed: 'bg-purple-500',
  task_deleted: 'bg-red-500',
  schedule_optimized: 'bg-purple-500',
  algorithm_executed: 'bg-purple-500',
  deadline_risk_detected: 'bg-orange-500',
};

export function TaskActivityTimeline({ activities, highlightId }: Props) {
  if (activities.length === 0) {
    return (
      <Card>
        <CardContent className='p-12 text-center text-muted-foreground'>
          No activity history yet
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className='text-base'>Activity Timeline</CardTitle>
      </CardHeader>
      <CardContent>
        <div className='relative space-y-4'>
          {/* Timeline line */}
          <div className='absolute left-4 top-0 bottom-0 w-0.5 bg-border' />

          {/* Activity items */}
          {activities.map((activity, index) => {
            const isHighlighted = highlightId === activity.id;
            const dotColor = EVENT_COLORS[activity.eventType] || 'bg-gray-500';

            return (
              <div
                key={activity.id}
                id={`activity-${activity.id}`}
                className={cn(
                  'relative pl-10 pb-4 transition-all duration-200',
                  isHighlighted && 'animate-pulse'
                )}
              >
                {/* Timeline dot */}
                <div
                  className={cn(
                    'absolute left-3 top-1.5 h-3 w-3 rounded-full border-2 border-background',
                    dotColor,
                    isHighlighted && 'ring-4 ring-amber-200 dark:ring-amber-800'
                  )}
                />

                {/* Content */}
                <div
                  className={cn(
                    'bg-muted/50 rounded-lg p-4',
                    isHighlighted && 'bg-amber-50 dark:bg-amber-950/20'
                  )}
                >
                  {/* Header */}
                  <div className='flex items-start justify-between gap-4 mb-2'>
                    <div className='flex-1 min-w-0'>
                      <h4 className='font-medium text-sm'>{activity.title}</h4>
                      {activity.description && (
                        <p className='text-sm text-muted-foreground mt-1'>
                          {activity.description}
                        </p>
                      )}
                    </div>

                    <Badge variant='outline' className='text-xs shrink-0'>
                      {activity.eventType.replace(/_/g, ' ')}
                    </Badge>
                  </div>

                  {/* Metadata */}
                  {activity.metadata && (
                    <div className='flex flex-wrap gap-2 mt-3'>
                      {Object.entries(activity.metadata).map(([key, value]) => (
                        <div
                          key={key}
                          className='text-xs bg-background px-2 py-1 rounded'
                        >
                          <span className='text-muted-foreground'>{key}:</span>{' '}
                          <span className='font-medium'>{String(value)}</span>
                        </div>
                      ))}
                    </div>
                  )}

                  {/* Footer */}
                  <div className='flex items-center gap-4 mt-3 text-xs text-muted-foreground'>
                    <span className='flex items-center gap-1'>
                      <Clock className='h-3 w-3' />
                      {formatDistanceToNow(new Date(activity.createdAt), {
                        addSuffix: true,
                      })}
                    </span>
                    <span className='flex items-center gap-1'>
                      <User className='h-3 w-3' />
                      User {activity.userId}
                    </span>
                    {activity.executionTimeMs && (
                      <span>Execution: {activity.executionTimeMs}ms</span>
                    )}
                  </div>

                  {/* Algorithm transparency */}
                  {activity.utilityBreakdown && (
                    <div className='mt-3 p-3 bg-purple-50 dark:bg-purple-950/20 rounded border border-purple-200 dark:border-purple-800'>
                      <p className='text-xs font-semibold text-purple-700 dark:text-purple-300 mb-2'>
                        Utility Breakdown
                      </p>
                      <div className='grid grid-cols-2 gap-2 text-xs'>
                        <div>
                          <span className='text-muted-foreground'>
                            Priority:
                          </span>{' '}
                          <span className='font-medium'>
                            {activity.utilityBreakdown.priorityScore.toFixed(2)}
                          </span>
                        </div>
                        <div>
                          <span className='text-muted-foreground'>
                            Deadline:
                          </span>{' '}
                          <span className='font-medium'>
                            {activity.utilityBreakdown.deadlineScore.toFixed(2)}
                          </span>
                        </div>
                        <div>
                          <span className='text-muted-foreground'>Focus:</span>{' '}
                          <span className='font-medium'>
                            {activity.utilityBreakdown.focusTimeBonus.toFixed(
                              2
                            )}
                          </span>
                        </div>
                        <div>
                          <span className='text-muted-foreground'>
                            Context Switch:
                          </span>{' '}
                          <span className='font-medium'>
                            {activity.utilityBreakdown.contextSwitchPenalty.toFixed(
                              2
                            )}
                          </span>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </CardContent>
    </Card>
  );
}
