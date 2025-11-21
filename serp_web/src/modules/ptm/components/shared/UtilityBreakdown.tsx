/**
 * PTM v2 - Utility Breakdown Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Show scheduling decision reasoning
 */

import { Info } from 'lucide-react';
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/shared/components/ui/tooltip';
import { Progress } from '@/shared/components/ui/progress';
import type { UtilityBreakdown as UtilityBreakdownType } from '../../types';

interface UtilityBreakdownProps {
  breakdown: UtilityBreakdownType;
}

export function UtilityBreakdown({ breakdown }: UtilityBreakdownProps) {
  const components = [
    {
      label: 'Priority',
      value: breakdown.priorityScore,
      color: 'bg-blue-500',
      description: 'Task priority weight',
    },
    {
      label: 'Deadline',
      value: breakdown.deadlineScore,
      color: 'bg-amber-500',
      description: 'Urgency based on deadline',
    },
    {
      label: 'Context Switch',
      value: Math.abs(breakdown.contextSwitchPenalty),
      color: 'bg-red-500',
      description: 'Penalty for switching task types',
      isNegative: true,
    },
    {
      label: 'Focus Bonus',
      value: breakdown.focusTimeBonus,
      color: 'bg-purple-500',
      description: 'Bonus for deep work time',
    },
  ];

  const maxValue = Math.max(...components.map((c) => c.value));

  return (
    <TooltipProvider>
      <div className='space-y-3'>
        <div className='flex items-center justify-between'>
          <span className='text-sm font-medium'>Utility Score</span>
          <span className='text-sm font-semibold'>
            {breakdown.totalUtility.toFixed(2)}
          </span>
        </div>

        <div className='space-y-2'>
          {components.map((component) => (
            <Tooltip key={component.label}>
              <TooltipTrigger asChild>
                <div className='space-y-1'>
                  <div className='flex items-center justify-between text-xs'>
                    <span className='text-muted-foreground'>
                      {component.label}
                    </span>
                    <span
                      className={component.isNegative ? 'text-red-600' : ''}
                    >
                      {component.isNegative ? '-' : '+'}
                      {component.value.toFixed(2)}
                    </span>
                  </div>
                  <div className='h-1.5 bg-muted rounded-full overflow-hidden'>
                    <div
                      className={`h-full ${component.color}`}
                      style={{
                        width: `${(component.value / maxValue) * 100}%`,
                      }}
                    />
                  </div>
                </div>
              </TooltipTrigger>
              <TooltipContent>
                <p>{component.description}</p>
              </TooltipContent>
            </Tooltip>
          ))}
        </div>

        <div className='pt-2 border-t'>
          <p className='text-xs text-muted-foreground flex items-start gap-1'>
            <Info className='h-3 w-3 mt-0.5 flex-shrink-0' />
            <span>{breakdown.reason}</span>
          </p>
        </div>
      </div>
    </TooltipProvider>
  );
}
