/**
 * PTM v2 - Project Detail View
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Detailed project view with tasks
 */

'use client';

import { useState } from 'react';
import {
  Calendar,
  Clock,
  CheckCircle,
  Star,
  Edit,
  Trash2,
  Plus,
  ArrowLeft,
  Target,
  TrendingUp,
} from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Card, CardContent, CardHeader } from '@/shared/components/ui/card';
import { Progress } from '@/shared/components/ui/progress';
import { Badge } from '@/shared/components/ui/badge';
import { Separator } from '@/shared/components/ui/separator';
import {
  useGetProjectQuery,
  useUpdateProjectMutation,
  useDeleteProjectMutation,
} from '../../services/projectApi';
import { useGetTasksQuery } from '../../services/taskApi';
import { EditProjectDialog } from './EditProjectDialog';
import { TaskList } from '../tasks/TaskList';
import { cn } from '@/shared/utils';
import { toast } from 'sonner';
import { Skeleton } from '@/shared/components/ui/skeleton';

interface ProjectDetailViewProps {
  projectId: string;
  onClose: () => void;
}

export function ProjectDetailView({
  projectId,
  onClose,
}: ProjectDetailViewProps) {
  const [editDialogOpen, setEditDialogOpen] = useState(false);

  const {
    data: project,
    isLoading: isLoadingProject,
    error: projectError,
  } = useGetProjectQuery(projectId, {
    skip: !projectId,
  });
  const { data: tasks = [], isLoading: isLoadingTasks } = useGetTasksQuery(
    {
      projectId,
    },
    {
      skip: !projectId,
    }
  );
  const [updateProject] = useUpdateProjectMutation();
  const [deleteProject] = useDeleteProjectMutation();

  const handleToggleFavorite = async () => {
    if (!project) return;
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
      await deleteProject(projectId).unwrap();
      toast.success('Project deleted');
      onClose();
    } catch (error) {
      toast.error('Failed to delete project');
    }
  };

  if (projectError) {
    return (
      <div className='space-y-6'>
        <Button variant='ghost' onClick={onClose}>
          <ArrowLeft className='mr-2 h-4 w-4' />
          Back to Projects
        </Button>
        <Card className='p-8 text-center'>
          <div className='text-red-600 dark:text-red-400 mb-4'>
            <p className='text-lg font-semibold'>Failed to load project</p>
            <p className='text-sm mt-2'>
              Please try again or contact support if the issue persists.
            </p>
          </div>
          <Button onClick={onClose}>Go Back</Button>
        </Card>
      </div>
    );
  }

  if (isLoadingProject || !project) {
    return (
      <div className='space-y-6'>
        <div className='flex items-center justify-between'>
          <Skeleton className='h-10 w-32' />
          <Skeleton className='h-10 w-40' />
        </div>
        <Skeleton className='h-64 w-full' />
        <Skeleton className='h-96 w-full' />
      </div>
    );
  }

  const completionRate =
    project.totalTasks > 0
      ? Math.round((project.completedTasks / project.totalTasks) * 100)
      : 0;

  const isOverdue =
    project.deadlineMs &&
    project.deadlineMs < Date.now() &&
    project.status !== 'COMPLETED';

  const projectTasks = tasks.filter((task) => task.projectId === projectId);

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <Button variant='ghost' onClick={onClose}>
          <ArrowLeft className='mr-2 h-4 w-4' />
          Back to Projects
        </Button>
        <div className='flex items-center gap-2'>
          <Button variant='outline' size='sm' onClick={handleToggleFavorite}>
            <Star
              className={cn(
                'h-4 w-4 mr-2',
                project.isFavorite && 'fill-yellow-400 text-yellow-400'
              )}
            />
            {project.isFavorite ? 'Favorited' : 'Favorite'}
          </Button>
          <Button
            variant='outline'
            size='sm'
            onClick={() => setEditDialogOpen(true)}
          >
            <Edit className='h-4 w-4 mr-2' />
            Edit
          </Button>
          <Button variant='destructive' size='sm' onClick={handleDelete}>
            <Trash2 className='h-4 w-4 mr-2' />
            Delete
          </Button>
        </div>
      </div>

      {/* Project Info Card */}
      <Card>
        <CardHeader>
          <div className='flex items-start gap-4'>
            <div
              className='w-2 h-20 rounded-full flex-shrink-0'
              style={{ backgroundColor: project.color }}
            />
            <div className='flex-1 min-w-0'>
              <h1 className='text-3xl font-bold mb-2'>{project.title}</h1>
              {project.description && (
                <p className='text-muted-foreground'>{project.description}</p>
              )}
              <div className='flex flex-wrap items-center gap-3 mt-4'>
                <Badge
                  className={cn(
                    'text-xs',
                    project.status === 'ACTIVE' && 'bg-blue-500',
                    project.status === 'COMPLETED' && 'bg-green-500',
                    project.status === 'ON_HOLD' && 'bg-amber-500',
                    project.status === 'ARCHIVED' && 'bg-gray-500'
                  )}
                >
                  {project.status}
                </Badge>
                {isOverdue && (
                  <Badge variant='destructive' className='text-xs'>
                    ⚠️ Overdue
                  </Badge>
                )}
              </div>
            </div>
          </div>
        </CardHeader>

        <CardContent className='space-y-6'>
          {/* Stats */}
          <div className='grid grid-cols-2 md:grid-cols-4 gap-4'>
            <div className='space-y-1'>
              <div className='flex items-center gap-2 text-muted-foreground text-sm'>
                <CheckCircle className='h-4 w-4' />
                <span>Tasks</span>
              </div>
              <p className='text-2xl font-bold'>
                {project.completedTasks}/{project.totalTasks}
              </p>
            </div>

            <div className='space-y-1'>
              <div className='flex items-center gap-2 text-muted-foreground text-sm'>
                <Clock className='h-4 w-4' />
                <span>Estimated</span>
              </div>
              <p className='text-2xl font-bold'>{project.estimatedHours}h</p>
            </div>

            <div className='space-y-1'>
              <div className='flex items-center gap-2 text-muted-foreground text-sm'>
                <Target className='h-4 w-4' />
                <span>Progress</span>
              </div>
              <p className='text-2xl font-bold'>{completionRate}%</p>
            </div>

            <div className='space-y-1'>
              <div className='flex items-center gap-2 text-muted-foreground text-sm'>
                <Calendar className='h-4 w-4' />
                <span>Deadline</span>
              </div>
              <p
                className={cn(
                  'text-lg font-semibold',
                  isOverdue && 'text-red-600 dark:text-red-400'
                )}
              >
                {project.deadlineMs
                  ? new Date(project.deadlineMs).toLocaleDateString()
                  : 'No deadline'}
              </p>
            </div>
          </div>

          {/* Progress Bar */}
          <div className='space-y-2'>
            <div className='flex items-center justify-between text-sm'>
              <span className='text-muted-foreground font-medium'>
                Overall Progress
              </span>
              <span className='font-semibold'>{completionRate}%</span>
            </div>
            <Progress value={completionRate} className='h-3' />
          </div>

          <Separator />

          {/* Timestamps */}
          <div className='grid grid-cols-2 gap-4 text-sm'>
            <div>
              <p className='text-muted-foreground'>Created</p>
              <p className='font-medium'>
                {new Date(project.createdAt).toLocaleString()}
              </p>
            </div>
            <div>
              <p className='text-muted-foreground'>Last Updated</p>
              <p className='font-medium'>
                {new Date(project.updatedAt).toLocaleString()}
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Tasks Section */}
      <div className='space-y-4'>
        <div className='flex items-center justify-between'>
          <h2 className='text-2xl font-bold'>Project Tasks</h2>
          <Button>
            <Plus className='h-4 w-4 mr-2' />
            Add Task
          </Button>
        </div>

        {projectTasks.length === 0 ? (
          <Card className='p-12 text-center'>
            <div className='flex flex-col items-center gap-2 text-muted-foreground'>
              <CheckCircle className='h-12 w-12' />
              <p className='text-lg font-medium'>No tasks yet</p>
              <p className='text-sm'>
                Add your first task to this project to get started
              </p>
            </div>
          </Card>
        ) : (
          <TaskList filterProjectId={projectId} />
        )}
      </div>

      {/* Edit Dialog */}
      <EditProjectDialog
        project={project}
        open={editDialogOpen}
        onOpenChange={setEditDialogOpen}
      />
    </div>
  );
}
