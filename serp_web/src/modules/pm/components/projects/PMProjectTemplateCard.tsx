/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project template selection card
 */

'use client';

import { ArrowRight, CheckCircle2 } from 'lucide-react';
import { Badge } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { PMProjectTemplateDefinition } from '../../types/project-create.types';
import { PMProjectTemplateBadge } from './PMProjectTemplateBadge';

interface PMProjectTemplateCardProps {
  template: PMProjectTemplateDefinition;
  isSelected: boolean;
  onSelect: (templateType: PMProjectTemplateDefinition['type']) => void;
}

export function PMProjectTemplateCard({
  template,
  isSelected,
  onSelect,
}: PMProjectTemplateCardProps) {
  return (
    <button
      type='button'
      onClick={() => onSelect(template.type)}
      className={cn(
        'group flex h-full w-full flex-col rounded-2xl border bg-card p-5 text-left text-card-foreground shadow-sm transition-all hover:-translate-y-0.5 hover:border-primary/40 hover:shadow-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
        isSelected &&
          'border-primary bg-primary/5 shadow-md ring-1 ring-primary/20'
      )}
      aria-pressed={isSelected}
    >
      <div className='flex items-start justify-between gap-4'>
        <div className='space-y-3'>
          <div className='flex flex-wrap items-center gap-2'>
            <PMProjectTemplateBadge templateType={template.type} />
            <Badge variant='outline' className='rounded-md text-[11px]'>
              Software template
            </Badge>
          </div>
          <div className='space-y-1'>
            <h3 className='text-base font-semibold'>{template.title}</h3>
            <p className='text-sm text-muted-foreground'>
              {template.description}
            </p>
          </div>
        </div>

        <div
          className={cn(
            'flex h-8 w-8 shrink-0 items-center justify-center rounded-full border text-muted-foreground transition-colors',
            isSelected &&
              'border-primary bg-primary text-primary-foreground shadow-sm'
          )}
        >
          {isSelected ? (
            <CheckCircle2 className='h-4 w-4' />
          ) : (
            <ArrowRight className='h-4 w-4 transition-transform group-hover:translate-x-0.5' />
          )}
        </div>
      </div>

      <div className='mt-5 space-y-4'>
        <div>
          <p className='text-xs font-semibold uppercase tracking-wide text-muted-foreground'>
            Best fit
          </p>
          <p className='mt-1 text-sm'>{template.usageHint}</p>
        </div>

        <div>
          <p className='text-xs font-semibold uppercase tracking-wide text-muted-foreground'>
            Preset summary
          </p>
          <ul className='mt-2 space-y-2 text-sm text-muted-foreground'>
            {template.presetSummary.map((item) => (
              <li key={item} className='flex items-start gap-2'>
                <span className='mt-1 h-1.5 w-1.5 shrink-0 rounded-full bg-primary/70' />
                <span>{item}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </button>
  );
}
