/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item detail primitives
 */

import type { ReactNode } from 'react';
import { ChevronRight, Flag, UserRound } from 'lucide-react';
import { Avatar, AvatarFallback, AvatarImage } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { getInitials } from './pmWorkItemDetail.utils';

export function DetailSection({
  title,
  children,
}: {
  title: string;
  children: ReactNode;
}) {
  return (
    <section className='space-y-2'>
      <h2 className='text-base font-semibold'>{title}</h2>
      {children}
    </section>
  );
}

export function DetailField({
  label,
  children,
}: {
  label: string;
  children: ReactNode;
}) {
  return (
    <div className='grid grid-cols-[132px_minmax(0,1fr)] gap-3 text-sm'>
      <dt className='font-medium text-muted-foreground'>{label}</dt>
      <dd className='min-w-0 text-foreground'>{children}</dd>
    </div>
  );
}

export function UserValue({
  name,
  avatarUrl,
}: {
  name: string;
  avatarUrl?: string | null;
}) {
  return (
    <span className='inline-flex min-w-0 items-center gap-2'>
      <PMUserAvatar name={name} avatarUrl={avatarUrl} />
      <span className='truncate'>{name}</span>
    </span>
  );
}

export function PMUserAvatar({
  name,
  avatarUrl,
}: {
  name: string;
  avatarUrl?: string | null;
}) {
  const empty = name === 'Unassigned' || name === 'None';
  return (
    <Avatar className='h-7 w-7 border'>
      {avatarUrl ? <AvatarImage src={avatarUrl} alt={name} /> : null}
      <AvatarFallback
        className={cn(
          'text-[10px] font-semibold',
          empty && 'text-muted-foreground'
        )}
      >
        {empty ? <UserRound className='h-3.5 w-3.5' /> : getInitials(name)}
      </AvatarFallback>
    </Avatar>
  );
}

export function PriorityValue({
  priority,
}: {
  priority?: { name?: string | null; color?: string | null } | null;
}) {
  return (
    <span className='inline-flex items-center gap-1.5 text-xs text-muted-foreground'>
      <Flag
        className='h-3.5 w-3.5'
        style={priority?.color ? { color: priority.color } : undefined}
      />
      {priority?.name ?? 'None'}
    </span>
  );
}

export function CollapsedPanel({
  title,
  subtitle,
  icon,
}: {
  title: string;
  subtitle?: string;
  icon: ReactNode;
}) {
  return (
    <button className='mt-3 flex w-full items-center justify-between rounded-lg border bg-background px-4 py-3 text-left hover:bg-muted/40'>
      <span className='inline-flex min-w-0 items-center gap-3 font-semibold'>
        <ChevronRight className='h-4 w-4 shrink-0 text-muted-foreground' />
        <span className='truncate'>{title}</span>
        <span className='shrink-0 text-muted-foreground'>{icon}</span>
        {subtitle ? (
          <span className='truncate text-xs font-normal text-muted-foreground'>
            {subtitle}
          </span>
        ) : null}
      </span>
    </button>
  );
}
