/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Driver handover workflow step
 */

import { CheckCircle2, Clock } from 'lucide-react';

import { cn } from '@/shared/utils';

interface DriverWorkflowStepProps {
  label: string;
  active?: boolean;
  done?: boolean;
}

export function DriverWorkflowStep({
  label,
  active,
  done,
}: DriverWorkflowStepProps) {
  return (
    <div className='flex items-center gap-2'>
      <div
        className={cn(
          'flex h-7 w-7 items-center justify-center rounded-full border text-xs',
          done && 'border-primary bg-primary text-primary-foreground',
          active && !done && 'border-primary text-primary'
        )}
      >
        {done ? (
          <CheckCircle2 className='h-4 w-4' />
        ) : (
          <Clock className='h-4 w-4' />
        )}
      </div>
      <span
        className={cn(
          'text-sm text-muted-foreground',
          (active || done) && 'font-medium text-foreground'
        )}
      >
        {label}
      </span>
    </div>
  );
}
