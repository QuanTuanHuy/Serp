/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings status badge component
 */

'use client';

import React from 'react';
import { Badge } from '@/shared/components/ui/badge';
import { cn } from '@/shared/utils';

type StatusType =
  | 'ACTIVE'
  | 'INACTIVE'
  | 'SUSPENDED'
  | 'PENDING'
  | 'EXPIRED'
  | 'CANCELLED'
  | 'TRIAL'
  | string;

export interface SettingsStatusBadgeProps {
  status: StatusType;
  className?: string;
}

/**
 * SettingsStatusBadge - Colored badge for various status types
 * Automatically determines color based on status value
 *
 * Usage:
 * ```tsx
 * <SettingsStatusBadge status="ACTIVE" />
 * <SettingsStatusBadge status="SUSPENDED" />
 * ```
 */
export const SettingsStatusBadge: React.FC<SettingsStatusBadgeProps> = ({
  status,
  className,
}) => {
  const getStatusConfig = (): {
    variant: 'default' | 'secondary' | 'destructive' | 'outline';
    label: string;
  } => {
    const statusUpper = status.toUpperCase();

    // Active/Success states
    if (
      ['ACTIVE', 'ENABLED', 'PUBLISHED', 'COMPLETED', 'PAID'].includes(
        statusUpper
      )
    ) {
      return { variant: 'default', label: status };
    }

    // Warning/Trial states
    if (
      ['TRIAL', 'PENDING', 'SUSPENDED', 'DRAFT', 'PROCESSING'].includes(
        statusUpper
      )
    ) {
      return { variant: 'secondary', label: status };
    }

    // Error/Inactive states
    if (
      [
        'INACTIVE',
        'DISABLED',
        'CANCELLED',
        'EXPIRED',
        'FAILED',
        'OVERDUE',
      ].includes(statusUpper)
    ) {
      return { variant: 'destructive', label: status };
    }

    // Default outline
    return { variant: 'outline', label: status };
  };

  const config = getStatusConfig();

  return (
    <Badge variant={config.variant} className={cn('uppercase', className)}>
      {config.label}
    </Badge>
  );
};
