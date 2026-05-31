/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM optimization run overview
 */

'use client';

import {
  Badge,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import { getPmOptimizationAlgorithmLabel } from '../../constants/optimization';
import type {
  PMOptimizationRunApi,
  PMOptimizationRunSummaryApi,
} from '../../types/api';

type PMOptimizationRunOverviewProps = {
  run: PMOptimizationRunApi;
  summary: PMOptimizationRunSummaryApi | null | undefined;
};

export function PMOptimizationRunOverview({
  run,
  summary,
}: PMOptimizationRunOverviewProps) {
  return (
    <div className='grid gap-4 xl:grid-cols-2'>
      <Card className='shadow-sm'>
        <CardHeader>
          <div className='flex items-start justify-between gap-3'>
            <div>
              <CardTitle className='text-base'>Run metadata</CardTitle>
              <p className='text-sm text-muted-foreground'>
                Clean optimization intent and solver context.
              </p>
            </div>
            <Badge variant='secondary'>{run.status || 'GENERATED'}</Badge>
          </div>
        </CardHeader>
        <CardContent className='grid gap-3 md:grid-cols-2'>
          <Field label='Objective' value={formatValue(run.objective)} />
          <Field label='Change scope' value={formatValue(run.changeScope)} />
          <Field
            label='Algorithm'
            value={getPmOptimizationAlgorithmLabel(run.algorithmKey)}
          />
          <Field label='Version' value={formatValue(run.algorithmVersion)} />
          <Field label='Solver status' value={formatValue(run.solverStatus)} />
          <Field
            label='Objective score'
            value={formatValue(run.objectiveScore)}
          />
          <Field label='Planning start' value={formatDate(run.planningStart)} />
          <Field label='Planning end' value={formatDate(run.planningEnd)} />
        </CardContent>
      </Card>

      <Card className='shadow-sm'>
        <CardHeader>
          <CardTitle className='text-base'>Summary</CardTitle>
        </CardHeader>
        <CardContent className='space-y-4'>
          <div className='grid gap-3 md:grid-cols-2 xl:grid-cols-3'>
            <Metric title='Scope' value={metricValue(summary?.scopeSize)} />
            <Metric
              title='Suggestions'
              value={metricValue(summary?.assignmentSuggestionCount)}
            />
            <Metric
              title='Scheduled'
              value={metricValue(summary?.scheduledItemCount)}
            />
            <Metric
              title='Warnings'
              value={metricValue(summary?.warningsCount)}
            />
            <Metric
              title='Confidence'
              value={summary?.confidenceLevel || 'UNKNOWN'}
            />
            <Metric
              title='Objective score'
              value={formatValue(run.objectiveScore)}
            />
          </div>
          <div className='grid gap-3 md:grid-cols-2'>
            <Field
              label='Late items before'
              value={metricValue(summary?.lateItemsBefore)}
            />
            <Field
              label='Late items after'
              value={metricValue(summary?.lateItemsAfter)}
            />
            <Field
              label='Overloaded before'
              value={metricValue(summary?.overloadedAssigneeCountBefore)}
            />
            <Field
              label='Overloaded after'
              value={metricValue(summary?.overloadedAssigneeCountAfter)}
            />
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

function Metric({ title, value }: { title: string; value: string }) {
  return (
    <div className='rounded-md border px-3 py-2'>
      <p className='text-xs font-medium uppercase tracking-wide text-muted-foreground'>
        {title}
      </p>
      <p className='mt-1 text-xl font-semibold'>{value}</p>
    </div>
  );
}

function Field({ label, value }: { label: string; value: string }) {
  return (
    <div className='rounded-md border px-3 py-2'>
      <p className='text-xs font-medium uppercase tracking-wide text-muted-foreground'>
        {label}
      </p>
      <p className='mt-1 text-sm font-medium'>{value}</p>
    </div>
  );
}

function formatDate(value?: number | null) {
  if (!value) return '-';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '-' : date.toLocaleString();
}

function formatValue(value?: string | number | null) {
  if (value === undefined || value === null || value === '') return '-';
  return String(value);
}

function metricValue(value?: number | null) {
  return typeof value === 'number' ? value.toString() : '0';
}
