/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM optimization run intent panel
 */

'use client';

import {
  Badge,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Input,
  Separator,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  PM_OPTIMIZATION_ALGORITHM_OPTIONS,
  PM_OPTIMIZATION_CHANGE_SCOPE_OPTIONS,
  PM_OPTIMIZATION_OBJECTIVE_OPTIONS,
} from '../../constants/optimization';
import type {
  PMOptimizationChangeScope,
  PMOptimizationObjective,
} from '../../types/api';

type PMOptimizationRunIntentPanelProps = {
  objective: PMOptimizationObjective;
  changeScope: PMOptimizationChangeScope;
  algorithmKey: string;
  planningStart: string;
  planningEnd: string;
  onObjectiveChange: (value: PMOptimizationObjective) => void;
  onChangeScopeChange: (value: PMOptimizationChangeScope) => void;
  onAlgorithmKeyChange: (value: string) => void;
  onPlanningStartChange: (value: string) => void;
  onPlanningEndChange: (value: string) => void;
};

export function PMOptimizationRunIntentPanel({
  objective,
  changeScope,
  algorithmKey,
  planningStart,
  planningEnd,
  onObjectiveChange,
  onChangeScopeChange,
  onAlgorithmKeyChange,
  onPlanningStartChange,
  onPlanningEndChange,
}: PMOptimizationRunIntentPanelProps) {
  return (
    <Card className='shadow-sm'>
      <CardHeader className='border-b'>
        <div className='space-y-1'>
          <CardTitle className='text-base'>Run settings</CardTitle>
          <p className='text-sm text-muted-foreground'>
            Choose what the solver should optimize and what it may change.
          </p>
        </div>
      </CardHeader>
      <CardContent className='space-y-4 p-4'>
        <div className='space-y-2'>
          <div className='flex items-center justify-between gap-3'>
            <p className='text-sm font-medium'>Objective</p>
            <Badge variant='secondary' className='h-5 px-1.5'>
              Preference
            </Badge>
          </div>
          <div className='grid gap-2'>
            {PM_OPTIMIZATION_OBJECTIVE_OPTIONS.map((option) => (
              <button
                key={option.value}
                type='button'
                onClick={() => onObjectiveChange(option.value)}
                className={cn(
                  'flex items-start justify-between gap-3 rounded-md border px-3 py-2 text-left',
                  objective === option.value &&
                    'border-primary bg-primary/10 text-primary'
                )}
              >
                <div className='space-y-0.5'>
                  <div className='text-sm font-medium'>{option.label}</div>
                  <div className='text-xs text-muted-foreground'>
                    {option.description}
                  </div>
                </div>
                <Badge variant='secondary'>{option.value}</Badge>
              </button>
            ))}
          </div>
        </div>

        <Separator />

        <div className='space-y-2'>
          <div className='flex items-center justify-between gap-3'>
            <p className='text-sm font-medium'>Change scope</p>
            <Badge variant='secondary' className='h-5 px-1.5'>
              Allowed changes
            </Badge>
          </div>
          <div className='grid gap-2'>
            {PM_OPTIMIZATION_CHANGE_SCOPE_OPTIONS.map((option) => (
              <button
                key={option.value}
                type='button'
                onClick={() => onChangeScopeChange(option.value)}
                className={cn(
                  'flex items-start justify-between gap-3 rounded-md border px-3 py-2 text-left',
                  changeScope === option.value &&
                    'border-primary bg-primary/10 text-primary'
                )}
              >
                <div className='space-y-0.5'>
                  <div className='text-sm font-medium'>{option.label}</div>
                  <div className='text-xs text-muted-foreground'>
                    {option.description}
                  </div>
                </div>
                <Badge variant='secondary'>{option.value}</Badge>
              </button>
            ))}
          </div>
        </div>

        <Separator />

        <div className='space-y-2'>
          <p className='text-sm font-medium'>Algorithm</p>
          <select
            value={algorithmKey}
            onChange={(event) => onAlgorithmKeyChange(event.target.value)}
            className='h-10 w-full rounded-md border bg-background px-3 text-sm'
          >
            {PM_OPTIMIZATION_ALGORITHM_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
          <p className='text-xs text-muted-foreground'>
            {PM_OPTIMIZATION_ALGORITHM_OPTIONS.find(
              (option) => option.value === algorithmKey
            )?.description || 'Choose the solver preset used for generation.'}
          </p>
        </div>

        <Separator />

        <div className='grid gap-3'>
          <label className='space-y-1'>
            <span className='text-sm font-medium'>Planning start</span>
            <Input
              type='date'
              value={planningStart}
              onChange={(event) => onPlanningStartChange(event.target.value)}
            />
          </label>
          <label className='space-y-1'>
            <span className='text-sm font-medium'>Planning end</span>
            <Input
              type='date'
              value={planningEnd}
              onChange={(event) => onPlanningEndChange(event.target.value)}
            />
          </label>
        </div>
      </CardContent>
    </Card>
  );
}
