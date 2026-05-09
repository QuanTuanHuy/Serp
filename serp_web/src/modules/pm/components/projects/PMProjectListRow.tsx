/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project list table row
 */

'use client';

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
} from '@/shared/components/ui';
import type { PMProjectListItem } from '../../types/project-list.types';
import { PMProjectStatusBadge } from './PMProjectStatusBadge';
import { PMProjectTemplateBadge } from './PMProjectTemplateBadge';

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

function getProjectAccent(templateType: PMProjectListItem['templateType']) {
  switch (templateType) {
    case 'KANBAN':
      return 'bg-blue-600';
    case 'SCRUM':
      return 'bg-violet-600';
    default:
      return 'bg-slate-700';
  }
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

  return (
    <TableRow
      className='cursor-pointer'
      tabIndex={0}
      onClick={() => onOpen(project.id)}
      onKeyDown={(event) => {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault();
          onOpen(project.id);
        }
      }}
    >
      <TableCell className='min-w-[280px] py-5'>
        <div className='flex items-start gap-4'>
          <div
            className={`mt-0.5 flex h-11 w-11 shrink-0 items-center justify-center rounded-xl text-sm font-bold text-white ${getProjectAccent(
              project.templateType
            )}`}
          >
            {project.key.slice(0, 2)}
          </div>

          <div className='min-w-0 space-y-1'>
            <div className='flex flex-wrap items-center gap-2'>
              <p className='truncate text-sm font-semibold text-foreground md:text-base'>
                {project.name}
              </p>
              {project.status === 'ARCHIVED' && (
                <PMProjectStatusBadge status='ARCHIVED' />
              )}
            </div>
            <p className='line-clamp-1 text-sm text-muted-foreground'>
              {project.description}
            </p>
            <p className='text-xs text-muted-foreground lg:hidden'>
              {project.category} · {project.lead.name}
            </p>
            <div className='flex flex-wrap items-center gap-2 md:hidden'>
              <PMProjectTemplateBadge templateType={project.templateType} />
              {project.status !== 'ARCHIVED' && (
                <PMProjectStatusBadge status={project.status} />
              )}
            </div>
          </div>
        </div>
      </TableCell>

      <TableCell className='hidden py-5 md:table-cell'>
        <span className='rounded-md bg-muted px-2 py-1 text-xs font-semibold tracking-wide text-muted-foreground'>
          {project.key}
        </span>
      </TableCell>

      <TableCell className='hidden py-5 lg:table-cell'>
        <div className='flex items-center gap-3'>
          <Avatar className='h-8 w-8 border'>
            {project.lead.avatarUrl && (
              <AvatarImage
                src={project.lead.avatarUrl}
                alt={project.lead.name}
              />
            )}
            <AvatarFallback>{getInitials(project.lead.name)}</AvatarFallback>
          </Avatar>
          <span className='text-sm font-medium'>{project.lead.name}</span>
        </div>
      </TableCell>

      <TableCell className='hidden py-5 xl:table-cell'>
        <PMProjectTemplateBadge templateType={project.templateType} />
      </TableCell>

      <TableCell className='hidden py-5 xl:table-cell'>
        <div className='space-y-1'>
          <p className='text-sm font-medium text-foreground'>
            {project.category}
          </p>
          {project.status !== 'ARCHIVED' && (
            <PMProjectStatusBadge status={project.status} />
          )}
        </div>
      </TableCell>

      <TableCell className='py-5'>
        <div className='space-y-1'>
          <p className='text-sm font-semibold text-foreground'>
            {project.openItemsCount} open items
          </p>
          <p className='text-xs text-muted-foreground'>
            Updated {updatedLabel}
          </p>
        </div>
      </TableCell>

      <TableCell className='hidden py-5 text-sm text-muted-foreground lg:table-cell'>
        {updatedLabel}
      </TableCell>

      <TableCell
        className='py-5 text-right'
        onClick={(event) => event.stopPropagation()}
      >
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant='ghost'
              size='icon'
              className='h-8 w-8'
              aria-label={`Open actions for ${project.name}`}
            >
              <MoreHorizontal className='h-4 w-4' />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align='end'>
            <DropdownMenuItem onClick={() => onOpen(project.id)}>
              <ArrowUpRight className='mr-2 h-4 w-4' />
              Open project
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => onEdit(project.id)}>
              <PencilLine className='mr-2 h-4 w-4' />
              Edit project
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem disabled>
              <Copy className='mr-2 h-4 w-4' />
              Duplicate
            </DropdownMenuItem>
            <DropdownMenuItem disabled>
              <Archive className='mr-2 h-4 w-4' />
              Archive
            </DropdownMenuItem>
            <DropdownMenuItem disabled className='text-destructive'>
              <Trash2 className='mr-2 h-4 w-4' />
              Delete
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </TableCell>
    </TableRow>
  );
}
