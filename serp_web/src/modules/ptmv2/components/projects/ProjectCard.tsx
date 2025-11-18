/**
 * PTM v2 - Project Card Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Project card for grid display
 */

'use client';

import { useState } from 'react';
import { Calendar, Clock, CheckCircle, MoreVertical, Star } from 'lucide-react';
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
import {
  useUpdateProjectMutation,
  useDeleteProjectMutation,
} from '../../services/projectApi';
import { EditProjectDialog } from './EditProjectDialog';
import type { Project } from '../../types';
import { toast } from 'sonner';

interface ProjectCardProps {
  project: Project;
  onClick?: (projectId: string) => void;
  className?: string;
}

export function ProjectCard({ project, onClick, className }: ProjectCardProps) {
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [updateProject] = useUpdateProjectMutation();
  const [deleteProject] = useDeleteProjectMutation();

  const completionRate =
    project.totalTasks > 0
      ? Math.round((project.completedTasks / project.totalTasks) * 100)
      : 0;

  const isOverdue =
    project.deadlineMs &&
    project.deadlineMs < Date.now() &&
    project.status !== 'COMPLETED';

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

  return (
    <Card
      className={cn(
        'cursor-pointer transition-all hover:shadow-lg group',
        isOverdue && 'border-red-300 dark:border-red-800',
        className
      )}
      onClick={() => onClick?.(project.id)}
    >
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
              <DropdownMenuTrigger asChild onClick={(e) => e.stopPropagation()}>
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
                  View Tasks
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

      <CardContent className='space-y-4'>
        {/* Stats */}
        <div className='grid grid-cols-2 gap-3 text-sm'>
          <div className='flex items-center gap-2 text-muted-foreground'>
            <CheckCircle className='h-4 w-4' />
            <span>
              {project.completedTasks}/{project.totalTasks} tasks
            </span>
          </div>
          <div className='flex items-center gap-2 text-muted-foreground'>
            <Clock className='h-4 w-4' />
            <span>{project.estimatedHours}h estimated</span>
          </div>
        </div>

        {/* Progress Bar */}
        <div className='space-y-2'>
          <div className='flex items-center justify-between text-xs'>
            <span className='text-muted-foreground'>Progress</span>
            <span className='font-medium'>{completionRate}%</span>
          </div>
          <Progress value={completionRate} className='h-2' />
        </div>

        {/* Deadline */}
        {project.deadlineMs && (
          <div
            className={cn(
              'flex items-center gap-2 text-sm',
              isOverdue && 'text-red-600 dark:text-red-400 font-medium'
            )}
          >
            <Calendar className='h-4 w-4' />
            <span>
              {isOverdue && '⚠️ '}
              Due: {new Date(project.deadlineMs).toLocaleDateString()}
            </span>
          </div>
        )}

        {/* Status Badge */}
        <div className='flex items-center gap-2'>
          <span
            className={cn(
              'px-2.5 py-1 rounded-full text-xs font-medium',
              project.status === 'ACTIVE' &&
                'bg-blue-100 text-blue-700 dark:bg-blue-950 dark:text-blue-400',
              project.status === 'COMPLETED' &&
                'bg-green-100 text-green-700 dark:bg-green-950 dark:text-green-400',
              project.status === 'ON_HOLD' &&
                'bg-amber-100 text-amber-700 dark:bg-amber-950 dark:text-amber-400',
              project.status === 'ARCHIVED' &&
                'bg-gray-100 text-gray-700 dark:bg-gray-950 dark:text-gray-400'
            )}
          >
            {project.status === 'ACTIVE' && 'Active'}
            {project.status === 'COMPLETED' && 'Completed'}
            {project.status === 'ON_HOLD' && 'On Hold'}
            {project.status === 'ARCHIVED' && 'Archived'}
          </span>
        </div>
      </CardContent>

      {/* Edit Dialog */}
      <EditProjectDialog
        project={project}
        open={editDialogOpen}
        onOpenChange={setEditDialogOpen}
      />
    </Card>
  );
}
