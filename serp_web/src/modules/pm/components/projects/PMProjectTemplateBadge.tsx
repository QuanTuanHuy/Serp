/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project template badge
 */

import { Badge } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { PMProjectTemplateType } from '../../types/project-list.types';

interface PMProjectTemplateBadgeProps {
  templateType: PMProjectTemplateType;
}

const TEMPLATE_LABELS: Record<PMProjectTemplateType, string> = {
  BLANK: 'Blank',
  KANBAN: 'Kanban',
  SCRUM: 'Scrum',
};

const TEMPLATE_STYLES: Record<PMProjectTemplateType, string> = {
  BLANK:
    'border-slate-200 bg-slate-100 text-slate-700 dark:border-slate-800 dark:bg-slate-900 dark:text-slate-300',
  KANBAN:
    'border-blue-200 bg-blue-50 text-blue-700 dark:border-blue-900 dark:bg-blue-950/70 dark:text-blue-300',
  SCRUM:
    'border-violet-200 bg-violet-50 text-violet-700 dark:border-violet-900 dark:bg-violet-950/70 dark:text-violet-300',
};

export function PMProjectTemplateBadge({
  templateType,
}: PMProjectTemplateBadgeProps) {
  return (
    <Badge
      variant='outline'
      className={cn(
        'rounded-md px-2 py-0.5 text-[11px] font-semibold uppercase tracking-wide',
        TEMPLATE_STYLES[templateType]
      )}
    >
      {TEMPLATE_LABELS[templateType]}
    </Badge>
  );
}
