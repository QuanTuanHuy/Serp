/**
 * PTM v2 - Quick Add Task Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - One-click task creation with minimal input
 */

'use client';

import { useState, useEffect } from 'react';
import { Plus, ChevronDown, ChevronUp } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
} from '@/shared/components/ui/dialog';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Textarea } from '@/shared/components/ui/textarea';
import { cn } from '@/shared/utils';
import {
  useCreateTaskMutation,
  useQuickAddTaskMutation,
} from '../../services/taskApi';
import { useGetProjectsQuery } from '../../services/projectApi';
import type { TaskPriority, Project, RepeatConfig } from '../../types';
import { RecurringTaskConfig } from './RecurringTaskConfig';
import { toast } from 'sonner';

interface QuickAddTaskProps {
  className?: string;
  showIcon?: boolean;
  variant?: 'default' | 'outline' | 'ghost';
}

export function QuickAddTask({
  className,
  showIcon = true,
  variant = 'default',
}: QuickAddTaskProps) {
  const [open, setOpen] = useState(false);
  const [title, setTitle] = useState('');
  const [showDetails, setShowDetails] = useState(false);

  // Optional details
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<TaskPriority>('MEDIUM');
  const [estimatedHours, setEstimatedHours] = useState('');
  const [projectId, setProjectId] = useState<number | string>('');
  const [tags, setTags] = useState('');
  const [repeatConfig, setRepeatConfig] = useState<RepeatConfig | null>(null);

  const [quickAddTask, { isLoading: isQuickAdding }] =
    useQuickAddTaskMutation();
  const [createTask, { isLoading: isCreating }] = useCreateTaskMutation();
  const { data: projectsResponse } = useGetProjectsQuery({});
  const projects = projectsResponse?.data.items || [];

  const isLoading = isQuickAdding || isCreating;

  // Keyboard shortcut: Cmd/Ctrl + K to open dialog
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        setOpen(true);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  const handleQuickSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!title.trim()) {
      toast.error('Please enter a task title');
      return;
    }

    try {
      if (showDetails) {
        // Create with details
        await createTask({
          title: title.trim(),
          description: description.trim() || undefined,
          priority,
          estimatedDurationHours: estimatedHours
            ? parseFloat(estimatedHours)
            : undefined,
          projectId: projectId
            ? typeof projectId === 'string'
              ? parseInt(projectId, 10)
              : projectId
            : undefined,
          tags: tags.trim() ? tags.split(',').map((t) => t.trim()) : undefined,
          repeatConfig: repeatConfig || undefined,
        }).unwrap();
      } else {
        // Quick add (only title)
        await quickAddTask({ title: title.trim() }).unwrap();
      }

      toast.success('Task created successfully!');
      handleClose();
    } catch (error) {
      toast.error('Failed to create task');
      console.error('Create task error:', error);
    }
  };

  const handleClose = () => {
    setOpen(false);
    setTitle('');
    setDescription('');
    setPriority('MEDIUM');
    setEstimatedHours('');
    setProjectId('');
    setTags('');
    setRepeatConfig(null);
    setShowDetails(false);
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size='lg' variant={variant} className={cn('gap-2', className)}>
          {showIcon && <Plus className='h-4 w-4' />}
          Quick Add
          <kbd className='ml-2 hidden sm:inline-flex h-5 select-none items-center gap-1 rounded border px-1.5 font-mono text-[10px] font-medium opacity-100'>
            ⌘K
          </kbd>
        </Button>
      </DialogTrigger>

      <DialogContent className='!max-w-2xl'>
        <DialogHeader>
          <DialogTitle className='flex items-center gap-2'>
            ⚡ Quick Add Task
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleQuickSubmit} className='space-y-4'>
          {/* Title Input - Always Visible */}
          <div className='space-y-2'>
            <Input
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder='What do you need to do?'
              className='text-lg'
              autoFocus
              disabled={isLoading}
            />
          </div>

          {/* Toggle Details Button */}
          <button
            type='button'
            onClick={() => setShowDetails(!showDetails)}
            className='flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors'
          >
            {showDetails ? (
              <ChevronUp className='h-4 w-4' />
            ) : (
              <ChevronDown className='h-4 w-4' />
            )}
            Optional Details{' '}
            {showDetails ? '(click to hide)' : '(click to expand)'}
          </button>

          {/* Optional Details - Collapsible */}
          {showDetails && (
            <div className='space-y-4 pt-2 border-t'>
              {/* Description */}
              <div className='space-y-2'>
                <Label htmlFor='description'>Description</Label>
                <Textarea
                  id='description'
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder='Add more details...'
                  rows={3}
                  disabled={isLoading}
                />
              </div>

              {/* Priority */}
              <div className='space-y-2'>
                <Label>Priority</Label>
                <div className='flex gap-4'>
                  <div className='flex items-center space-x-2'>
                    <input
                      type='radio'
                      id='low'
                      name='priority'
                      value='LOW'
                      checked={priority === 'LOW'}
                      onChange={(e) =>
                        setPriority(e.target.value as TaskPriority)
                      }
                      disabled={isLoading}
                      className='cursor-pointer'
                    />
                    <Label htmlFor='low' className='cursor-pointer font-normal'>
                      Low
                    </Label>
                  </div>
                  <div className='flex items-center space-x-2'>
                    <input
                      type='radio'
                      id='medium'
                      name='priority'
                      value='MEDIUM'
                      checked={priority === 'MEDIUM'}
                      onChange={(e) =>
                        setPriority(e.target.value as TaskPriority)
                      }
                      disabled={isLoading}
                      className='cursor-pointer'
                    />
                    <Label
                      htmlFor='medium'
                      className='cursor-pointer font-normal'
                    >
                      Medium
                    </Label>
                  </div>
                  <div className='flex items-center space-x-2'>
                    <input
                      type='radio'
                      id='high'
                      name='priority'
                      value='HIGH'
                      checked={priority === 'HIGH'}
                      onChange={(e) =>
                        setPriority(e.target.value as TaskPriority)
                      }
                      disabled={isLoading}
                      className='cursor-pointer'
                    />
                    <Label
                      htmlFor='high'
                      className='cursor-pointer font-normal'
                    >
                      High
                    </Label>
                  </div>
                </div>
              </div>

              {/* Duration & Project */}
              <div className='grid grid-cols-2 gap-4'>
                <div className='space-y-2'>
                  <Label htmlFor='duration'>Duration (hours)</Label>
                  <Input
                    id='duration'
                    type='number'
                    step='0.5'
                    min='0.5'
                    value={estimatedHours}
                    onChange={(e) => setEstimatedHours(e.target.value)}
                    placeholder='2.5'
                    disabled={isLoading}
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='project'>Project</Label>
                  <select
                    id='project'
                    value={projectId}
                    onChange={(e) => setProjectId(e.target.value)}
                    className='flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50'
                    disabled={isLoading}
                  >
                    <option value=''>No project</option>
                    {projects?.map((project: Project) => (
                      <option key={project.id} value={project.id}>
                        {project.title}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Tags */}
              <div className='space-y-2'>
                <Label htmlFor='tags'>Tags (comma-separated)</Label>
                <Input
                  id='tags'
                  value={tags}
                  onChange={(e) => setTags(e.target.value)}
                  placeholder='backend, urgent, review'
                  disabled={isLoading}
                />
              </div>

              {/* Recurring Configuration */}
              <RecurringTaskConfig
                value={repeatConfig}
                onChange={setRepeatConfig}
              />
            </div>
          )}

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={handleClose}
              disabled={isLoading}
            >
              Cancel
            </Button>
            <Button type='submit' disabled={!title.trim() || isLoading}>
              {isLoading ? 'Creating...' : 'Create Task'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
