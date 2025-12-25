/**
 * PTM v2 - Project Card Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Project card for grid display
 */

'use client';

import { useState } from 'react';
import {
  Calendar,
  Clock,
  MoreVertical,
  Star,
  AlertCircle,
  ListTodo,
} from 'lucide-react';
import { Card, CardContent, CardHeader } from '@/shared/components/ui/card';
import { Progress } from '@/shared/components/ui/progress';
import { Button } from '@/shared/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  DropdownMenuSeparator,
} from '@/shared/components/ui/dropdown-menu';
import { cn } from '@/shared/utils';
import { useUpdateProjectMutation, useDeleteProjectMutation } from '../../api';
import { EditProjectDialog } from './EditProjectDialog';
import type { Project } from '../../types';
import { toast } from 'sonner';

interface ProjectCardProps {
  project: Project;
  onClick?: (projectId: number) => void;
  className?: string;
  viewMode?: 'grid' | 'list';
}

export function ProjectCard({
  project,
  onClick,
  className,
  viewMode = 'grid',
}: ProjectCardProps) {
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [updateProject] = useUpdateProjectMutation();
  const [deleteProject] = useDeleteProjectMutation();

  const completionRate =
    project.totalTasks && project.totalTasks > 0
      ? Math.round(((project.completedTasks || 0) / project.totalTasks) * 100)
      : 0;

  const isOverdue =
    project.deadlineMs &&
    project.deadlineMs < Date.now() &&
    project.status !== 'COMPLETED';

  const getPriorityColor = () => {
    switch (project.priority) {
      case 'HIGH':
        return 'text-red-600 dark:text-red-400';
      case 'MEDIUM':
        return 'text-amber-600 dark:text-amber-400';
      case 'LOW':
        return 'text-green-600 dark:text-green-400';
      default:
        return 'text-muted-foreground';
    }
  };

  const handleToggleFavorite = async (e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await updateProject({
        id: project.id,
        isFavorite: !project.isFavorite,
      }).unwrap();
      toast.success(
        project.isFavorite ? 'Removed from favorites' : 'Added to favorites'
      );
    } catch (error) {
      toast.error('Failed to update project');
    }
  };

  const handleDelete = async () => {
    if (!confirm('Are you sure you want to delete this project?')) return;

    try {
      await deleteProject(project.id).unwrap();
      toast.success('Project deleted');
    } catch (error) {
      toast.error('Failed to delete project');
    }
  };

  // List view layout
  if (viewMode === 'list') {
    return (
      <>
        <Card
          className={cn(
            'group relative overflow-hidden cursor-pointer transition-all',
            'hover:shadow-md hover:border-primary/50',
            'active:scale-[0.99]',
            isOverdue && 'border-red-300 dark:border-red-800',
            className
          )}
          onClick={() => onClick?.(project.id)}
        >
          <div className='flex items-center gap-4 p-4'>
            {/* Color indicator */}
            <div
              className='w-1 h-16 rounded-full flex-shrink-0'
              style={{ backgroundColor: project.color }}
            />

            {/* Content */}
            <div className='flex-1 min-w-0 grid grid-cols-12 gap-4 items-center'>
              {/* Title & Description */}
              <div className='col-span-4 min-w-0'>
                <div className='flex items-center gap-2 mb-1'>
                  <h3 className='font-semibold text-base truncate'>
                    {project.title}
                  </h3>
                  {project.isFavorite && (
                    <Star className='h-4 w-4 fill-yellow-400 text-yellow-400 flex-shrink-0' />
                  )}
                </div>
                {project.description && (
                  <p className='text-xs text-muted-foreground truncate'>
                    {project.description}
                  </p>
                )}
              </div>

              {/* Progress */}
              <div className='col-span-2 text-center'>
                <p className='text-2xl font-bold'>{completionRate}%</p>
                <p className='text-xs text-muted-foreground'>Complete</p>
              </div>

              {/* Tasks */}
              <div className='col-span-2 text-center'>
                <p className='text-lg font-semibold'>
                  {project.completedTasks}/{project.totalTasks}
                </p>
                <p className='text-xs text-muted-foreground'>Tasks</p>
              </div>

              {/* Priority */}
              <div className='col-span-1 text-center'>
                <p className={cn('text-sm font-medium', getPriorityColor())}>
                  {project.priority}
                </p>
              </div>

              {/* Deadline */}
              <div className='col-span-2'>
                {project.deadlineMs ? (
                  <div
                    className={cn(
                      'text-xs',
                      isOverdue
                        ? 'text-red-600 dark:text-red-400 font-medium'
                        : 'text-muted-foreground'
                    )}
                  >
                    {isOverdue && '⚠️ '}
                    {new Date(project.deadlineMs).toLocaleDateString()}
                  </div>
                ) : (
                  <div className='text-xs text-muted-foreground'>
                    No deadline
                  </div>
                )}
              </div>

              {/* Status & Actions */}
              <div className='col-span-1 flex items-center justify-end gap-1'>
                <DropdownMenu>
                  <DropdownMenuTrigger
                    asChild
                    onClick={(e) => e.stopPropagation()}
                  >
                    <Button variant='ghost' size='icon' className='h-8 w-8'>
                      <MoreVertical className='h-4 w-4' />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align='end'>
                    <DropdownMenuItem
                      onClick={(e) => {
                        e.stopPropagation();
                        handleToggleFavorite(e);
                      }}
                    >
                      {project.isFavorite
                        ? 'Remove from Favorites'
                        : 'Add to Favorites'}
                    </DropdownMenuItem>
                    <DropdownMenuItem
                      onClick={(e) => {
                        e.stopPropagation();
                        setEditDialogOpen(true);
                      }}
                    >
                      Edit Project
                    </DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem
                      onClick={(e) => {
                        e.stopPropagation();
                        handleDelete();
                      }}
                      className='text-red-600'
                    >
                      Delete
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>
            </div>
          </div>
        </Card>
        <EditProjectDialog
          project={project}
          open={editDialogOpen}
          onOpenChange={setEditDialogOpen}
        />
      </>
    );
  }

  // Grid view layout (optimized)
  return (
    <>
      <Card
        className={cn(
          'group relative overflow-hidden cursor-pointer transition-all',
          'hover:shadow-xl hover:scale-[1.02] hover:border-primary/50',
          'active:scale-[0.98]',
          isOverdue &&
            'border-red-300 dark:border-red-800 shadow-red-100 dark:shadow-red-900/20',
          className
        )}
        onClick={() => onClick?.(project.id)}
      >
        {/* Priority indicator line */}
        <div
          className='absolute top-0 left-0 w-full h-1 transition-all group-hover:h-1.5'
          style={{ backgroundColor: project.color }}
        />

        <CardHeader className='pb-3'>
          <div className='flex items-start justify-between gap-2'>
            {/* Color indicator & Title */}
            <div className='flex items-start gap-3 flex-1 min-w-0'>
              <div
                className='w-1 h-12 rounded-full flex-shrink-0'
                style={{ backgroundColor: project.color }}
              />
              <div className='flex-1 min-w-0'>
                <h3 className='font-semibold text-base leading-tight mb-1 truncate'>
                  {project.title}
                </h3>
                {project.description && (
                  <p className='text-xs text-muted-foreground line-clamp-2'>
                    {project.description}
                  </p>
                )}
              </div>
            </div>

            {/* Actions */}
            <div className='flex items-center gap-1 flex-shrink-0'>
              <Button
                variant='ghost'
                size='icon'
                className='h-8 w-8'
                onClick={handleToggleFavorite}
              >
                <Star
                  className={cn(
                    'h-4 w-4',
                    project.isFavorite && 'fill-yellow-400 text-yellow-400'
                  )}
                />
              </Button>

              <DropdownMenu>
                <DropdownMenuTrigger
                  asChild
                  onClick={(e) => e.stopPropagation()}
                >
                  <Button variant='ghost' size='icon' className='h-8 w-8'>
                    <MoreVertical className='h-4 w-4' />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align='end'>
                  <DropdownMenuItem
                    onClick={(e) => {
                      e.stopPropagation();
                      setEditDialogOpen(true);
                    }}
                  >
                    Edit
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    onClick={(e) => {
                      e.stopPropagation();
                      onClick?.(project.id);
                    }}
                  >
                    View Details
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDelete();
                    }}
                    className='text-red-600'
                  >
                    Delete
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
          </div>
        </CardHeader>

        <CardContent className='space-y-4 pt-6'>
          {/* Circular Progress & Stats */}
          <div className='flex items-center gap-4'>
            {/* Circular Progress */}
            <div className='relative flex-shrink-0'>
              <svg className='w-16 h-16 transform -rotate-90'>
                <circle
                  cx='32'
                  cy='32'
                  r='28'
                  stroke='currentColor'
                  strokeWidth='4'
                  fill='none'
                  className='text-muted-foreground/20'
                />
                <circle
                  cx='32'
                  cy='32'
                  r='28'
                  stroke={project.color}
                  strokeWidth='4'
                  fill='none'
                  strokeDasharray={`${completionRate * 1.76} 176`}
                  className='transition-all duration-500'
                />
              </svg>
              <div className='absolute inset-0 flex items-center justify-center'>
                <span className='text-lg font-bold'>{completionRate}%</span>
              </div>
            </div>

            {/* Stats Grid */}
            <div className='flex-1 grid grid-cols-2 gap-2 text-xs'>
              <div className='space-y-1'>
                <div className='flex items-center gap-1 text-muted-foreground'>
                  <ListTodo className='h-3 w-3' />
                  <span>Tasks</span>
                </div>
                <p className='text-base font-semibold'>
                  {project.completedTasks}
                  <span className='text-muted-foreground'>
                    /{project.totalTasks}
                  </span>
                </p>
              </div>

              <div className='space-y-1'>
                <div className='flex items-center gap-1 text-muted-foreground'>
                  <Clock className='h-3 w-3' />
                  <span>Hours</span>
                </div>
                <p className='text-base font-semibold'>
                  {project.estimatedHours}h
                </p>
              </div>

              <div className='space-y-1 col-span-2'>
                <div className='flex items-center gap-1 text-muted-foreground'>
                  <span className={cn('font-medium', getPriorityColor())}>
                    {project.priority} Priority
                  </span>
                </div>
              </div>
            </div>
          </div>

          {/* Deadline & Status Row */}
          <div className='flex items-center justify-between gap-2'>
            {/* Deadline */}
            {project.deadlineMs ? (
              <div
                className={cn(
                  'flex items-center gap-1.5 text-xs',
                  isOverdue
                    ? 'text-red-600 dark:text-red-400 font-medium'
                    : 'text-muted-foreground'
                )}
              >
                {isOverdue ? (
                  <AlertCircle className='h-3.5 w-3.5' />
                ) : (
                  <Calendar className='h-3.5 w-3.5' />
                )}
                <span>
                  {isOverdue && '⚠️ '}
                  {new Date(project.deadlineMs).toLocaleDateString()}
                </span>
              </div>
            ) : (
              <div className='text-xs text-muted-foreground'>No deadline</div>
            )}

            {/* Status Badge */}
            <span
              className={cn(
                'px-2 py-0.5 rounded-full text-xs font-medium transition-colors',
                (project.status === 'NEW' ||
                  project.status === 'IN_PROGRESS') &&
                  'bg-blue-100 text-blue-700 dark:bg-blue-950/50 dark:text-blue-400',
                project.status === 'COMPLETED' &&
                  'bg-green-100 text-green-700 dark:bg-green-950/50 dark:text-green-400',
                project.status === 'ON_HOLD' &&
                  'bg-amber-100 text-amber-700 dark:bg-amber-950/50 dark:text-amber-400',
                project.status === 'ARCHIVED' &&
                  'bg-gray-100 text-gray-700 dark:bg-gray-950/50 dark:text-gray-400'
              )}
            >
              {project.status === 'NEW' && 'New'}
              {project.status === 'IN_PROGRESS' && 'In Progress'}
              {project.status === 'COMPLETED' && '✓ Completed'}
              {project.status === 'ON_HOLD' && 'On Hold'}
              {project.status === 'ARCHIVED' && 'Archived'}
            </span>
          </div>
        </CardContent>
      </Card>

      {/* Edit Dialog */}
      <EditProjectDialog
        project={project}
        open={editDialogOpen}
        onOpenChange={setEditDialogOpen}
      />
    </>
  );
}
