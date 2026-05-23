/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project list table row
 */

'use client';

import { useMemo } from 'react';
import {
  ArrowUpRight,
  Archive,
  Copy,
  MoreHorizontal,
  PencilLine,
  Trash2,
} from 'lucide-react';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
  Button,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
  TableCell,
  TableRow,
  Progress,
} from '@/shared/components/ui';
import type { PMProjectListItem } from '../../types/project-list.types';
import { PMProjectStatusBadge } from './PMProjectStatusBadge';

interface PMProjectListRowProps {
  project: PMProjectListItem;
  onOpen: (projectId: string) => void;
  onEdit: (projectId: string) => void;
}

function getInitials(name: string) {
  return name
    .split(' ')
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase();
}

function getProjectAccentGradient(
  key: string,
  status: PMProjectListItem['status']
) {
  if (status === 'ARCHIVED') {
    return 'bg-gradient-to-br from-zinc-500 to-zinc-700 text-zinc-100 shadow-sm';
  }

  const charCode = key.charCodeAt(0) || 0;
  const gradients = [
    'bg-gradient-to-br from-indigo-500 via-purple-500 to-pink-500 text-white shadow-indigo-500/20',
    'bg-gradient-to-br from-blue-500 via-indigo-500 to-violet-500 text-white shadow-blue-500/20',
    'bg-gradient-to-br from-violet-500 via-purple-500 to-indigo-500 text-white shadow-violet-500/20',
    'bg-gradient-to-br from-emerald-500 via-teal-500 to-cyan-500 text-white shadow-emerald-500/20',
    'bg-gradient-to-br from-orange-500 via-rose-500 to-red-500 text-white shadow-orange-500/20',
  ];
  return gradients[charCode % gradients.length];
}

function formatRelativeTime(value: string) {
  const timestamp = new Date(value).getTime();
  const now = Date.now();
  const diffMinutes = Math.max(1, Math.floor((now - timestamp) / 60000));

  if (diffMinutes < 60) {
    return `${diffMinutes}m ago`;
  }

  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) {
    return `${diffHours}h ago`;
  }

  const diffDays = Math.floor(diffHours / 24);
  if (diffDays < 30) {
    return `${diffDays}d ago`;
  }

  return new Intl.DateTimeFormat('en-GB', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(new Date(value));
}

export function PMProjectListRow({
  project,
  onOpen,
  onEdit,
}: PMProjectListRowProps) {
  const updatedLabel = formatRelativeTime(project.updatedAt);
  const keyGradient = getProjectAccentGradient(project.key, project.status);

  // Generate a deterministic mock completion rate for modern presentation
  const mockProgress = useMemo(() => {
    const hash = project.id
      .split('')
      .reduce((acc, char) => acc + char.charCodeAt(0), 0);
    return (hash % 40) + 55; // 55% - 95%
  }, [project.id]);

  return (
    <TableRow
      className='group cursor-pointer transition-all duration-200 hover:bg-muted/40 hover:shadow-[inset_0_0_12px_rgba(124,58,237,0.02)]'
      tabIndex={0}
      onClick={() => onOpen(project.id)}
      onKeyDown={(event) => {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault();
          onOpen(project.id);
        }
      }}
    >
      <TableCell className='min-w-[280px] py-4.5 pl-6'>
        <div className='flex items-center gap-4'>
          {/* Key avatar with scale effect and shadow */}
          <div
            className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-xl text-sm font-bold shadow-md transition-transform duration-300 group-hover:scale-105 ${keyGradient}`}
          >
            {project.key.slice(0, 2)}
          </div>

          <div className='min-w-0 space-y-0.5'>
            <div className='flex flex-wrap items-center gap-2'>
              <p className='truncate text-sm font-semibold text-foreground group-hover:text-primary transition-colors md:text-base'>
                {project.name}
              </p>
              {project.status === 'ARCHIVED' && (
                <PMProjectStatusBadge status='ARCHIVED' />
              )}
            </div>
            <p className='line-clamp-1 text-xs text-muted-foreground max-w-[400px]'>
              {project.description}
            </p>
            <p className='text-[11px] text-muted-foreground lg:hidden'>
              {project.category} · {project.lead.name}
            </p>
            {project.status !== 'ARCHIVED' && (
              <div className='md:hidden mt-1'>
                <PMProjectStatusBadge status={project.status} />
              </div>
            )}
          </div>
        </div>
      </TableCell>

      <TableCell className='hidden py-4.5 md:table-cell'>
        <span className='rounded-md bg-muted/60 border border-border/50 px-2 py-0.5 text-xs font-semibold tracking-wide text-muted-foreground'>
          {project.key}
        </span>
      </TableCell>

      <TableCell className='hidden py-4.5 lg:table-cell'>
        <div className='flex items-center gap-2.5'>
          <Avatar className='h-7 w-7 border border-border/60 shadow-sm transition-transform duration-300 group-hover:scale-105'>
            {project.lead.avatarUrl && (
              <AvatarImage
                src={project.lead.avatarUrl}
                alt={project.lead.name}
              />
            )}
            <AvatarFallback className='text-[10px] bg-primary/5 text-primary font-bold animate-pulse'>
              {getInitials(project.lead.name)}
            </AvatarFallback>
          </Avatar>
          <span className='text-sm font-medium text-foreground/80 group-hover:text-foreground transition-colors'>
            {project.lead.name}
          </span>
        </div>
      </TableCell>

      <TableCell className='hidden py-4.5 xl:table-cell'>
        <div className='space-y-2 w-48'>
          <div className='flex items-center justify-between text-xs'>
            <span className='font-medium text-foreground/80'>
              {project.category}
            </span>
            {project.status !== 'ARCHIVED' && (
              <PMProjectStatusBadge status={project.status} />
            )}
          </div>
          {project.status !== 'ARCHIVED' && (
            <div className='space-y-1'>
              <div className='flex items-center justify-between text-[10px] text-muted-foreground'>
                <span>Progress</span>
                <span className='font-semibold'>{mockProgress}%</span>
              </div>
              <Progress value={mockProgress} className='h-1.5 bg-muted' />
            </div>
          )}
        </div>
      </TableCell>

      <TableCell className='py-4.5'>
        <p className='text-xs text-muted-foreground whitespace-nowrap'>
          Updated {updatedLabel}
        </p>
      </TableCell>

      <TableCell
        className='py-4.5 pr-6 text-right'
        onClick={(event) => event.stopPropagation()}
      >
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant='ghost'
              size='icon'
              className='h-8 w-8 hover:bg-muted/80 rounded-lg text-muted-foreground hover:text-foreground'
              aria-label={`Open actions for ${project.name}`}
            >
              <MoreHorizontal className='h-4 w-4' />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent
            align='end'
            className='rounded-xl border border-border/80 bg-popover/95 backdrop-blur-sm'
          >
            <DropdownMenuItem
              onClick={() => onOpen(project.id)}
              className='rounded-lg'
            >
              <ArrowUpRight className='mr-2 h-4 w-4 text-muted-foreground' />
              Open project
            </DropdownMenuItem>
            <DropdownMenuItem
              onClick={() => onEdit(project.id)}
              className='rounded-lg'
            >
              <PencilLine className='mr-2 h-4 w-4 text-muted-foreground' />
              Edit project
            </DropdownMenuItem>
            <DropdownMenuSeparator className='border-border/50' />
            <DropdownMenuItem disabled className='rounded-lg'>
              <Copy className='mr-2 h-4 w-4 text-muted-foreground' />
              Duplicate
            </DropdownMenuItem>
            <DropdownMenuItem disabled className='rounded-lg'>
              <Archive className='mr-2 h-4 w-4 text-muted-foreground' />
              Archive
            </DropdownMenuItem>
            <DropdownMenuItem
              disabled
              className='text-destructive hover:text-destructive rounded-lg'
            >
              <Trash2 className='mr-2 h-4 w-4' />
              Delete
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </TableCell>
    </TableRow>
  );
}
