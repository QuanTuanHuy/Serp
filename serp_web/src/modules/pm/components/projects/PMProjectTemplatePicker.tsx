/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project template picker
 */

'use client';

import { AlertCircle } from 'lucide-react';
import { cn } from '@/shared/utils';
import type { PMProjectTemplateDefinition } from '../../types/project-create.types';
import type { PMProjectTemplateType } from '../../types/project-list.types';
import { PMProjectTemplateCard } from './PMProjectTemplateCard';

interface PMProjectTemplatePickerProps {
  value?: PMProjectTemplateType | null;
  onValueChange: (value: PMProjectTemplateType) => void;
  options: PMProjectTemplateDefinition[];
  error?: string;
}

export function PMProjectTemplatePicker({
  value,
  onValueChange,
  options,
  error,
}: PMProjectTemplatePickerProps) {
  return (
    <div className='space-y-4'>
      <div className='space-y-2'>
        <p className='text-sm font-semibold uppercase tracking-wide text-muted-foreground'>
          Step 1
        </p>
        <div>
          <h2 className='text-2xl font-semibold tracking-tight'>
            Choose a software template
          </h2>
          <p className='text-sm text-muted-foreground'>
            Start from the company workflow that best matches how the team plans
            and ships work.
          </p>
        </div>
      </div>

      <div className='grid gap-4'>
        {options.map((option) => (
          <PMProjectTemplateCard
            key={option.type}
            template={option}
            isSelected={value === option.type}
            onSelect={onValueChange}
          />
        ))}
      </div>

      <div
        className={cn(
          'flex min-h-5 items-center gap-2 text-sm text-muted-foreground',
          error && 'text-destructive'
        )}
      >
        {error ? <AlertCircle className='h-4 w-4 shrink-0' /> : null}
        <span>
          {error ||
            'One template must be selected before the project can be created.'}
        </span>
      </div>
    </div>
  );
}
