/**
 * QuickActions Component - Quick action buttons for CRM dashboard
 * @author QuanTuanHuy
 * @description Part of Serp Project - CRM Dashboard quick action cards
 */

'use client';

import React from 'react';
import { cn } from '@/shared/utils';
import {
  UserPlus,
  Target,
  FileText,
  Calendar,
  Mail,
  Phone,
  type LucideIcon,
} from 'lucide-react';

export interface QuickActionItem {
  id: string;
  label: string;
  description?: string;
  icon: LucideIcon;
  href?: string;
  onClick?: () => void;
  variant?: 'default' | 'primary' | 'success' | 'warning';
  badge?: string | number;
}

export interface QuickActionsProps {
  actions?: QuickActionItem[];
  title?: string;
  isLoading?: boolean;
  className?: string;
  columns?: 2 | 3;
}

const variantStyles = {
  default: {
    bg: 'bg-muted/50 hover:bg-muted',
    icon: 'bg-background text-foreground',
    border: 'border-border',
  },
  primary: {
    bg: 'bg-blue-50 hover:bg-blue-100 dark:bg-blue-950/30 dark:hover:bg-blue-950/50',
    icon: 'bg-blue-500 text-white',
    border: 'border-blue-200/50 dark:border-blue-800/30',
  },
  success: {
    bg: 'bg-emerald-50 hover:bg-emerald-100 dark:bg-emerald-950/30 dark:hover:bg-emerald-950/50',
    icon: 'bg-emerald-500 text-white',
    border: 'border-emerald-200/50 dark:border-emerald-800/30',
  },
  warning: {
    bg: 'bg-amber-50 hover:bg-amber-100 dark:bg-amber-950/30 dark:hover:bg-amber-950/50',
    icon: 'bg-amber-500 text-white',
    border: 'border-amber-200/50 dark:border-amber-800/30',
  },
};

const DEFAULT_ACTIONS: QuickActionItem[] = [
  {
    id: 'add-customer',
    label: 'Add Customer',
    description: 'Create new customer record',
    icon: UserPlus,
    variant: 'primary',
    href: '/crm/customers/create',
  },
  {
    id: 'add-lead',
    label: 'Add Lead',
    description: 'Capture new prospect',
    icon: Target,
    variant: 'warning',
    href: '/crm/leads/create',
  },
  {
    id: 'create-quote',
    label: 'Create Quote',
    description: 'Generate new proposal',
    icon: FileText,
    variant: 'success',
    href: '/crm/opportunities/create',
  },
  {
    id: 'schedule-meeting',
    label: 'Schedule Meeting',
    description: 'Book a calendar event',
    icon: Calendar,
    variant: 'default',
  },
  {
    id: 'send-email',
    label: 'Send Email',
    description: 'Compose email to contact',
    icon: Mail,
    variant: 'default',
  },
  {
    id: 'log-call',
    label: 'Log Call',
    description: 'Record phone conversation',
    icon: Phone,
    variant: 'default',
  },
];

export const QuickActions: React.FC<QuickActionsProps> = ({
  actions = DEFAULT_ACTIONS,
  title = 'Quick Actions',
  isLoading = false,
  className,
  columns = 3,
}) => {
  if (isLoading) {
    return (
      <div
        className={cn('rounded-xl border bg-card p-6 animate-pulse', className)}
      >
        <div className='h-5 w-32 bg-muted rounded mb-6' />
        <div
          className={cn(
            'grid gap-3',
            columns === 2 ? 'grid-cols-2' : 'grid-cols-2 md:grid-cols-3'
          )}
        >
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <div key={i} className='h-20 bg-muted rounded-lg' />
          ))}
        </div>
      </div>
    );
  }

  const handleClick = (action: QuickActionItem) => {
    if (action.onClick) {
      action.onClick();
    } else if (action.href) {
      window.location.href = action.href;
    }
  };

  return (
    <div className={cn('rounded-xl border bg-card p-6', className)}>
      {/* Header */}
      <h3 className='text-lg font-semibold text-foreground mb-4'>{title}</h3>

      {/* Actions Grid */}
      <div
        className={cn(
          'grid gap-3',
          columns === 2 ? 'grid-cols-2' : 'grid-cols-2 md:grid-cols-3'
        )}
      >
        {actions.map((action) => {
          const styles = variantStyles[action.variant || 'default'];
          const Icon = action.icon;

          return (
            <button
              key={action.id}
              onClick={() => handleClick(action)}
              className={cn(
                'group relative flex flex-col items-center justify-center gap-2 p-4 rounded-xl border transition-all duration-200',
                'hover:shadow-md cursor-pointer',
                styles.bg,
                styles.border
              )}
            >
              {/* Badge */}
              {action.badge && (
                <span className='absolute -top-1.5 -right-1.5 h-5 min-w-5 flex items-center justify-center px-1.5 text-xs font-medium bg-rose-500 text-white rounded-full'>
                  {action.badge}
                </span>
              )}

              {/* Icon */}
              <div
                className={cn(
                  'flex h-10 w-10 items-center justify-center rounded-lg shadow-sm transition-transform duration-200 group-hover:scale-110',
                  styles.icon
                )}
              >
                <Icon className='h-5 w-5' />
              </div>

              {/* Label */}
              <div className='text-center'>
                <p className='text-sm font-medium text-foreground'>
                  {action.label}
                </p>
                {action.description && (
                  <p className='text-xs text-muted-foreground mt-0.5 hidden md:block'>
                    {action.description}
                  </p>
                )}
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
};
