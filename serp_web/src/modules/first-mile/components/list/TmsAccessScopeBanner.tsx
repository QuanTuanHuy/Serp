/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Compact TMS access scope banner
 */

import React from 'react';
import { Badge } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { Shield } from 'lucide-react';

interface TmsAccessScopeBannerProps {
  canAccess: boolean;
  badgeLabel: string;
  description: string;
  className?: string;
}

export const TmsAccessScopeBanner: React.FC<TmsAccessScopeBannerProps> = ({
  canAccess,
  badgeLabel,
  description,
  className,
}) => {
  return (
    <div
      className={cn(
        'flex flex-col gap-2 rounded-lg border bg-muted/30 px-3 py-2 sm:flex-row sm:items-center sm:gap-3',
        className
      )}
    >
      <div className='flex min-w-0 items-center gap-2'>
        <Shield className='h-4 w-4 shrink-0 text-muted-foreground' />
        <span className='text-xs font-medium uppercase tracking-wide text-muted-foreground'>
          Access scope
        </span>
        <Badge
          variant={canAccess ? 'secondary' : 'destructive'}
          className='shrink-0'
        >
          {badgeLabel}
        </Badge>
      </div>
      <p className='min-w-0 text-xs text-muted-foreground sm:flex-1'>
        {description}
      </p>
    </div>
  );
};
