/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

'use client';

import { useState } from 'react';
import { Plus, Loader2, Calendar as CalendarIcon } from 'lucide-react';
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
import { Calendar } from '@/shared/components/ui/calendar';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui/popover';
import { format } from 'date-fns';
import { useProjectForm } from '../../hooks';
import type { Project, ProjectStatus, ProjectPriority } from '../../types';

interface ProjectFormDialogProps {
  mode: 'create' | 'edit';
  project?: Project;
  trigger?: React.ReactNode;
  open?: boolean;
  onOpenChange?: (open: boolean) => void;
}

const DIALOG_CONFIG = {
  create: {
    title: 'Create New Project',
    description: 'Add a new project to organize your tasks and track progress.',
    submitText: 'Create Project',
  },
  edit: {
    title: 'Edit Project',
    description: 'Update project details and settings.',
    submitText: 'Save Changes',
  },
};

export function ProjectFormDialog({
  mode,
  project,
  trigger,
  open: controlledOpen,
  onOpenChange: controlledOnOpenChange,
}: ProjectFormDialogProps) {
  const [internalOpen, setInternalOpen] = useState(false);

  const isControlled = controlledOpen !== undefined;
  const open = isControlled ? controlledOpen : internalOpen;
  const setOpen = isControlled ? controlledOnOpenChange! : setInternalOpen;

  const {
    title,
    setTitle,
    description,
    setDescription,
    priority,
    setPriority,
    status,
    setStatus,
    color,
    setColor,
    deadline,
    setDeadline,
    isCalendarOpen,
    setIsCalendarOpen,
    isLoading,
    handleSubmit,
    colors,
  } = useProjectForm({
    mode,
    project,
    onSuccess: () => setOpen(false),
  });

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const success = await handleSubmit();
    if (success) {
      setOpen(false);
    }
  };

  const config = DIALOG_CONFIG[mode];

  const dialogContent = (
    <DialogContent className='sm:max-w-[500px] !max-w-3xl'>
      <form onSubmit={onSubmit}>
        <DialogHeader>
          <DialogTitle>{config.title}</DialogTitle>
          <DialogDescription>{config.description}</DialogDescription>
        </DialogHeader>

        <div className='space-y-4 py-4'>
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

          {mode === 'edit' && (
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
                    <SelectItem value='NEW'>New</SelectItem>
                    <SelectItem value='IN_PROGRESS'>In Progress</SelectItem>
                    <SelectItem value='ON_HOLD'>On Hold</SelectItem>
                    <SelectItem value='COMPLETED'>Completed</SelectItem>
                    <SelectItem value='ARCHIVED'>Archived</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='priority'>Priority</Label>
                <Select
                  value={priority}
                  onValueChange={(value) =>
                    setPriority(value as ProjectPriority)
                  }
                  disabled={isLoading}
                >
                  <SelectTrigger id='priority'>
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
          )}

          {mode === 'create' && (
            <div className='space-y-2'>
              <Label htmlFor='priority'>Priority</Label>
              <Select
                value={priority}
                onValueChange={(value) => setPriority(value as ProjectPriority)}
                disabled={isLoading}
              >
                <SelectTrigger id='priority'>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='HIGH'>High</SelectItem>
                  <SelectItem value='MEDIUM'>Medium</SelectItem>
                  <SelectItem value='LOW'>Low</SelectItem>
                </SelectContent>
              </Select>
            </div>
          )}

          <div className='grid grid-cols-2 gap-4'>
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
                    <span>{colors.find((c) => c.value === color)?.label}</span>
                  </div>
                </SelectTrigger>
                <SelectContent>
                  {colors.map((c) => (
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
              <Label htmlFor='deadline'>Deadline</Label>
              <Popover open={isCalendarOpen} onOpenChange={setIsCalendarOpen}>
                <PopoverTrigger asChild>
                  <Button
                    variant='outline'
                    className={`w-full justify-start text-left font-normal ${
                      !deadline && 'text-muted-foreground'
                    }`}
                    disabled={isLoading}
                  >
                    <CalendarIcon className='mr-2 h-4 w-4' />
                    {deadline
                      ? format(new Date(deadline), 'PPP')
                      : 'Pick a date'}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className='w-auto p-0' align='start'>
                  <Calendar
                    mode='single'
                    selected={deadline ? new Date(deadline) : undefined}
                    onSelect={(date) => {
                      setDeadline(date ? date.toISOString().split('T')[0] : '');
                      setIsCalendarOpen(false);
                    }}
                    disabled={(date) =>
                      date < new Date(new Date().setHours(0, 0, 0, 0))
                    }
                    autoFocus={mode === 'edit'}
                  />
                </PopoverContent>
              </Popover>
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
            {config.submitText}
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  );

  if (mode === 'create' && !isControlled) {
    return (
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogTrigger asChild>
          {trigger || (
            <Button>
              <Plus className='h-4 w-4 mr-2' />
              New Project
            </Button>
          )}
        </DialogTrigger>
        {dialogContent}
      </Dialog>
    );
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      {dialogContent}
    </Dialog>
  );
}
