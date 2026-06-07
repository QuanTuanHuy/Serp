/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project list grid
 */

'use client';

import type React from 'react';
import {
  Archive,
  ArrowUpRight,
  ChevronLeft,
  ChevronRight,
  MoreHorizontal,
  PencilLine,
} from 'lucide-react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
  Button,
  Card,
  CardContent,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/shared/components/ui';
import type { PMProjectListItem } from '../../types/project-list.types';
import {
  useArchivePmProjectMutation,
  useUnarchivePmProjectMutation,
} from '../../api';
import { PMProjectStatusBadge } from './PMProjectStatusBadge';

interface PMProjectListGridProps {
  projects: PMProjectListItem[];
  isLoading: boolean;
  currentPage: number;
  pageSize: number;
  totalCount: number;
  totalFilteredCount: number;
  totalPages: number;
  emptyTitle?: string;
  emptyDescription?: string;
  onPageChange: (page: number) => void;
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

export function PMProjectListGrid({
  projects,
  isLoading,
  currentPage,
  pageSize,
  totalCount,
  totalFilteredCount,
  totalPages,
  emptyTitle = 'No projects match the current filters',
  emptyDescription = 'Try adjusting your search, status, or category filters.',
  onPageChange,
  onOpen,
  onEdit,
}: PMProjectListGridProps) {
  const rangeStart =
    projects.length === 0 ? 0 : (currentPage - 1) * pageSize + 1;
  const rangeEnd = projects.length === 0 ? 0 : rangeStart + projects.length - 1;

  return (
    <div className='space-y-4'>
      {isLoading ? (
        <GridState
          title='Loading projects...'
          description='Fetching the latest project list from PM Core.'
        />
      ) : projects.length > 0 ? (
        <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-3'>
          {projects.map((project) => (
            <PMProjectGridCard
              key={project.id}
              project={project}
              onOpen={onOpen}
              onEdit={onEdit}
            />
          ))}
        </div>
      ) : (
        <GridState title={emptyTitle} description={emptyDescription} />
      )}

      <div className='flex flex-col gap-3 rounded-lg border border-border/80 bg-muted/20 px-6 py-4 text-sm text-muted-foreground lg:flex-row lg:items-center lg:justify-between'>
        <div>
          {isLoading
            ? 'Loading project results'
            : projects.length === 0
              ? `No results from ${totalCount} software projects`
              : `Showing ${rangeStart}-${rangeEnd} of ${totalFilteredCount} matching projects`}
        </div>

        <div className='flex items-center justify-between gap-3 lg:justify-end'>
          <span className='text-xs uppercase tracking-[0.18em] text-muted-foreground'>
            Page {currentPage} of {Math.max(totalPages, 1)}
          </span>
          <div className='flex items-center gap-2'>
            <Button
              type='button'
              variant='outline'
              size='sm'
              onClick={() => onPageChange(currentPage - 1)}
              disabled={currentPage <= 1}
              className='h-8 bg-background/50 border-border/60 hover:bg-muted/70 text-xs font-semibold rounded-lg'
            >
              <ChevronLeft className='mr-1 h-3.5 w-3.5' />
              Previous
            </Button>
            <Button
              type='button'
              variant='outline'
              size='sm'
              onClick={() => onPageChange(currentPage + 1)}
              disabled={currentPage >= totalPages}
              className='h-8 bg-background/50 border-border/60 hover:bg-muted/70 text-xs font-semibold rounded-lg'
            >
              Next
              <ChevronRight className='ml-1 h-3.5 w-3.5' />
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}

function GridState({
  title,
  description,
}: {
  title: string;
  description: string;
}) {
  return (
    <Card className='rounded-lg border border-border/80 bg-card/40 shadow-sm'>
      <CardContent className='px-6 py-16 text-center'>
        <div className='space-y-2'>
          <p className='text-base font-semibold text-foreground'>{title}</p>
          <p className='text-sm text-muted-foreground'>{description}</p>
        </div>
      </CardContent>
    </Card>
  );
}

function PMProjectGridCard({
  project,
  onOpen,
  onEdit,
}: {
  project: PMProjectListItem;
  onOpen: (projectId: string) => void;
  onEdit: (projectId: string) => void;
}) {
  const updatedLabel = formatRelativeTime(project.updatedAt);
  const keyGradient = getProjectAccentGradient(project.key, project.status);
  const [archivePmProject, { isLoading: isArchiving }] =
    useArchivePmProjectMutation();
  const [unarchivePmProject, { isLoading: isUnarchiving }] =
    useUnarchivePmProjectMutation();

  const handleArchive = async (event: React.MouseEvent) => {
    event.stopPropagation();
    const confirmed = window.confirm(
      `Are you sure you want to archive project "${project.name}"?`
    );
    if (!confirmed) return;

    try {
      await archivePmProject(project.id).unwrap();
      toast.success(`Project "${project.name}" has been archived.`);
    } catch (error) {
      toast.error(getErrorMessage(error));
    }
  };

  const handleUnarchive = async (event: React.MouseEvent) => {
    event.stopPropagation();
    try {
      await unarchivePmProject(project.id).unwrap();
      toast.success(`Project "${project.name}" has been unarchived.`);
    } catch (error) {
      toast.error(getErrorMessage(error));
    }
  };

  return (
    <Card
      className='group cursor-pointer rounded-lg border border-border/80 bg-card/50 shadow-sm transition-colors hover:bg-card'
      tabIndex={0}
      onClick={() => onOpen(project.id)}
      onKeyDown={(event) => {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault();
          onOpen(project.id);
        }
      }}
    >
      <CardContent className='flex h-full min-h-[230px] flex-col p-5'>
        <div className='flex items-start justify-between gap-3'>
          <div className='flex min-w-0 items-center gap-3'>
            <div
              className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-lg text-sm font-bold shadow-md transition-transform duration-300 group-hover:scale-105 ${keyGradient}`}
            >
              {project.key.slice(0, 2)}
            </div>
            <div className='min-w-0'>
              <div className='flex items-center gap-2'>
                <h3 className='truncate text-base font-semibold text-foreground group-hover:text-primary'>
                  {project.name}
                </h3>
                {project.status === 'ARCHIVED' ? (
                  <PMProjectStatusBadge status='ARCHIVED' />
                ) : null}
              </div>
              <p className='mt-0.5 text-xs font-semibold tracking-wide text-muted-foreground'>
                {project.key}
              </p>
            </div>
          </div>

          <div onClick={(event) => event.stopPropagation()}>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button
                  variant='ghost'
                  size='icon'
                  className='h-8 w-8 rounded-lg text-muted-foreground hover:bg-muted/80 hover:text-foreground'
                  aria-label={`Open actions for ${project.name}`}
                >
                  <MoreHorizontal className='h-4 w-4' />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align='end' className='rounded-lg'>
                <DropdownMenuItem
                  onClick={() => onOpen(project.id)}
                  className='rounded-md'
                >
                  <ArrowUpRight className='mr-2 h-4 w-4 text-muted-foreground' />
                  Open project
                </DropdownMenuItem>
                <DropdownMenuItem
                  onClick={() => onEdit(project.id)}
                  className='rounded-md'
                >
                  <PencilLine className='mr-2 h-4 w-4 text-muted-foreground' />
                  Edit project
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                {project.status === 'ARCHIVED' ? (
                  <DropdownMenuItem
                    disabled={isUnarchiving}
                    onClick={handleUnarchive}
                    className='rounded-md text-emerald-600 focus:text-emerald-600 dark:text-emerald-400 dark:focus:text-emerald-400'
                  >
                    <Archive className='mr-2 h-4 w-4 text-emerald-600 dark:text-emerald-400' />
                    Unarchive project
                  </DropdownMenuItem>
                ) : (
                  <DropdownMenuItem
                    disabled={isArchiving}
                    onClick={handleArchive}
                    variant='destructive'
                    className='rounded-md'
                  >
                    <Archive className='mr-2 h-4 w-4' />
                    Archive project
                  </DropdownMenuItem>
                )}
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>

        <p className='mt-4 line-clamp-3 text-sm text-muted-foreground'>
          {project.description}
        </p>

        <div className='mt-auto space-y-4 pt-5'>
          <div className='flex items-center justify-between gap-3'>
            <div className='flex min-w-0 items-center gap-2.5'>
              <Avatar className='h-7 w-7 border border-border/60 shadow-sm'>
                {project.lead.avatarUrl && (
                  <AvatarImage
                    src={project.lead.avatarUrl}
                    alt={project.lead.name}
                  />
                )}
                <AvatarFallback className='bg-primary/5 text-[10px] font-bold text-primary'>
                  {getInitials(project.lead.name)}
                </AvatarFallback>
              </Avatar>
              <span className='truncate text-sm font-medium text-foreground/80'>
                {project.lead.name}
              </span>
            </div>

            {project.status !== 'ARCHIVED' ? (
              <PMProjectStatusBadge status={project.status} />
            ) : null}
          </div>

          <div className='flex items-center justify-between gap-3 border-t border-border/60 pt-3 text-xs text-muted-foreground'>
            <span className='truncate'>{project.category}</span>
            <span className='whitespace-nowrap'>Updated {updatedLabel}</span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
