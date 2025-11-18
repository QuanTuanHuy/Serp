/**
 * PTM v2 - Edit Project Dialog
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Dialog for editing existing projects
 */

'use client';

import { useState, useEffect } from 'react';
import { Loader2 } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
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
import { useUpdateProjectMutation } from '../../services/projectApi';
import type { Project, ProjectStatus, ProjectPriority } from '../../types';
import { toast } from 'sonner';

interface EditProjectDialogProps {
  project: Project;
  open: boolean;
  onOpenChange: (open: boolean) => void;
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

export function EditProjectDialog({
  project,
  open,
  onOpenChange,
}: EditProjectDialogProps) {
  const [title, setTitle] = useState(project.title);
  const [description, setDescription] = useState(project.description || '');
  const [status, setStatus] = useState<ProjectStatus>(project.status);
  const [priority, setPriority] = useState<ProjectPriority>(project.priority);
  const [color, setColor] = useState(project.color);
  const [deadline, setDeadline] = useState(
    project.deadlineMs
      ? new Date(project.deadlineMs).toISOString().split('T')[0]
      : ''
  );

  const [updateProject, { isLoading }] = useUpdateProjectMutation();

  // Reset form when project changes
  useEffect(() => {
    setTitle(project.title);
    setDescription(project.description || '');
    setStatus(project.status);
    setPriority(project.priority);
    setColor(project.color);
    setDeadline(
      project.deadlineMs
        ? new Date(project.deadlineMs).toISOString().split('T')[0]
        : ''
    );
  }, [project]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!title.trim()) {
      toast.error('Project title is required');
      return;
    }

    try {
      await updateProject({
        id: project.id,
        title: title.trim(),
        description: description.trim() || undefined,
        status,
        priority,
        color,
        deadlineMs: deadline ? new Date(deadline).getTime() : undefined,
      }).unwrap();

      toast.success('Project updated successfully');
      onOpenChange(false);
    } catch (error) {
      toast.error('Failed to update project');
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='!sm:max-w-[500px] !max-w-3xl'>
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>Edit Project</DialogTitle>
            <DialogDescription>
              Update project details and settings.
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-4 py-4'>
            {/* Title */}
            <div className='space-y-2'>
              <Label htmlFor='edit-title'>
                Project Title <span className='text-red-500'>*</span>
              </Label>
              <Input
                id='edit-title'
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder='Enter project title...'
                disabled={isLoading}
                autoFocus
              />
            </div>

            {/* Description */}
            <div className='space-y-2'>
              <Label htmlFor='edit-description'>Description</Label>
              <Textarea
                id='edit-description'
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder='Project description (optional)...'
                rows={3}
                disabled={isLoading}
              />
            </div>

            {/* Status & Priority */}
            <div className='grid grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='edit-status'>Status</Label>
                <Select
                  value={status}
                  onValueChange={(value) => setStatus(value as ProjectStatus)}
                  disabled={isLoading}
                >
                  <SelectTrigger id='edit-status'>
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
                <Label htmlFor='edit-priority'>Priority</Label>
                <Select
                  value={priority}
                  onValueChange={(value) =>
                    setPriority(value as ProjectPriority)
                  }
                  disabled={isLoading}
                >
                  <SelectTrigger id='edit-priority'>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='HIGH'>High</SelectItem>
                    <SelectItem value='MEDIUM'>Medium</SelectItem>
                    <SelectItem value='LOW'>Low</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            {/* Color & Deadline */}
            <div className='grid grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='edit-color'>Color</Label>
                <Select
                  value={color}
                  onValueChange={setColor}
                  disabled={isLoading}
                >
                  <SelectTrigger id='edit-color'>
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

              <div className='space-y-2'>
                <Label htmlFor='edit-deadline'>Deadline</Label>
                <Input
                  id='edit-deadline'
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
              onClick={() => onOpenChange(false)}
              disabled={isLoading}
            >
              Cancel
            </Button>
            <Button type='submit' disabled={isLoading}>
              {isLoading && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
              Save Changes
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
