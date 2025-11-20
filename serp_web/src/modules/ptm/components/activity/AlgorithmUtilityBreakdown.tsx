/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Algorithm Utility Breakdown Component
 */

'use client';

import { Sparkles, Clock, Layers } from 'lucide-react';
import { Badge } from '@/shared/components/ui/badge';
import { Progress } from '@/shared/components/ui/progress';
import type { UtilityBreakdown, AlgorithmType } from '../../types';

interface Props {
  breakdown: UtilityBreakdown;
  algorithmType?: AlgorithmType;
  executionTimeMs?: number;
  tasksAffected?: number;
}

const ALGORITHM_LABELS: Record<AlgorithmType, string> = {
  local_heuristic: 'Heuristic',
  hybrid: 'Hybrid',
  milp_optimized: 'MILP',
};

export function AlgorithmUtilityBreakdown({
  breakdown,
  algorithmType,
  executionTimeMs,
  tasksAffected,
}: Props) {
  const totalUtility = breakdown.totalUtility;

  return (
    <div className='mt-3 p-3 bg-purple-50 dark:bg-purple-950/20 rounded-lg border border-purple-200 dark:border-purple-800'>
      {/* Header */}
      <div className='flex items-center justify-between mb-3'>
        <span className='text-xs font-semibold text-purple-700 dark:text-purple-300 flex items-center gap-1.5'>
          <Sparkles className='h-3.5 w-3.5' />
          Algorithm Utility Breakdown
        </span>

        <div className='flex items-center gap-2'>
          {algorithmType && (
            <Badge variant='secondary' className='text-xs'>
              {ALGORITHM_LABELS[algorithmType]}
            </Badge>
          )}
          {executionTimeMs && (
            <span className='text-xs text-muted-foreground flex items-center gap-1'>
              <Clock className='h-3 w-3' />
              {executionTimeMs}ms
            </span>
          )}
        </div>
      </div>

      {/* Utility Scores */}
      <div className='space-y-2'>
        {/* Priority Score */}
        <div className='space-y-1'>
          <div className='flex items-center justify-between text-xs'>
            <span className='text-muted-foreground'>Priority Score</span>
            <span className='font-mono font-medium text-red-600'>
              {breakdown.priorityScore.toFixed(2)}
            </span>
          </div>
          <Progress
            value={(breakdown.priorityScore / 1) * 100}
            className='h-1.5 bg-red-100 dark:bg-red-950/30'
          />
        </div>

        {/* Deadline Score */}
        <div className='space-y-1'>
          <div className='flex items-center justify-between text-xs'>
            <span className='text-muted-foreground'>Deadline Score</span>
            <span className='font-mono font-medium text-orange-600'>
              {breakdown.deadlineScore.toFixed(2)}
            </span>
          </div>
          <Progress
            value={(breakdown.deadlineScore / 1) * 100}
            className='h-1.5 bg-orange-100 dark:bg-orange-950/30'
          />
        </div>

        {/* Focus Time Bonus */}
        <div className='space-y-1'>
          <div className='flex items-center justify-between text-xs'>
            <span className='text-muted-foreground'>Focus Time Bonus</span>
            <span className='font-mono font-medium text-purple-600'>
              +{breakdown.focusTimeBonus.toFixed(2)}
            </span>
          </div>
          <Progress
            value={Math.abs(breakdown.focusTimeBonus) * 100}
            className='h-1.5 bg-purple-100 dark:bg-purple-950/30'
          />
        </div>

        {/* Context Switch Penalty */}
        <div className='space-y-1'>
          <div className='flex items-center justify-between text-xs'>
            <span className='text-muted-foreground'>
              Context Switch Penalty
            </span>
            <span className='font-mono font-medium text-gray-600'>
              {breakdown.contextSwitchPenalty.toFixed(2)}
            </span>
          </div>
          <Progress
            value={Math.abs(breakdown.contextSwitchPenalty) * 100}
            className='h-1.5 bg-gray-100 dark:bg-gray-950/30'
          />
        </div>
      </div>

      {/* Total Utility */}
      <div className='mt-3 pt-3 border-t border-purple-200 dark:border-purple-800'>
        <div className='flex items-center justify-between'>
          <span className='text-xs font-semibold text-purple-700 dark:text-purple-300'>
            Total Utility Score:
          </span>
          <span className='text-sm font-bold text-purple-600'>
            {totalUtility.toFixed(2)}
          </span>
        </div>

        {tasksAffected && (
          <p className='text-xs text-muted-foreground mt-1 flex items-center gap-1'>
            <Layers className='h-3 w-3' />
            {tasksAffected} tasks optimized
          </p>
        )}
      </div>
    </div>
  );
}
