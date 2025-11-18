/**
 * PTM v2 - Create Project Dialog
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Dialog for creating new projects
 */

'use client';

import { useState } from 'react';
import { Plus, Loader2 } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/shared/components/ui/dialog';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Textarea } from '@/shared/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { useCreateProjectMutation } from '../../services/projectApi';
import type { ProjectStatus } from '../../types';
import { toast } from 'sonner';

interface CreateProjectDialogProps {
  trigger?: React.ReactNode;
}

const PROJECT_COLORS = [
  { value: '#3b82f6', label: 'Blue' },
  { value: '#8b5cf6', label: 'Purple' },
  { value: '#ec4899', label: 'Pink' },
  { value: '#f59e0b', label: 'Amber' },
  { value: '#10b981', label: 'Green' },
  { value: '#ef4444', label: 'Red' },
  { value: '#6366f1', label: 'Indigo' },
  { value: '#14b8a6', label: 'Teal' },
];

export function CreateProjectDialog({ trigger }: CreateProjectDialogProps) {
  const [open, setOpen] = useState(false);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [status, setStatus] = useState<ProjectStatus>('ACTIVE');
  const [color, setColor] = useState(PROJECT_COLORS[0].value);
  const [estimatedHours, setEstimatedHours] = useState('');
  const [deadline, setDeadline] = useState('');

  const [createProject, { isLoading }] = useCreateProjectMutation();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!title.trim()) {
      toast.error('Project title is required');
      return;
    }

    try {
      await createProject({
        title: title.trim(),
        description: description.trim() || undefined,
        status,
        color,
        estimatedHours: estimatedHours ? parseInt(estimatedHours) : 0,
        deadlineMs: deadline ? new Date(deadline).getTime() : undefined,
      }).unwrap();

      toast.success('Project created successfully');
      setOpen(false);
      resetForm();
    } catch (error) {
      toast.error('Failed to create project');
    }
  };

  const resetForm = () => {
    setTitle('');
    setDescription('');
    setStatus('ACTIVE');
    setColor(PROJECT_COLORS[0].value);
    setEstimatedHours('');
    setDeadline('');
  };

  const handleOpenChange = (newOpen: boolean) => {
    setOpen(newOpen);
    if (!newOpen) {
      resetForm();
    }
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        {trigger || (
          <Button>
            <Plus className='h-4 w-4 mr-2' />
            New Project
          </Button>
        )}
      </DialogTrigger>
      <DialogContent className='sm:max-w-[500px]'>
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>Create New Project</DialogTitle>
            <DialogDescription>
              Add a new project to organize your tasks and track progress.
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-4 py-4'>
            {/* Title */}
            <div className='space-y-2'>
              <Label htmlFor='title'>
                Project Title <span className='text-red-500'>*</span>
              </Label>
              <Input
                id='title'
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder='Enter project title...'
                disabled={isLoading}
                autoFocus
              />
            </div>

            {/* Description */}
            <div className='space-y-2'>
              <Label htmlFor='description'>Description</Label>
              <Textarea
                id='description'
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder='Project description (optional)...'
                rows={3}
                disabled={isLoading}
              />
            </div>

            {/* Status & Color */}
            <div className='grid grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='status'>Status</Label>
                <Select
                  value={status}
                  onValueChange={(value) => setStatus(value as ProjectStatus)}
                  disabled={isLoading}
                >
                  <SelectTrigger id='status'>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='ACTIVE'>Active</SelectItem>
                    <SelectItem value='ON_HOLD'>On Hold</SelectItem>
                    <SelectItem value='COMPLETED'>Completed</SelectItem>
                    <SelectItem value='ARCHIVED'>Archived</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='color'>Color</Label>
                <Select
                  value={color}
                  onValueChange={setColor}
                  disabled={isLoading}
                >
                  <SelectTrigger id='color'>
                    <div className='flex items-center gap-2'>
                      <div
                        className='w-4 h-4 rounded-full'
                        style={{ backgroundColor: color }}
                      />
                      <span>
                        {PROJECT_COLORS.find((c) => c.value === color)?.label}
                      </span>
                    </div>
                  </SelectTrigger>
                  <SelectContent>
                    {PROJECT_COLORS.map((c) => (
                      <SelectItem key={c.value} value={c.value}>
                        <div className='flex items-center gap-2'>
                          <div
                            className='w-4 h-4 rounded-full'
                            style={{ backgroundColor: c.value }}
                          />
                          <span>{c.label}</span>
                        </div>
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            {/* Estimated Hours & Deadline */}
            <div className='grid grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='estimatedHours'>Estimated Hours</Label>
                <Input
                  id='estimatedHours'
                  type='number'
                  min='0'
                  value={estimatedHours}
                  onChange={(e) => setEstimatedHours(e.target.value)}
                  placeholder='0'
                  disabled={isLoading}
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='deadline'>Deadline</Label>
                <Input
                  id='deadline'
                  type='date'
                  value={deadline}
                  onChange={(e) => setDeadline(e.target.value)}
                  disabled={isLoading}
                />
              </div>
            </div>
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => setOpen(false)}
              disabled={isLoading}
            >
              Cancel
            </Button>
            <Button type='submit' disabled={isLoading}>
              {isLoading && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
              Create Project
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
