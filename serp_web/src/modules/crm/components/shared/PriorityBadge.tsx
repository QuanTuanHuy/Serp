// PriorityBadge Component (authors: QuanTuanHuy, Description: Part of Serp Project)

import { Badge } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { Priority } from '../../types';

interface PriorityBadgeProps {
  priority: Priority;
  variant?: 'default' | 'secondary' | 'destructive' | 'outline';
  className?: string;
}

// Priority color mappings
const priorityColors: Record<Priority, string> = {
  LOW: 'bg-blue-100 text-blue-800 border-blue-200 hover:bg-blue-200',
  MEDIUM: 'bg-yellow-100 text-yellow-800 border-yellow-200 hover:bg-yellow-200',
  HIGH: 'bg-orange-100 text-orange-800 border-orange-200 hover:bg-orange-200',
  URGENT: 'bg-red-100 text-red-800 border-red-200 hover:bg-red-200',
};

// Priority display text mappings
const priorityText: Record<Priority, string> = {
  LOW: 'Low',
  MEDIUM: 'Medium',
  HIGH: 'High',
  URGENT: 'Urgent',
};

// Priority icons
const priorityIcons: Record<Priority, string> = {
  LOW: '⬇️',
  MEDIUM: '➡️',
  HIGH: '⬆️',
  URGENT: '🔥',
};

export const PriorityBadge: React.FC<PriorityBadgeProps> = ({
  priority,
  variant = 'outline',
  className,
}) => {
  const colorClass =
    priorityColors[priority] || 'bg-gray-100 text-gray-800 border-gray-200';
  const displayText = priorityText[priority] || priority;
  const icon = priorityIcons[priority];

  return (
    <Badge
      variant={variant}
      className={cn(
        'inline-flex items-center gap-1 rounded-full border px-2.5 py-0.5 text-xs font-medium transition-colors',
        variant === 'outline' ? colorClass : '',
        className
      )}
    >
      <span className='text-xs'>{icon}</span>
      {displayText}
    </Badge>
  );
};

export default PriorityBadge;
